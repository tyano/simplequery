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
import com.shelfmap.simplequery.domain.impl.DefaultAttributeConverter;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author Tsutomu YANO
 */
public class IsNotNullMatcher implements Matcher<Void> {
    private final AttributeConverter<Void> attributeConverter = new DefaultAttributeConverter<Void>(Void.class);
    
    public IsNotNullMatcher() {
    }

    @Override
    public Matcher<Void> withAttributeConverter(AttributeConverter<Void> attributeConverter) {
        return this;
    }


    @Override
    public String describe() {
        return "is not null";
    }

    @Override
    public Collection<Void> getValues() {
        return Collections.emptyList();
    }

    @Override
    public boolean isAttributeConverterApplied() {
        return true;
    }

    @Override
    public void setAttributeConverter(AttributeConverter<Void> attributeConverter) {
    }

    @Override
    public AttributeConverter<Void> getAttributeConverter() {
        return this.attributeConverter;
    }
}
