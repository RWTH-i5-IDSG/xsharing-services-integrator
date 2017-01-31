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

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.02.2015
 */
@Slf4j
@Provider
@ServerInterceptor
public class LogInterceptor implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        URI uri = containerRequestContext.getUriInfo().getRequestUri();
        log.info("Requested URL: {}", uri.toString());
    }
}
