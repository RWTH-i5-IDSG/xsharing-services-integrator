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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by Wolfgang Kluth on 13/05/15.
 */
public class BookingParams {

    @Getter @Setter
    @ToString
    public static class Create {
        private String userId;
        private String vehicleId;

        private String fromPlaceId;
        private String toPlaceId;

        private Long startTime;
        private Long endTime;
        private Integer maxWaitInMin;
    }

    @Getter @Setter
    @ToString(callSuper = true)
    public static class Update extends Basic {
        private Long startTime;
        private Long endTime;
        private Integer maxWaitInMin;
    }

    @Getter @Setter
    @ToString
    public static class Basic {
        private String userId;
        private String bookingNo;
    }
}
