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

package com.shelfmap.simplequery.expression;

import com.amazonaws.services.simpledb.model.Item;

/**
 *
 * @author Tsutomu YANO
 */
public class CanNotConvertItemException extends Exception {
    private static final long serialVersionUID = 1L;
    
    private Item item;

    public CanNotConvertItemException(Throwable thrwbl, Item item) {
        super(thrwbl);
        this.item = item;
    }

    public CanNotConvertItemException(String string, Throwable thrwbl, Item item) {
        super(string, thrwbl);
        this.item = item;
    }

    public CanNotConvertItemException(String string, Item item) {
        super(string);
        this.item = item;
    }

    public CanNotConvertItemException(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }
}
