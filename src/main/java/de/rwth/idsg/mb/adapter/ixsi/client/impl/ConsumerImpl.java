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

import de.rwth.idsg.mb.adapter.ixsi.IxsiProcessingException;
import de.rwth.idsg.mb.adapter.ixsi.TransactionCounter;
import de.rwth.idsg.mb.adapter.ixsi.client.api.Consumer;
import de.rwth.idsg.mb.adapter.ixsi.client.api.IncomingIxsiDispatcher;
import de.rwth.idsg.mb.adapter.ixsi.context.CommunicationContext;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.IxsiMessageType;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.10.2014
 */
@Slf4j
@Stateless
public class ConsumerImpl implements Consumer {

    @EJB private IncomingIxsiDispatcher dispatcher;

    @Override
    public void consume(CommunicationContext context) {
        log.trace("Entered consume...");
        String str = context.getIncomingString();

        try {
            IxsiMessageType ixsi = ParserImpl.SINGLETON.unmarshal(str);
            context.setIncomingIxsi(ixsi);
            TransactionCounter.INSTANCE.incrementResponse(ixsi);
            dispatcher.handle(context);

        } catch (JAXBException e) {
            throw new IxsiProcessingException("Could not unmarshal incoming message: " + str, e);
        }
    }
}
