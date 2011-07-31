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
package com.shelfmap.simplequery.domain;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.shelfmap.simplequery.BaseStoryRunner;
import com.shelfmap.simplequery.Client;
import com.shelfmap.simplequery.ClientFactory;
import com.shelfmap.simplequery.IClientHolder;
import com.shelfmap.simplequery.StoryPath;
import com.shelfmap.simplequery.TestContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

/**
 *
 * @author Tsutomu YANO
 */
@StoryPath("stories/BlobReferenceSpec.story")
public class BlobReferenceTest extends BaseStoryRunner {
    @Override
    protected void configureTestContext(Binder binder) {
        binder.bind(IClientHolder.class).to(TestContext.class).in(Scopes.SINGLETON);
        binder.bind(TestContext.class).in(Scopes.SINGLETON);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<? extends Class<?>> getStepsClasses() {
        return Arrays.asList(ClientFactory.class);
    }

    
    @Inject
    TestContext ctx;
    
    BufferedImage testImage;
    Client client;
    
    private static final String BUCKET_NAME = "simplequery-tokyo-test-bucket";
    private static final String KEY_NAME = "test-key-name";
    
    @Given("a S3 resource")
    public void createTestS3Resource() throws IOException {
        AmazonS3 s3 = ctx.getClient().getS3();
        s3.setEndpoint("s3-ap-northeast-1.amazonaws.com");
        
        if(!s3.doesBucketExist(BUCKET_NAME)) {
            CreateBucketRequest request = new CreateBucketRequest(BUCKET_NAME);
            request.setRegion("ap-northeast-1");
            s3.createBucket(request);
        }
        
        boolean found = false;
        ObjectListing listing = s3.listObjects(BUCKET_NAME);
        for(S3ObjectSummary s : listing.getObjectSummaries()) {
            if(s.getKey().equals(KEY_NAME)) {
                found = true;
                break;
            }
        }
        
        InputStream is = getClass().getResourceAsStream("/images/testimage.jpg");
        try {
            testImage = ImageIO.read(is);
        } finally {
            IOUtils.closeQuietly(is);
        }
        
        InputStream uploadSource = null;
        try {
            if(!found) {
                uploadSource = getClass().getResourceAsStream("/images/testimage.jpg");
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.addUserMetadata("type", "jpeg");
                PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, KEY_NAME, uploadSource, metadata);
                s3.putObject(request);
            }
        } finally {
            IOUtils.closeQuietly(uploadSource);
        }

    }

    BlobReference<BufferedImage> blob;
    
    @When("retrieve the content of a BlobReference, which describes a given s3 resource")
    public void retrieveResource() {
        Map<String,Object> metadata = new HashMap<String, Object>();
        metadata.put(ImageContentConverter.BUFFER_SIZE_KEY, 1024*1000);
        metadata.put(ImageContentConverter.IMAGE_FORMAT_KEY, "jpeg");
        blob = new DefaultBlobReference<BufferedImage>(new S3Resource(BUCKET_NAME, KEY_NAME), BufferedImage.class, new ImageContentConverter(metadata));
    }

    @Then("we can get a deserialized java object.")
    public void assertRerource() throws BlobRestoreException, IOException {
        BufferedImage image = blob.getContent(ctx.getClient());
        
        ByteArrayOutputStream source = new ByteArrayOutputStream();
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        
        try {
            ImageIO.write(testImage, "jpeg", source);
            ImageIO.write(image, "jpeg", target);
        } finally {
            IOUtils.closeQuietly(source);
            IOUtils.closeQuietly(target);
        }
        
        assertThat(source.toByteArray(), is(target.toByteArray()));
    }
}
