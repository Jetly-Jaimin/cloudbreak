package com.sequenceiq.it.cloudbreak.scaling;

import java.io.IOException;
import java.net.URISyntaxException;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.StackV4Endpoint;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.HostGroupAdjustmentV4Request;
import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.UpdateClusterV4Request;
import com.sequenceiq.cloudbreak.client.CloudbreakClient;
import com.sequenceiq.it.IntegrationTestContext;
import com.sequenceiq.it.cloudbreak.AbstractCloudbreakIntegrationTest;
import com.sequenceiq.it.cloudbreak.CloudbreakITContextConstants;
import com.sequenceiq.it.cloudbreak.CloudbreakUtil;

public class ClusterAndStackDownscaleTest extends AbstractCloudbreakIntegrationTest {

    @BeforeMethod
    public void setContextParameters() {
        Assert.assertNotNull(getItContext().getContextParam(CloudbreakITContextConstants.STACK_ID), "Stack id is mandatory.");
        Assert.assertNotNull(getItContext().getContextParam(CloudbreakITContextConstants.AMBARI_USER_ID), "Ambari user id is mandatory.");
        Assert.assertNotNull(getItContext().getContextParam(CloudbreakITContextConstants.AMBARI_PASSWORD_ID), "Ambari password id is mandatory.");
        Assert.assertNotNull(getItContext().getContextParam(CloudbreakITContextConstants.AMBARI_PORT_ID), "Ambari port id is mandatory.");
    }

    @Test
    @Parameters({ "instanceGroup", "scalingAdjustment" })
    public void testClusterAndStackDownscale(@Optional("slave_1") String instanceGroup, int scalingAdjustment) throws IOException, URISyntaxException {
        // GIVEN
        IntegrationTestContext itContext = getItContext();
        String stackName = itContext.getContextParam(CloudbreakITContextConstants.STACK_NAME);
        StackV4Endpoint stackV4Endpoint = itContext.getContextParam(CloudbreakITContextConstants.CLOUDBREAK_CLIENT,
                CloudbreakClient.class).stackV4Endpoint();
        Long workspaceId = itContext.getContextParam(CloudbreakITContextConstants.WORKSPACE_ID, Long.class);
        String ambariUser = itContext.getContextParam(CloudbreakITContextConstants.AMBARI_USER_ID);
        String ambariPassword = itContext.getContextParam(CloudbreakITContextConstants.AMBARI_PASSWORD_ID);
        String ambariPort = itContext.getContextParam(CloudbreakITContextConstants.AMBARI_PORT_ID);
        int expectedNodeCountStack = ScalingUtil.getNodeCountStack(stackV4Endpoint, workspaceId, stackName) + scalingAdjustment;
        int expectedNodeCountCluster = ScalingUtil.getNodeCountAmbari(stackV4Endpoint, ambariPort, workspaceId, stackName, ambariUser, ambariPassword, itContext)
                + scalingAdjustment;
        // WHEN
        UpdateClusterV4Request updateClusterJson = new UpdateClusterV4Request();
        HostGroupAdjustmentV4Request hostGroupAdjustmentJson = new HostGroupAdjustmentV4Request();
        hostGroupAdjustmentJson.setHostGroup(instanceGroup);
        hostGroupAdjustmentJson.setWithStackUpdate(true);
        hostGroupAdjustmentJson.setScalingAdjustment(scalingAdjustment);
        updateClusterJson.setHostGroupAdjustment(hostGroupAdjustmentJson);
        getCloudbreakClient().stackV4Endpoint().putCluster(workspaceId, stackName, updateClusterJson);
        CloudbreakUtil.waitAndCheckClusterStatus(getCloudbreakClient(), workspaceId, stackName, "AVAILABLE");
        CloudbreakUtil.waitAndCheckStackStatus(getCloudbreakClient(), workspaceId, stackName, "AVAILABLE");
        // THEN
        ScalingUtil.checkStackScaled(stackV4Endpoint, workspaceId, stackName, expectedNodeCountStack);
        ScalingUtil.checkClusterScaled(stackV4Endpoint, ambariPort, workspaceId, stackName, ambariUser, ambariPassword, expectedNodeCountCluster, itContext);
        ScalingUtil.putInstanceCountToContext(itContext, workspaceId, stackName);
    }
}
