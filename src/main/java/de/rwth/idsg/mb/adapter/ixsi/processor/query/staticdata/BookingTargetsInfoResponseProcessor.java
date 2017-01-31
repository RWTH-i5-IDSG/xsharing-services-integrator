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
package de.rwth.idsg.mb.adapter.ixsi.processor.query.staticdata;

import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.context.InOutContext;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;
import de.rwth.idsg.mb.adapter.ixsi.intercept.ErrorLog;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.StaticResponseProcessor;
import de.rwth.idsg.mb.adapter.ixsi.repository.AttributeRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.BookingTargetRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.FloatingAreaRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.PlaceGroupRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.PlaceRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.ProviderRepository;
import de.rwth.idsg.mb.adapter.ixsi.repository.ServerSystemRepository;
import de.rwth.idsg.mb.adapter.ixsi.service.EnabledSubscriptionService;
import de.rwth.idsg.mb.controller.ProviderLookupTable;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import xjc.schema.ixsi.BookingTargetsInfoRequestType;
import xjc.schema.ixsi.BookingTargetsInfoResponseType;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 26.09.2014
 */
@Stateless
@Slf4j
public class BookingTargetsInfoResponseProcessor
        implements StaticResponseProcessor<BookingTargetsInfoRequestType, BookingTargetsInfoResponseType> {

    @EJB private ServerSystemRepository serverSystemRepository;
    @EJB private AttributeRepository attributeRepository;
    @EJB private ProviderRepository providerRepository;
    @EJB private PlaceRepository placeRepository;
    @EJB private PlaceGroupRepository placeGroupRepository;
    @EJB private FloatingAreaRepository floatingAreaRepository;
    @EJB private BookingTargetRepository bookingTargetRepository;

    @EJB private EnabledSubscriptionService subscriptionService;

    @Inject private ProviderLookupTable providerLookupTable;

    @Override
    public IxsiFeature getRelatedFeature() {
        return IxsiFeature.QS_BookingTargetsInfo;
    }

    @Override
    public Class<BookingTargetsInfoResponseType> getProcessingClass() {
        return BookingTargetsInfoResponseType.class;
    }

    @Override
    @ErrorLog
    public void process(InOutContext<BookingTargetsInfoRequestType, BookingTargetsInfoResponseType> context) {
        BookingTargetsInfoResponseType response = context.getIncoming();
        int partnerId = context.getPartnerContext().getConfig().getPartnerId();

        // -------------------------------------------------------------------------
        // The sequence is important due to dependencies
        // -------------------------------------------------------------------------

        if (response.isSetAttributes()) {
            attributeRepository.upsertAttributeList(partnerId, response.getAttributes());
        }

        if (response.isSetProvider()) {
            providerRepository.upsertProviderList(partnerId, response.getProvider());
        }

        if (response.isSetPlace()) {
            placeRepository.upsertPlaceList(partnerId, response.getPlace());
        }

        if (response.isSetPlaceGroup()) {
            placeGroupRepository.upsertPlaceGroupList(partnerId, response.getPlaceGroup());
        }

        if (response.isSetFreeFloatingArea()) {
            floatingAreaRepository.upsertFloatingAreaList(partnerId, response.getFreeFloatingArea());
        }

        if (response.isSetBookee()) {
            bookingTargetRepository.upsertBookingTargetList(partnerId, response.getBookee());
        }

        // update cache for communication with regioit
        providerLookupTable.retrieve();

        // -------------------------------------------------------------------------
        // Update the delivery timestamp
        // -------------------------------------------------------------------------

        DateTime dateTime = response.getTimestamp();
        if (dateTime == null) {
            dateTime = new DateTime();
        }

        serverSystemRepository.updateBTIRDeliveryTimestamp(partnerId, dateTime);

        // -------------------------------------------------------------------------
        // Subscribe all
        // -------------------------------------------------------------------------

        PartnerContext ctx = context.getPartnerContext();

        if (ctx.getState().isSetup()) {
            ctx.getEngine().subscribeAll(ctx);
        }

    }
}
