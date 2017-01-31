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
package de.rwth.idsg.mb.adapter.ixsi.context;

import lombok.Getter;
import lombok.ToString;

/**
 * Holder for two-way / incoming-outgoing communication (e.g. every IXSI request/response messaging)
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 10.07.2015
 */
@Getter
@ToString
public class InOutContext<OUT, IN> {
    private final String partnerName;
    private final OUT outgoing;

    private IN incoming;
    private PartnerContext partnerContext;

    /**
     * We use PartnerContext during init only to extract partner name, which is included for the only purpose of
     * debugging (using the monitor page): If we do not get a response, we should be able to find out which partner
     * failed to send a response without looking at the logs.
     */
    public InOutContext(PartnerContext pctx, OUT outgoing) {
        this.partnerName = pctx.getConfig().getPartnerName();
        this.outgoing = outgoing;
    }

    public void setAfterResponse(IN incoming, PartnerContext partnerContext) {
        this.partnerContext = partnerContext;
        this.incoming = incoming;
    }
}
