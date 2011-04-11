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
package com.shelfmap.simplequery.expression;

import com.shelfmap.simplequery.expression.impl.Select;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.shelfmap.simplequery.Configuration;
import static org.junit.Assert.*;
import static com.shelfmap.simplequery.expression.matcher.MatcherFactory.*;
import com.shelfmap.simplequery.Domain;
import com.shelfmap.simplequery.IntAttribute;
import com.shelfmap.specsfinder.Steps;
import java.io.File;
import java.io.IOException;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

/**
 *
 * @author Tsutomu YANO
 */
@Steps
public class WhereExpressionTestSteps {

    Expression<?> expression;
    AmazonSimpleDB simpleDB;
    Configuration configuration = new DummyConfiguration();
    
    private String getSecurityCredentialPath() {
        return "/Users/t_yano/aws.credential.properties";
    }
    
    @Given("a AmazonSimpleDB client")
    public void createClient() throws IOException {
        simpleDB = new AmazonSimpleDBClient(new PropertiesCredentials(new File(getSecurityCredentialPath())));
    }
    
    @When("TestDomain don't have any @SimpleDBAttribute annotation on it's properties,")
    public void createExpression() {
        expression = new Select(simpleDB, configuration).from(DomainWithoutAttribute.class).where("saving", greaterThan(100000));
    }

    @Then("WhereExpression will generate a simple expression with no padding and no offset.")
    public void assertExpression() {
        assertThat(expression.describe(), Matchers.is("select * from `testdomain` where `saving` > '100000'"));
    }

    @When("TestDomain have a @SimpleDBAttribute on a property whose name is same with the attribute specified in a expression")
    public void createExpressionWithAnnotation() {
        expression = new Select(simpleDB, configuration).from(DomainWithAttribute.class).where("saving", greaterThan(100000));
    }

    @Then("WhereExpression will use a padding and a offset on the annotation")
    public void assertExpressionWithAnnotation() {
        assertThat(expression.describe(), Matchers.is("select * from `with-attribute` where `saving` > '01100000'"));
    }

    @When("a property have a @SimpleDBAttribute annotation, but the annotation don't have attributeName,")
    public void createExpressionWithoutAttributeName() {
        expression = new Select(simpleDB, configuration).from(DomainWithoutAttributeName.class).where("saving", greaterThan(500));
    }

    @Then("this library will find the annotation through the name of property instead of the attributeName, and uses values of the annotation.")
    public void assertExpressionWithoutAttributeName() {
        assertThat(expression.describe(), Matchers.is("select * from `without-attributename` where `saving` > '0200500'"));
    }

    
    @Domain("testdomain")
    public static interface DomainWithoutAttribute {

        int getSaving();
    }

    @Domain("with-attribute")
    public static interface DomainWithAttribute {

        @IntAttribute(attributeName = "saving", padding = 8, offset = 1000000)
        int getAccountSaving();
    }
    
    @Domain("without-attributename")
    public static interface DomainWithoutAttributeName {
        @IntAttribute(padding=7, offset=200000)
        int getSaving();
    }

}
