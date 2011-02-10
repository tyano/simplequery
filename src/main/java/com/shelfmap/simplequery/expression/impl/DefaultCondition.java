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

import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.expression.Condition;
import com.shelfmap.simplequery.expression.Matcher;
import com.shelfmap.simplequery.expression.Operator;
import com.shelfmap.simplequery.expression.ValueConverter;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultCondition implements Condition {

    private Condition parent;
    private Operator operator;
    private String attributeName;
    private Matcher matcher;
    private boolean grouped = false;

    public DefaultCondition(Condition parent, Operator operator, String attributeName, Matcher matcher) {
        isNotNull("parent", parent);
        isNotNull("operator", operator);
        isNotNull("attributeName", attributeName);
        isNotNull("matcher", matcher);
        this.parent = parent;
        this.operator = operator;
        this.attributeName = attributeName;
        this.matcher = matcher;
    }

    public DefaultCondition(String attributeName, Matcher matcher) {
        this(NullCondition.INSTANCE, NullOperator.INSTANCE, attributeName, matcher);
    }

    public String getAttributeName() {
        return attributeName;
    }

    public Matcher getMatcher() {
        return matcher;
    }
    
    @Override
    public Condition and(Condition other) {
        return new DefaultCondition(this, BasicOperator.AND, attributeName, matcher);
    }

    @Override
    public Condition and(String attributeName, Matcher matcher) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Condition or(Condition other) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Condition or(String attributeName, Matcher matcher) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Condition group() {
        this.grouped = true;
        return this;
    }
    
    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        
        if(grouped) sb.append("(");
        
        sb.append(getParent().describe());
        sb.append(getOperator().describe());
        
        ValueConverter converter = converter();
        sb.append(converter.convertAttribute(getAttributeName())).append(getMatcher().describe());
        
        if(grouped) sb.append(")");
        
        return sb.toString();
    }

    public Operator getOperator() {
        return operator;
    }

    public Condition getParent() {
        return parent;
    }

    protected ValueConverter converter() {
        return null;
    }
}
