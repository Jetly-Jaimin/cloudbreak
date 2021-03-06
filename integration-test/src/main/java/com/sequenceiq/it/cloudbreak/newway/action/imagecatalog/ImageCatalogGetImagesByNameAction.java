package com.sequenceiq.it.cloudbreak.newway.action.imagecatalog;

import static com.sequenceiq.it.cloudbreak.newway.log.Log.logJSON;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.cloudbreak.api.endpoint.v4.common.mappable.CloudPlatform;
import com.sequenceiq.cloudbreak.api.endpoint.v4.imagecatalog.ImageCatalogV4Endpoint;
import com.sequenceiq.cloudbreak.api.endpoint.v4.imagecatalog.responses.ImagesV4Response;
import com.sequenceiq.it.cloudbreak.newway.CloudbreakClient;
import com.sequenceiq.it.cloudbreak.newway.action.Action;
import com.sequenceiq.it.cloudbreak.newway.context.TestContext;
import com.sequenceiq.it.cloudbreak.newway.entity.imagecatalog.ImageCatalogTestDto;

public class ImageCatalogGetImagesByNameAction implements Action<ImageCatalogTestDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageCatalogGetImagesByNameAction.class);

    private CloudPlatform platform = CloudPlatform.MOCK;

    private String stackName;

    public ImageCatalogGetImagesByNameAction() {
    }

    public ImageCatalogGetImagesByNameAction(CloudPlatform platform) {
        this.platform = platform;
    }

    public ImageCatalogGetImagesByNameAction(String stackName) {
        this.stackName = stackName;
    }

    @Override
    public ImageCatalogTestDto action(TestContext testContext, ImageCatalogTestDto entity, CloudbreakClient cloudbreakClient) throws Exception {
        LOGGER.info("Get images of ImageCatalog within workspace by catalog name: {}", entity.getRequest().getName());
        try {
            ImageCatalogV4Endpoint imageCatalogV4Endpoint = cloudbreakClient
                    .getCloudbreakClient()
                    .imageCatalogV4Endpoint();

            entity.setResponseByProvider(getImagesV4Response(entity, cloudbreakClient, imageCatalogV4Endpoint));
            logJSON(LOGGER, "images have been fetched successfully: ", entity.getRequest());
        } catch (Exception e) {
            LOGGER.warn("Cannot get images of ImageCatalog : {}", entity.getRequest().getName());
            throw e;
        }
        return entity;
    }

    private ImagesV4Response getImagesV4Response(ImageCatalogTestDto entity, CloudbreakClient cloudbreakClient, ImageCatalogV4Endpoint imageCatalogV4Endpoint)
            throws Exception {
        return StringUtils.isNoneEmpty(stackName)
                ? imageCatalogV4Endpoint.getImagesByName(cloudbreakClient.getWorkspaceId(), entity.getName(), stackName, null)
                : imageCatalogV4Endpoint.getImagesByName(cloudbreakClient.getWorkspaceId(), entity.getName(), null, platform.name());
    }
}

