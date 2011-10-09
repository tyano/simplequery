/*
 * Copyright 2011 Tsutomu YANO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.shelfmap.simplequery.util;

import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Tsutomu YANO
 */
public final class Objects {

    private Objects() {
    }
    
    public static Collection<Class<?>> linearlize(Class<?> parentClass) {
        List<Class<?>> resultList = new ArrayList<Class<?>>();
        appendAllInterfaces(parentClass, resultList);
        
        Class<?> superClass = parentClass.getSuperclass();
        while(superClass != null && superClass != Object.class) {
            appendAllInterfaces(superClass, resultList);
            superClass = superClass.getSuperclass();
        }
        return resultList;
    }
    
    private static void appendAllInterfaces(Class<?> clazz, List<Class<?>> list) {
        list.add(clazz);
        list.addAll(asList(clazz.getInterfaces()));
    }
}
