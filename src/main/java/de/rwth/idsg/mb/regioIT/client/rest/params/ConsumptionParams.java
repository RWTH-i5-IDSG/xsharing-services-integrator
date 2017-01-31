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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Parameters for the REST API
 *
 * @author Wolfgang Kluth <kluth@dbis.rwth-aachen.de>
 * @since 20.04.2015
 */
@ToString
@Setter
@Getter
@Builder
public class ConsumptionParams {
    private String bookingNo;
    private String providerName;
    private Long startDateTime;
    private Long endDateTime;
    private BigDecimal value;
    private String unit;

    @JsonProperty(value = "final")
    private boolean finalized;
}
