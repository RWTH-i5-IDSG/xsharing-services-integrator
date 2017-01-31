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
package de.rwth.idsg.mb.controller.parse;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import de.ivu.realtime.modules.ura.data.request.ReturnString;
import de.ivu.realtime.modules.ura.data.request.UraRequestParseException;
import de.rwth.idsg.mb.controller.NonIxsiProvider;
import de.rwth.idsg.mb.pg.TsRange;
import de.rwth.idsg.mb.pg.range.IncExcTsRange;
import de.rwth.idsg.mb.utils.BasicUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Main parser class utilizing other Guava helpers
 *
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 01.07.2015
 */
@Slf4j
public class UraStringParser {

    private static final Splitter.MapSplitter QUERY_SPLITTER = Splitter.on('&').withKeyValueSeparator("=");
    private static final Joiner.MapJoiner MAP_JOINER = Joiner.on('&').withKeyValueSeparator("=");

    private static final ToListFunction TO_LIST_FUNCTION = new ToListFunction();
    private static final NoProviderPredicate NO_PROVIDER_PREDICATE = new NoProviderPredicate();
    private static final RemovePredicate REMOVE_PREDICATE = new RemovePredicate();
    private static final ValueTransformer VALUE_TRANSFORMER = new ValueTransformer();
    private static final ToStringTransformer TO_STRING_TRANSFORMER = new ToStringTransformer();

    private Map<String, List<String>> listValueMap;
    private Set<String> localProviders;

    @Getter private List<String> supportedProviders;

    public UraStringParser(String uraParams, Set<String> localProviders) {
        this.localProviders = localProviders;

        // Build the map
        Map<String, String> firstMap = QUERY_SPLITTER.split(uraParams);

        // String -> List
        listValueMap = Maps.transformValues(firstMap, TO_LIST_FUNCTION);
    }

    public String cleanUpAseagParams() {
        // Remove provider parameter
        Map<String, List<String>> noProviderMap = Maps.filterKeys(listValueMap, NO_PROVIDER_PREDICATE);

        // Remove invalid values of parameters
        Map<String, List<String>> resultMap2 = Maps.transformEntries(noProviderMap, VALUE_TRANSFORMER);

        // Remove keys with empty lists
        Map<String, List<String>> resultMap3 = Maps.filterEntries(resultMap2, REMOVE_PREDICATE);

        // List -> String
        Map<String, String> resultMap4 = Maps.transformEntries(resultMap3, TO_STRING_TRANSFORMER);

        // Join the map
        return MAP_JOINER.join(resultMap4);
    }

    public boolean isLocalRelevant() {
        List<String> providers = getProviders();
        supportedProviders = getIntersection(providers, localProviders);
        return providers.isEmpty() | !supportedProviders.isEmpty();
    }

    public boolean isAseagRelevant() {
        List<String> providers = getProviders();
        List<String> stopTypes = getStopPointTypes();

        // Aseag server is not capable of processing EstimatedTime. It returns:
        // HTTP Status 400 - Illegal filter parameter 'EstimatedTime' for version URA_2_0
        //
        // Therefore, querying Aseag when this filter is set would be wrong.
        boolean isEstimationFilterSet = listValueMap.containsKey("EstimatedTime");

        boolean isAseagSet = false;
        for (String s : providers) {
            if (s.equalsIgnoreCase(NonIxsiProvider.ASEAG.value())) {
                isAseagSet = true;
                break;
            }
        }

        return stopTypes.isEmpty()
                & !isEstimationFilterSet
                & (isAseagSet || providers.isEmpty());
    }

    private List<String> getProviders() {
        return getItem("Provider");
    }

    public List<String> getStopPointTypes() {
        return getItem("StopPointType");
    }

    public List<String> getStopId() {
        return getItem("StopID");
    }

    public List<Double> getCircleFilter() {
        List<String> list = getItem("Circle");

        List<Double> result = new ArrayList<>(list.size());
        for (String s : list) {
            result.add(Double.parseDouble(s));
        }
        return result;
    }

    /**
     * Example: EstimatedTime=>1436280117150,<1436283717150
     */
    public TsRange getEstimatedTimeFilter() throws UraRequestParseException {
        List<String> list = getItem("EstimatedTime");
        log.debug("EstimatedTime: {}", list);

        // Validate input
        //
        if (list.isEmpty()) {
            return null;
        } else if (list.size() != 2) {
            throw new UraRequestParseException("EstimatedTime must contain exactly two elements");
        }

        Timestamp beginTimestamp = null;
        Timestamp endTimestamp = null;

        for (String item : list) {
            if (item.startsWith(">")) {
                beginTimestamp = buildDateTime(item);

            } else if (item.startsWith("<")) {
                endTimestamp = buildDateTime(item);
            }
        }

        // Validate output
        //
        try {
            validateRange(beginTimestamp, endTimestamp);
        } catch (IllegalArgumentException e) {
            throw new UraRequestParseException("Invalid EstimatedTime: " + list + "; Reason: " + e.getMessage());
        }

        TsRange etp = new IncExcTsRange(beginTimestamp, endTimestamp);
        log.debug("TsRange: {}", etp);
        return etp;
    }

    /**
     * Semantically same as
     * <p/>
     * de.ivu.realtime.modules.ura.data.request.UraQueryParameterParseUtils#asReturnStringList(java.lang.String)
     */
    public List<ReturnString> getReturnStringList() throws UraRequestParseException {
        List<String> list = getItem("ReturnList");

        List<ReturnString> result = new ArrayList<>();
        for (String s : list) {
            try {
                result.add(ReturnString.byName(s));
            } catch (IllegalArgumentException e) {
                throw new UraRequestParseException("Cannot parse " + s + " as ReturnString.");
            }
        }
        return result;
    }

    private Timestamp buildDateTime(String value) throws UraRequestParseException {
        try {
            return new Timestamp(Long.parseLong(value.substring(1)));
        } catch (IllegalArgumentException e) {
            throw new UraRequestParseException("Cannot parse " + value + " as Unix Timestamp.");
        }
    }

    /**
     * Prevent the NPE, when the key is missing
     */
    private List<String> getItem(String key) {
        List<String> list = listValueMap.get(key);
        if (list == null) {
            return Collections.emptyList();
        } else {
            return list;
        }
    }

    private List<String> getIntersection(List<String> queryList, Set<String> appList) {
        if (BasicUtils.hasNoElements(queryList)) {
            return Collections.emptyList();
        } else {
            Set<String> intersection = new HashSet<>(queryList);
            intersection.retainAll(appList);
            return new ArrayList<>(intersection);
        }
    }

    private void validateRange(Timestamp begin, Timestamp end) {
        if (begin == null || end == null) {
            throw new IllegalArgumentException("'Begin' and 'end' must be both set");
        }

        if (begin.equals(end)) {
            throw new IllegalArgumentException("'Begin' is equal to 'end'");
        }

        if (end.before(begin)) {
            throw new IllegalArgumentException("'Begin' must be before 'end'");
        }

        Timestamp now = new Timestamp(new DateTime().getMillis());
        if (end.before(now)) {
            throw new IllegalArgumentException("'End' must be in future");
        }
    }
}
