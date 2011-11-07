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
package com.shelfmap.simplequery.domain.testdomain;

import com.shelfmap.simplequery.annotation.ForwardDomainReference;
import com.shelfmap.simplequery.annotation.IntAttribute;
import com.shelfmap.simplequery.annotation.ItemName;
import com.shelfmap.simplequery.annotation.SimpleDbDomain;
import com.shelfmap.simplequery.domain.DomainReferenceTest;
import com.shelfmap.simplequery.domain.ToOneDomainReference;

/**
 *
 * @author Tsutomu YANO
 */
@SimpleDbDomain(value = DomainReferenceTest.CHILD_DOMAIN)
public interface ToManyDetail {

    @ItemName
    String getItemName();

    void setItemName(String itemName);

    String getName();

    void setName(String name);

    @IntAttribute(padding = 5)
    int getAmount();

    void setAmount(int amount);

    @ForwardDomainReference(attributeName = "parentItemName", targetDomainClass = ToManyPurchaseRecord.class)
    ToOneDomainReference<ToManyPurchaseRecord> getParentRecordReference();
}
