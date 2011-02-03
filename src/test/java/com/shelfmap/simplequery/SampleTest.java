/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.shelfmap.simplequery;

import java.util.List;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.ListDomainsResult;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author t_yano
 */
public class SampleTest {
    private String getSecurityCredentialPath() {
        return "/Users/t_yano/aws.credential.properties";
    }
    
    private AmazonSimpleDB sdb;
    
    @Before
    public void createClient() throws IOException {
        AWSCredentials credential = new PropertiesCredentials(new File(getSecurityCredentialPath()));
        sdb = new AmazonSimpleDBClient(credential);
    }
    
    @After
    public void destroy() {
        sdb = null;
    }
    
    @Test
    public void checkDomain() {
        ListDomainsResult result = sdb.listDomains();
        List<String> names = result.getDomainNames();
        assertThat(names.size(), is(1));
        assertThat(names.get(0), is("sample"));
    }
    
}
