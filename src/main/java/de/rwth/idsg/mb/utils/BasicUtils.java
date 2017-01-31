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

import de.rwth.idsg.mb.Constants;
import de.rwth.idsg.mb.pg.TsRange;
import de.rwth.idsg.mb.pg.range.IncExcTsRange;
import jooq.db.ixsi.enums.Language;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.postgresql.geometric.PGpoint;
import org.postgresql.geometric.PGpolygon;
import xjc.schema.ixsi.CoordType;
import xjc.schema.ixsi.TextType;
import xjc.schema.ixsi.TimePeriodType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 10.12.2014
 */
public final class BasicUtils {

    private BasicUtils() { }

    public static boolean getTypeSafeBoolean(@Nullable Boolean b) {
        if (b == null) {
            return false;
        } else {
            return b;
        }
    }

    @Nullable
    public static BigDecimal toBigDecimal(@Nullable Float f) {
        if (f == null) {
            return null;
        } else {
            return BigDecimal.valueOf(f);
        }
    }

    @Nullable
    public static Long toSeconds(@Nullable Timestamp ts) {
        if (ts == null) {
            return null;
        } else {
            return TimeUnit.MILLISECONDS.toSeconds(ts.getTime());
        }
    }

    @Nullable
    public static Integer toSeconds(@Nullable Period period) {
        if (period == null) {
            return null;
        } else {
            return period.toStandardSeconds().getSeconds();
        }
    }

    @Nullable
    public static Long toSeconds(@Nullable DateTime dateTime) {
        if (dateTime == null) {
            return null;
        } else {
            return TimeUnit.MILLISECONDS.toSeconds(dateTime.getMillis());
        }
    }

    @Nullable
    public static PGpolygon toPolygon(List<CoordType> coordList) {
        if (coordList.isEmpty()) {
            return null;
        }

        PGpoint[] points = new PGpoint[coordList.size()];
        int counter = 0;
        for (CoordType c : coordList) {
            points[counter] = toPoint(c);
            counter++;
        }
        return new PGpolygon(points);
    }

    @Nullable
    public static PGpoint toPoint(@Nullable CoordType c) {
        if (c == null) {
            return null;
        }

        return new PGpoint(c.getLongitude().doubleValue(),
                           c.getLatitude().doubleValue());
    }

    @Nullable
    public static TsRange toTsRange(@Nullable TimePeriodType t) {
        if (t == null) {
            return null;
        }

        return new IncExcTsRange(new Timestamp(t.getBegin().getMillis()),
                                 new Timestamp(t.getEnd().getMillis()));
    }

    public static boolean isSetAndValid(@Nullable TimePeriodType pt) {
        return pt != null && pt.getBegin().isBefore(pt.getEnd());
    }

    /**
     * TODO:
     * This is only a short-term workaround. Actually,
     * the RegioIT interface should have multi-language support
     */
    public static List<String> extractGermanTextList(List<TextType> text) {
        // if there is only one text set, return it no matter what the language is
        // otherwise, we might lose information during the type conversion
        if (text.size() == 1) {
            return Collections.singletonList(text.get(0).getText());
        }

        List<String> germanTexts = new ArrayList<>();
        for (TextType tt : text) {
            if (isGerman(tt)) {
                germanTexts.add(tt.getText());
            }
        }
        return germanTexts;
    }

    public static boolean isGerman(@Nonnull TextType tt) {
        xjc.schema.ixsi.Language lan = tt.getLanguage();
        if (lan != null) {
            String value = lan.getValue();
            if ("DE".equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    public static Language getDatabaseLangOrDefault(@Nullable xjc.schema.ixsi.Language ixsiLan) {
        if (ixsiLan == null) {
            return Constants.Ixsi.DEFAULT_LANGUAGE;
        }

        for (Language dbLan : Language.values()) {
            if (dbLan.getLiteral().equalsIgnoreCase(ixsiLan.getValue())) {
                return dbLan;
            }
        }
        throw new IllegalArgumentException("The language '" + ixsiLan.getValue() + "' is unknown");
    }

    public static boolean hasNoElements(List someList) {
        return someList == null || someList.isEmpty();
    }

    public static boolean hasElements(List someList) {
        return !hasNoElements(someList);
    }

    public static boolean hasOneElement(List someList) {
        return someList != null && someList.size() == 1;
    }

    /**
     * [scheme:][//authority][path][?query][#fragment]
     */
    public static String getAsString(URI uri) {
        return uri.getScheme() + "://" + uri.getAuthority() + uri.getPath();
    }

    public static boolean containsIgnoreCase(String str, List<String> list) {
        if (hasNoElements(list)) {
            return false;
        }

        for (String i : list) {
            if (i.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }
}
