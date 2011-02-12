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
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.shelfmap.simplequery.expression.Condition;
import com.shelfmap.simplequery.expression.DomainExpression;
import com.shelfmap.simplequery.expression.Matcher;
import com.shelfmap.simplequery.expression.WhereExpression;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultDomainExpression<T> extends BaseExpression<T> implements DomainExpression<T> {
    private AmazonSimpleDB simpleDB;
    private Select selectObject;
    private String domainName;
    private Class<T> typeToken;

    public DefaultDomainExpression(AmazonSimpleDB simpleDB, Select selectObject, String domainName, Class<T> typeToken) {
        isNotNull("simpleDB", simpleDB);
        isNotNull("selectObject", selectObject);
        isNotNull("domainName", domainName);
        isNotNull("typeToken", typeToken);
        this.simpleDB = simpleDB;
        this.selectObject = selectObject;
        this.domainName = domainName;
        this.typeToken = typeToken;
    }
    
    @Override
    public WhereExpression<T> where(Condition expression) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WhereExpression<T> where(String attributeName, Matcher<T> matcher) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String describe() {
        return selectObject.describe() + " from " + domainName;
    }

    public String getDomainName() {
        return domainName;
    }

    public Select getSelectObject() {
        return selectObject;
    }

    public AmazonSimpleDB getSimpleDB() {
        return simpleDB;
    }

    public Class<T> getTypeToken() {
        return typeToken;
    }
}
