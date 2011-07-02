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
package com.shelfmap.simplequery;

import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Tsutomu YANO
 */
public class SimpleDbUtil {

    public SimpleDbUtil() {
    }
    
    public static ReplaceableAttribute attr(String name, String value, boolean replace) {
        return new ReplaceableAttribute(name, value, replace);
    }

    public static ReplaceableItem item(String itemName, List<ReplaceableAttribute> attrs) {
        return new ReplaceableItem(itemName, attrs);
    }
    
    public static ReplaceableItem item(String itemName, ReplaceableAttribute... attrs) {
        return new ReplaceableItem(itemName, Arrays.asList(attrs));
    }    
}
