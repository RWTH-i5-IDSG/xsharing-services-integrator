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
package de.rwth.idsg.mb.regioIT.client.rest;

import de.rwth.idsg.mb.AppConfiguration;
import de.rwth.idsg.mb.regioIT.AuthCredentials;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 08.05.2015
 */
@Slf4j
@Provider
@AuthHeaderCheck
public class AuthHeaderFilter implements ClientRequestFilter, ContainerRequestFilter {

    @Inject private AuthCredentials authCredentials;
    @Inject private AppConfiguration appConfiguration;

    private String authHeader;
    private String authToken;

    @PostConstruct
    public void init() {
        this.authHeader = appConfiguration.getResource().getAuthHeader();
        this.authToken = appConfiguration.getResource().getAuthToken();
    }

    // -------------------------------------------------------------------------
    // Adapter as REST client
    // -------------------------------------------------------------------------

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.getHeaders().add("X-MB-AUTH-HEADER", authCredentials.getToken());
    }

    // -------------------------------------------------------------------------
    // Adapter as REST resource
    // -------------------------------------------------------------------------

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String value = requestContext.getHeaderString(authHeader);

        if (!isValid(value)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }

    private boolean isValid(String value) {
        return authToken.equals(value);
    }
}
