/**
 * Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
 * <p>
 * http://aws.amazon.com/apache2.0/
 * <p>
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
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
