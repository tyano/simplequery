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
import org.scalatest.matchers.ShouldMatchers
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest
import com.amazonaws.services.simpledb.model.CreateDomainRequest
import com.amazonaws.services.simpledb.model.ReplaceableAttribute
import com.amazonaws.services.simpledb.model.ReplaceableItem
import com.shelfmap.simplequery.SimpleQueryClient
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import Conditions._
import MatcherFactory._

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
  "selecting items that have age of more than 19" should "return 2 items" in {
    val exp = client.select("*").from(classOf[ExpressionTestDomain]).where("age", greaterThan(19)).orderBy("name", SortOrder.Asc)
    val results = exp.getResults
    
    results.size() should be === 2
  }
  
  def initialize(): Unit = {
    val createDomainReq = new CreateDomainRequest(domainName)
    simpleDB.createDomain(createDomainReq)

    import java.util.{List => JList}
    import collection.JavaConversions._
    val putData: JList[ReplaceableItem] = List(new ReplaceableItem()
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
    val batchReq: BatchPutAttributesRequest = new BatchPutAttributesRequest(domainName, putData)  
    simpleDB.batchPutAttributes(batchReq)
  }
}



import _root_.com.shelfmap.simplequery.Domain
import _root_.com.shelfmap.simplequery.SimpleDBAttribute

@Domain("expression-test")
class ExpressionTestDomain {
  var id: String = ""
  var name: String = ""
  var age: Int = 0

  def getId() = id
  def getName() = name
  
  @SimpleDBAttribute(maxDigitLeft=3)
  def getAge() = age
}
