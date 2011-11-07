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

import com.shelfmap.simplequery.Context;
import static com.shelfmap.simplequery.attribute.Attributes.attr;
import com.shelfmap.simplequery.attribute.ConditionAttribute;
import com.shelfmap.simplequery.domain.Domain;
import com.shelfmap.simplequery.domain.DomainFactory;
import com.shelfmap.simplequery.domain.ReverseToManyDomainReference;
import com.shelfmap.simplequery.domain.impl.DefaultReverseToManyDomainReference;
import java.util.Date;

/**
 *
 * @author Tsutomu YANO
 */
public class ToManyPurchaseRecordImpl implements ToManyPurchaseRecord {
    String itemName;
    Date requestDate;
    final ReverseToManyDomainReference<ToManyDetail> detailReference;

    public ToManyPurchaseRecordImpl(Context context) {
        this(context, null, null);
    }

    public ToManyPurchaseRecordImpl(Context context, String itemName, Date requestDate) {
        this.itemName = itemName;
        this.requestDate = requestDate == null ? null : new Date(requestDate.getTime());
        DomainFactory factory = context.getDomainFactory();
        Domain<ToManyDetail> detailDomain = factory.createDomain(ToManyDetail.class);
        ConditionAttribute targetAttribute = attr("parentItemName");
        this.detailReference = new DefaultReverseToManyDomainReference<ToManyPurchaseRecord, ToManyDetail>(context, this, detailDomain, targetAttribute);
    }

    @Override
    public String getItemName() {
        return this.itemName;
    }

    @Override
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    @Override
    public Date getRequestDate() {
        return new Date(this.requestDate.getTime());
    }

    @Override
    public void setRequestDate(Date requestDate) {
        this.requestDate = new Date(requestDate.getTime());
    }

    @Override
    public ReverseToManyDomainReference<ToManyDetail> getDetailReference() {
        return this.detailReference;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ToManyPurchaseRecordImpl other = (ToManyPurchaseRecordImpl) obj;
        if ((this.itemName == null) ? (other.itemName != null) : !this.itemName.equals(other.itemName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.itemName != null ? this.itemName.hashCode() : 0);
        return hash;
    }
}
