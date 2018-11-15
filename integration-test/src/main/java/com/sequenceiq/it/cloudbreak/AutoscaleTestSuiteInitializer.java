package com.sequenceiq.it.cloudbreak;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.StringUtils;
import org.testng.ITestContext;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import com.sequenceiq.it.IntegrationTestContext;
import com.sequenceiq.it.SuiteContext;
import com.sequenceiq.it.config.IntegrationTestConfiguration;
import com.sequenceiq.periscope.client.AutoscaleClient;
import com.sequenceiq.periscope.client.AutoscaleClient.AutoscaleClientBuilder;

@ContextConfiguration(classes = IntegrationTestConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class)
public class AutoscaleTestSuiteInitializer extends AbstractTestNGSpringContextTests {
    private static final Logger LOG = LoggerFactory.getLogger(AutoscaleTestSuiteInitializer.class);

    @Value("${integrationtest.periscope.server}")
    private String defaultPeriscopeServer;

    @Value("${integrationtest.caas.protocol}")
    private String defaultCaasProtocol;

    @Value("${integrationtest.caas.address}")
    private String defaultCaasAddress;

    @Value("${server.contextPath:/as}")
    private String autoscaleRootContextPath;

    @Inject
    private SuiteContext suiteContext;

    private IntegrationTestContext itContext;

    @BeforeSuite(dependsOnGroups = "suiteInit")
    public void initContext(ITestContext testContext) throws Exception {
        springTestContextBeforeTestClass();
        springTestContextPrepareTestInstance();

        itContext = suiteContext.getItContext(testContext.getSuite().getName());
    }

    @BeforeSuite(dependsOnMethods = "initContext")
    @Parameters("periscopeServer")
    public void initCloudbreakSuite(@Optional("") String periscopeServer, @Optional("") String caasProtocol, @Optional("") String caasAddress) {
        periscopeServer = StringUtils.hasLength(periscopeServer) ? periscopeServer : defaultPeriscopeServer;
        caasProtocol = StringUtils.hasLength(caasProtocol) ? caasProtocol : defaultCaasProtocol;
        caasAddress = StringUtils.hasLength(caasAddress) ? caasAddress : defaultCaasAddress;
        String refreshToken = itContext.getContextParam(IntegrationTestContext.REFRESH_TOKEN);

        AutoscaleClient autoscaleClient = new AutoscaleClientBuilder(periscopeServer + autoscaleRootContextPath, caasProtocol, caasAddress)
                .withCertificateValidation(false)
                .withDebug(true)
                .withCredential(refreshToken)
                .withIgnorePreValidation(false)
                .build();

        itContext.putContextParam(CloudbreakITContextConstants.AUTOSCALE_CLIENT, autoscaleClient);
    }

}
