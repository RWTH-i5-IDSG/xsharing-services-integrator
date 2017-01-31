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
package de.rwth.idsg.mb.controller.resource;

import de.ivu.realtime.modules.ura.data.request.UraRequestParseException;
import de.rwth.idsg.mb.adapter.ixsi.repository.BookingTargetRepository;
import de.rwth.idsg.mb.controller.service.UraService;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 02.02.2015
 */
@Slf4j
@Path("/")
public class UraResource {

    @Inject private UraService uraService;
    @EJB private BookingTargetRepository bookingTargetRepository;

    @GET
    @Path("instant")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response printObject(@Context HttpServletRequest request) {
        try {

            // TODO: This is uncool. Instead, we have to set the default encoding of Wildfly to UTF-8.
            String queryParamString = URLDecoder.decode(request.getQueryString(), "UTF-8");
            String uraResponseString = uraService.fetchUraResponseString(queryParamString);

            return Response.status(Response.Status.OK)
                           .entity(uraResponseString)
                           .build();

        } catch (UraRequestParseException | UnsupportedEncodingException e) {
            log.warn("Exception happened - '{}: {}'", e.getClass().getSimpleName(), e.getMessage());

            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Could not parse the string into URA request. Reason: " + getExceptionDetails(e))
                           .build();
        }
    }

    private String getExceptionDetails(Exception e) {
        return "'" + e.getClass().getSimpleName() + ": " + e.getMessage() + "'";
    }
}
