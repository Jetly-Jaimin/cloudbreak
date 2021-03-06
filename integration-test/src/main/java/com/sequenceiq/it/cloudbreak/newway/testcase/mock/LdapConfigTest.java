package com.sequenceiq.it.cloudbreak.newway.testcase.mock;

import static com.sequenceiq.it.cloudbreak.newway.context.RunningParameter.expectedMessage;
import static com.sequenceiq.it.cloudbreak.newway.context.RunningParameter.key;
import static org.junit.Assert.assertNotNull;

import java.util.UnknownFormatConversionException;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sequenceiq.cloudbreak.api.endpoint.v4.ldaps.DirectoryType;
import com.sequenceiq.it.cloudbreak.newway.client.LdapConfigTestClient;
import com.sequenceiq.it.cloudbreak.newway.context.Description;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.cloudbreak.newway.entity.ldap.LdapConfigTestDto;
import com.sequenceiq.it.cloudbreak.newway.testcase.AbstractIntegrationTest;
import com.sequenceiq.it.util.LongStringGeneratorUtil;

public class LdapConfigTest extends AbstractIntegrationTest {

    private static final String INVALID_LDAP_NAME = "a-@#$%|:&*;";

    @Inject
    private LongStringGeneratorUtil longStringGenerator;

    @Inject
    private LdapConfigTestClient ldapConfigTestClient;

    @BeforeMethod
    public void beforeMethod(Object[] data) {
        minimalSetupForClusterCreation((TestContext) data[0]);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(Object[] data) {
        ((TestContext) data[0]).cleanupTestContextEntity();
    }

    @Test(dataProvider = TEST_CONTEXT_WITH_MOCK)
    @Description(
            given = "a valid ldap request",
            when = "calling create ldap",
            then = "the ldap should be created")
    public void testCreateValidLdap(TestContext testContext) {
        String name = getNameGenerator().getRandomNameForResource();
        testContext
                .given(LdapConfigTestDto.class)
                .valid()
                .withName(name)
                .when(ldapConfigTestClient.post())
                .then((tc, entity, cc) -> {
                    assertNotNull(entity);
                    assertNotNull(entity.getResponse());
                    return entity;
                })
                .validate();
    }

    @Test(dataProvider = TEST_CONTEXT_WITH_MOCK)
    @Description(
            given = "a valid active directory request",
            when = "calling create ldap",
            then = "the ldap should be created")
    public void testCreateValidAd(TestContext testContext) {
        String name = getNameGenerator().getRandomNameForResource();
        testContext
                .given(LdapConfigTestDto.class)
                .valid()
                .withName(name)
                .withDirectoryType(DirectoryType.ACTIVE_DIRECTORY)
                .when(ldapConfigTestClient.post())
                .then((tc, entity, cc) -> {
                    assertNotNull(entity);
                    assertNotNull(entity.getResponse());
                    return entity;
                })
                .validate();
    }

    @Test(dataProvider = TEST_CONTEXT_WITH_MOCK)
    @Description(
            given = "an invalid ldap request with empty name",
            when = "calling create ldap",
            then = "getting BadRequestException because ldap needs a valid name")
    public void testCreateLdapWithMissingName(TestContext testContext) {
        String key = getNameGenerator().getRandomNameForResource();
        testContext
                .given(LdapConfigTestDto.class)
                .valid()
                .withName("")
                .when(ldapConfigTestClient.post(), key(key))
                .expect(BadRequestException.class,
                        expectedMessage("The length of the ldap config's name has to be in range of 1 to 100")
                                .withKey(key))
                .validate();
    }

    @Test(dataProvider = TEST_CONTEXT_WITH_MOCK)
    @Description(
            given = "an invalid ldap request with specific characters in the name",
            when = "calling create ldap",
            then = "getting BadRequestException because ldap needs a valid name")
    public void testCreateLdapWithInvalidName(TestContext testContext) {
        testContext
                .given(LdapConfigTestDto.class)
                .valid()
                .withName(INVALID_LDAP_NAME)
                .when(ldapConfigTestClient.post(), key(INVALID_LDAP_NAME))
                .expect(UnknownFormatConversionException.class,
                        expectedMessage("Conversion = '|'")
                                .withKey(INVALID_LDAP_NAME))
                .validate();
    }

    @Test(dataProvider = TEST_CONTEXT_WITH_MOCK)
    @Description(
            given = "an invalid ldap request with too long name",
            when = "calling create ldap",
            then = "getting BadRequestException because ldap needs a valid name")
    public void testCreateLdapWithLongName(TestContext testContext) {
        String longName = longStringGenerator.stringGenerator(101);
        testContext
                .given(LdapConfigTestDto.class)
                .valid()
                .withName(longName)
                .when(ldapConfigTestClient.post(), key(longName))
                .expect(BadRequestException.class,
                        expectedMessage("The length of the ldap config's name has to be in range of 1 to 100")
                                .withKey(longName))
                .validate();
    }

    @Test(dataProvider = TEST_CONTEXT_WITH_MOCK)
    @Description(
            given = "an invalid ldap request with too long description",
            when = "calling create ldap",
            then = "getting BadRequestException because ldap needs a valid description")
    public void testCreateLdapWithLongDesc(TestContext testContext) {
        String name = getNameGenerator().getRandomNameForResource();
        String longDesc = longStringGenerator.stringGenerator(1001);
        testContext
                .given(LdapConfigTestDto.class)
                .valid()
                .withName(name)
                .withDescription(longDesc)
                .when(ldapConfigTestClient.post(), key(longDesc))
                .expect(BadRequestException.class,
                        expectedMessage("The length of the ldap config's description has to be in range of 0 to 1000")
                                .withKey(longDesc))
                .validate();
    }

    @Test(dataProvider = TEST_CONTEXT_WITH_MOCK)
    @Description(
            given = "a valid ldap request",
            when = "calling create ldap and delete and create again with the same name",
            then = "ldap should be created")
    public void testCreateDeleteCreateAgain(TestContext testContext) {
        String name = getNameGenerator().getRandomNameForResource();
        testContext
                .given(name, LdapConfigTestDto.class)
                .valid()
                .withName(name)
                .when(ldapConfigTestClient.post(), key(name))
                .when(ldapConfigTestClient.delete(), key(name))
                .when(ldapConfigTestClient.post(), key(name))
                .then((tc, entity, cc) -> {
                    assertNotNull(entity);
                    assertNotNull(entity.getResponse());
                    return entity;
                }, key(name))
                .validate();
    }

    @Test(dataProvider = TEST_CONTEXT_WITH_MOCK)
    @Description(
            given = "a valid ldap request",
            when = "calling create ldap and create again with the same name",
            then = "getting BadRequestException because only one ldap can exist with same name in a workspace at the same time")
    public void testCreateLdapWithSameName(TestContext testContext) {
        String name = getNameGenerator().getRandomNameForResource();
        testContext
                .given(name, LdapConfigTestDto.class)
                .valid()
                .withName(name)
                .when(ldapConfigTestClient.post(), key(name))
                .then((tc, entity, cc) -> {
                    assertNotNull(entity);
                    assertNotNull(entity.getResponse());
                    return entity;
                }, key(name))

                .given(name, LdapConfigTestDto.class)
                .valid()
                .withName(name)
                .when(ldapConfigTestClient.post(), key(name))
                .expect(BadRequestException.class,
                        expectedMessage("dap already exists with name")
                                .withKey(name))
                .validate();
    }
}