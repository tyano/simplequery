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
public class BetweenMatcher<T> extends BaseMatcher<T> {
    private T second;
    
    @SuppressWarnings("unchecked")
    public BetweenMatcher(T value) {
        super(value);
    }

    @SuppressWarnings("unchecked")
    protected BetweenMatcher(AttributeConverter<T> attributeInfo, T... value) {
        super(attributeInfo, value);
    }
    
    public BetweenMatcher<T> and(T value) {
        this.second = value;
        return this;
    }

    @Override
    protected String expression() {
        return "between";
    }

    @Override
    public String describe() {
        if(second == null) throw new IllegalStateException("the second argument not found. You need pass a second argument by the 'and()' method.");
        return super.describe() + " and " + getAttributeInfo().convertValue(second);
    }

    @Override
    protected BetweenMatcher<T> newMatcher(AttributeConverter<T> attributeInfo, T... values) {
        return new BetweenMatcher<T>(attributeInfo, values);
    }

    @Override
    public Matcher<T> withAttributeInfo(AttributeConverter<T> attributeInfo) {
        BetweenMatcher<T> newMatcher = newMatcher(attributeInfo, values());
        newMatcher.second = this.second;
        return newMatcher;
    }
}
