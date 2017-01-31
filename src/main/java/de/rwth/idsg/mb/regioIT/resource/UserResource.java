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
import de.rwth.idsg.mb.regioIT.client.rest.params.UserParams;
import de.rwth.idsg.mb.regioIT.client.rest.params.UserResponse;
import de.rwth.idsg.mb.utils.RegioITUtils;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.AuthType;
import xjc.schema.ixsi.ErrorType;
import xjc.schema.ixsi.UserFeatureClassType;
import xjc.schema.ixsi.UserFeatureType;
import xjc.schema.ixsi.UserInfoType;
import xjc.schema.ixsi.UserStateType;
import xjc.schema.ixsi.UserType;

import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static de.rwth.idsg.mb.Constants.MAJOR_CUSTOMER;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 06.05.2015
 */
@Slf4j
@Path("users/{providerName}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@AuthHeaderCheck
@Singleton
public class UserResource {

    @Inject private UserInOutContextStore userInOutContextStore;
    @Inject private EnabledQueryService queryService;
    @Inject private ProviderLookupTable providerLookupTable;
    @Inject private WebSocketClientEndpointStore endpointStore;

    // -------------------------------------------------------------------------
    // Requests
    // -------------------------------------------------------------------------

    @POST
    public void create(@PathParam("providerName") String providerName, List<UserParams> userParamsList,
                       @Suspended AsyncResponse res) {
        log.info("Received user create @ {}: {}", providerName, userParamsList);

        UserInOutContext pc;
        try {
            ProviderDTO dto = providerLookupTable.getIxsiInfo(providerName);
            Integer partnerId = dto.getPartnerId();
            String providerId = dto.getProviderId();

            AuthType auth = new AuthType().withAnonymous(true);
            List<UserType> users = createIxsiUserList(providerId, userParamsList);

            pc = queryService.createUser(get(partnerId), users, auth, null);
        } catch (Exception e) {
            internalError(RegioITUtils.buildAdapterError(e), res);
            return;
        }

        pc.setAsyncResponse(res);
        userInOutContextStore.add(pc.getTransaction(), pc);
    }

    @PUT
    public void update(@PathParam("providerName") String providerName, List<UserParams> userParamsList,
                       @Suspended AsyncResponse res) {
        log.info("Received user update @ {}: {}", providerName, userParamsList);

        UserInOutContext pc;
        try {
            ProviderDTO dto = providerLookupTable.getIxsiInfo(providerName);
            Integer partnerId = dto.getPartnerId();
            String providerId = dto.getProviderId();

            AuthType auth = new AuthType().withAnonymous(true);
            List<UserType> users = createIxsiUserList(providerId, userParamsList);

            pc = queryService.changeUser(get(partnerId), users, auth, null);
        } catch (Exception e) {
            internalError(RegioITUtils.buildAdapterError(e), res);
            return;
        }

        pc.setAsyncResponse(res);
        userInOutContextStore.add(pc.getTransaction(), pc);
    }

    // -------------------------------------------------------------------------
    // Responses
    // -----------------------------------------------------------------------

    @Asynchronous
    public void asyncResponse(List<UserType> ixsiList, AsyncResponse res) {
        List<String> userIds = new ArrayList<>(ixsiList.size());
        for (UserType u : ixsiList) {
            userIds.add(u.getID().getUserID());
        }
        res.resume(new UserResponse(userIds));
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

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private PartnerContext get(int partnerId) {
        return endpointStore.getPartnerContext(partnerId);
    }

    private List<UserType> createIxsiUserList(String providerId, List<UserParams> userParamsList) {
        List<UserType> ixsiList = new ArrayList<>(userParamsList.size());

        for (UserParams u : userParamsList) {
            UserInfoType ui = new UserInfoType()
                    .withProviderID(providerId)
                    .withUserID(u.getUserId())
                    .withPassword(u.getPassword()); // actually optional, but if null, will not be serialized

            UserType userType = new UserType()
                    .withID(ui)
                    .withState(createIxsiUserState(u.getState()))
                    .withFeatures(createIxsiFeatures(u));

            ixsiList.add(userType);
        }

        return ixsiList;
    }

    private UserStateType createIxsiUserState(UserParams.UserState state) {
        switch (state) {
            case OPERATIVE      : return UserStateType.OPERATIVE;
            case INOPERATIVE    : return UserStateType.INOPERATIVE;
            case DELETED        : return UserStateType.DELETED;
            default             : throw new RuntimeException();
        }
    }

    private List<UserFeatureType> createIxsiFeatures(UserParams u) {
        List<UserFeatureType> list = new ArrayList<>();

        UserFeatureType mj = new UserFeatureType()
                .withClazz(UserFeatureClassType.MAJOR_CUSTOMER_NAME)
                .withValue(MAJOR_CUSTOMER);

        list.add(mj);

        if (u.getPin() != null) {
            UserFeatureType pin = new UserFeatureType()
                    .withClazz(UserFeatureClassType.RFID_CARD_PIN)
                    .withValue(u.getPin());

            list.add(pin);
        }

        return list;
    }
}
