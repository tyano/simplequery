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

import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.expression.AttributeInfo;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultAttributeInfo<T> implements AttributeInfo<T> {

    private final Class<? extends T> clazz;

    public DefaultAttributeInfo(Class<? extends T> clazz) {
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
    public DefaultAttributeInfo(T sampleValue) {
        this.clazz = (Class<? extends T>) sampleValue.getClass();
    }

    @Override
    public String convertValue(T targetValue) {
        isNotNull("targetValue", targetValue);
        return SimpleDBUtils.quoteValue(targetValue.toString());
    }

    @Override
    public T restoreValue(String targetValue) {
        if (clazz.isAssignableFrom(String.class)) {
            return clazz.cast(targetValue);
        }
        
        try {
            try {
                Constructor<? extends T> cons = clazz.getConstructor(String.class);
                return cons.newInstance(targetValue);
            } catch (NoSuchMethodException e) {
                try {
                    Method m = clazz.getMethod("valueOf", String.class);
                    if (clazz.isAssignableFrom(m.getReturnType())) {
                        return clazz.cast(m.invoke(null, targetValue));
                    } else {
                        return null;
                    }
                } catch (NoSuchMethodException ex) {
                    return null;
                }
            }
        } catch (InstantiationException ex) {
            throw new IllegalStateException(ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
