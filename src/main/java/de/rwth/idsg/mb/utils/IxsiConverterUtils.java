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

import org.postgresql.geometric.PGpoint;
import xjc.schema.ixsi.AddressType;
import xjc.schema.ixsi.AttributeClassType;
import xjc.schema.ixsi.AttributeType;
import xjc.schema.ixsi.BookingTargetAvailabilityType;
import xjc.schema.ixsi.BookingTargetChangeAvailabilityType;
import xjc.schema.ixsi.BookingTargetType;
import xjc.schema.ixsi.ConsumptionType;
import xjc.schema.ixsi.CoordType;
import xjc.schema.ixsi.EngineType;
import xjc.schema.ixsi.EuroPriceType;
import xjc.schema.ixsi.GeoPositionType;
import xjc.schema.ixsi.PercentType;
import xjc.schema.ixsi.StopLinkType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;

import static de.rwth.idsg.mb.utils.BasicUtils.getTypeSafeBoolean;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 20.05.2016
 */
public final class IxsiConverterUtils {

    private IxsiConverterUtils() { }

    public static boolean isTrue(@Nullable Boolean b) {
        return getTypeSafeBoolean(b);
    }

    public static boolean isSetCoord(@Nonnull BookingTargetChangeAvailabilityType item) {
        GeoPositionType t = item.getGeoPosition();
        // Since GeoPositionType.CoordType is "required" it has to be set when GeoPositionType is set.
        return t != null;
    }

    public static boolean isSetAddress(@Nonnull BookingTargetChangeAvailabilityType item) {
        GeoPositionType t = item.getGeoPosition();
        if (t != null) {
            AddressType a = t.getAddress();
            if (a != null) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static Integer getPriceOrNull(@Nonnull ConsumptionType c) {
        EuroPriceType e = c.getPrice();
        if (e != null) {
            return e.getValue();
        }
        return null;
    }

    @Nullable
    public static Float getValueOrNull(@Nonnull ConsumptionType c) {
        BigDecimal v = c.getValue();
        if (v != null) {
            return v.floatValue();
        }
        return null;
    }

    @Nullable
    public static Short getImportanceOrNull(@Nonnull AttributeType item) {
        PercentType percentType = item.getImportance();
        if (percentType != null) {
            Integer value = percentType.getValue();
            if (value != null) {
                return value.shortValue();
            }
        }
        return null;
    }

    @Nullable
    public static String getClazzOrNull(@Nonnull AttributeType item) {
        AttributeClassType clazz = item.getClazz();
        if (clazz != null) {
            return clazz.value();
        }
        return null;
    }

    @Nullable
    public static String getEngineTypeOrNull(@Nonnull BookingTargetType b) {
        EngineType en = b.getEngine();
        if (en != null) {
            return en.value();
        }
        return null;
    }

    @Nullable
    public static Short getDistanceOrNull(@Nonnull StopLinkType item) {
        Integer m = item.getDistance();
        if (m != null) {
            return m.shortValue();
        }
        return null;
    }

    @Nullable
    public static Integer getProbabilityOrNull(@Nullable PercentType p) {
        if (p != null) {
            return p.getValue();
        }
        return null;
    }

    @Nullable
    public static PGpoint getPointOrNull(@Nonnull BookingTargetAvailabilityType item) {
        GeoPositionType geo = item.getGeoPosition();
        if (geo != null) {
            CoordType coord = geo.getCoord();
            return BasicUtils.toPoint(coord);
        }
        return null;
    }

    @Nullable
    public static Integer getSocOrNull(@Nonnull BookingTargetAvailabilityType item) {
        PercentType p = item.getCurrentStateOfCharge();
        if (p != null) {
            return p.getValue();
        }
        return null;
    }
}
