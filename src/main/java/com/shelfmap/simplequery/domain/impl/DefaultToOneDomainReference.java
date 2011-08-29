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
package com.shelfmap.simplequery.domain.impl;

import static com.shelfmap.simplequery.expression.matcher.MatcherFactory.is;
import com.shelfmap.simplequery.Client;
import com.shelfmap.simplequery.annotation.ItemName;
import com.shelfmap.simplequery.domain.ToOneDomainReference;
import com.shelfmap.simplequery.expression.Expression;
import com.shelfmap.simplequery.expression.MultipleResultsExistException;
import com.shelfmap.simplequery.expression.QueryResults;
import com.shelfmap.simplequery.expression.SimpleQueryException;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @param <T>
 * @author Tsutomu YANO
 */
public class DefaultToOneDomainReference<T> implements ToOneDomainReference<T> {

    private final Client client;
    private final Class<T> domainClass;
    private String targetItemName = null;

    private Method itemNameReader;

    public DefaultToOneDomainReference(Client client, Class<T> domainClass) {
        this.client = client;
        this.domainClass = domainClass;
    }

    @Override
    public Class<T> getDomainClass() {
        return domainClass;
    }

    @Override
    public T get(boolean consistent) throws SimpleQueryException, MultipleResultsExistException {
        return createExpression().getSingleResult(consistent);
    }

    @Override
    public QueryResults<T> getResults(boolean consistent) throws SimpleQueryException {
        return createExpression().getResults(consistent);
    }

    private Expression<T> createExpression() {
        return client.select().from(getDomainClass()).whereItemName(is(getTargetItemName()));
    }

    public Client getClient() {
        return client;
    }

    public String getTargetItemName() {
        return targetItemName;
    }

    @Override
    public void set(T object) {
        if(this.itemNameReader == null) {
            this.itemNameReader = findItemNameReader();
        }
        try {
            Object o = this.itemNameReader.invoke(object, new Object[0]);
            this.targetItemName = (String)o;
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Method findItemNameReader() {
        try {
            BeanInfo info = Introspector.getBeanInfo(domainClass);
            PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
                //do not handle the properties of Object class.
                //(Object class have only one property 'getClass()')
                if (!descriptor.getName().equals("class")) {
                    Method getter = descriptor.getReadMethod();

                    if (getter.isAnnotationPresent(ItemName.class)) {
                        return getter;
                    }
                }
            }
            throw new IllegalStateException("@ItemName annotation is not found on the domain-class: " + domainClass.getName());
        } catch (IntrospectionException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
