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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.simpledb.AmazonSimpleDB;

/**
 *
 * @author Tsutomu YANO
 */
public class TokyoContext extends DefaultContext {
    private static final long serialVersionUID = 1L;

    public TokyoContext(AWSCredentials credentials) {
        super(credentials);
    }

    @Override
    public AmazonS3 createS3(AWSCredentials securityCredential) {
        AmazonS3 s3 =  super.createS3(securityCredential);
        s3.setEndpoint("s3-ap-northeast-1.amazonaws.com");
        return s3;
    }

    @Override
    protected AmazonSimpleDB createSimpleDb(AWSCredentials securityCredential) {
        AmazonSimpleDB simpleDB = super.createSimpleDb(securityCredential);
        simpleDB.setEndpoint("sdb.ap-northeast-1.amazonaws.com");
        return simpleDB;
    }
}
