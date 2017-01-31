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

import com.google.common.base.Joiner;
import xjc.schema.ixsi.AttributeClassType;
import xjc.schema.ixsi.AttributeType;
import xjc.schema.ixsi.ErrorCodeType;
import xjc.schema.ixsi.ErrorType;
import xjc.schema.ixsi.InfoType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static de.rwth.idsg.mb.utils.BasicUtils.extractGermanTextList;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 27.05.2015
 */
public final class RegioITUtils {

    private RegioITUtils() { }

    public static List<ErrorType> buildAdapterError(@Nonnull Exception e) {
        ErrorType error = new ErrorType().withCode(ErrorCodeType.SYS_BACKEND_FAILED)
                                         .withSystemMessage("Mobility-Broker-Adapter: " + e.getMessage());

        return Collections.singletonList(error);
    }

    public static List<ErrorType> buildAdapterError(String msg) {
        ErrorType error = new ErrorType().withCode(ErrorCodeType.SYS_BACKEND_FAILED)
                                         .withSystemMessage("Mobility-Broker-Adapter: " + msg);

        return Collections.singletonList(error);
    }

    @Nullable
    public static String infoToDetailsString(List<InfoType> infoList) {
        if (BasicUtils.hasNoElements(infoList)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        Iterator<InfoType> iterator = infoList.iterator();

        appendInfo(sb, iterator.next());
        while (iterator.hasNext()) {
            sb.append("; ");
            appendInfo(sb, iterator.next());
        }
        return sb.toString();
    }

    private static void appendInfo(StringBuilder sb, @Nonnull InfoType info) {
        appendTypeSafeAttribute(sb, info.getClazz());

        if (info.isSetText() && info.isWithText()) {
            if (info.isSetClazz()) {
                sb.append(": ");
            }
            Joiner.on(", ").appendTo(sb, info.getText());
        }
    }

    public static List<String> attributesToStringList(List<AttributeType> attributes) {
        if (BasicUtils.hasNoElements(attributes)) {
            return Collections.emptyList();
        }

        List<String> returnList = new ArrayList<>(attributes.size());
        for (AttributeType attr : attributes) {

            StringBuilder sb = new StringBuilder();
            appendTypeSafeAttribute(sb, attr.getClazz());

            if (attr.isSetText() && attr.isWithText()) {
                if (attr.isSetClazz()) {
                    sb.append(": ");
                }
                Joiner.on(", ").appendTo(sb, extractGermanTextList(attr.getText()));
            }

            returnList.add(sb.toString());
        }
        return returnList;
    }

    private static void appendTypeSafeAttribute(@Nonnull StringBuilder sb,
                                                @Nullable AttributeClassType clazz) {
        if (clazz != null) {
            sb.append(clazz.value());
        }
    }


}
