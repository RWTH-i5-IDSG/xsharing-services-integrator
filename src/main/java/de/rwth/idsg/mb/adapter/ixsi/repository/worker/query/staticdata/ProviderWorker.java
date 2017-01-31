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

import de.rwth.idsg.mb.adapter.ixsi.repository.worker.ParentWorker;
import jooq.db.ixsi.enums.Status;
import lombok.RequiredArgsConstructor;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import xjc.schema.ixsi.ProviderType;

import java.util.List;

import static jooq.db.ixsi.tables.Provider.PROVIDER;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 16.12.2014
 */
@RequiredArgsConstructor
public class ProviderWorker implements ParentWorker<ProviderType> {
    private final DSLContext ctx;
    private final List<String> dbList;
    private final int partnerId;

    private BatchBindStep insertBatch;
    private BatchBindStep updateBatch;

    @Override
    public void prepare() {
        insertBatch = ctx.batch(
                ctx.insertInto(PROVIDER,
                        PROVIDER.STATUS,
                        PROVIDER.PARTNER_ID,
                        PROVIDER.PROVIDER_ID,
                        PROVIDER.NAME,
                        PROVIDER.CUSTOMER_CHOICE,
                        PROVIDER.SHORT_NAME)
                   .values(null, 0, "", null, null, null)
        );

        updateBatch = ctx.batch(
                ctx.update(PROVIDER)
                   .set(PROVIDER.STATUS, (Status) null)
                   .set(PROVIDER.NAME, "")
                   .set(PROVIDER.CUSTOMER_CHOICE, false)
                   .set(PROVIDER.SHORT_NAME, "")
                   .where(PROVIDER.PARTNER_ID.equal(0))
                   .and(PROVIDER.PROVIDER_ID.equal(""))
        );
    }

    @Override
    public void bind(ProviderType p) {
        String providerId = p.getID();

        if (dbList.contains(providerId)) {
            updateBatch.bind(
                    Status.ACTIVE,
                    p.getName(),
                    p.isCustomerChoice(),
                    p.getShortName(),
                    partnerId,
                    providerId
            );
        } else {
            insertBatch.bind(
                    Status.ACTIVE,
                    partnerId,
                    providerId,
                    p.getName(),
                    p.isCustomerChoice(),
                    p.getShortName()
            );
        }
    }

    @Override
    public void execute() {
        insertBatch.execute();
        updateBatch.execute();
    }
}
