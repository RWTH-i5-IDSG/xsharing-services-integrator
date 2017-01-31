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
package de.rwth.idsg.mb.adapter.ixsi.service;

import de.rwth.idsg.mb.adapter.ixsi.IxsiFeature;
import de.rwth.idsg.mb.adapter.ixsi.IxsiProcessingException;
import de.rwth.idsg.mb.adapter.ixsi.context.PartnerContext;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 11.02.2016
 */
public interface EnabledService {

    default boolean isEnabled(PartnerContext pctx, IxsiFeature feature) {
        return pctx.getConfig().getFeatures().contains(feature);
    }

    default void assertEnabled(PartnerContext pctx, IxsiFeature feature) {
        if (!isEnabled(pctx, feature)) {
            throw new IxsiProcessingException("The feature " + feature.name() + " is not enabled for this partner/provider");
        }
    }

}
