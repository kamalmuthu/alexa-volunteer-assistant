package com.volunteerassistant.alexa;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.*;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Volunteer assistant speechlet to handle skill lifecyle events
 */
public class VolunteerAssistantSpeechlet implements SpeechletV2 {
    private static final Logger log = LoggerFactory.getLogger(VolunteerAssistantSpeechlet.class);

    private AmazonDynamoDBClient amazonDynamoDBClient;

    private VAManager vaManager;

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        log.info("onSessionStarted requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());

        initializeComponents();

    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        log.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());

        // Check if Account is linked
        if (StringUtils.isNullOrEmpty(requestEnvelope.getSession().getUser().getAccessToken())) {
            return getAccountLinkResponse();
        }

        return vaManager.getLaunchResponse(requestEnvelope.getRequest(), requestEnvelope.getSession());
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();

        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        initializeComponents();

        Intent intent = request.getIntent();

        if ("RequestVolunteers".equals(intent.getName())) {
            return vaManager.getRequestVolunteersResponse(intent, session);
        } else if ("AMAZON.HelpIntent".equals(intent.getName())) {
            return getHelpResponse();
        } else if ("AMAZON.CancelIntent".equals(intent.getName()) || "AMAZON.StopIntent".equals(intent.getName())) {
            return vaManager.getCancelResponse(intent, session);
        } else {
            return getAskResponse("HelloWorld", "This is unsupported.  Please try something else.");
        }
    }


    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        log.info("onSessionEnded requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        // any cleanup logic goes here
    }


    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse() {
        String speechText = "You can say 'I am looking for volunteers'!";
        return getAskResponse("HelloWorld", speechText);
    }

    /**
     * Helper method that creates a card object.
     * @param title title of the card
     * @param content body of the card
     * @return SimpleCard the display card to be sent along with the voice response.
     */
    private SimpleCard getSimpleCard(String title, String content) {
        SimpleCard card = new SimpleCard();
        card.setTitle(title);
        card.setContent(content);

        return card;
    }

    private SpeechletResponse getAccountLinkResponse() {
        LinkAccountCard card = new LinkAccountCard();
        card.setTitle("Volunteer Assist: Account setup using Amazon");
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech("To use this skill, you would need to authenticate using Amazon.");
        Reprompt reprompt = getReprompt(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }


    /**
     * Helper method for retrieving an OutputSpeech object when given a string of TTS.
     * @param speechText the text that should be spoken out to the user.
     * @return an instance of SpeechOutput.
     */
    private PlainTextOutputSpeech getPlainTextOutputSpeech(String speechText) {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return speech;
    }

    /**
     * Helper method that returns a reprompt object. This is used in Ask responses where you want
     * the user to be able to respond to your speech.
     * @param outputSpeech The OutputSpeech object that will be said once and repeated if necessary.
     * @return Reprompt instance.
     */
    private Reprompt getReprompt(OutputSpeech outputSpeech) {
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(outputSpeech);

        return reprompt;
    }

    /**
     * Helper method for retrieving an Ask response with a simple card and reprompt included.
     * @param cardTitle Title of the card that you want displayed.
     * @param speechText speech text that will be spoken to the user.
     * @return the resulting card and speech text.
     */
    private SpeechletResponse getAskResponse(String cardTitle, String speechText) {
        SimpleCard card = getSimpleCard(cardTitle, speechText);
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);
        Reprompt reprompt = getReprompt(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }


    /**
     * Initialize linked DyanmoDB
     */
    private void initializeComponents() {
        if (amazonDynamoDBClient == null) {
            amazonDynamoDBClient = new AmazonDynamoDBClient();
            this.vaManager = new VAManager(amazonDynamoDBClient);
        }
    }
}
