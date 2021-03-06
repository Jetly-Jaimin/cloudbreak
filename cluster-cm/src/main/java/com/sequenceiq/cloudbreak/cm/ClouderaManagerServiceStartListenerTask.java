package com.sequenceiq.cloudbreak.cm;

import org.springframework.stereotype.Service;

@Service
public class ClouderaManagerServiceStartListenerTask extends AbstractClouderaManagerCommandCheckerTask<ClouderaManagerCommandPollerObject> {

    @Override
    public void handleTimeout(ClouderaManagerCommandPollerObject toolsResourceApi) {
        throw new ClouderaManagerOperationFailedException("Operation timed out. Failed to start Cloudera Manager services.");
    }

    @Override
    public String successMessage(ClouderaManagerCommandPollerObject toolsResourceApi) {
        return "Cloudera Manager all service start finished with success result.";
    }

    @Override
    String getCommandName() {
        return "Service start";
    }
}
