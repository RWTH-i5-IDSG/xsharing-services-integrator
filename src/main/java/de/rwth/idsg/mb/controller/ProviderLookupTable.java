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
package de.rwth.idsg.mb.controller;

import de.rwth.idsg.mb.adapter.ixsi.IxsiProcessingException;
import de.rwth.idsg.mb.controller.dto.ProviderDTO;
import de.rwth.idsg.mb.controller.repository.IxsiProviderRepository;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 30.06.2015
 */
@Singleton
@Startup
@Slf4j
public class ProviderLookupTable {

    @EJB private IxsiProviderRepository ixsiProviderRepository;

    private Map<String, ProviderDTO> ixsiProviderNameIdMap;
    private Set<String> localProviders;

    @PostConstruct
    public void retrieve() {
        ixsiProviderNameIdMap = ixsiProviderRepository.getProviders();
        localProviders = new HashSet<>(ixsiProviderNameIdMap.keySet());
    }

    public ProviderDTO getIxsiInfo(String name) {
        ProviderDTO dto = ixsiProviderNameIdMap.get(name);
        if (dto == null) {
            throw new IxsiProcessingException("Provider '" + name + "' not found");
        } else {
            return dto;
        }
    }

    public String getProviderName(String providerId) {
        for (Map.Entry<String, ProviderDTO> entry: ixsiProviderNameIdMap.entrySet()) {
            if (entry.getValue().getProviderId().equals(providerId)) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("No provider name is found for the provider id " + providerId);
    }

    public Set<String> getLocalProviders() {
        return this.localProviders;
    }
}
