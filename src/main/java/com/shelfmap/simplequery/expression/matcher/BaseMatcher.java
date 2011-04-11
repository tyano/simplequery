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
import static com.shelfmap.simplequery.util.Assertion.*;
import com.shelfmap.simplequery.domain.impl.DefaultAttributeConverter;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Tsutomu YANO
 */
public abstract class BaseMatcher<T> implements Matcher<T> {

    private T[] values;
    private AttributeConverter<T> attributeInfo;

    public BaseMatcher(T... values) {
        this(new DefaultAttributeConverter<T>(values[0]), values);
    }
    
    protected BaseMatcher(AttributeConverter<T> attributeInfo, T... values) {
        isNotNull("attributeInfo", attributeInfo);
        isNotNull("values", values);
        isNotEmpty("values", values);
        
        this.attributeInfo = attributeInfo;
        this.values = values;
    }

    protected abstract BaseMatcher<T> newMatcher(AttributeConverter<T> attributeInfo, T... values);

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append(expression()).append(" ");
        sb.append(attributeInfo.convertValue(values[0]));
        return sb.toString();
    }

    @Override
    public Matcher<T> withAttributeInfo(AttributeConverter<T> attributeInfo) {
        return newMatcher(attributeInfo, values);
    }
    
    @Override
    public AttributeConverter<T> getAttributeInfo() {
        return this.attributeInfo;
    }
    
    @Override 
    public void setAttributeInfo(AttributeConverter<T> attributeInfo) {
        this.attributeInfo = attributeInfo;
    }

    protected abstract String expression();

    protected T[] values() {
        return values;
    }

    @Override
    public Collection<T> getValues() {
        return Arrays.asList(values);
    }

    @Override
    public boolean isAttributeInfoApplied() {
        return (this.attributeInfo instanceof DefaultAttributeConverter);
    }
}
