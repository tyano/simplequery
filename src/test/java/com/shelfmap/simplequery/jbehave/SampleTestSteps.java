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

package com.shelfmap.simplequery.jbehave;

import com.shelfmap.specsfinder.Steps;
import java.util.Arrays;
import org.jbehave.core.annotations.Then;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.ListDomainsResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.When;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author Tsutomu YANO
 */
@Steps
public class SampleTestSteps {
    private AmazonSimpleDB sdb;
    private List<String> domainNames;
    
    @Given("A SimpleDB Client")
    public void createClient() throws IOException {
        AWSCredentials credential = new PropertiesCredentials(new File(getSecurityCredentialPath()));
        sdb = new AmazonSimpleDBClient(credential);
    }
    
    @When("I retrieve all of the domains from sample db,")
    public void retrieveDomainNames() {
        ListDomainsResult result = sdb.listDomains();
        domainNames = result.getDomainNames();
    }
    
    @Then("the count of the domains should be $count.")
    public void checkDomainCountAndName(int count) {
        assertThat(domainNames.size(), is(count));
    }
    
    private String getSecurityCredentialPath() {
        return "/Users/t_yano/aws.credential.properties";
    }
    
    
    @When("I put a new item named as '$itemName' into our sample db")
    public void createNewRow(String itemName) {
        PutAttributesRequest request = new PutAttributesRequest("sample", itemName, Arrays.asList(new ReplaceableAttribute("name", "t_yano", false), new ReplaceableAttribute("place", "Tokyo", false)));
        sdb.putAttributes(request);
    }
    
    @Then("I can get the '$itemName' item from the db")
    public void canRetrieveNewRow(String itemName) {
        GetAttributesRequest request = new GetAttributesRequest("sample", itemName);
        request.setAttributeNames(Arrays.asList("name", "place"));
        GetAttributesResult result = sdb.getAttributes(request);
        
        List<Attribute> attributes = result.getAttributes();
        assertThat(attributes.size(), is(2));
        
        for(Attribute attribute : attributes) {
            if(attribute.getName().equals("name")) {
                assertThat(attribute.getValue(), is("t_yano"));
            } else if(attribute.getName().equals("place")) {
                assertThat(attribute.getValue(), is("Tokyo"));
            }
        }
    }
    
}
