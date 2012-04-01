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


import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.attribute.SelectAttribute;
import com.shelfmap.simplequery.attribute.impl.AllAttribute;
import com.shelfmap.simplequery.domain.Domain;
import com.shelfmap.simplequery.domain.DomainFactory;
import com.shelfmap.simplequery.expression.DomainExpression;
import com.shelfmap.simplequery.expression.SelectQuery;
import java.io.Serializable;

/**
 *
 * @author Tsutomu YANO
 */
public class Select implements SelectQuery, Serializable {
    private static final long serialVersionUID = 1L;
    private final Context context;
    private final List<SelectAttribute> attributes = new ArrayList<SelectAttribute>();

    public Select(Context context, SelectAttribute... attribute) {
        this.context = context;
        innerAddAttribute(attribute);
    }

    @Override
    public <T> DomainExpression<T> from(Class<T> target) {
        DomainFactory factory = getContext().getDomainFactory();
        Domain<T> domain = factory.createDomain(target);
        return new DefaultDomainExpression<T>(this.context, this, domain);
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
        return new Select(this.context, newAttributes.toArray(new SelectAttribute[newAttributes.size()]));
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

    @Override
    public Context getContext() {
        return context;
    }
}
