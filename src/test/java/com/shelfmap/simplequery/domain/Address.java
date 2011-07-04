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
package com.shelfmap.simplequery.domain;

import com.shelfmap.simplequery.annotation.Domain;
import com.shelfmap.simplequery.annotation.FlatAttribute;
import com.shelfmap.simplequery.annotation.IntAttribute;

/**
 *
 * @author Tsutomu YANO
 */
@Domain(value = "flatattribute-test")
public class Address {
    private int postalCode;
    private TelInfo telInfo;
    private String address1;
    private String address2;

    public Address() {
    }

    public Address(int postalCode, TelInfo telInfo) {
        this.postalCode = postalCode;
        this.telInfo = telInfo;
    }

    @IntAttribute(padding = 7)
    public int getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(int postalCode) {
        this.postalCode = postalCode;
    }

    @FlatAttribute
    public TelInfo getTelInfo() {
        return telInfo;
    }

    public void setTelInfo(TelInfo telInfo) {
        this.telInfo = telInfo;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

}
