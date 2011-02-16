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

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.shelfmap.simplequery.Domain;
import com.shelfmap.simplequery.expression.Attribute;
import com.shelfmap.simplequery.expression.DomainExpression;
import com.shelfmap.simplequery.expression.SelectQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Tsutomu YANO
 */
public class Select implements SelectQuery {

    private final AmazonSimpleDB sdb;
    private final List<Attribute> attributes = new ArrayList<Attribute>();

    public Select(AmazonSimpleDB client, Attribute... attribute) {
        this.sdb = client;
        innerAddAttribute(attribute);
    }

    @Override
    public <T> DomainExpression<T> from(Class<T> target) {
        Domain annotation = target.getAnnotation(Domain.class);
        if (annotation == null) {
            throw new IllegalArgumentException("the class object must have @Domain annotation.");
        }
        String domainName = annotation.value();
        return new DefaultDomainExpression<T>(sdb, this, domainName, target);
    }

    public AmazonSimpleDB getSimpleDB() {
        return sdb;
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");

        synchronized (attributes) {
            if (attributes.isEmpty()) {
                sb.append("*");
            } else {
                StringBuilder attrs = new StringBuilder();
                for (Attribute attribute : attributes) {
                    if (attrs.length() > 0) {
                        attrs.append(", ");
                    }
                    attrs.append(attribute.getName());
                }
                sb.append(attrs);
            }
        }

        return sb.toString();
    }

    @Override
    public SelectQuery withAttribute(Attribute attribute) {
        addAttributes(attribute);
        return this;
    }

    @Override
    public Collection<Attribute> getAttributes() {
        Collection<Attribute> result = null;
        synchronized(this.attributes) {
            result = new ArrayList<Attribute>(this.attributes);
        }
        return result;
    }

    @Override
    public void addAttributes(Attribute... attributes) {
        innerAddAttribute(attributes);
    }
    
    private void innerAddAttribute(Attribute... attributes) {
        if (attributes != null && attributes.length > 0) {
            synchronized (this.attributes) {
                this.attributes.addAll(Arrays.asList(attributes));
            }
        }
    }
}
