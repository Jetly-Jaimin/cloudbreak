package com.sequenceiq.cloudbreak.cloud.gcp.context;

import java.util.List;

import org.springframework.stereotype.Service;

import com.google.api.services.compute.Compute;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.gcp.GcpConstants;
import com.sequenceiq.cloudbreak.cloud.gcp.util.GcpStackUtil;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.cloudbreak.cloud.model.CloudResource;
import com.sequenceiq.cloudbreak.cloud.model.Location;
import com.sequenceiq.cloudbreak.cloud.model.Network;
import com.sequenceiq.cloudbreak.cloud.model.Platform;
import com.sequenceiq.cloudbreak.cloud.model.Variant;
import com.sequenceiq.cloudbreak.cloud.template.ResourceContextBuilder;

@Service
public class GcpContextBuilder implements ResourceContextBuilder<GcpContext> {

    public static final int PARALLEL_RESOURCE_REQUEST = 30;

    @Override
    public GcpContext contextInit(CloudContext context, AuthenticatedContext auth, Network network, List<CloudResource> resources, boolean build) {
        CloudCredential credential = auth.getCloudCredential();
        String projectId = GcpStackUtil.getProjectId(credential);
        String serviceAccountId = GcpStackUtil.getServiceAccountId(credential);
        Compute compute = GcpStackUtil.buildCompute(credential);
        Location location = context.getLocation();
        boolean noPublicIp = network != null ? GcpStackUtil.noPublicIp(network) : false;
        return new GcpContext(context.getName(), location, projectId, serviceAccountId, compute, noPublicIp, PARALLEL_RESOURCE_REQUEST, build);
    }

    @Override
    public Platform platform() {
        return GcpConstants.GCP_PLATFORM;
    }

    @Override
    public Variant variant() {
        return GcpConstants.GCP_VARIANT;
    }
}
