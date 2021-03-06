package com.sequenceiq.it.cloudbreak.newway.action.clusterdefinition;

import static com.sequenceiq.it.cloudbreak.newway.log.Log.log;
import static com.sequenceiq.it.cloudbreak.newway.log.Log.logJSON;
import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.it.cloudbreak.newway.entity.clusterdefinition.ClusterDefinitionTestDto;
import com.sequenceiq.it.cloudbreak.newway.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.newway.action.Action;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;

public class ClusterDefinitionGetAction implements Action<ClusterDefinitionTestDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterDefinitionGetAction.class);

    @Override
    public ClusterDefinitionTestDto action(TestContext testContext, ClusterDefinitionTestDto entity, CloudbreakClient client) throws Exception {
        log(LOGGER, format(" Name: %s", entity.getName()));
        logJSON(LOGGER, format(" Cluster definition get response:%n"), entity.getName());
        entity.setResponse(
                client.getCloudbreakClient()
                        .clusterDefinitionV4Endpoint()
                        .get(client.getWorkspaceId(), entity.getName()));
        logJSON(LOGGER, format(" Cluster definition get successfully:%n"), entity.getResponse());

        return entity;
    }

}