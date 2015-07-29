package com.sequenceiq.cloudbreak.cloud.event.resource;

import com.sequenceiq.cloudbreak.cloud.event.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;

import reactor.rx.Promise;

public class LaunchStackRequest<T> extends CloudStackRequest<T> {
    public LaunchStackRequest(CloudContext cloudContext, CloudCredential cloudCredential, CloudStack cloudStack, Promise<T> result) {
        super(cloudContext, cloudCredential, cloudStack, result);
    }
}
