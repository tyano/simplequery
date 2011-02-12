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

package com.shelfmap.simplequery.expression;

import com.shelfmap.simplequery.expression.impl.BasicOperator;
import com.shelfmap.simplequery.expression.impl.DefaultCondition;

/**
 *
 * @author Tsutomu YANO
 */
public final class Conditions {
    private Conditions() {
    }
    
    public static <T> Condition condition(String attributeName, Matcher<T> matcher) {
        return new DefaultCondition(attributeName, matcher);
    }
    
    public static <T> Condition condition(Condition first, BasicOperator op, Condition second) {
        switch(op) {
            case AND:
                return first.group().and(second.group());
            case OR:
                return first.group().or(second.group());
            default:
                throw new IllegalArgumentException("Operator not supported: " + op);
        }
    }
    
    public static <T> Condition group(Condition condition) {
        return condition.group();
    }
}