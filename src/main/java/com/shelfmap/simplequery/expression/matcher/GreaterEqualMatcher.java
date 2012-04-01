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

package com.shelfmap.simplequery.expression.matcher;

import com.shelfmap.simplequery.domain.AttributeConverter;

/**
 *
 * @author Tsutomu YANO
 */
public class GreaterEqualMatcher<T> extends BaseMatcher<T> {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public GreaterEqualMatcher(T value) {
        super(value);
    }

    protected GreaterEqualMatcher(AttributeConverter<T> attributeConverter, T[] values) {
        super(attributeConverter, values);
    }

    @Override
    protected String expression() {
        return ">=";
    }

    @Override
    protected GreaterEqualMatcher<T> newMatcher(AttributeConverter<T> attributeConverter, T... values) {
        return new GreaterEqualMatcher<T>(attributeConverter, values);
    }
}
