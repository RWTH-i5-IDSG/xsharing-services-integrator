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
import de.rwth.idsg.mb.adapter.ixsi.client.WebSocketClientEndpoint;
import de.rwth.idsg.mb.adapter.ixsi.client.api.Producer;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.store.WebSocketClientEndpointStore;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.IxsiMessageType;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.10.2014
 */
@Slf4j
@Stateless
public class ProducerImpl implements Producer {

    @Inject private WebSocketClientEndpointStore store;

    @Override
    public void send(PartnerContext pctx, IxsiMessageType outgoingIxsi) {
        log.trace("Entered send...");

        try {
            WebSocketClientEndpoint endpoint = store.getNext(pctx.getConfig().getPartnerId());
            String outgoingText = ParserImpl.SINGLETON.marshal(outgoingIxsi);
            endpoint.sendMessage(outgoingText);

            // It's important that this is the last call in this block,
            // because we want to increment the counter only
            // when the message is actually sent (i.e. there were no exceptions)
            TransactionCounter.INSTANCE.incrementRequest(outgoingIxsi);

        } catch (NoSuchElementException e) {
            throw new IxsiProcessingException("There is no connection to the IXSI server");

        } catch (JAXBException e) {
            throw new IxsiProcessingException("Could not marshal outgoing IXSI message", e);

        } catch (IOException e) {
            throw new IxsiProcessingException("Failed to communicate with the remote IXSI system", e);
        }
    }
}
