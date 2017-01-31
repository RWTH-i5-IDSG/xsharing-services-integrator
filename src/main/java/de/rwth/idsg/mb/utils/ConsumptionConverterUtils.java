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
package de.rwth.idsg.mb.utils;

import de.rwth.idsg.mb.adapter.ixsi.repository.dto.ConsumptionDTO;
import de.rwth.idsg.mb.regioIT.client.rest.params.ConsumptionParams;
import jooq.db.ixsi.tables.records.ConsumptionRecord;
import org.joda.time.DateTime;
import xjc.schema.ixsi.ConsumptionClassType;
import xjc.schema.ixsi.ConsumptionType;
import xjc.schema.ixsi.EuroPriceType;
import xjc.schema.ixsi.TimePeriodType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.rwth.idsg.mb.utils.BasicUtils.getTypeSafeBoolean;
import static de.rwth.idsg.mb.utils.BasicUtils.toSeconds;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 09.05.2016
 */
public final class ConsumptionConverterUtils {

    private ConsumptionConverterUtils() { }

    public static List<ConsumptionParams> toParams(List<ConsumptionDTO> list, Map<String, String> lookupMap) {
        List<ConsumptionParams> consumptionParamsList = new ArrayList<>();
        for (ConsumptionDTO dto : list) {
            ConsumptionType consumption = dto.getConsumption();
            ConsumptionParams consumptionParams = ConsumptionParams
                    .builder()
                    .providerName(lookupMap.get(consumption.getBookingID()))
                    .bookingNo(consumption.getBookingID())
                    .value(consumption.getValue())
                    .unit(consumption.getUnit())
                    .finalized(getTypeSafeBoolean(consumption.isFinal()))
                    .build();

            if (consumption.isSetTimePeriod()) {
                consumptionParams.setStartDateTime(toSeconds(consumption.getTimePeriod().getBegin()));
                consumptionParams.setEndDateTime(toSeconds(consumption.getTimePeriod().getEnd()));
            }

            consumptionParamsList.add(consumptionParams);
        }

        return consumptionParamsList;
    }

    public static List<ConsumptionDTO> toDto(List<ConsumptionRecord> list) {
        return list.stream()
                   .map(ConsumptionConverterUtils::toDto)
                   .collect(Collectors.toList());
    }

    private static ConsumptionDTO toDto(ConsumptionRecord record) {
        ConsumptionType type = new ConsumptionType();
        type.setBookingID(record.getBookingId());
        type.setType(ConsumptionClassType.fromValue(record.getClass_()));
        type.setUnit(record.getUnit());
        type.setFinal(record.getFinal());
        type.setPrice(new EuroPriceType().withValue(record.getPriceInEuroCents()));

        Timestamp from = record.getTimePeriodFrom();
        Timestamp to = record.getTimePeriodTo();

        if (from != null && to != null) {
            type.setTimePeriod(
                    new TimePeriodType().withBegin(new DateTime(from.getTime()))
                                        .withEnd(new DateTime(to.getTime()))
            );
        }

        if (record.getValue() != null) {
            type.setValue(BigDecimal.valueOf(record.getValue()));
        }

        int consumptionId = record.getConsumptionId();
        return new ConsumptionDTO(consumptionId, type);
    }
}
