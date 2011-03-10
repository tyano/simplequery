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

import com.shelfmap.simplequery.expression.AttributeInfo;

/**
 *
 * @author Tsutomu YANO
 */
public class IsMatcher<T> extends BaseMatcher<T> {

    @SuppressWarnings("unchecked")
    public IsMatcher(T value) {
        super(value);
    }

    protected IsMatcher(AttributeInfo<T> attributeInfo, T[] values) {
        super(attributeInfo, values);
    }

    @Override
    protected String expression() {
        return "=";
    }

    @Override
    protected IsMatcher<T> newMatcher(AttributeInfo<T> attributeInfo, T... values) {
        return new IsMatcher<T>(attributeInfo, values);
    }
}
