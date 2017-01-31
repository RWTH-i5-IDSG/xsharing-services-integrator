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
package de.rwth.idsg.mb.regioIT.resource;

import de.rwth.idsg.mb.adapter.ixsi.repository.ConsumptionRepository;
import de.rwth.idsg.mb.regioIT.client.rest.AuthHeaderCheck;
import de.rwth.idsg.mb.regioIT.client.rest.params.ConsumptionParams;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 09.05.2016
 */
@Slf4j
@Path("consumptions/{providerName}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@AuthHeaderCheck
@Singleton
public class ConsumptionResource {

    @Inject private ConsumptionRepository consumptionRepository;

    @GET
    @Path("/{bookingId}")
    public List<ConsumptionParams> get(@PathParam("providerName") String providerName,
                                       @PathParam("bookingId") String bookingId) {

        return consumptionRepository.getRecords(providerName, bookingId);
    }

}
