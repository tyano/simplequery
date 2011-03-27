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

package com.shelfmap.simplequery.expression

/**
 * Test class for 'getResults()' and 'getSingleResult()' of the BaseExpression class.
 */
import org.scalatest.FlatSpec
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest
import com.amazonaws.services.simpledb.model.CreateDomainRequest
import com.amazonaws.services.simpledb.model.DeleteDomainRequest
import com.amazonaws.services.simpledb.model.ReplaceableAttribute
import com.amazonaws.services.simpledb.model.ReplaceableItem
import com.shelfmap.simplequery.SimpleQueryClient
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import Conditions._
import MatcherFactory._
import collection.JavaConversions._
import org.scalatest.matchers.ShouldMatchers
import scala.reflect.BeanProperty

@RunWith(classOf[JUnitRunner])
class BaseExpressionSpec extends FlatSpec with ShouldMatchers with AWSSecurityCredential with ConfigurationAware {
  val simpleDB = getAmazonSimpleDB
  val configuration = getConfiguration
  val domainName = "expression-test"
  
  //create a domain into Amazon SimpleDB for testing.
  initialize()
  
  //create a client object
  val client = new SimpleQueryClient(simpleDB, configuration)
  
  //tests.
  val exp = client.select("*").from(classOf[ExpressionTestDomain]).where("age", greaterThan(19)).orderBy("age", SortOrder.Desc)
  
  "selecting items that have age of more than 19" should "return 2 items" in {
    exp.getResults.size should be === 2
  }
  //TODO size()を呼び出したときにExpressionが書き換えられて、それ以降正常にクエリを投げられない。
  
  "2 items of results" should "have age of 20 and 21, name of 'test-3' and 'test-4'" in {
    for (result <- exp.getResults) {
      result.name should (be === "test-3" or be === "test-4")
      result.age should (be === 20 or be === 21)
      
      if(result.name == "test-3") result.age should be === 20
      if(result.name == "test-4") result.age should be === 21
    }
  }
  
  def initialize(): Unit = {
    simpleDB.deleteDomain(new DeleteDomainRequest(domainName))
    simpleDB.createDomain(new CreateDomainRequest(domainName))

    val putData = List(new ReplaceableItem()
                         .withName("1")
                         .withAttributes(
                           new ReplaceableAttribute("name", "test-1", true),
                           new ReplaceableAttribute("age", "018", true)
                         ),
                       new ReplaceableItem()
                         .withName("2")
                         .withAttributes(
                           new ReplaceableAttribute("name", "test-2", true),
                           new ReplaceableAttribute("age", "019", true)
                         ),
                       new ReplaceableItem()
                         .withName("3")
                         .withAttributes(
                           new ReplaceableAttribute("name", "test-3", true),
                           new ReplaceableAttribute("age", "020", true)
                         ),
                       new ReplaceableItem()
                         .withName("4")
                         .withAttributes(
                           new ReplaceableAttribute("name", "test-4", true),
                           new ReplaceableAttribute("age", "021", true)
                         ))
    simpleDB.batchPutAttributes(new BatchPutAttributesRequest(domainName, putData))
  }
}



import _root_.com.shelfmap.simplequery.Domain
import _root_.com.shelfmap.simplequery.SimpleDBAttribute

@Domain("expression-test")
class ExpressionTestDomain {
  @BeanProperty var id: String = ""
  @BeanProperty var name: String = ""
  var age: Int = 0

  @SimpleDBAttribute(maxDigitLeft=3)
  def getAge() = age
  
  def setAge(age: Int):Unit = {
    this.age = age
  }
}
