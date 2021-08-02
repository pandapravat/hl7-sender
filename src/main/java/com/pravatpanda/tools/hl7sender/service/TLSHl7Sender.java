/*
 * Copyright (c) 2021. Pravat Panda
 * All rights reserved
 */

package com.pravatpanda.tools.hl7sender.service;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.ParserConfiguration;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.ValidationContext;
import ca.uhn.hl7v2.validation.impl.ValidationContextFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by 212791719 (Pravat Panda)on 7/22/2021.
 */
public class TLSHl7Sender {

    /**
     * Send string.
     *
     * @param host               the host
     * @param port               the port
     * @param message            the message
     * @param tls                the tls
     * @param trustFileLoc       the trust file loc
     * @param trustStorePassText the trust store pass text
     * @return the string
     * @throws HL7Exception the hl 7 exception
     * @throws IOException  the io exception
     * @throws LLPException the llp exception
     */
    public String send(String host, int port, String message, boolean tls, String trustFileLoc, String trustStorePassText)
            throws HL7Exception, IOException, LLPException {

        if(tls) {
            System.setProperty("javax.net.ssl.trustStore", trustFileLoc);
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassText);
        }

        DefaultHapiContext hapiContext = new DefaultHapiContext();
        Connection connection = hapiContext.newClient(host, port, tls);
        try {
            hapiContext.setValidationContext((ValidationContext)ValidationContextFactory.noValidation());
            hapiContext.setParserConfiguration(getParserConfig());
            PipeParser pipeParser = getPipeParser(hapiContext);
            return sendADTMessage(message, connection, pipeParser);
        } finally {
            connection.close();
            hapiContext.getConnectionHub().discard(connection);
            connection = null;
            hapiContext.close();
        }
    }

    /**
     * Gets parser config.
     *
     * @return the parser config
     */
    ParserConfiguration getParserConfig() {
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setAllowUnknownVersions(true);
        parserConfiguration.setValidating(false);
        parserConfiguration.setDefaultObx2Type("ST");
        parserConfiguration.setInvalidObx2Type("ST");
        return parserConfiguration;
    }
    private PipeParser getPipeParser(HapiContext hapiContext) {

        PipeParser p = new PipeParser(hapiContext) {
            public String getVersion(String message) throws HL7Exception {
                return "2.6";
            }
        };
        return p;
    }

    private static String sendADTMessage(String binding, Connection conn, PipeParser p) throws HL7Exception, LLPException, IOException {
        //println data

        String value = binding.replaceAll("\n", "\r");

        Initiator initiator = conn.getInitiator();

        Message adt = p.parse(value);
        initiator.setTimeout(30000L, TimeUnit.MILLISECONDS);

        Message ack = initiator.sendAndReceive(adt);

        return "ACKNOWLEDGMENT ::::::\n " + ack.encode().replaceAll("\r", "\n");
    }
}
