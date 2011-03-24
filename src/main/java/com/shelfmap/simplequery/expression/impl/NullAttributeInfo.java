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

package com.shelfmap.simplequery.expression.impl;

import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.expression.AttributeInfo;

/**
 *
 * @author Tsutomu YANO
 */
public class NullAttributeInfo<T> implements AttributeInfo<T> {
    @Override
    public String convertValue(T targetValue) {
        isNotNull("targetValue", targetValue);
        return SimpleDBUtils.quoteValue(targetValue.toString());
    }

    @Override
    public T restoreValue(String targetValue) {
        //TODO decide how to convert a string to the type assigned by a Type Parameter.
        return null;
    }
}
