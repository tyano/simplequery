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
import Conditions._
import MatcherFactory._
import collection.JavaConversions._
import org.scalatest.matchers.ShouldMatchers
import scala.reflect.BeanProperty
import java.util.ArrayList
import java.util.{List => JList}

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
  
  "2 items of results" should "have age of 20 and 21, name of 'test-3' and 'test-4'" in {
    val results = exp.getResults
    for (result <- results) {
      result.name should (be === "test-3" or be === "test-4")
      result.age should (be === 20 or be === 21)
      
      if(result.name == "test-3") result.age should be === 20
      if(result.name == "test-4") result.age should be === 21
    }
  }
  
  def initialize(): Unit = {
    simpleDB.deleteDomain(new DeleteDomainRequest(domainName))
    simpleDB.createDomain(new CreateDomainRequest(domainName))

    implicit def itemListToReplaceableItemList(l: List[Itm]): JList[ReplaceableItem] = {
      for(i <- l) yield {
        new ReplaceableItem().withName(i.itemName).withAttributes(
                        for(a <- i.attrs) yield { new ReplaceableAttribute(a.name, a.value, a.replace) }
                       )
      }
    }
    
    val putData = List(Itm("1", Attr("name", "test-1", true), Attr("age", "018", true)),
                       Itm("2", Attr("name", "test-2", true), Attr("age", "019", true)),
                       Itm("3", Attr("name", "test-3", true), Attr("age", "020", true)),
                       Itm("4", Attr("name", "test-4", true), Attr("age", "021", true)))
    
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

case class Attr(val name: String, val value: String, val replace: Boolean)
case class Itm(val itemName: String, val attrs: Attr*)
