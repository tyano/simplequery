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
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.Upload;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.shelfmap.simplequery.BaseStoryRunner;
import com.shelfmap.simplequery.Client;
import com.shelfmap.simplequery.ClientFactory;
import com.shelfmap.simplequery.IClientHolder;
import com.shelfmap.simplequery.StoryPath;
import com.shelfmap.simplequery.TestContext;
import com.shelfmap.simplequery.domain.impl.ImageContentConverter;
import com.shelfmap.simplequery.util.IO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

/**
 *
 * @author Tsutomu YANO
 */
@StoryPath("stories/BlobReferenceStreamSupport.story")
public class BlobReferenceStreamTest extends BaseStoryRunner {

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
    private static final String BUCKET_NAME = "simplequery-tokyo-test-bucket";
    private static final String KEY_NAME = "test-key-name";
    private static final String PUT_KEY_NAME = "put-key-name";

    @Given("a S3 resource")
    public void createTestS3Resource() throws IOException {
        AmazonS3 s3 = ctx.getClient().getS3();

        boolean found = false;
        ObjectListing listing = s3.listObjects(BUCKET_NAME);
        for (S3ObjectSummary s : listing.getObjectSummaries()) {
            if (s.getKey().equals(KEY_NAME)) {
                found = true;
                break;
            }
        }

        InputStream uploadSource = null;
        try {
            if (!found) {
                uploadSource = getClass().getResourceAsStream("/images/testimage.jpg");
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.addUserMetadata("format", "jpeg");
                PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, KEY_NAME, uploadSource, metadata);
                s3.putObject(request);
            }
        } finally {
            IOUtils.closeQuietly(uploadSource);
        }
    }

    BlobReference<BufferedImage> blob;

    @When("we have a BlobReference object which point to a key in S3 storage")
    public void createBlob() {
        blob = new DefaultBlobReference<BufferedImage>(ctx.getClient(), new S3Resource(BUCKET_NAME, KEY_NAME), BufferedImage.class, new ImageContentConverter());
    }

    @Then("we must be able to retrieve the data though an InputStream as a byte stream.")
    public void inputDataFromBlob() throws Exception {
        InputStream stream = null;
        InputStream source = null;
        try {
            stream = blob.getInputStream();
            source = getClass().getResourceAsStream("/images/testimage.jpg");
            byte[] data = IO.readBytes(stream);
            byte[] sourceData = IO.readBytes(source);

            assertThat(data, is(sourceData));
        } finally {
            IO.close(stream, this);
            IO.close(source, this);
        }
    }

    @When("we have a BlobReference object which have no data but point to a key in S3 storage")
    public void createBlobReference() {
        blob = new DefaultBlobReference<BufferedImage>(ctx.getClient(), new S3Resource(BUCKET_NAME, PUT_KEY_NAME), BufferedImage.class, new ImageContentConverter());
    }

    @Then("we must be able to put data into S3 storage through an OutputStream gotten by BlobReference#getUploadStream() method.")
    public void assertPuttingData() throws Exception {
        byte[] data = uploadTestData();

        Upload upload = blob.getLastUpload();
        upload.waitForCompletion();

        Client client = ctx.getClient();
        AmazonS3 s3 = client.getS3();

        GetObjectRequest request = new GetObjectRequest(BUCKET_NAME, PUT_KEY_NAME);
        S3Object object = s3.getObject(request);

        InputStream stream = null;
        byte[] uploadedData = null;
        try {
            stream = object.getObjectContent();
            uploadedData = IO.readBytes(stream);
        } finally {
            IO.close(stream, this);
        }

        assertThat(uploadedData, is(data));
    }

    private byte[] uploadTestData() throws BlobOutputException, IOException {

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("format", "jpeg");

        OutputStream output = null;
        InputStream source = null;
        try {
            source = getClass().getResourceAsStream("/images/testimage.jpg");
            byte[] data = IO.readBytes(source);

            output = new BufferedOutputStream(blob.getUploadStream(metadata));
            output.write(data);
            output.flush();
            return data;
        } finally {
            IO.close(output, this);
            IO.close(source, this);
        }
    }
}
