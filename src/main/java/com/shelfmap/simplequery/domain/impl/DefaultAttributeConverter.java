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

import com.shelfmap.simplequery.ClassReference;
import com.shelfmap.simplequery.SimpleClassReference;
import com.shelfmap.simplequery.domain.AttributeConverter;
import com.shelfmap.simplequery.expression.CanNotRestoreAttributeException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This implementation convert an object to String through the toString() method of
 * the passed object, and restore a string to an object through the constructor
 * with a String argument or through the static method 'valueOf()' with a String
 * argument.
 *
 * @author Tsutomu YANO
 */
public class DefaultAttributeConverter<T> implements AttributeConverter<T>, Serializable {
    private static final long serialVersionUID = 1L;

    private final ClassReference classRef;

    public DefaultAttributeConverter(Class<T> clazz) {
        this.classRef = new SimpleClassReference(clazz);
    }

    @SuppressWarnings("unchecked")
    public DefaultAttributeConverter(T sampleValue) {
        this.classRef = new SimpleClassReference(sampleValue.getClass());
    }

    @Override
    public String convertValue(T targetValue) {
        if(targetValue == null) return null;
        return targetValue.toString();
    }

    /**
     * @{inheritDoc}
     *
     * @param targetValue a String object to restore.
     * @return restored object whose type is the type of the target property.
     * @throws CanNotRestoreAttributeException
     *         if any exception is thrown when accessing the target class object with reflection api.
     *         Or the class object do not have a constructor with single string argument or
     *         a valueOf(String) static method.
     */
    @Override
    public T restoreValue(String targetValue) throws CanNotRestoreAttributeException {
        if(targetValue == null) return null;

        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) classRef.get();

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
                        //the return-type of valueOf() method do not match with the target class.
                        throw new CanNotRestoreAttributeException("There are no constructor with a String argument nor 'valueOf(String)' static method in the target class.", targetValue, clazz);
                    }
                } catch (NoSuchMethodException ex) {
                    //There are no constructor nor method for restoring a value.
                    throw new CanNotRestoreAttributeException("There are no constructor with a String argument nor 'valueOf(String)' static method in the target class.", ex, targetValue, clazz);
                }
            }
        } catch (InstantiationException ex) {
            throw new CanNotRestoreAttributeException(ex, targetValue, clazz);
        } catch (IllegalAccessException ex) {
            throw new CanNotRestoreAttributeException(ex, targetValue, clazz);
        } catch (IllegalArgumentException ex) {
            throw new CanNotRestoreAttributeException(ex, targetValue, clazz);
        } catch (InvocationTargetException ex) {
            throw new CanNotRestoreAttributeException(ex, targetValue, clazz);
        }
    }
}
