package com.sequenceiq.it.cloudbreak.newway.action.ldap;

import static com.sequenceiq.it.cloudbreak.newway.log.Log.log;
import static com.sequenceiq.it.cloudbreak.newway.log.Log.logJSON;
import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.it.cloudbreak.newway.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.newway.action.Action;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.cloudbreak.newway.entity.ldap.LdapConfigTestDto;

public class LdapConfigPostAction implements Action<LdapConfigTestDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapConfigPostAction.class);

    @Override
    public LdapConfigTestDto action(TestContext testContext, LdapConfigTestDto entity, CloudbreakClient client) throws Exception {
        log(LOGGER, format(" Name: %s", entity.getRequest().getName()));
        logJSON(LOGGER, " Ldap post request:\n", entity.getRequest());
        entity.setResponse(
                client.getCloudbreakClient()
                        .ldapConfigV4Endpoint()
                        .post(client.getWorkspaceId(), entity.getRequest()));
        logJSON(LOGGER, " Ldap was created successfully:\n", entity.getResponse());
        log(LOGGER, format(" ID: %s", entity.getResponse().getId()));

        return entity;
    }

}