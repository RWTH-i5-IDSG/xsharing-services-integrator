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
package de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.user;

import de.rwth.idsg.mb.adapter.ixsi.repository.worker.ParentWorker;
import lombok.RequiredArgsConstructor;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import xjc.schema.ixsi.UserFeatureClassType;
import xjc.schema.ixsi.UserFeatureType;
import xjc.schema.ixsi.UserType;

import java.sql.Timestamp;
import java.util.List;

import static jooq.db.ixsi.tables.User.USER;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 16.07.2015
 */
@RequiredArgsConstructor
public class UserWorker implements ParentWorker<UserType> {
    private final DSLContext ctx;
    private final List<String> dbList;
    private final Timestamp eventTimestamp;

    private BatchBindStep insertBatch;
    private BatchBindStep updateBatch;

    @Override
    public void prepare() {
        insertBatch = ctx.batch(
                ctx.insertInto(USER,
                        USER.PROVIDER_ID,
                        USER.USER_ID,
                        USER.PASSWORD,
                        USER.STATE,
                        USER.PIN,
                        USER.CREATED,
                        USER.UPDATED)
                   .values("", "", "", "", "", null, null)
        );

        updateBatch = ctx.batch(
                ctx.update(USER)
                   .set(USER.PASSWORD, "")
                   .set(USER.STATE, "")
                   .set(USER.PIN, "")
                   .set(USER.UPDATED, (Timestamp) null)
                   .where(USER.PROVIDER_ID.eq(""))
                   .and(USER.USER_ID.eq(""))

        );
    }

    @Override
    public void bind(UserType item) {
        if (dbList.contains(item.getID().getUserID())) {
            updateBatch.bind(
                    item.getID().getPassword(),
                    item.getState().value(),
                    getPin(item.getFeatures()),
                    eventTimestamp,
                    item.getID().getProviderID(),
                    item.getID().getUserID()
            );
        } else {
            insertBatch.bind(
                    item.getID().getProviderID(),
                    item.getID().getUserID(),
                    item.getID().getPassword(),
                    item.getState().value(),
                    getPin(item.getFeatures()),
                    eventTimestamp,
                    eventTimestamp
            );
        }
    }

    @Override
    public void execute() {
        insertBatch.execute();
        updateBatch.execute();
    }

    private String getPin(List<UserFeatureType> features) {
        for (UserFeatureType feat : features) {
            if (feat.getClazz() == UserFeatureClassType.RFID_CARD_PIN) {
                return feat.getValue();
            }
        }
        return null;
    }
}
