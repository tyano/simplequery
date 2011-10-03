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

import com.amazonaws.services.simpledb.AmazonSimpleDB;

/**
 *
 * @author Tsutomu YANO
 */
public class TestContext implements IClientHolder {
    private AmazonSimpleDB simpleDb;
    private Client client;
    private Context context;

    @Override
    public AmazonSimpleDB getSimpleDb() {
        return simpleDb;
    }

    @Override
    public void setSimpleDb(AmazonSimpleDB client) {
        this.simpleDb = client;
    }

    @Override
    public Client getClient() {
        return client;
    }

    @Override
    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void setContext(Context configuration) {
        this.context = configuration;
    }

}
