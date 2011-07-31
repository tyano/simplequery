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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.shelfmap.simplequery.attribute.SelectAttribute;
import com.shelfmap.simplequery.expression.SelectQuery;
import com.shelfmap.simplequery.expression.impl.Select;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tsutomu YANO
 */
public class SimpleQueryClient implements Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleQueryClient.class);
    private final AWSCredentials credentials;
    private final AmazonSimpleDB simpleDB;
    private final Configuration configuration;
    private final AmazonS3 s3;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public SimpleQueryClient(File securityCredencial, Configuration configuration) throws ClientConfigurationException {
        try {
            this.credentials = new PropertiesCredentials(securityCredencial);
            this.simpleDB = createSimpleDb(credentials);
            this.s3 = createS3(credentials);
            this.configuration = configuration;
        } catch (FileNotFoundException ex) {
            throw new ClientConfigurationException("security credential file ' + " + securityCredencial.getAbsolutePath() + "' does not exist.", ex);
        } catch (IOException ex) {
            throw new ClientConfigurationException("could not read the security credential file '" + securityCredencial.getAbsolutePath() + "'.", ex);
        } catch (IllegalArgumentException ex) {
            throw new ClientConfigurationException("the security information of security credential file ('" + securityCredencial.getAbsolutePath() + "') is not enough or not correct.");
        }
    }

    @Override
    public AmazonSimpleDB getSimpleDB() {
        return this.simpleDB;
    }

    @Override
    public SelectQuery select(SelectAttribute... attribute) {
        return newSelectQuery(attribute);
    }

    protected SelectQuery newSelectQuery(SelectAttribute... attribute) {
        return new Select(simpleDB, configuration, attribute);
    }

    @Override
    public AmazonS3 getS3() {
        return this.s3;
    }

    protected AmazonSimpleDB createSimpleDb(AWSCredentials securityCredential) throws FileNotFoundException, IOException {
        ClientConfiguration clientConfig = configureSimpleDb();
        return clientConfig == null
                ? new AmazonSimpleDBClient(securityCredential)
                : new AmazonSimpleDBClient(securityCredential, clientConfig);
    }

    protected ClientConfiguration configureSimpleDb() {
        return null;
    }

    protected AmazonS3 createS3(AWSCredentials securityCredential) {
        ClientConfiguration clientConfig = configureS3();
        return clientConfig == null
                ? new AmazonS3Client(securityCredential)
                : new AmazonS3Client(securityCredential, clientConfig);
    }

    protected ClientConfiguration configureS3() {
        return null;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public AWSCredentials getCredentials() {
        return credentials;
    }
}
