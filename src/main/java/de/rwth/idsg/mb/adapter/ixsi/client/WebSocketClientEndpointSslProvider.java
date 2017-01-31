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
package de.rwth.idsg.mb.adapter.ixsi.client;

import io.undertow.websockets.jsr.DefaultWebSocketClientSslProvider;
import io.undertow.websockets.jsr.WebsocketClientSslProvider;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.XnioWorker;
import org.xnio.ssl.JsseXnioSsl;
import org.xnio.ssl.XnioSsl;

import javax.net.ssl.SSLContext;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Since {@link DefaultWebSocketClientSslProvider} caused us connection problems,
 * we decided to use a simpler provider
 *
 * Partial inspiration:
 * https://blog.heckel.xyz/2014/10/30/http-basic-auth-for-websocket-connections-with-undertow/
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 22.12.2015
 */
public class WebSocketClientEndpointSslProvider implements WebsocketClientSslProvider {

    private static final SSLContext SSL_CONTEXT = getSSLContext();

    @Override
    public XnioSsl getSsl(XnioWorker worker, Class<?> annotatedEndpoint, URI uri) {
        return getSslInternal(worker);
    }

    @Override
    public XnioSsl getSsl(XnioWorker worker, Object annotatedEndpointInstance, URI uri) {
        return getSslInternal(worker);
    }

    @Override
    public XnioSsl getSsl(XnioWorker worker, Endpoint endpoint, ClientEndpointConfig cec, URI uri) {
        return getSslInternal(worker);
    }

    private XnioSsl getSslInternal(XnioWorker worker) {
        return new JsseXnioSsl(worker.getXnio(), OptionMap.create(Options.USE_DIRECT_BUFFERS, true), SSL_CONTEXT);
    }

    /**
     * https://issues.jboss.org/browse/UNDERTOW-414
     * https://github.com/TooTallNate/Java-WebSocket/issues/263
     */
    private static SSLContext getSSLContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            // will use java's default key and trust store
            // which is sufficient unless you deal with self-signed certificates
            sslContext.init(null, null, null);
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);

        }
    }
}
