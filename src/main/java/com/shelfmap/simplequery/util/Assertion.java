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

package com.shelfmap.simplequery.util;

import java.util.Collection;

/**
 *
 * @author Tsutomu YANO
 */
public final class Assertion {
    private Assertion() {
        super();
    }
    
    public static void isNotNull(String parameterName, Object value) {
        if(value == null) throw new IllegalArgumentException("the parameter '" + parameterName + "' must not be null.");
    }
    
    public static void isNotEmpty(String parameterName, String value) {
        isNotNull(parameterName, value);
        if(value.isEmpty()) throw new IllegalArgumentException("the parameter '" + parameterName + "' must not be empty.");
    }
    
    public static void isNotEmpty(String parameterName, Collection<?> values) {
        if(values.isEmpty()) throw new IllegalArgumentException("the parameter '" + parameterName + "' must not be empty.");
    }

    public static void isNotEmpty(String parameterName, Object[] values) {
        if(values.length == 0) throw new IllegalArgumentException("the parameter '" + parameterName + "' must not be empty.");
    }
    
    public static <T> T isNotNullAndGet(String parameterName, Object value, Accessor<T> accessor) {
        isNotNull(parameterName, value);
        return accessor.get();
    }
    
    public static <T> T isNotNullAndReturn(String parameterName, T value) {
        isNotNull(parameterName, value);
        return value;
    }
            
    public static interface Accessor<T> {
        T get();
    }
}
