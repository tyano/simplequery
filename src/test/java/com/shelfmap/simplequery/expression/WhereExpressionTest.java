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

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.shelfmap.simplequery.BaseStoryRunner;
import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.DefaultContext;
import com.shelfmap.simplequery.StoryPath;
import com.shelfmap.simplequery.annotation.IntAttribute;
import com.shelfmap.simplequery.annotation.SimpleDbDomain;
import static com.shelfmap.simplequery.attribute.Attributes.attr;
import com.shelfmap.simplequery.expression.impl.Select;
import static com.shelfmap.simplequery.expression.matcher.MatcherFactory.greaterThan;
import java.io.File;
import java.io.IOException;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Tsutomu YANO
 */
@StoryPath("stories/WhereExpressionSpec.story")
public class WhereExpressionTest extends BaseStoryRunner {
    Expression<?> expression;
    AmazonSimpleDB simpleDB;
    Context context;

    public WhereExpressionTest() throws IOException {
        this.context = new DefaultContext(new PropertiesCredentials(new File(getSecurityCredentialPath())));
    }

    private String getSecurityCredentialPath() {
        return "/Users/t_yano/aws.credential.properties";
    }

    @Given("a AmazonSimpleDB client")
    public void createClient() throws IOException {
        simpleDB = new AmazonSimpleDBClient(context.getCredentials());
    }

    @When("TestDomain don't have any @Attribute annotation on it's properties,")
    public void createExpression() {
        expression = new Select(context).from(DomainWithoutAttribute.class).where(attr("saving"), greaterThan(100000));
    }

    @Then("WhereExpression will generate a simple expression with no padding nor offset.")
    public void assertExpression() {
        assertThat(expression.describe(), Matchers.is("select * from `testdomain` where `saving` > '100000'"));
    }

    @When("TestDomain have a @Attribute on a property whose name is same with the attribute specified in a expression")
    public void createExpressionWithAnnotation() {
        expression = new Select(context).from(DomainWithAttribute.class).where(attr("saving"), greaterThan(100000));
    }

    @Then("WhereExpression will use a padding and a offset on the annotation")
    public void assertExpressionWithAnnotation() {
        assertThat(expression.describe(), Matchers.is("select * from `with-attribute` where `saving` > '01100000'"));
    }

    @When("a property have a @Attribute annotation, but the annotation don't have attributeName,")
    public void createExpressionWithoutAttributeName() {
        expression = new Select(context).from(DomainWithoutAttributeName.class).where(attr("saving"), greaterThan(500));
    }

    @Then("this library will find the annotation through the name of property instead of the attributeName, and uses values of the annotation.")
    public void assertExpressionWithoutAttributeName() {
        assertThat(expression.describe(), Matchers.is("select * from `without-attributename` where `saving` > '0200500'"));
    }


    @SimpleDbDomain("testdomain")
    public static interface DomainWithoutAttribute {

        int getSaving();
    }

    @SimpleDbDomain("with-attribute")
    public static interface DomainWithAttribute {

        @IntAttribute(attributeName = "saving", padding = 8, offset = 1000000)
        int getAccountSaving();
    }

    @SimpleDbDomain("without-attributename")
    public static interface DomainWithoutAttributeName {
        @IntAttribute(padding=7, offset=200000)
        int getSaving();
    }
}
