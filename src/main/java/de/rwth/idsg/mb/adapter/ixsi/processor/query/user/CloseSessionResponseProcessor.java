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
import de.rwth.idsg.mb.adapter.ixsi.context.UserInOutContext;
import de.rwth.idsg.mb.adapter.ixsi.intercept.ErrorLog;
import de.rwth.idsg.mb.adapter.ixsi.processor.api.UserResponseProcessor;
import lombok.extern.slf4j.Slf4j;
import xjc.schema.ixsi.CloseSessionRequestType;
import xjc.schema.ixsi.CloseSessionResponseType;

import javax.ejb.Stateless;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 26.09.2014
 */
@Slf4j
@Stateless
public class CloseSessionResponseProcessor
        implements UserResponseProcessor<CloseSessionRequestType, CloseSessionResponseType> {

    @Override
    public IxsiFeature getRelatedFeature() {
        return IxsiFeature.QU_CloseSession;
    }

    @Override
    public Class<CloseSessionResponseType> getProcessingClass() {
        return CloseSessionResponseType.class;
    }

    @Override
    @ErrorLog
    public void process(UserInOutContext<CloseSessionRequestType, CloseSessionResponseType> context) {
        log.warn("Received an unexpected message (This feature is not supported)");
    }
}
