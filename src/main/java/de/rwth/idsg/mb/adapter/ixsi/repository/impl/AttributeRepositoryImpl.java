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

import de.rwth.idsg.mb.adapter.ixsi.repository.AttributeRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.AttributeTextWorker;
import de.rwth.idsg.mb.adapter.ixsi.repository.worker.query.staticdata.AttributeWorker;
import de.rwth.idsg.mb.utils.ItemIdComparator;
import jooq.db.ixsi.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import xjc.schema.ixsi.AttributeType;
import xjc.schema.ixsi.TextType;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

import static jooq.db.ixsi.tables.Attribute.ATTRIBUTE;

/**
 * Created by max on 08/12/14.
 */
@Slf4j
@Stateless
public class AttributeRepositoryImpl implements AttributeRepository {

    @Inject private DSLContext ctx;

    @Override
    public List<String> getIds(int partnerId) {
        return ctx.select(ATTRIBUTE.ATTRIBUTE_ID)
                  .from(ATTRIBUTE)
                  .where(ATTRIBUTE.PARTNER_ID.eq(partnerId))
                  .fetch(ATTRIBUTE.ATTRIBUTE_ID);
    }

    @Override
    @Transactional
    public void upsertAttributeList(int partnerId, final List<AttributeType> attributeList) {
        log.debug("Size of the arrived attribute list: {}", attributeList.size());

        List<String> dbList = getIds(partnerId);

        // -------------------------------------------------------------------------
        // Prepare environment
        // -------------------------------------------------------------------------

        AttributeWorker attributeWorker = new AttributeWorker(ctx, dbList, partnerId);
        attributeWorker.prepare();

        AttributeTextWorker attributeTextWorker = new AttributeTextWorker(ctx);
        attributeTextWorker.prepare();

        // -------------------------------------------------------------------------
        // Bind values
        // -------------------------------------------------------------------------

        for (AttributeType attribute : attributeList) {
            attributeWorker.bind(attribute);

            if (attribute.isWithText() && attribute.isSetText()) {
                String attributeId = attribute.getID();
                attributeTextWorker.bindDelete(attributeId);

                for (TextType text : attribute.getText()) {
                    attributeTextWorker.bindInsert(attributeId, text);
                }
            }
        }

        // -------------------------------------------------------------------------
        // Execute
        // -------------------------------------------------------------------------

        attributeWorker.execute();
        attributeTextWorker.execute();

        // -------------------------------------------------------------------------
        // Set others to inactive
        // -------------------------------------------------------------------------

        List<String> stringList = new ArrayList<>(attributeList.size());
        for (AttributeType at : attributeList) {
            stringList.add(at.getID());
        }

        ItemIdComparator<String> comparator = new ItemIdComparator<>();
        comparator.setDatabaseList(dbList);
        comparator.setNewList(stringList);

        updateStatus(partnerId, comparator);
    }

    private void updateStatus(int partnerId, ItemIdComparator<String> comparator) {
        List<String> toDelete = comparator.getForDelete();

        ctx.update(ATTRIBUTE)
           .set(ATTRIBUTE.STATUS, Status.INACTIVE)
           .where(ATTRIBUTE.PARTNER_ID.eq(partnerId))
           .and(ATTRIBUTE.ATTRIBUTE_ID.in(toDelete))
           .execute();
    }
}
