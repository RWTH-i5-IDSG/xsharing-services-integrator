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
package de.rwth.idsg.mb.regioIT.client.rest.api;

import de.rwth.idsg.mb.regioIT.client.rest.params.BookingAlertParams;
import de.rwth.idsg.mb.regioIT.client.rest.params.ExternalBookingParams;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Wolfgang Kluth <kluth@dbis.rwth-aachen.de>
 * @since 20.04.2015
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON})
public interface BookingClient {

    @PUT
    @Path("bookingadapter/bookings/{providerName}/{bookingNo}")
    Response.Status putExternalBooking(@PathParam("providerName") String providerName,
                                       @PathParam("bookingNo") String bookingNo,
                                       ExternalBookingParams externalBookingParams);

    @POST
    @Path("bookingadapter/bookings/alert")
    Response.Status postBookingAlert(List<BookingAlertParams> bookingAlertParams);
}
