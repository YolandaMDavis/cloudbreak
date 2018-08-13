package com.sequenceiq.cloudbreak.controller;

import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v3.RdsConfigV3Endpoint;
import com.sequenceiq.cloudbreak.api.model.rds.RDSConfigRequest;
import com.sequenceiq.cloudbreak.api.model.rds.RDSConfigResponse;
import com.sequenceiq.cloudbreak.api.model.rds.RDSTestRequest;
import com.sequenceiq.cloudbreak.api.model.rds.RdsTestResult;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.common.type.ResourceEvent;
import com.sequenceiq.cloudbreak.controller.exception.BadRequestException;
import com.sequenceiq.cloudbreak.domain.RDSConfig;
import com.sequenceiq.cloudbreak.service.AuthenticatedUserService;
import com.sequenceiq.cloudbreak.service.rdsconfig.RdsConfigService;

@Component
@Transactional(Transactional.TxType.NEVER)
public class RdsConfigV3Controller extends NotificationController implements RdsConfigV3Endpoint {

    @Inject
    private RdsConfigService rdsConfigService;

    @Inject
    @Named("conversionService")
    private ConversionService conversionService;

    @Inject
    private AuthenticatedUserService authenticatedUserService;

    @Override
    public Set<RDSConfigResponse> listByOrganization(Long organizationId) {
        return rdsConfigService.listByOrganizationId(organizationId).stream()
                .map(rdsConfig -> conversionService.convert(rdsConfig, RDSConfigResponse.class))
                .collect(Collectors.toSet());
    }

    @Override
    public RDSConfigResponse getByNameInOrganization(Long organizationId, String name) {
        RDSConfig rdsConfig = rdsConfigService.getByName(name, organizationId);
        return conversionService.convert(rdsConfig, RDSConfigResponse.class);
    }

    @Override
    public RDSConfigResponse createInOrganization(Long organizationId, @Valid RDSConfigRequest request) {
        RDSConfig rdsConfig = conversionService.convert(request, RDSConfig.class);
        rdsConfig = rdsConfigService.create(rdsConfig, organizationId);
        notify(authenticatedUserService.getCbUser(), ResourceEvent.RECIPE_CREATED);
        return conversionService.convert(rdsConfig, RDSConfigResponse.class);
    }

    @Override
    public RDSConfigResponse deleteInOrganization(Long organizationId, String name) {
        RDSConfig deleted = rdsConfigService.delete(name, organizationId);
        IdentityUser identityUser = authenticatedUserService.getCbUser();
        notify(identityUser, ResourceEvent.RECIPE_DELETED);
        return conversionService.convert(deleted, RDSConfigResponse.class);
    }

    @Override
    public RdsTestResult testRdsConnection(Long organizationId, @Valid RDSTestRequest rdsTestRequest) {
        String existingRDSConfigName = rdsTestRequest.getName();
        RDSConfigRequest configRequest = rdsTestRequest.getRdsConfig();
        if (existingRDSConfigName == null && configRequest == null) {
            throw new BadRequestException("Either an RDSConfig id, name or an RDSConfig request needs to be specified in the request. ");
        }
        RDSConfig rdsConfig = configRequest != null ? conversionService.convert(configRequest, RDSConfig.class) : null;
        return new RdsTestResult(rdsConfigService.testRdsConnection(existingRDSConfigName, rdsConfig));
    }

    @Override
    public RDSConfigRequest getRequestFromName(Long organizationId, String name) {
        RDSConfig rdsConfig = rdsConfigService.getByName(name, organizationId);
        return conversionService.convert(rdsConfig, RDSConfigRequest.class);
    }
}