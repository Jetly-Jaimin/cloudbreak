package com.sequenceiq.cloudbreak.cm;

import java.util.Collections;

import javax.inject.Inject;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.cloudera.api.swagger.ClouderaManagerResourceApi;
import com.cloudera.api.swagger.ClustersResourceApi;
import com.cloudera.api.swagger.client.ApiClient;
import com.cloudera.api.swagger.client.ApiException;
import com.cloudera.api.swagger.model.ApiCommand;
import com.cloudera.api.swagger.model.ApiConfig;
import com.cloudera.api.swagger.model.ApiConfigList;
import com.cloudera.api.swagger.model.ApiConfigureForKerberosArguments;
import com.sequenceiq.cloudbreak.client.HttpClientConfig;
import com.sequenceiq.cloudbreak.cm.polling.ClouderaManagerPollingServiceProvider;
import com.sequenceiq.cloudbreak.domain.KerberosConfig;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.domain.stack.cluster.Cluster;
import com.sequenceiq.cloudbreak.service.CloudbreakException;

@Service
public class ClouderaManagerKerberosService {

    @Inject
    private ClouderaManagerPollingServiceProvider clouderaManagerPollingServiceProvider;

    @Inject
    private ApplicationContext applicationContext;

    public void setupKerberos(ApiClient client, HttpClientConfig clientConfig, Stack stack) throws ApiException, CloudbreakException {
        Cluster cluster = stack.getCluster();
        ClouderaManagerResourceApi clouderaManagerResourceApi = new ClouderaManagerResourceApi(client);
        if (cluster.isAdJoinable()) {
            KerberosConfig kerberosConfig = cluster.getKerberosConfig();
            ApiConfigList apiConfigList = new ApiConfigList()
                    .addItemsItem(new ApiConfig().name("kdc_type").value("Active Directory"))
                    .addItemsItem(new ApiConfig().name("security_realm").value(kerberosConfig.getRealm()))
                    .addItemsItem(new ApiConfig().name("kdc_host").value(kerberosConfig.getUrl()))
                    .addItemsItem(new ApiConfig().name("kdc_admin_host").value(kerberosConfig.getAdminUrl()))
                    .addItemsItem(new ApiConfig().name("ad_kdc_domain").value(kerberosConfig.getContainerDn()));
            clouderaManagerResourceApi.updateConfig("Add kerberos configuration", apiConfigList);
            ApiCommand importAdminCredentials =
                    clouderaManagerResourceApi.importAdminCredentials(kerberosConfig.getPassword(), kerberosConfig.getPrincipal());
            clouderaManagerPollingServiceProvider.kerberosConfigurePollingService(stack, client, importAdminCredentials.getId());
            ClouderaManagerModificationService modificationService = applicationContext.getBean(ClouderaManagerModificationService.class, stack, clientConfig);
            modificationService.stopCluster();
            ClustersResourceApi clustersResourceApi = new ClustersResourceApi(client);
            ApiCommand configureForKerberos = clustersResourceApi.configureForKerberos(cluster.getName(), new ApiConfigureForKerberosArguments());
            clouderaManagerPollingServiceProvider.kerberosConfigurePollingService(stack, client, configureForKerberos.getId());
            ApiCommand generateCredentials = clouderaManagerResourceApi.generateCredentialsCommand();
            clouderaManagerPollingServiceProvider.kerberosConfigurePollingService(stack, client, generateCredentials.getId());
            modificationService.startCluster(Collections.emptySet());
        }
    }
}
