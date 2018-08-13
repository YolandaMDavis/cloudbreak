package com.sequenceiq.cloudbreak.service.rdsconfig;

import static com.sequenceiq.cloudbreak.controller.exception.NotFoundException.notFound;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.api.model.ResourceStatus;
import com.sequenceiq.cloudbreak.api.model.rds.RdsType;
import com.sequenceiq.cloudbreak.authorization.OrganizationResource;
import com.sequenceiq.cloudbreak.controller.exception.BadRequestException;
import com.sequenceiq.cloudbreak.controller.exception.NotFoundException;
import com.sequenceiq.cloudbreak.controller.validation.rds.RdsConnectionValidator;
import com.sequenceiq.cloudbreak.domain.RDSConfig;
import com.sequenceiq.cloudbreak.domain.organization.Organization;
import com.sequenceiq.cloudbreak.domain.stack.cluster.Cluster;
import com.sequenceiq.cloudbreak.repository.OrganizationResourceRepository;
import com.sequenceiq.cloudbreak.repository.RdsConfigRepository;
import com.sequenceiq.cloudbreak.service.AbstractOrganizationAwareResourceService;
import com.sequenceiq.cloudbreak.service.cluster.ClusterService;
import com.sequenceiq.cloudbreak.util.NameUtil;

@Service
public class RdsConfigService extends AbstractOrganizationAwareResourceService<RDSConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RdsConfigService.class);

    @Inject
    private RdsConfigRepository rdsConfigRepository;

    @Inject
    private ClusterService clusterService;

    @Inject
    private RdsConnectionValidator rdsConnectionValidator;

    public Set<RDSConfig> retrieveRdsConfigs() {
        Organization organization = getOrganizationService().getDefaultOrganizationForCurrentUser();
        return rdsConfigRepository.findAll(organization.getId());
    }

    public RDSConfig getByName(String name) {
        Organization organization = getOrganizationService().getDefaultOrganizationForCurrentUser();
        return getByName(name, organization.getId());
    }

    public RDSConfig getByName(String name, Long organizationId) {
        return Optional.ofNullable(rdsConfigRepository.findByName(name, organizationId))
                .orElseThrow(notFound("RDS configuration", name));
    }

    public RDSConfig get(Long id) {
        return rdsConfigRepository.findById(id).orElseThrow(notFound("RDS configuration", id));
    }

    public void delete(Long id) {
        Organization organization = getOrganizationService().getDefaultOrganizationForCurrentUser();
        RDSConfig rdsConfig = Optional.ofNullable(rdsConfigRepository.findById(id, organization.getId()))
                .orElseThrow(notFound("RDS configuration", id));
        delete(rdsConfig);
    }

    public void delete(String name) {
        Organization organization = getOrganizationService().getDefaultOrganizationForCurrentUser();
        delete(name, organization.getId());
    }

    public RDSConfig delete(String name, Long organizationId) {
        RDSConfig rdsConfig = Optional.ofNullable(rdsConfigRepository.findByName(name, organizationId))
                .orElseThrow(notFound("RDS configuration", name));
        delete(rdsConfig);
        return rdsConfig;
    }

    public RDSConfig createIfNotExists(RDSConfig rdsConfig) {
        try {
            return getByName(rdsConfig.getName());
        } catch (AccessDeniedException | NotFoundException ignored) {
            return createInDefaultOrganization(rdsConfig);
        }
    }

    public Set<RDSConfig> findByClusterId(Long clusterId) {
        Organization organization = getOrganizationService().getDefaultOrganizationForCurrentUser();
        return rdsConfigRepository.findByClusterId(clusterId, organization.getId());
    }

    public RDSConfig findByClusterIdAndType(Long clusterId, RdsType rdsType) {
        Organization organization = getOrganizationService().getDefaultOrganizationForCurrentUser();
        return rdsConfigRepository.findByClusterIdAndType(clusterId, rdsType.name(), organization.getId());
    }

    public Set<RDSConfig> findUserManagedByClusterId(Long clusterId) {
        Organization organization = getOrganizationService().getDefaultOrganizationForCurrentUser();
        return rdsConfigRepository.findUserManagedByClusterId(clusterId, organization.getId());
    }

    public void deleteDefaultRdsConfigs(Set<RDSConfig> rdsConfigs) {
        rdsConfigs.stream().filter(rdsConfig -> ResourceStatus.DEFAULT == rdsConfig.getStatus()).forEach(this::setStatusToDeleted);
    }

    private void checkRdsConfigNotAssociated(RDSConfig rdsConfig) {
        LOGGER.info("Deleting rds configuration with name: {}", rdsConfig.getName());
        List<Cluster> clustersWithProvidedRds = new ArrayList<>(clusterService.findAllClustersByRDSConfig(rdsConfig.getId()));
        if (!clustersWithProvidedRds.isEmpty()) {
            if (clustersWithProvidedRds.size() > 1) {
                String clusters = clustersWithProvidedRds
                        .stream()
                        .map(Cluster::getName)
                        .collect(Collectors.joining(", "));
                throw new BadRequestException(String.format(
                        "There are clusters associated with RDS config '%s'. Please remove these before deleting the RDS configuration. "
                                + "The following clusters are using this RDS: [%s]", rdsConfig.getName(), clusters));
            } else {
                throw new BadRequestException(String.format("There is a cluster ['%s'] which uses RDS config '%s'. Please remove this "
                        + "cluster before deleting the RDS", clustersWithProvidedRds.get(0).getName(), rdsConfig.getName()));
            }
        }
    }

    private void setStatusToDeleted(RDSConfig rdsConfig) {
        rdsConfig.setName(NameUtil.postfixWithTimestamp(rdsConfig.getName()));
        rdsConfig.setStatus(ResourceStatus.DEFAULT_DELETED);
        rdsConfigRepository.save(rdsConfig);
    }

    public Set<RDSConfig> listByOrganizationId(Long organizationId) {
        return rdsConfigRepository.findAll(organizationId);
    }

    @Override
    protected OrganizationResourceRepository<RDSConfig, Long> repository() {
        return rdsConfigRepository;
    }

    @Override
    protected OrganizationResource resource() {
        return OrganizationResource.RDS;
    }

    @Override
    protected boolean canDelete(RDSConfig resource) {
        checkRdsConfigNotAssociated(resource);
        if (ResourceStatus.USER_MANAGED.equals(resource.getStatus())) {
            return true;
        }
        setStatusToDeleted(resource);
        return false;
    }

    @Override
    protected void prepareCreation(RDSConfig resource) {
    }

    public String testRdsConnection(String existingRDSConfigName, RDSConfig rdsConfig) {
        if (existingRDSConfigName != null) {
            try {
                RDSConfig config = getByName(existingRDSConfigName);
                return testRDSConnectivity(config);
            } catch (AccessDeniedException | NotFoundException e) {
                return "not found or access is denied";
            }
        } else {
            return testRDSConnectivity(rdsConfig);
        }
    }

    private String testRDSConnectivity(RDSConfig rdsConfig) {
        try {
            if (rdsConfig == null) {
                return "not found";
            } else {
                rdsConnectionValidator.validateRdsConnection(rdsConfig);
                return "connected";
            }
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }
}
