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

import com.shelfmap.simplequery.domain.impl.ImageContentConverter;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.shelfmap.simplequery.BaseStoryRunner;
import com.shelfmap.simplequery.ClientFactory;
import com.shelfmap.simplequery.IClientHolder;
import com.shelfmap.simplequery.StoryPath;
import com.shelfmap.simplequery.TestContext;
import com.shelfmap.simplequery.domain.impl.StringContentConverter;
import com.shelfmap.simplequery.util.IO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jbehave.core.annotations.AfterStory;
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
    private static final String BUCKET_NAME = "simplequery-tokyo-test-bucket";
    private static final String KEY_NAME = "test-key-name";

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

        InputStream is = getClass().getResourceAsStream("/images/testimage.jpg");
        try {
            testImage = ImageIO.read(is);
        } finally {
            IOUtils.closeQuietly(is);
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

    @When("retrieve the content of a BlobReference, which describes a given s3 resource")
    public void retrieveResource() {
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(ImageContentConverter.BUFFER_SIZE_KEY, 1024 * 1000);
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
    private String testKeyName;
    private BufferedImage sourceImage;

    @When("putting a object as the content of a BlobReference")
    public void putImageToS3() throws Exception {
        testKeyName = "testUpload" + RandomStringUtils.randomAlphanumeric(10);


        AmazonS3 s3 = ctx.getClient().getS3();

        ByteArrayOutputStream byteOutput = null;
        ByteArrayInputStream byteInput = null;
        InputStream source = null;
        try {
            //Created an image for assertion.
            //First, we must write testImage to byteArray with ImageIO.
            //Then we must create an image for assertion from the byteArray.
            //Because the data from getResourceStream() and the data create ImageIO.write() are not same binary data.
            byteOutput = new ByteArrayOutputStream();
            ImageIO.write(testImage, "jpeg", byteOutput);
            byteOutput.close();

            byte[] data = byteOutput.toByteArray();
            byteInput = new ByteArrayInputStream(data);

            //This is the image which will be used at assertion-time.
            sourceImage = ImageIO.read(byteInput);

            Map<String, Object> conversionInfo = createConversionInfo();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.addUserMetadata("format", "jpeg");

            BlobReference<BufferedImage> imageReference = new DefaultBlobReference<BufferedImage>(new S3Resource(BUCKET_NAME, testKeyName), BufferedImage.class, new ImageContentConverter(conversionInfo));

            //Here we use testImage (not sourceImage) for setContent().
            //setContent() will create binary stream with ImageIO.write().
            //so the data is same with the source binary data of sourceImage.
            imageReference.setContent(ctx.getClient(), testImage, metadata);
        } finally {
            IOUtils.closeQuietly(source);
            IOUtils.closeQuietly(byteInput);
            IOUtils.closeQuietly(byteOutput);
        }
    }

    @Then("the put object must immediately be uploaded to S3 storage")
    public void assertTheUploadResult() throws Exception {
        Map<String, Object> conversionInfo = createConversionInfo();
        BlobReference<BufferedImage> imageRestoreReference = new DefaultBlobReference<BufferedImage>(new S3Resource(BUCKET_NAME, testKeyName), BufferedImage.class, new ImageContentConverter(conversionInfo));

        BufferedImage image = imageRestoreReference.getContent(ctx.getClient());

        ByteArrayOutputStream source = new ByteArrayOutputStream();
        ByteArrayOutputStream target = new ByteArrayOutputStream();
        try {
            ImageIO.write(sourceImage, "jpeg", source);
            ImageIO.write(image, "jpeg", target);
        } finally {
            IOUtils.closeQuietly(source);
            IOUtils.closeQuietly(target);
        }

        byte[] sourceBytes = source.toByteArray();
        byte[] targetBytes = target.toByteArray();
        assertThat(sourceBytes, is(targetBytes));
    }

    @AfterStory
    public void deleteTestKey() {
        AmazonS3 s3 = ctx.getClient().getS3();
        s3.setEndpoint("s3-ap-northeast-1.amazonaws.com");

        if (s3.doesBucketExist(BUCKET_NAME)) {
            DeleteObjectRequest request = new DeleteObjectRequest(BUCKET_NAME, testKeyName);
            s3.deleteObject(request);
        }
    }

    private Map<String, Object> createConversionInfo() {
        Map<String, Object> conversionInfo = new HashMap<String, Object>();
        conversionInfo.put(ImageContentConverter.BUFFER_SIZE_KEY, 1024 * 1000);
        conversionInfo.put(ImageContentConverter.IMAGE_FORMAT_KEY, "jpeg");
        return conversionInfo;
    }

    @Given("a S3 bucket")
    public void initS3Bucket() {
        AmazonS3 s3 = ctx.getClient().getS3();
        s3.setEndpoint("s3-ap-northeast-1.amazonaws.com");

        if (!s3.doesBucketExist(BUCKET_NAME)) {
            CreateBucketRequest request = new CreateBucketRequest(BUCKET_NAME);
            request.setRegion("ap-northeast-1");
            s3.createBucket(request);
        }
    }

    private static final int BUF_SIZE = 1024;
    private static final String TEXT_KEY = "hojoki.txt";
    private static final String TEST_STRING;

    static {
        InputStream input = null;
        Reader inputReader = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        try {
            input = BlobReferenceTest.class.getResourceAsStream("/text/hojoki.txt");
            inputReader = new InputStreamReader(input, "UTF-8");
            br = new BufferedReader(inputReader);

            char[] buf = new char[BUF_SIZE];
            for(int i = br.read(buf); i >= 0; i = br.read(buf)) {
                if(i > 0) {
                    sb.append(buf, 0, i);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            IO.close(br, BlobReferenceTest.class);
            IO.close(inputReader, BlobReferenceTest.class);
            IO.close(input, BlobReferenceTest.class);
        }
        TEST_STRING = sb.toString();
    }

    @When("putting a string object through BlobReference")
    public void writeStringToS3() throws BlobOutputException {
        BlobReference<String> stringBlob = new DefaultBlobReference<String>(new S3Resource(BUCKET_NAME, TEXT_KEY), String.class, new StringContentConverter(createStringConversionInfo()));
        stringBlob.setContent(ctx.getClient(), TEST_STRING, new ObjectMetadata());
    }

    @Then("it must be regeneratable by the other BlobReference of same key and bucket.")
    public void readTheStringFromS3() throws BlobRestoreException {
        BlobReference<String> inputBlob = new DefaultBlobReference<String>(new S3Resource(BUCKET_NAME, TEXT_KEY), String.class, new StringContentConverter(createStringConversionInfo()));
        String content = inputBlob.getContent(ctx.getClient());

        assertThat(content, is(TEST_STRING));
    }

    private Map<String,Object> createStringConversionInfo() {
        Map<String,Object> info = new HashMap<String, Object>();
        info.put(StringContentConverter.BUFFER_SIZE_KEY, 1024*1000);
        return info;
    }
}
