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

import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import com.shelfmap.simplequery.domain.AttributeConverter;
import static com.shelfmap.simplequery.util.Assertion.*;
import com.shelfmap.simplequery.domain.impl.DefaultAttributeConverter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Tsutomu YANO
 */
public abstract class BaseMatcher<T> implements Matcher<T>, Serializable {
    private static final long serialVersionUID = 1L;

    private T[] values;
    private AttributeConverter<T> attributeConverter;

    public BaseMatcher(T... values) {
        this(new DefaultAttributeConverter<T>(values[0]), values);
    }

    protected BaseMatcher(AttributeConverter<T> attributeConverter, T... values) {
        isNotNull("attributeConverter", attributeConverter);
        isNotNull("values", values);
        isNotEmpty("values", values);

        this.attributeConverter = attributeConverter;
        this.values = values;
    }

    protected abstract BaseMatcher<T> newMatcher(AttributeConverter<T> attributeConverter, T... values);

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append(expression()).append(" ");
        sb.append(SimpleDBUtils.quoteValue(attributeConverter.convertValue(values[0])));
        return sb.toString();
    }

    @Override
    public Matcher<T> withAttributeConverter(AttributeConverter<T> attributeConverter) {
        return newMatcher(attributeConverter, values);
    }

    @Override
    public AttributeConverter<T> getAttributeConverter() {
        return this.attributeConverter;
    }

    @Override
    public void setAttributeConverter(AttributeConverter<T> attributeConverter) {
        this.attributeConverter = attributeConverter;
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
    public boolean isAttributeConverterApplied() {
        return (this.attributeConverter instanceof DefaultAttributeConverter);
    }
}
