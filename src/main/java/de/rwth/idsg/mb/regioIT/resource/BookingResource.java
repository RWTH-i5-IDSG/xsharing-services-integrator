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

import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.context.UserInOutContext;
import de.rwth.idsg.mb.adapter.ixsi.service.EnabledQueryService;
import de.rwth.idsg.mb.adapter.ixsi.store.UserInOutContextStore;
import de.rwth.idsg.mb.adapter.ixsi.store.WebSocketClientEndpointStore;
import de.rwth.idsg.mb.controller.ProviderLookupTable;
import de.rwth.idsg.mb.controller.dto.ProviderDTO;
import de.rwth.idsg.mb.regioIT.client.rest.AuthHeaderCheck;
import de.rwth.idsg.mb.regioIT.client.rest.params.BookingParams;
import de.rwth.idsg.mb.regioIT.client.rest.params.BookingResponse;
import de.rwth.idsg.mb.regioIT.client.rest.params.UnlockBookingResponse;
import de.rwth.idsg.mb.utils.BasicUtils;
import de.rwth.idsg.mb.utils.RegioITUtils;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import xjc.schema.ixsi.AttributeType;
import xjc.schema.ixsi.AuthType;
import xjc.schema.ixsi.BookingStateType;
import xjc.schema.ixsi.BookingType;
import xjc.schema.ixsi.ErrorType;
import xjc.schema.ixsi.OriginDestType;
import xjc.schema.ixsi.UserInfoType;

import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Wolfgang Kluth <kluth@dbis.rwth-aachen.de>
 * @since 13.05.2015
 */
