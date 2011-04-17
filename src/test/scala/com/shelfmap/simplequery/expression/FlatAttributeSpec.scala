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


import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import scala.reflect.BeanProperty
import com.shelfmap.simplequery.{Domain, IntAttribute, FlatAttribute}
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest
import com.amazonaws.services.simpledb.model.CreateDomainRequest
import com.amazonaws.services.simpledb.model.DeleteDomainRequest
import com.amazonaws.services.simpledb.model.ReplaceableAttribute
import com.amazonaws.services.simpledb.model.ReplaceableItem
import com.shelfmap.simplequery.SimpleQueryClient
import Conditions._
import matcher.MatcherFactory._
import collection.JavaConversions._
import java.util.{List => JList}

class FlatAttributeSpec extends FlatSpec with ShouldMatchers with AWSSecurityCredential with ConfigurationAware {
  val simpleDB = getAmazonSimpleDB
  val configuration = getConfiguration
  val domainName = "flatattribute-test"

  //create a domain into Amazon SimpleDB for testing.
  initialize()
  
  //create a client object
  val client = new SimpleQueryClient(simpleDB, configuration)
  
  
  val exp = client.select("*").from(classOf[User]).where("name", is("test-1"))
  
  "select an item whose name is 'test-1', then the user" should "have an Address object whose postalCode is 1640011" in {
    val exp = client.select("*").from(classOf[User]).where("name", is("test-1"))
    val user = exp.getSingleResult
    user should not be null
    user.name should be === "test-1"
    user.getAddress.getPostalCode should be === 1640011
    user.getAddress.getAddress1 should be === "test1-住所1"
    user.getAddress.getAddress2 should be === "test1-住所2"
  }
  
  "selecting an item whose address1 of the address property is 'test2-住所1" should "return the item whose name is 'test-2" in {
    val exp = client.select().from(classOf[User]).where("address1", is("test2-住所1"))
    val user = exp.getSingleResult
    user should not be null
    user.name should be === "test-2"
  }
  
  "selecting an item, then we " should "be able to access telNo and faxNo through the address.telInfo property." in {
    val exp = client.select().from(classOf[User]).where("name", is("test-3"))
    val user = exp.getSingleResult
    user should not be null
    user.getAddress.getTelInfo should not be null
    user.getAddress.getTelInfo.getTelNo should be === "0300010003"
    user.getAddress.getTelInfo.getFaxNo should be === "0300020003"
  }
  
  "we" should "be able to get a separated Address object by selecting an item with Address class." in {
    val exp = client.select().from(classOf[Address]).where("address1", is("test4-住所1"))
    val address = exp.getSingleResult
    address should not be null
    address.getAddress2 should be === "test4-住所2"
    address.getTelInfo should not be null
    address.getTelInfo.getTelNo should be === "0300010004"
    address.getTelInfo.getFaxNo should be === "0300020004"
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
    
    val putData = List(Itm("1", Attr("name", "test-1", true), Attr("age", "018", true), Attr("postalCode", "1640011", true), Attr("address1", "test1-住所1", true), Attr("address2", "test1-住所2", true), Attr("telNo", "0300010001", true), Attr("faxNo", "0300020001", true)),
                       Itm("2", Attr("name", "test-2", true), Attr("age", "019", true), Attr("postalCode", "1640012", true), Attr("address1", "test2-住所1", true), Attr("address2", "test2-住所2", true), Attr("telNo", "0300010002", true), Attr("faxNo", "0300020002", true)),
                       Itm("3", Attr("name", "test-3", true), Attr("age", "020", true), Attr("postalCode", "1640013", true), Attr("address1", "test3-住所1", true), Attr("address2", "test3-住所2", true), Attr("telNo", "0300010003", true), Attr("faxNo", "0300020003", true)),
                       Itm("4", Attr("name", "test-4", true), Attr("age", "021", true), Attr("postalCode", "1640014", true), Attr("address1", "test4-住所1", true), Attr("address2", "test4-住所2", true), Attr("telNo", "0300010004", true), Attr("faxNo", "0300020004", true)))
    
    simpleDB.batchPutAttributes(new BatchPutAttributesRequest(domainName, putData))
  }  
}

@Domain("flatattribute-test")
class User {
  private var _age: Int = 0;
  private var _address: Address = new Address()
  
  @BeanProperty var name: String = "";
  
  def setAge(age: Int): Unit = {
    _age = age
  }
  
  @IntAttribute(padding=3)
  def getAge(): Int = _age
  
  def setAddress(address: Address) {
    _address = address
  }
  
  @FlatAttribute
  def getAddress(): Address = _address
}


@Domain("flatattribute-test")
class Address {
  private var _postalCode: Int = 0
  private var _telInfo: TelInfo = new TelInfo()
  
  def setPostalCode(code: Int) {
    _postalCode = code
  }
  
  @IntAttribute(padding=7)
  def getPostalCode(): Int = _postalCode
  
  @BeanProperty var address1: String = ""
  @BeanProperty var address2: String = ""
  
  def setTelInfo(info: TelInfo) {
    _telInfo = info
  }
  
  @FlatAttribute
  def getTelInfo(): TelInfo = _telInfo
}

class TelInfo {
  @BeanProperty var telNo: String = ""
  @BeanProperty var faxNo: String = ""
}
