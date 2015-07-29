package com.sequenceiq.cloudbreak.cloud.openstack;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.cloud.InstanceConnector;
import com.sequenceiq.cloudbreak.cloud.event.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.CloudVmInstanceStatus;
import com.sequenceiq.cloudbreak.cloud.model.Instance;

@Service
public class OpenStackInstanceConnector implements InstanceConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenStackInstanceConnector.class);


    @Inject
    private OpenStackClient openStackClient;

    @Inject
    private OpenStackMetadataCollector metadataCollector;


    @Override
    public Set<String> getSSHFingerprints(AuthenticatedContext authenticatedContext, Instance vm) {
        return null;
    }

    @Override
    public List<CloudVmInstanceStatus> collectMetadata(AuthenticatedContext authenticatedContext, List<CloudResource> resources, List<Instance> vms) {
        return metadataCollector.collectVmMetadata(authenticatedContext, resources, vms);
    }

    @Override
    public List<CloudVmInstanceStatus> start(AuthenticatedContext ac, List<CloudResource> resources, List<Instance> vms) {
        return null;
    }

    @Override
    public List<CloudVmInstanceStatus> stop(AuthenticatedContext ac, List<CloudResource> resources, List<Instance> vms) {
        return null;
    }

    @Override
    public List<CloudVmInstanceStatus> check(AuthenticatedContext ac, List<CloudResource> resources, List<Instance> vms) {
        return null;
    }
}
