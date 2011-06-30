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

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import org.jbehave.core.annotations.Given;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tsutomu YANO
 */
public class ClientFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientFactory.class);

    @Inject
    IClientHolder holder;

    @Given("a SimpleQuery client")
    public void createClinet() {
        LOGGER.debug("create client.");

        AmazonSimpleDB simpleDb = getAmazonSimpleDB();
        Configuration conf = new com.shelfmap.simplequery.DefaultConfiguration();

        holder.setSimpleDb(getAmazonSimpleDB());
        holder.setClient(new SimpleQueryClient(simpleDb, conf));
        holder.setConfiguration(conf);
    }

    public String getSecurityCredentialPath() {
        return "/Users/t_yano/aws.credential.properties";
    }

    public AmazonSimpleDB getAmazonSimpleDB() {
        try {
            return new AmazonSimpleDBClient(new PropertiesCredentials(new File(getSecurityCredentialPath())));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