@Slf4j
@Path("bookings/{providerName}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@AuthHeaderCheck
@Singleton
public class BookingResource {

    @Inject private UserInOutContextStore userInOutContextStore;
    @Inject private EnabledQueryService queryService;
    @Inject private ProviderLookupTable providerLookupTable;
    @Inject private WebSocketClientEndpointStore endpointStore;

    // -------------------------------------------------------------------------
    // Requests
    // -------------------------------------------------------------------------

    @POST
    public void create(@PathParam("providerName") String providerName, BookingParams.Create params,
                       @Suspended AsyncResponse response) {
        log.info("Received booking create @ {}: {}", providerName, params);

        UserInOutContext pc;
        try {
            ProviderDTO dto = providerLookupTable.getIxsiInfo(providerName);
            Integer partnerId = dto.getPartnerId();
            String providerId = dto.getProviderId();

            AuthType authType = buildAuth(providerId, params.getUserId());

            String fromPlaceId = params.getFromPlaceId();
            String toPlaceId = params.getToPlaceId();

            OriginDestType from = null;
            if (fromPlaceId != null) {
                from = new OriginDestType().withPlaceID(fromPlaceId);
            }

            OriginDestType to = null;
            if (toPlaceId != null) {
                to = new OriginDestType().withPlaceID(toPlaceId);
            }

            pc = queryService.createBooking(
                    get(partnerId), providerId, params.getVehicleId(),
                    from,
                    to,
                    secondsToDateTime(params.getStartTime()),
                    secondsToDateTime(params.getEndTime()),
                    params.getMaxWaitInMin(),
                    authType, null);
        } catch (Exception e) {
            internalError(RegioITUtils.buildAdapterError(e), response);
            return;
        }

        pc.setAsyncResponse(response);
        userInOutContextStore.add(pc.getTransaction(), pc);
    }

    @PUT
    public void update(@PathParam("providerName") String providerName, BookingParams.Update params,
                       @Suspended AsyncResponse response) {
        log.info("Received booking update @ {}: {}", providerName, params);

        UserInOutContext pc;
        try {
            ProviderDTO dto = providerLookupTable.getIxsiInfo(providerName);
            Integer partnerId = dto.getPartnerId();
            String providerId = dto.getProviderId();

            AuthType authType = buildAuth(providerId, params.getUserId());

            pc = queryService.changeBookingTime(
                    get(partnerId), params.getBookingNo(),
                    secondsToDateTime(params.getStartTime()),
                    secondsToDateTime(params.getEndTime()),
                    params.getMaxWaitInMin(),
                    authType, null);
        } catch (Exception e) {
            internalError(RegioITUtils.buildAdapterError(e), response);
            return;
        }

        pc.setAsyncResponse(response);
        userInOutContextStore.add(pc.getTransaction(), pc);
    }

    @DELETE
    public void cancel(@PathParam("providerName") String providerName, BookingParams.Basic params,
                       @Suspended AsyncResponse response) {
        log.info("Received booking cancel @ {}: {}", providerName, params);

        UserInOutContext pc;
        try {
            ProviderDTO dto = providerLookupTable.getIxsiInfo(providerName);
            Integer partnerId = dto.getPartnerId();
            String providerId = dto.getProviderId();

            AuthType authType = buildAuth(providerId, params.getUserId());

            pc = queryService.cancelBooking(get(partnerId), params.getBookingNo(), authType, null);
        } catch (Exception e) {
            internalError(RegioITUtils.buildAdapterError(e), response);
            return;
        }

        pc.setAsyncResponse(response);
        userInOutContextStore.add(pc.getTransaction(), pc);
    }

    @PUT
    @Path("/unlock")
    public void unlock(@PathParam("providerName") String providerName, BookingParams.Basic params,
                       @Suspended AsyncResponse response) {
        log.info("Received booking unlock @ {}: {}", providerName, params);

        UserInOutContext pc;
        try {
            Integer partnerId = providerLookupTable.getIxsiInfo(providerName).getPartnerId();
            AuthType authType = buildAuth(providerName, params.getUserId());

            pc = queryService.changeBookingState(get(partnerId), params.getBookingNo(),
                                                 BookingStateType.OPEN,
                                                 authType, null);
        } catch (Exception e) {
            internalError(RegioITUtils.buildAdapterError(e), response);
            return;
        }

        pc.setAsyncResponse(response);
        userInOutContextStore.add(pc.getTransaction(), pc);
    }

    // -------------------------------------------------------------------------
    // Responses
    // -------------------------------------------------------------------------

    // Used for both create and update
    @Asynchronous
    public void asyncBookingResponse(BookingType booking, AsyncResponse httpResponse) {
        Long startTime = null;
        Long endTime = null;
        String details = null;

        if (booking.isSetTimePeriod()) {
            startTime = BasicUtils.toSeconds(booking.getTimePeriod().getBegin());
            endTime = BasicUtils.toSeconds(booking.getTimePeriod().getEnd());
        }

        if (booking.isSetInfo()) {
            details = RegioITUtils.infoToDetailsString(booking.getInfo());
        }

        httpResponse.resume(new BookingResponse(booking.getID(), startTime, endTime, details));
    }

    @Asynchronous
    public void asyncBookingUnlockResponse(List<AttributeType> attributes, AsyncResponse httpResponse) {
        List<String> attribute = RegioITUtils.attributesToStringList(attributes);
        httpResponse.resume(new UnlockBookingResponse(attribute));
    }

    @Asynchronous
    public void asyncErrorResponse(List<ErrorType> ixsiErrors, AsyncResponse httpResponse) {
        httpResponse.resume(
                Response.status(Response.Status.BAD_REQUEST)
                        .entity(ixsiErrors)
                        .build()
        );
    }

    private void internalError(List<ErrorType> ixsiErrors, AsyncResponse httpResponse) {
        httpResponse.resume(
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(ixsiErrors)
                        .build()
        );
    }

    @Asynchronous
    public void asyncOkResponse(AsyncResponse httpResponse) {
        httpResponse.resume(
                Response.status(Response.Status.OK)
                        .build()
        );
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private PartnerContext get(int partnerId) {
        return endpointStore.getPartnerContext(partnerId);
    }

    private DateTime secondsToDateTime(long seconds) {
        return new DateTime(TimeUnit.SECONDS.toMillis(seconds));
    }

    private AuthType buildAuth(String providerId, String userId) {
        UserInfoType userInfo = new UserInfoType()
                .withUserID(userId)
                .withProviderID(providerId);

        return new AuthType().withUserInfo(userInfo);
    }
}
