package com.sequenceiq.it.cloudbreak.newway.testcase.mock;


import static com.sequenceiq.it.cloudbreak.newway.mock.model.AmbariMock.CLUSTER_DEFINITIONS;
import static com.sequenceiq.it.spark.ITResponse.AMBARI_API_ROOT;
import static com.sequenceiq.it.spark.ITResponse.MOCK_ROOT;
import static com.sequenceiq.it.spark.ITResponse.SALT_API_ROOT;
import static com.sequenceiq.it.spark.ITResponse.SALT_BOOT_ROOT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sequenceiq.it.cloudbreak.newway.Stack;
import com.sequenceiq.it.cloudbreak.newway.action.stack.StackScalePostAction;
import com.sequenceiq.it.cloudbreak.newway.action.stack.StackTestAction;
import com.sequenceiq.it.cloudbreak.newway.assertion.MockVerification;
import com.sequenceiq.it.cloudbreak.newway.context.Description;
import com.sequenceiq.it.cloudbreak.newway.context.MockedTestContext;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.cloudbreak.newway.entity.stack.StackTestDto;
import com.sequenceiq.it.cloudbreak.newway.testcase.AbstractIntegrationTest;

import spark.Route;

public class UpscaleTest extends AbstractIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpscaleTest.class);

    @BeforeMethod
    public void beforeMethod(Object[] data) {
        MockedTestContext testContext = (MockedTestContext) data[0];
        LOGGER.info("All routes added: {}", testContext.getSparkServer().getSparkService().getPaths());
        minimalSetupForClusterCreation(testContext);
    }

    @AfterMethod(alwaysRun = true)
    public void tear(Object[] data) {
        MockedTestContext testContext = (MockedTestContext) data[0];
        testContext.cleanupTestContextEntity();
    }

    @Test(dataProvider = TEST_CONTEXT_WITH_MOCK)
    @Description(
            given = "a stack with stack scale",
            when = "upscale to 15 after it downscale to 6",
            then = "stack is running")
    public void testStackScaling(TestContext testContext) {
        testContext
                .given(StackTestDto.class)
                .when(StackTestAction::create)
                .await(STACK_AVAILABLE)
                .when(StackScalePostAction.valid().withDesiredCount(15))
                .await(STACK_AVAILABLE)
                .when(StackScalePostAction.valid().withDesiredCount(6))
                .await(STACK_AVAILABLE)
                .validate();
    }

    @Test(dataProvider = TEST_CONTEXT_WITH_MOCK)
    @Description(
            given = "a stack with upscale",
            when = "upscale to 15",
            then = "stack is running")
    public void testUpscale(MockedTestContext testContext) {
        String clusterName = getNameGenerator().getRandomNameForResource();
        int originalWorkedCount = 1;
        int desiredWorkedCount = 15;
        int addedNodes = desiredWorkedCount - originalWorkedCount;
        testContext.given(StackTestDto.class)
                .withName(clusterName)
                .withGatewayPort(testContext.getSparkServer().getPort())
                .when(Stack.postV4())
                .await(STACK_AVAILABLE)
                .when(StackScalePostAction.valid().withDesiredCount(desiredWorkedCount))
                .await(StackTestDto.class, STACK_AVAILABLE)
                .then(MockVerification.verify(HttpMethod.POST, "/api/v1/blueprints/"))
                .then(MockVerification.verify(HttpMethod.POST, MOCK_ROOT + "/cloud_instance_statuses").exactTimes(1))
                .then(MockVerification.verify(HttpMethod.POST, MOCK_ROOT + "/cloud_metadata_statuses")
                        .bodyContains("CREATE_REQUESTED", addedNodes).exactTimes(1))
                .then(MockVerification.verify(HttpMethod.GET, SALT_BOOT_ROOT + "/health").atLeast(1))
                .then(MockVerification.verify(HttpMethod.POST, SALT_BOOT_ROOT + "/salt/action/distribute").atLeast(1))
                .then(MockVerification.verify(HttpMethod.POST, SALT_API_ROOT + "/run").bodyContains("fun=network.ipaddrs").atLeast(1))
                .then(MockVerification.verify(HttpMethod.POST, SALT_API_ROOT + "/run").bodyContains("arg=roles&arg=ambari_server").atLeast(2))
                .then(MockVerification.verify(HttpMethod.POST, SALT_API_ROOT + "/run").bodyContains("fun=saltutil.sync_all").atLeast(1))
                .then(MockVerification.verify(HttpMethod.POST, SALT_API_ROOT + "/run").bodyContains("fun=mine.update").atLeast(1))
                .then(MockVerification.verify(HttpMethod.POST, SALT_API_ROOT + "/run").bodyContains("fun=state.highstate").atLeast(2))
                .then(MockVerification.verify(HttpMethod.POST, SALT_API_ROOT + "/run").bodyContains("fun=grains.remove").exactTimes(4))
                .then(MockVerification.verify(HttpMethod.POST, SALT_BOOT_ROOT + "/hostname/distribute")
                        .bodyRegexp("^.*\\[([\"0-9\\.]+([,]{0,1})){" + addedNodes + "}\\].*").exactTimes(2))
                .then(MockVerification.verify(HttpMethod.GET, AMBARI_API_ROOT + "/hosts").atLeast(1))
                .then(MockVerification.verify(HttpMethod.GET, AMBARI_API_ROOT + "/clusters").exactTimes(22))
                .then(MockVerification.verify(HttpMethod.GET, AMBARI_API_ROOT + "/clusters/" + clusterName).atLeast(1))
                .validate();
    }

    @Test(dataProvider = TEST_CONTEXT_WITH_MOCK)
    @Description(
            given = "a stack",
            when = "ambari is failing",
            then = "stack state is failed")
    public void testAmbariFailure(MockedTestContext testContext) {
        mockAmbariClusterDefinitionFail(testContext);
        testContext
                .given(StackTestDto.class)
                .when(Stack.postV4())
                .await(STACK_FAILED)
                .then(MockVerification.verify(HttpMethod.POST, "/api/v1/blueprints/").atLeast(1))
                .validate();
    }

    private void mockAmbariClusterDefinitionFail(MockedTestContext testContext) {
        Route customResponse = (request, response) -> {
            response.type("text/plain");
            response.status(400);
            response.body("Bad cluster definition format");
            return "";
        };
        testContext.getModel().getAmbariMock().getDynamicRouteStack().clearPost(CLUSTER_DEFINITIONS);
        testContext.getModel().getAmbariMock().getDynamicRouteStack().post(CLUSTER_DEFINITIONS, customResponse);
    }

}
