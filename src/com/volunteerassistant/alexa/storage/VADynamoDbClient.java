package com.volunteerassistant.alexa.storage;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

/**
 * Client for DynamoDB persistance layer for the restaurant assistant skill.
 */
public class VADynamoDbClient {
    private final AmazonDynamoDBClient dynamoDBClient;

    public VADynamoDbClient(final AmazonDynamoDBClient dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
    }

    /**
     * Loads
     *
     * @param tableItem
     * @return
     */
    public VolunteerAssistantRequestItem loadItem(final VolunteerAssistantRequestItem tableItem) {
        DynamoDBMapper mapper = createDynamoDBMapper();
        VolunteerAssistantRequestItem item = mapper.load(tableItem);
        return item;
    }

    /**
     * Stores an item to DynamoDB.
     *
     * @param tableItem
     */
    public void saveItem(final VolunteerAssistantRequestItem tableItem) {
        DynamoDBMapper mapper = createDynamoDBMapper();
        mapper.save(tableItem);
    }

    /**
     * Creates a {@link DynamoDBMapper} using the default configurations.
     *
     * @return
     */
    private DynamoDBMapper createDynamoDBMapper() {
        return new DynamoDBMapper(dynamoDBClient);
    }
}
