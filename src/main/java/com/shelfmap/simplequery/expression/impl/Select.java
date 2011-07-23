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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.amazonaws.services.simpledb.AmazonSimpleDB;

import com.shelfmap.simplequery.Configuration;
import com.shelfmap.simplequery.annotation.Domain;
import com.shelfmap.simplequery.attribute.SelectAttribute;
import com.shelfmap.simplequery.attribute.impl.AllAttribute;
import com.shelfmap.simplequery.expression.DomainExpression;
import com.shelfmap.simplequery.expression.SelectQuery;

/**
 *
 * @author Tsutomu YANO
 */
public class Select implements SelectQuery {
    private final AmazonSimpleDB simpleDB;
    private final Configuration configuration;
    private final List<SelectAttribute> attributes = new ArrayList<SelectAttribute>();

    public Select(AmazonSimpleDB simpleDB, Configuration configuration, SelectAttribute... attribute) {
        this.simpleDB = simpleDB;
        this.configuration = configuration;
        innerAddAttribute(attribute);
    }

    @Override
    public <T> DomainExpression<T> from(Class<T> target) {
        Domain annotation = target.getAnnotation(Domain.class);
        if (annotation == null) {
            throw new IllegalArgumentException("the class object must have @Domain annotation.");
        }
        String domainName = annotation.value();
        return new DefaultDomainExpression<T>(this.simpleDB, this.configuration, this, domainName, target);
    }

    @Override
    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");

        synchronized (attributes) {
            if (attributes.isEmpty()) {
                sb.append(AllAttribute.INSTANCE.describe());
            } else {
                StringBuilder attrs = new StringBuilder();
                for (SelectAttribute attribute : attributes) {
                    if (attrs.length() > 0) {
                        attrs.append(", ");
                    }
                    attrs.append(attribute.describe());
                }
                sb.append(attrs);
            }
        }

        return sb.toString();
    }

    @Override
    public SelectQuery withAttributes(SelectAttribute... attributeArray) {
        List<SelectAttribute> newAttributes = null;
        synchronized (this.attributes) {
            newAttributes = new ArrayList<SelectAttribute>(this.attributes);
        }
        newAttributes.addAll(Arrays.asList(attributeArray));
        return new Select(this.simpleDB, this.configuration, newAttributes.toArray(new SelectAttribute[newAttributes.size()]));
    }

    @Override
    public Collection<SelectAttribute> getAttributes() {
        Collection<SelectAttribute> result = null;
        synchronized (this.attributes) {
            result = new ArrayList<SelectAttribute>(this.attributes);
        }
        return result;
    }

    @Override
    public void addAttributes(SelectAttribute... attributes) {
        innerAddAttribute(attributes);
    }

    private void innerAddAttribute(SelectAttribute... attributes) {
        if (attributes != null && attributes.length > 0) {
            synchronized (this.attributes) {
                this.attributes.addAll(Arrays.asList(attributes));
            }
        }
    }
}
