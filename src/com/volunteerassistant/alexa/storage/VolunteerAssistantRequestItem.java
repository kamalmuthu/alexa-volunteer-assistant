package com.volunteerassistant.alexa.storage;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.util.UUID;

/**
 * Model representing a volunteer request
 */
@DynamoDBTable(tableName = "VolunteerAssistantRequestItem")
public class VolunteerAssistantRequestItem {

    private String guid;

    private String customerId;

    private String requestorName;

    private String headCount;

    private String date;

    private String time;

    public void VolunteerAssistantRequestItem() {
        this.guid = UUID.randomUUID().toString();
    }

    @DynamoDBHashKey(attributeName = "GUID")
    public String getGuid() {
        if (null == this.guid) this.guid = UUID.randomUUID().toString();
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getRequestorName() {
        return requestorName;
    }

    public void setRequestorName(String requestorName) {
        this.requestorName = requestorName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getHeadCount() {
        return headCount;
    }

    public void setHeadCount(String headCount) {
        this.headCount = headCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
