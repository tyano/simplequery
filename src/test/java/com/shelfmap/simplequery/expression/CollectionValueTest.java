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

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author Tsutomu YANO
 */
public class CollectionValueTest extends AWSTestBase {

    private final AmazonSimpleDB simpleDB = getAmazonSimpleDB();
    private final String domainName = "collectionvalue";
    private final String itemName = "item1";

    
    @Test
    public void AmazonSimpleDBにおける多値の扱いについて調べるテスト() throws Exception {
        simpleDB.deleteDomain(new DeleteDomainRequest(domainName));
        simpleDB.createDomain(new CreateDomainRequest(domainName));
    
        ReplaceableAttribute attr1 = new ReplaceableAttribute("color", "blue", false);
        ReplaceableAttribute attr2 = new ReplaceableAttribute("color", "red", false);
        
        PutAttributesRequest req = new PutAttributesRequest()
                                        .withDomainName(domainName)
                                        .withItemName(itemName)
                                        .withAttributes(attr1, attr2);
        
        
        simpleDB.putAttributes(req);
        
        GetAttributesRequest getReq = new GetAttributesRequest(domainName, itemName).withConsistentRead(true);
        GetAttributesResult result = simpleDB.getAttributes(getReq);
        
        for (Attribute attr : result.getAttributes()) {
            System.out.println("name: " + attr.getName() + "\tvalue: " + attr.getValue());
        }
    }
    
    @Test
    public void コレクションのクオートの動作をテスト() throws Exception {
        List<String> values = Arrays.asList("test1", "test2", "test3");
        System.out.println("コレクションのクォート: " + SimpleDBUtils.quoteValues(values));
    }
    
    @Test
    public void クォートしたコレクションをValueに設定するとどうなるか() throws Exception {
        List<String> values = Arrays.asList("test1", "test2", "test3");

        simpleDB.deleteDomain(new DeleteDomainRequest(domainName));
        simpleDB.createDomain(new CreateDomainRequest(domainName));

        ReplaceableAttribute attr1 = new ReplaceableAttribute("name", SimpleDBUtils.quoteValues(values), true);

        PutAttributesRequest req = new PutAttributesRequest()
                                        .withDomainName(domainName)
                                        .withItemName(itemName)
                                        .withAttributes(attr1);
        
        
        simpleDB.putAttributes(req);
        
        GetAttributesRequest getReq = new GetAttributesRequest(domainName, itemName).withConsistentRead(true);
        GetAttributesResult result = simpleDB.getAttributes(getReq);
        
        for (Attribute attr : result.getAttributes()) {
            System.out.println("name: " + attr.getName() + "\tvalue: " + attr.getValue());
        }        
    }
}
