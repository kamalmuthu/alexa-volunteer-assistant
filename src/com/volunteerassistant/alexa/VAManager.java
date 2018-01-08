package com.volunteerassistant.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volunteerassistant.alexa.storage.VADynamoDbClient;
import com.volunteerassistant.alexa.storage.VolunteerAssistantDAO;
import com.volunteerassistant.alexa.storage.VolunteerAssistantRequestItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Restaurant Assistant's VUI flow manager
 */
public class VAManager {

    private static final Logger log = LoggerFactory.getLogger(VAManager.class);

    private static final String HEAD_COUNT = "headCount";
    private static final String DATE_REQUESTED = "date";
    private static final String TIME_REQUESTED = "time";

    private VolunteerAssistantDAO vaDao = null;

    public VAManager(AmazonDynamoDBClient dbClient) {
        this.vaDao = new VolunteerAssistantDAO(new VADynamoDbClient(dbClient));
    }

    /**
     * Creates an initial launch response based on user invocation style
     *
     * @param request
     * @param session
     * @return
     */
    public SpeechletResponse getLaunchResponse(LaunchRequest request, Session session) {
        String speechText = "Welcome to Volunteer Assist. How many volunteers are you looking for?";
        String repromptText = "I would like to know the number of volunteers and date time when you would need them.";

        log.info("Providing launch response:" + speechText);

        return getAskSpeechletResponse(speechText, repromptText);
    }

    public SpeechletResponse getRequestVolunteersResponse(Intent intent, Session session) {

        // Check if headCount was already specified
        boolean needHeadCount = false;
        boolean needDate = false;
        boolean needTime = false;

        // Fetch slots from intent and store them in session attributes
        if (null != intent.getSlot(HEAD_COUNT).getValue()) {
            session.getAttributes().put(HEAD_COUNT, intent.getSlot(HEAD_COUNT).getValue());
        }

        if (null != intent.getSlot(DATE_REQUESTED).getValue()) {
            session.getAttributes().put(DATE_REQUESTED, intent.getSlot(DATE_REQUESTED).getValue());
        }

        if (null != intent.getSlot(TIME_REQUESTED).getValue()) {
            session.getAttributes().put(TIME_REQUESTED, intent.getSlot(TIME_REQUESTED).getValue());
        }

        String headCount = (String) session.getAttributes().get(HEAD_COUNT);
        String reqDate = (String) session.getAttributes().get(DATE_REQUESTED);
        String reqTime = (String) session.getAttributes().get(TIME_REQUESTED);

        // Check to see if we have all the slot values
        if (StringUtils.isNullOrEmpty(headCount)) needHeadCount = true;
        if (StringUtils.isNullOrEmpty(reqDate)) needDate = true;
        if (StringUtils.isNullOrEmpty(reqTime)) needTime = true;

        String speechText = "";
        String repromptText = "";

        if (needHeadCount) {
            speechText = repromptText = "How many volunteers are you looking for?";
        } else if (needDate) {
            speechText = repromptText = "Okay. What date and time would you like them to be here?";
        } else if (needTime) {
            speechText = repromptText = "Okay. What time would do you want them on " + reqDate + "?";
        } else if (!needHeadCount && !needDate && !needTime) {
            String name = getUserProfileData(session);

            // We have all the slots. Save it in DynamoDb and send confirmation
            VolunteerAssistantRequestItem requestItem = new VolunteerAssistantRequestItem();
            requestItem.setCustomerId(session.getUser().getUserId());
            requestItem.setDate(reqDate);
            requestItem.setHeadCount(headCount);
            requestItem.setTime(reqTime);
            requestItem.setRequestorName(name);
            vaDao.saveVolunteerRequest(requestItem);
            speechText = repromptText = "Got it. I will text backup volunteers to see if anybody is available on " + reqDate + " at " + reqTime + ".";

            // Clear the session now that we have stored the request
            clearSession(session);
        } else {
            // Catch all to once again ask for all the values
            speechText = repromptText = "Oops! Something went wrong. Can you once again tell me how many volunteers and what date and time?";
        }

        log.info("Volunteers assist slots:" + headCount + " " + reqDate + " " + reqTime);

//        VolunteerAssistantRequestItem customerPlan = raDAO.fetchCustomerPlan(session);
//        VAPlanDataItem plan = VAPlanDataItem.newInstance();
//        plan.setCuisine(cuisine);
//        plan.setRestaurantName(restaurant);
//        customerPlan.getCustomerPlans().add(plan);
//
//        raDAO.saveCustomerPlan(customerPlan);

        return getAskSpeechletResponse(speechText, repromptText);
    }


    /**
     * Returns an ask Speechlet response for a speech and reprompt text.
     *
     * @param speechText   Text for speech output
     * @param repromptText Text for reprompt output
     * @return ask Speechlet response for a speech and reprompt text
     */
    private SpeechletResponse getAskSpeechletResponse(String speechText, String repromptText) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Session");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText(repromptText);
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Stop the session and clear any data that was captured during this session
     *
     * @param intent
     * @param session
     * @return
     */
    public SpeechletResponse getCancelResponse(Intent intent, Session session) {
        String speechText = "Okay. Goodbye!";
        String repromptText = "Okay. Goodbye!";

        log.info("Providing cancel response:" + speechText);

        clearSession(session);

        return getAskSpeechletResponse(speechText, repromptText);
    }

    private void clearSession(Session session) {
        session.getAttributes().put(HEAD_COUNT, null);
        session.getAttributes().put(DATE_REQUESTED, null);
        session.getAttributes().put(TIME_REQUESTED, null);
    }

    // Fetch user profile data
    public String getUserProfileData(Session session) {
        String name = null;
        try {
            URL url = new URL("https://api.amazon.com/user/profile?access_token=" + session.getUser().getAccessToken());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (conn.getInputStream())));
                String line = null;
                String body = "";
                while ((line = br.readLine()) != null) {
                    body += line;
                }

                HashMap<String, Object> result =
                        new ObjectMapper().readValue(body, HashMap.class);

                name = (String) result.get("name");

                log.info("User name: " + name);
            }
        } catch (Exception e) {
            log.info("Could not fetch user profile data!" + e.getMessage());
            return null;
        }

        return name;
    }

}
