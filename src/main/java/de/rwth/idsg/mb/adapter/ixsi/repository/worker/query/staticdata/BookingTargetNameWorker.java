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
package de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata;

import de.rwth.idsg.mb.adapter.ixsi.repository.worker.ChildWorker;
import de.rwth.idsg.mb.utils.BasicUtils;
import jooq.db.ixsi.enums.Language;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import xjc.schema.ixsi.BookingTargetIDType;
import xjc.schema.ixsi.TextType;

import static jooq.db.ixsi.tables.BookingTargetName.BOOKING_TARGET_NAME;

/**
 * Created by swam on 17/12/14.
 */

@Slf4j
@RequiredArgsConstructor
public class BookingTargetNameWorker implements ChildWorker<BookingTargetIDType, TextType> {
    private final DSLContext ctx;

    private BatchBindStep deleteBatch;
    private BatchBindStep insertBatch;

    @Override
    public void prepare() {
        deleteBatch = ctx.batch(
                ctx.delete(BOOKING_TARGET_NAME)
                        .where(BOOKING_TARGET_NAME.BOOKING_TARGET_ID.equal(""))
                        .and(BOOKING_TARGET_NAME.PROVIDER_ID.equal(""))
        );

        insertBatch = ctx.batch(
                ctx.insertInto(BOOKING_TARGET_NAME,
                        BOOKING_TARGET_NAME.BOOKING_TARGET_ID,
                        BOOKING_TARGET_NAME.PROVIDER_ID,
                        BOOKING_TARGET_NAME.LANGUAGE,
                        BOOKING_TARGET_NAME.VALUE)
                   .values("", "", null, null)
        );
    }

    @Override
    public void bindDelete(BookingTargetIDType parentId) {
        deleteBatch.bind(
                parentId.getBookeeID(),
                parentId.getProviderID()
        );
    }

    @Override
    public void bindInsert(BookingTargetIDType parentId, TextType name) {
        Language lng = BasicUtils.getDatabaseLangOrDefault(name.getLanguage());

        insertBatch.bind(
                parentId.getBookeeID(),
                parentId.getProviderID(),
                lng,
                name.getText()
        );
    }

    /**
     * @deprecated  Use the individual bind methods instead
     */
    @Override
    @Deprecated
    public void bind(BookingTargetIDType parentId, TextType item) {
        // No-op
    }

    @Override
    public void execute() {
        deleteBatch.execute();
        insertBatch.execute();
    }
}
