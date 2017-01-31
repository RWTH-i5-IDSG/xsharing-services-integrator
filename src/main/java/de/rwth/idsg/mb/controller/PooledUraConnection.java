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
package de.rwth.idsg.mb.controller;

import de.ivu.realtime.modules.ura.client.ApacheHttpUraConnection;
import de.ivu.realtime.modules.ura.client.UraConnection;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Functionality-wise this implementation differs from the default {@link ApacheHttpUraConnection} in two ways:
 *
 * 1) Instead of creating an HttpClient and a connection per request, uses a pool with a connection manager.
 * 2) Uses SLF4J as logger instead of JUL. This was NOT possible by extending the class. Therefore, the basic
 * implementation was just copied and the logger calls were adapted to use SLF4J.
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 02.07.2015
 */
@Slf4j
public class PooledUraConnection implements UraConnection, Closeable {

    private final String endpoint;
    private final PoolingHttpClientConnectionManager connectionManager;
    private final CloseableHttpClient threadSafeClient;

    public PooledUraConnection(String host, int port) {
        this.endpoint = createEndpoint(host, port);
        this.connectionManager = createConnectionManager();
        this.threadSafeClient = createHttpClient();
    }

    @Override
    public void close() {
        try {
            threadSafeClient.close();
        } catch (IOException e) {
            log.error("Exception occurred", e);
        }
        connectionManager.close();
    }

    @Override
    public String sendRequest(String requestUrl) throws IOException, ClientProtocolException {
        try {
            final HttpGet get = new HttpGet(getEndpoint() + '/' + requestUrl);
            log.trace("Created GET-Request {}", get);
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                @Override
                public String handleResponse(HttpResponse response) throws IOException {
                    int status = response.getStatusLine().getStatusCode();
                    log.trace("[{}] got status {}", get, status);
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        String json = entity != null ? EntityUtils.toString(entity) : null;
                        log.trace("[{}] got response {}", get, json);
                        return json;
                    } else {
                        log.trace("[{}] got wrong status {}", get, status);
                        throw new ClientProtocolException(buildErrorMessage(get, status));
                    }
                }
            };
            return threadSafeClient.execute(get, responseHandler);
        } catch (Exception e) {
            log.warn("URA request failed with message: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String createEndpoint(String host, int port) {
        return "http://" + host + ':' + port + "/interfaces/ura";
    }

    private PoolingHttpClientConnectionManager createConnectionManager() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        // Increase max total connection to 20
        cm.setMaxTotal(20);
        // Increase default max connection per route to 20
        cm.setDefaultMaxPerRoute(20);
        return cm;
    }

    private CloseableHttpClient createHttpClient() {
        return HttpClients.custom()
                          .setConnectionManager(connectionManager)
                          .setConnectionManagerShared(true)
                          .build();
    }

    private String buildErrorMessage(HttpGet get, int status) {
        return "[" + get + "] returned unexpected response status: " + status;
    }
}
