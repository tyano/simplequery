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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
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

    public static final String CREDENTIAL_PATH = "/Users/t_yano/aws.credential.properties";
    
    @Inject
    ContextHolder holder;

    @Given("a SimpleQuery client")
    public void createClinet() throws IOException {
        LOGGER.debug("create client.");

        File securityFile = new File(getSecurityCredentialPath());
        AWSCredentials credentials = new PropertiesCredentials(securityFile);

        Context context = new DefaultContext(credentials);

        Client client = new SimpleQueryClient(context, context.getCredentials());
        holder.setSimpleDb(client.getSimpleDB());
        holder.setContext(context);

//        holder.getClient().getS3().setEndpoint("s3-ap-northeast-1.amazonaws.com");
//        holder.getSimpleDb().setEndpoint("sdb.ap-northeast-1.amazonaws.com");
    }

    public String getSecurityCredentialPath() {
        return CREDENTIAL_PATH;
    }
}
