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
package de.rwth.idsg.mb.adapter.ixsi.processor.query.user;

import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.context.UserInOutContext;
import de.rwth.idsg.mb.adapter.ixsi.intercept.ErrorLog;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.UserResponseProcessor;
import de.rwth.idsg.mb.adapter.ixsi.repository.UserRepository;
import de.rwth.idsg.mb.adapter.ixsi.service.EnabledSubscriptionService;
import de.rwth.idsg.mb.regioIT.resource.UserResource;
import xjc.schema.ixsi.ChangeUserRequestType;
import xjc.schema.ixsi.ChangeUserResponseType;
import xjc.schema.ixsi.UserInfoType;
import xjc.schema.ixsi.UserStateType;
import xjc.schema.ixsi.UserType;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 06.05.2015
 */
@Stateless
public class ChangeUserResponseProcessor
        implements UserResponseProcessor<ChangeUserRequestType, ChangeUserResponseType> {

    @Inject private UserResource userResource;
    @Inject private UserRepository userRepository;
    @Inject private EnabledSubscriptionService subscriptionService;

    @Override
    public IxsiFeature getRelatedFeature() {
        return IxsiFeature.QU_ChangeUser;
    }

    @Override
    public Class<ChangeUserResponseType> getProcessingClass() {
        return ChangeUserResponseType.class;
    }

    @Override
    @ErrorLog
    public void process(UserInOutContext<ChangeUserRequestType, ChangeUserResponseType> context) {
        ChangeUserResponseType response = context.getIncoming();

        if (response.isSetError()) {
            userResource.asyncErrorResponse(response.getError(), context.getAsyncResponse());
        } else {
            userResource.asyncResponse(response.getUser(), context.getAsyncResponse());
        }

        if (!response.getUser().isEmpty()) {
            userRepository.upsertUserList(context.getTransaction().getTimeStamp(), response.getUser());
            externalBookingUnSub(context.getPartnerContext(), response.getUser());
        }
    }

    private void externalBookingUnSub(PartnerContext pctx, List<UserType> userList) {
        List<UserInfoType> deleteList = new ArrayList<>();

        for (UserType userType : userList) {
            if (userType.getState() == UserStateType.DELETED) {

                // Strip the user info down to essentials (we only need to identify the user for sub)
                UserInfoType userInfo = userType.getID();
                userInfo.setPassword(null);
                userInfo.setToken(null);

                deleteList.add(userInfo);
            }
        }

        subscriptionService.externalBookingUnSub(pctx, deleteList);
    }
}
