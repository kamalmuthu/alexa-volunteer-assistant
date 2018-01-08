package com.volunteerassistant.alexa.storage;

import com.amazon.speech.speechlet.Session;
import org.apache.commons.lang3.StringUtils;

/**
 * DAO to interface with the DyanmoDB persistence layer for the skill
 */
public class VolunteerAssistantDAO {

    private final VADynamoDbClient dynamoDbClient;

    public VolunteerAssistantDAO(VADynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public VolunteerAssistantRequestItem fetchVolunteerRequest(Session session) {
        VolunteerAssistantRequestItem requestItem = new VolunteerAssistantRequestItem();
        requestItem.setCustomerId(session.getUser().getUserId());

        requestItem = dynamoDbClient.loadItem(requestItem);

        if (null == requestItem) {
            requestItem = new VolunteerAssistantRequestItem();
        }

        return requestItem;
    }


    public void saveVolunteerRequest(VolunteerAssistantRequestItem requestItem) {
        if (StringUtils.isEmpty(requestItem.getCustomerId())) {
            throw new IllegalStateException("Need customerId to save the request!");
        }

        dynamoDbClient.saveItem(requestItem);
    }
}
