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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.List;

import static jooq.db.ixsi.tables.ProviderAttribute.PROVIDER_ATTRIBUTE;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 16.12.2014
 */
@Slf4j
@RequiredArgsConstructor
public class ProviderAttributeWorker implements ChildWorker<String, String> {
    private final DSLContext ctx;
    private BatchBindStep insertBatch;
    private List<String> itemIdList = new ArrayList<>();

    @Override
    public void prepare() {
        insertBatch = ctx.batch(
                ctx.insertInto(PROVIDER_ATTRIBUTE,
                        PROVIDER_ATTRIBUTE.PROVIDER_ID,
                        PROVIDER_ATTRIBUTE.ATTRIBUTE_ID)
                        .values("", null)
        );
    }

    @Override
    public void bindDelete(String parentId) {
        itemIdList.add(parentId);
    }

    @Override
    public void bindInsert(String parentId, String item) {
        insertBatch.bind(
                parentId,
                item
        );
    }

    /**
     * @deprecated  Use the individual bind methods instead
     */
    @Override
    @Deprecated
    public void bind(String parentId, String item) {
        // no-op
    }

    @Override
    public void execute() {
        if (itemIdList.isEmpty()) {
            log.debug("Nothing to write in table {}", PROVIDER_ATTRIBUTE.getName());
        } else {
            ctx.delete(PROVIDER_ATTRIBUTE)
               .where(PROVIDER_ATTRIBUTE.PROVIDER_ID.in(itemIdList))
               .execute();

            insertBatch.execute();
        }
    }
}
