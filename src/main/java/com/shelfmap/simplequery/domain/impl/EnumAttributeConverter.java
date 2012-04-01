/*
 * Copyright 2011 Tsutomu YANO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author Tsutomu YANO
 */
public class EnumAttributeConverter<T extends Enum<T>> implements AttributeConverter<T>, Serializable {
    private static final long serialVersionUID = 1L;
    private ClassReference enumClassRef;

    public EnumAttributeConverter(Class<T> enumClass) {
        this.enumClassRef = new SimpleClassReference(enumClass);
    }

    @Override
    public String convertValue(T targetValue) {
        return targetValue.name();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T restoreValue(String targetValue) throws CanNotRestoreAttributeException {

        Class<T> enumClass = (Class<T>) enumClassRef.get();

        try {
            Method m = enumClass.getMethod("valueOf", String.class);
            if (enumClass.isAssignableFrom(m.getReturnType())) {
                try {
                    return enumClass.cast(m.invoke(null, targetValue));
                } catch (IllegalAccessException ex) {
                    throw new CanNotRestoreAttributeException(ex, targetValue, enumClass);
                } catch (IllegalArgumentException ex) {
                    throw new CanNotRestoreAttributeException(ex, targetValue, enumClass);
                } catch (InvocationTargetException ex) {
                    throw new CanNotRestoreAttributeException(ex, targetValue, enumClass);
                }
            } else {
                //the return-type of valueOf() method do not match with the target class.
                throw new CanNotRestoreAttributeException("There are no constructor with a String argument nor 'valueOf(String)' static method in the target class.", targetValue, enumClass);
            }
        } catch (NoSuchMethodException ex) {
            //There are no method for restoring a value.
            throw new CanNotRestoreAttributeException("There are no constructor with a String argument nor 'valueOf(String)' static method in the target class.", ex, targetValue, enumClass);
        }
    }
}
