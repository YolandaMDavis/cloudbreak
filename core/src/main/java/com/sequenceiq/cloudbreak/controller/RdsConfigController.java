package com.sequenceiq.cloudbreak.controller;

import static com.sequenceiq.cloudbreak.util.SqlUtil.getProperSqlErrorMessage;

import java.util.Set;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v1.RdsConfigEndpoint;
import com.sequenceiq.cloudbreak.api.model.rds.RDSConfigRequest;
import com.sequenceiq.cloudbreak.api.model.rds.RDSConfigResponse;
import com.sequenceiq.cloudbreak.api.model.rds.RDSTestRequest;
import com.sequenceiq.cloudbreak.api.model.rds.RdsTestResult;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.common.type.APIResourceType;
import com.sequenceiq.cloudbreak.common.type.ResourceEvent;
import com.sequenceiq.cloudbreak.controller.exception.BadRequestException;
import com.sequenceiq.cloudbreak.domain.RDSConfig;
import com.sequenceiq.cloudbreak.service.AuthenticatedUserService;
import com.sequenceiq.cloudbreak.service.rdsconfig.RdsConfigService;

@Component
@Transactional(TxType.NEVER)
public class RdsConfigController extends NotificationController implements RdsConfigEndpoint {

    @Autowired
    private RdsConfigService rdsConfigService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    @Qualifier("conversionService")
    private ConversionService conversionService;

    @Override
    public RDSConfigResponse postPrivate(@Valid RDSConfigRequest rdsConfigRequest) {
        return createRdsConfig(rdsConfigRequest, false);
    }

    @Override
    public RDSConfigResponse postPublic(@Valid RDSConfigRequest rdsConfigRequest) {
        return createRdsConfig(rdsConfigRequest, true);
    }

    @Override
    public Set<RDSConfigResponse> getPrivates() {
        Set<RDSConfig> rdsConfigs = rdsConfigService.retrieveRdsConfigs();
        return toJsonList(rdsConfigs);
    }

    @Override
    public RDSConfigResponse getPrivate(String name) {
        RDSConfig rdsConfig = rdsConfigService.getByName(name);
        return conversionService.convert(rdsConfig, RDSConfigResponse.class);
    }

    @Override
    public RDSConfigResponse getPublic(String name) {
        RDSConfig rdsConfig = rdsConfigService.getByName(name);
        return conversionService.convert(rdsConfig, RDSConfigResponse.class);
    }

    @Override
    public Set<RDSConfigResponse> getPublics() {
        Set<RDSConfig> rdsConfigs = rdsConfigService.retrieveRdsConfigs();
        return toJsonList(rdsConfigs);
    }

    @Override
    public RDSConfigResponse get(Long id) {
        RDSConfig rdsConfig = rdsConfigService.get(id);
        return conversionService.convert(rdsConfig, RDSConfigResponse.class);
    }

    @Override
    public void delete(Long id) {
        executeAndNotify(user -> rdsConfigService.delete(id), ResourceEvent.RDS_CONFIG_DELETED);
    }

    @Override
    public void deletePublic(String name) {
        executeAndNotify(user -> rdsConfigService.delete(name), ResourceEvent.RDS_CONFIG_DELETED);
    }

    @Override
    public void deletePrivate(String name) {
        executeAndNotify(user -> rdsConfigService.delete(name), ResourceEvent.RDS_CONFIG_DELETED);
    }

    @Override
    public RdsTestResult testRdsConnection(RDSTestRequest rdsTestRequest) {
        String existingRDSConfigName = rdsTestRequest.getName();
        RDSConfigRequest configRequest = rdsTestRequest.getRdsConfig();
        if (existingRDSConfigName == null && configRequest == null) {
            throw new BadRequestException("Either an RDSConfig id, name or an RDSConfig request needs to be specified in the request. ");
        }
        RDSConfig rdsConfig = configRequest != null ? conversionService.convert(configRequest, RDSConfig.class) : null;
        return new RdsTestResult(rdsConfigService.testRdsConnection(existingRDSConfigName, rdsConfig));
    }

    @Override
    public RDSConfigRequest getRequestFromName(String name) {
        RDSConfig rdsConfig = rdsConfigService.getByName(name);
        return conversionService.convert(rdsConfig, RDSConfigRequest.class);
    }

    private RDSConfigResponse createRdsConfig(RDSConfigRequest rdsConfigJson, boolean publicInAccount) {
        IdentityUser user = authenticatedUserService.getCbUser();
        RDSConfig rdsConfig = conversionService.convert(rdsConfigJson, RDSConfig.class);
        rdsConfig.setPublicInAccount(publicInAccount);
        rdsConfig.setOwner(user.getUserId());
        rdsConfig.setAccount(user.getAccount());
        try {
            rdsConfig = rdsConfigService.createInDefaultOrganization(rdsConfig);
            notify(user, ResourceEvent.RDS_CONFIG_CREATED);
        } catch (DataIntegrityViolationException ex) {
            String msg = String.format("Error with resource [%s], %s", APIResourceType.RDS_CONFIG, getProperSqlErrorMessage(ex));
            throw new BadRequestException(msg);
        }
        return conversionService.convert(rdsConfig, RDSConfigResponse.class);
    }

    private Set<RDSConfigResponse> toJsonList(Set<RDSConfig> rdsConfigs) {
        return (Set<RDSConfigResponse>) conversionService.convert(rdsConfigs,
                TypeDescriptor.forObject(rdsConfigs),
                TypeDescriptor.collection(Set.class, TypeDescriptor.valueOf(RDSConfigResponse.class)));
    }
}
