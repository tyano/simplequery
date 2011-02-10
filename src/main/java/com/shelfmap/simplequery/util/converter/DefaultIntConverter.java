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
package com.shelfmap.simplequery.util.converter;

import com.shelfmap.simplequery.util.IntConverter;
import com.shelfmap.simplequery.util.ValueGreaterThanIntMaxException;
import com.shelfmap.simplequery.util.ValueIsNotNumberException;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultIntConverter implements IntConverter {

    public static final long OFFSET = 3000000000L;

    @Override
    public String convert(int value) {
        long offsetValue = OFFSET + (long) value;
        return "" + offsetValue;
    }

    @Override
    public int restore(String value) throws ValueGreaterThanIntMaxException, ValueIsNotNumberException {
        try {
            long longValue = Long.parseLong(value);
            long offsetValue = longValue - OFFSET;
            if(offsetValue > Integer.MAX_VALUE) {
                throw new ValueGreaterThanIntMaxException("the numeral value + '" + value + "' is greater than Integer.MAX. Can not convert to a int value.");
            }
            
            return (int)offsetValue;
        } catch (NumberFormatException ex) {
            throw new ValueIsNotNumberException("Can not convert the string '" + value + "' to a number.");
        }
    }
}
