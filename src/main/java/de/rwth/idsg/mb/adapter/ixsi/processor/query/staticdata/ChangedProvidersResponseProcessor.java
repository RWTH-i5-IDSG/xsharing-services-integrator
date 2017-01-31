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
import de.rwth.idsg.mb.adapter.ixsi.intercept.ErrorLog;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.StaticResponseProcessor;
import de.rwth.idsg.mb.adapter.ixsi.service.EnabledQueryService;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.ChangedProvidersRequestType;
import xjc.schema.ixsi.ChangedProvidersResponseType;

import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 26.09.2014
 */
@Slf4j
@Stateless
public class ChangedProvidersResponseProcessor
        implements StaticResponseProcessor<ChangedProvidersRequestType, ChangedProvidersResponseType> {

    @EJB private EnabledQueryService queryService;

    @Override
    public IxsiFeature getRelatedFeature() {
        return IxsiFeature.QS_ChangedProviders;
    }

    @Override
    public Class<ChangedProvidersResponseType> getProcessingClass() {
        return ChangedProvidersResponseType.class;
    }

    @Override
    @ErrorLog
    public void process(InOutContext<ChangedProvidersRequestType, ChangedProvidersResponseType> context) {
        ChangedProvidersResponseType response = context.getIncoming();
        if (response.isSetProvider()) {
            queryService.getBookingTargetsInfo(context.getPartnerContext(), response.getProvider(), null);
        } else {
            log.debug("No changed providers.");
        }
    }
}
