/*
 * Copyright 2011 Tsutomu YANO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shelfmap.simplequery.domain.impl;

import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import com.shelfmap.simplequery.domain.AttributeConverter;
import com.shelfmap.simplequery.expression.CanNotRestoreAttributeException;
import java.text.ParseException;
import java.util.Date;

/**
 *
 * @author Tsutomu YANO
 */
public class DateAttributeConverter implements AttributeConverter<Date> {

    @Override
    public String convertValue(Date targetValue) {
        if(targetValue == null) return null;
        return SimpleDBUtils.encodeDate(targetValue);
    }

    @Override
    public Date restoreValue(String targetValue) throws CanNotRestoreAttributeException {
        if(targetValue == null) return null;
        try {
            return SimpleDBUtils.decodeDate(targetValue);
        } catch (ParseException ex) {
            throw new CanNotRestoreAttributeException(ex, targetValue, Date.class);
        }
    }

}
