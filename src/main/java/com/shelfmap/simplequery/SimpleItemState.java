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
package com.shelfmap.simplequery;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.DeletableItem;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Tsutomu YANO
 */
public class SimpleItemState implements ItemState {
    final String domainName;
    List<Attribute> deletedList = new ArrayList<Attribute>();
    List<ReplaceableAttribute> changedList = new ArrayList<ReplaceableAttribute>();

    public SimpleItemState(String domainName) {
        super();
        this.domainName = domainName;
    }

    @Override
    public String getDomainName() {
        return domainName;
    }

    @Override
    public List<ReplaceableAttribute> getChangedItems() {
        return new ArrayList<ReplaceableAttribute>(changedList);
    }

    @Override
    public List<Attribute> getDeletedItems() {
        return new ArrayList<Attribute>(deletedList);
    }

    @Override
    public void addChanged(ReplaceableAttribute... changed) {
        this.changedList.addAll(Arrays.asList(changed));
    }

    @Override
    public void addDeleted(Attribute... deleted) {
        this.deletedList.addAll(Arrays.asList(deleted));
    }
}
