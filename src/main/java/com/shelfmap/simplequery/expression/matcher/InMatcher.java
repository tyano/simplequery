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

/**
 *
 * @author Tsutomu YANO
 */
public class InMatcher<T> extends BaseMatcher<T> {

    public InMatcher(T... values) {
        super(values);
    }

    @Override
    protected String expression() {
        return "in";
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append(expression()).append(" (");
        
        StringBuilder parameters = new StringBuilder();
        for (T value : getValues()) {
            if(parameters.length() > 0) parameters.append(", ");
            parameters.append(convertValue(value));
        }
        sb.append(parameters.toString());
        sb.append(")");
        return sb.toString();
    }
}