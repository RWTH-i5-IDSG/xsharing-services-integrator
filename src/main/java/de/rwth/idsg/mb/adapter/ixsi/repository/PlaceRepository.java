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
package de.rwth.idsg.mb.adapter.ixsi.repository;

import xjc.schema.ixsi.PlaceAvailabilityType;
import xjc.schema.ixsi.PlaceType;
import xjc.schema.ixsi.ProviderPlaceIDType;

import java.util.List;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.11.2014
 */
public interface PlaceRepository {
    List<ProviderPlaceIDType> getIds(int partnerId);
    List<ProviderPlaceIDType> getActiveIds(int partnerId);
    
    void upsertPlaceList(int partnerId, List<PlaceType> placeList);
    void updatePlaceAvailabilityList(List<PlaceAvailabilityType> placeAvailabilityTypes);
}
