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
package de.rwth.idsg.mb.monitor.dto;

import de.rwth.idsg.mb.pg.TsRange;
import de.rwth.idsg.mb.pg.range.IncIncTsRange;
import de.rwth.idsg.mb.regioIT.client.rest.params.ConsumptionParams;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 23.09.2016
 */
@Getter
public class ConsumptionParamsDate {

    private String bookingNo;
    private String providerName;
    private TsRange timePeriod;
    private BigDecimal value;
    private String unit;
    private boolean finalized;

    public ConsumptionParamsDate(ConsumptionParams params) {
        this.bookingNo = params.getBookingNo();
        this.providerName = params.getProviderName();
        this.value = params.getValue();
        this.unit = params.getUnit();
        this.finalized = params.isFinalized();

        this.timePeriod = new IncIncTsRange(
                secondsToTimestamp(params.getStartDateTime()),
                secondsToTimestamp(params.getEndDateTime())
        );
    }

    private static Timestamp secondsToTimestamp(Long seconds) {
        if (seconds == null) {
            return null;
        } else {
            return new Timestamp(TimeUnit.SECONDS.toMillis(seconds));
        }
    }
}
