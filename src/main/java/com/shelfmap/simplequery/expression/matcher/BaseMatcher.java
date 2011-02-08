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

import static com.shelfmap.simplequery.util.Assertion.isNull;
import com.shelfmap.simplequery.expression.Matcher;
import com.shelfmap.simplequery.expression.ValueConverter;

/**
 *
 * @author Tsutomu YANO
 */
public abstract class BaseMatcher implements Matcher{
    private String attribute;
    private Object value;
    private Class<?> type;

    public BaseMatcher(String attribute, Class<?> type, Object value) {
        isNull("attribute", attribute);
        isNull("type", type);
        this.attribute = attribute;
        this.type = type;
        this.value = value;
    }
    
    @Override
    public String describe() {
        ValueConverter converter = getValueConverter();
        return converter.convertAttribute(attribute) + " " + expression() + " " + converter.convertValue(type, value);
    }
    
    protected abstract String expression();

    
    private ValueConverter getValueConverter() {
        return null;
    }
}
