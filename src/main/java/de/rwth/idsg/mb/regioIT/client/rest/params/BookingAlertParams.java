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
package de.rwth.idsg.mb.regioIT.client.rest.params;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.05.2015
 */
@ToString
@Builder
@Getter
@Setter
public class BookingAlertParams {
    private String providerName;
    private String bookingNo;
    private AlertType alertType;
    private String reason;
    private Long newBeginBooking;   // optional, only when newPeriod
    private Long newEndBooking;     // optional, only when newPeriod

    // -------------------------------------------------------------------------

    public enum AlertType {
        CANCELLED("cancelled"),
        NEW_PERIOD("new_period"),
        NOTIFICATION("notification"),
        IMPOSSIBLE("impossible"),
        POSSIBLE_AGAIN("possible_again");

        private final String value;

        AlertType(String v) {
            value = v;
        }

        @JsonValue // serialize
        public String value() {
            return value;
        }

        @JsonCreator // deserialize
        public static AlertType fromValue(String v) {
            for (AlertType c : AlertType.values()) {
                if (c.value.equalsIgnoreCase(v)) {
                    return c;
                }
            }
            throw new IllegalArgumentException(v);
        }

    }
}
