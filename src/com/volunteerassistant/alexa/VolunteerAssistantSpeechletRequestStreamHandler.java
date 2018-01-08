package com.volunteerassistant.alexa;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Handler link for Lambda for VolunteerAssist skill
 */
public final class VolunteerAssistantSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Logger log = LoggerFactory.getLogger(VolunteerAssistantSpeechletRequestStreamHandler.class);
    private static final Set<String> supportedApplicationIds;

    static {
        supportedApplicationIds = new HashSet<String>();
        supportedApplicationIds.add("amzn1.ask.skill.1f35fc68-5489-4c2d-a1f6-a602d17b456c");
    }

    public VolunteerAssistantSpeechletRequestStreamHandler() {
        super(new VolunteerAssistantSpeechlet(), supportedApplicationIds);
        log.info("Launch handler");
    }
}
