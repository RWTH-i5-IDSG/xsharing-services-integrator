/*
 * Copyright (C) 2014-2017 RWTH Aachen University - Information Systems - Intelligent Distributed Systems Group.
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.rwth.idsg.mb.adapter.ixsi.client.impl;

import de.rwth.idsg.mb.Constants;
import de.rwth.idsg.mb.adapter.ixsi.IxsiProcessingException;
import de.rwth.idsg.mb.adapter.ixsi.client.api.Parser;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;
import xjc.schema.ixsi.IxsiMessageType;
import xjc.schema.ixsi.ObjectFactory;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.10.2014
 */
@Slf4j
public enum ParserImpl implements Parser {
    SINGLETON;

    private final JAXBContext jaxbContext;
    private final ObjectFactory objectFactory;
    private final Schema schema;

    ParserImpl() {
        try {
            // is thread-safe
            jaxbContext = JAXBContext.newInstance(IxsiMessageType.class);
        } catch (JAXBException e) {
            throw new IxsiProcessingException(e);
        }

        objectFactory = new ObjectFactory();

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        URL xsdURL = getClass().getClassLoader().getResource(Constants.Ixsi.XML_SCHEMA_FILE);

        if (xsdURL == null) {
            throw new IxsiProcessingException("XML schema could not be found/loaded");
        } else {
            try {
                schema = schemaFactory.newSchema(xsdURL);
            } catch (SAXException e) {
                throw new IxsiProcessingException("Error occurred", e);
            }
        }
    }

    @Override
    public IxsiMessageType unmarshal(String str) throws JAXBException {
        log.trace("Entered unmarshal...");

        Unmarshaller um = jaxbContext.createUnmarshaller();
        // Validate against the schema
        um.setSchema(schema);
        StringReader reader = new StringReader(str);
        StreamSource source = new StreamSource(reader);
        return um.unmarshal(source, IxsiMessageType.class).getValue();
    }

    @Override
    public String marshal(IxsiMessageType ixsi) throws JAXBException {
        log.trace("Entered marshal...");

        JAXBElement<IxsiMessageType> outgoing = objectFactory.createIxsi(ixsi);
        Marshaller m = jaxbContext.createMarshaller();
        // Validate against the schema
        m.setSchema(schema);
        // Pretty print?
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        // Drop the XML declaration?
        m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

        StringWriter stringWriter = new StringWriter();
        m.marshal(outgoing, stringWriter);
        return stringWriter.toString();
    }
}
