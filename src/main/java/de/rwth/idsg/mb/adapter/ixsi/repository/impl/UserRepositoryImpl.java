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
package de.rwth.idsg.mb.adapter.ixsi.repository.impl;

import de.rwth.idsg.mb.adapter.ixsi.repository.UserRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.user.UserWorker;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.RecordMapper;
import xjc.schema.ixsi.UserInfoType;
import xjc.schema.ixsi.UserStateType;
import xjc.schema.ixsi.UserType;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.List;

import static jooq.db.ixsi.tables.Provider.PROVIDER;
import static jooq.db.ixsi.tables.User.USER;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 13.07.2015
 */
@Slf4j
@Stateless
public class UserRepositoryImpl implements UserRepository {

    @Inject private DSLContext ctx;

    @Override
    public List<UserInfoType> getUserInfos(int partnerId) {
        return ctx.select(USER.PROVIDER_ID,
                          USER.USER_ID)
                  .from(USER)
                  .join(PROVIDER)
                    .on(PROVIDER.PROVIDER_ID.eq(USER.PROVIDER_ID))
                  .where(PROVIDER.PARTNER_ID.eq(partnerId))
                    .and(USER.STATE.eq(UserStateType.OPERATIVE.value()))
                  .fetch()
                  .map(new UserInfoMapper());
    }

    @Override
    public List<String> getIds(String providerId) {
        return ctx.select(USER.USER_ID)
                  .from(USER)
                  .where(USER.PROVIDER_ID.eq(providerId))
                  .fetch(USER.USER_ID);
    }

    @Override
    public void upsertUserList(DateTime eventTimestamp, List<UserType> userList) {
        log.debug("Size of the arrived user list: {}", userList.size());

        // Assumption: All users in a request belong to the same provider
        List<String> dbList = getIds(userList.get(0).getID().getProviderID());

        // -------------------------------------------------------------------------
        // Prepare environment
        // -------------------------------------------------------------------------

        UserWorker userWorker = new UserWorker(ctx, dbList, new Timestamp(eventTimestamp.getMillis()));
        userWorker.prepare();

        // -------------------------------------------------------------------------
        // Bind values
        // -------------------------------------------------------------------------

        for (UserType user : userList) {
            userWorker.bind(user);
        }

        // -------------------------------------------------------------------------
        // Execute
        // -------------------------------------------------------------------------

        userWorker.execute();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static class UserInfoMapper implements RecordMapper<Record2<String, String>, UserInfoType> {

        @Override
        public UserInfoType map(Record2<String, String> record) {
            return new UserInfoType().withProviderID(record.value1())
                                     .withUserID(record.value2());
        }
    }
}
