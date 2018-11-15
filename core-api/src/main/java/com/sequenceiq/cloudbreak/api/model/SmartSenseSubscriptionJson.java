package com.sequenceiq.cloudbreak.api.model;

import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions.SmartSenseSubscriptionModelDescription;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmartSenseSubscriptionJson implements JsonEntity {

    public static final String ID_PATTERN = "^([a-zA-Z]{1}-[0-9]{8}-[a-zA-Z]{1}-[0-9]{8}$)";

    public static final String ID_FORMAT = "The given SmartSense subscription id is not in A-xxxxxxxx-C-xxxxxxxx format!";

    @ApiModelProperty(value = ModelDescriptions.ID, readOnly = true)
    private Long id;

    @ApiModelProperty(value = SmartSenseSubscriptionModelDescription.SUBSCRIPTION_ID, required = true)
    @Pattern(regexp = ID_PATTERN, message = ID_FORMAT)
    private String subscriptionId;

    @ApiModelProperty(value = ModelDescriptions.PUBLIC_IN_ACCOUNT, readOnly = true)
    private boolean publicInAccount = true;

    @ApiModelProperty(SmartSenseSubscriptionModelDescription.AUTOGENERATED)
    private boolean autoGenerated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public boolean isPublicInAccount() {
        return publicInAccount;
    }

    public void setPublicInAccount(boolean publicInAccount) {
        this.publicInAccount = publicInAccount;
    }

    public boolean isAutoGenerated() {
        return autoGenerated;
    }

    public void setAutoGenerated(boolean autoGenerated) {
        this.autoGenerated = autoGenerated;
    }
}
