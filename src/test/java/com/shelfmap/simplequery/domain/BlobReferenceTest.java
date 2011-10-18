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

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import com.shelfmap.simplequery.*;
import com.shelfmap.simplequery.domain.impl.DefaultBlobReference;
import com.shelfmap.simplequery.domain.impl.ImageContentConverter;
import com.shelfmap.simplequery.domain.impl.StringContentConverter;
import com.shelfmap.simplequery.util.IO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import static org.hamcrest.Matchers.is;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Pending;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import static org.junit.Assert.assertThat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tsutomu YANO
 */
@StoryPath("stories/BlobReferenceSpec.story")
public class BlobReferenceTest extends BaseStoryRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlobReferenceTest.class);
    
    @Override
    protected void configureTestContext(Binder binder) {
        binder.bind(ContextHolder.class).to(TestContext.class).in(Scopes.SINGLETON);
        binder.bind(TestContext.class).in(Scopes.SINGLETON);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<? extends Class<?>> getStepsClasses() {
        return Arrays.asList(TestClientFactory.class);
    }
    @Inject
    TestContext ctx;
    BufferedImage testImage;
    private static final String BUCKET_NAME = "simplequery-tokyo-test-bucket";
    private static final String KEY_NAME = "test-key-name";

    @Given("a S3 resource")
    public void createTestS3Resource() throws IOException {
        AmazonS3 s3 = ctx.getContext().getClientFactory().create().getS3();

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
            IO.close(is, this);
        }

        InputStream uploadSource = null;
        try {
            if (!found) {
                uploadSource = getClass().getResourceAsStream("/images/testimage.jpg");
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.addUserMetadata("format", "jpeg");
                metadata.setContentType("image/jpeg");
                PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, KEY_NAME, uploadSource, metadata);
                s3.putObject(request);
            }
        } finally {
            IO.close(uploadSource, this);
        }

    }
    BlobReference<BufferedImage> blob;

    @When("retrieve the content of a BlobReference, which describes a given s3 resource")
    public void retrieveResource() {
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(ImageContentConverter.BUFFER_SIZE_KEY, 1024 * 1000);
        metadata.put(ImageContentConverter.IMAGE_FORMAT_KEY, "jpeg");
        blob = new DefaultBlobReference<BufferedImage>(ctx.getContext(), new S3Resource(BUCKET_NAME, KEY_NAME), BufferedImage.class, new ImageContentConverter(metadata));
    }

    @Then("we can get a deserialized java object.")
    public void assertRerource() throws BlobRestoreException, IOException {
        BufferedImage image = blob.getContent();

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

        ByteArrayOutputStream byteOutput = null;
        ByteArrayInputStream byteInput = null;
        InputStream source = null;
        try {
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
            metadata.setContentType("image/jpeg");

            BlobReference<BufferedImage> imageReference = new DefaultBlobReference<BufferedImage>(ctx.getContext(), new S3Resource(BUCKET_NAME, testKeyName), BufferedImage.class, new ImageContentConverter(conversionInfo));

            //Here we use testImage (not sourceImage) for setContent().
            //setContent() will create binary stream with ImageIO.write().
            //so the data is same with the source binary data of sourceImage.
            imageReference.setContent(testImage, metadata);
        } finally {
            IO.close(source, this);
            IO.close(byteInput, this);
            IO.close(byteOutput, this);
        }
    }

    @Then("the put object must immediately be uploaded to S3 storage")
    public void assertTheUploadResult() throws IOException, BlobRestoreException {
        try {
            Map<String, Object> conversionInfo = createConversionInfo();
            BlobReference<BufferedImage> imageRestoreReference = new DefaultBlobReference<BufferedImage>(ctx.getContext(), new S3Resource(BUCKET_NAME, testKeyName), BufferedImage.class, new ImageContentConverter(conversionInfo));

            BufferedImage image = null;
            try {
                image = imageRestoreReference.getContent();
            } catch (BlobRestoreException ex) {
                LOGGER.error("could not restore.", ex);
                throw ex;
            }

            ByteArrayOutputStream source = new ByteArrayOutputStream();
            ByteArrayOutputStream target = new ByteArrayOutputStream();
            try {
                ImageIO.write(sourceImage, "jpeg", source);
                ImageIO.write(image, "jpeg", target);
            } finally {
                IO.close(source, this);
                IO.close(target, this);
            }

            byte[] sourceBytes = source.toByteArray();
            byte[] targetBytes = target.toByteArray();
            assertThat(sourceBytes, is(targetBytes));
        } finally {
            deleteTestKey();
        }
    }

    private void deleteTestKey() {
        AmazonS3 s3 = ctx.getContext().getClientFactory().create().getS3();
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
        AmazonS3 s3 = ctx.getContext().getClientFactory().create().getS3();
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
    public void writeStringToS3() throws BlobOutputException, AmazonClientException, AmazonServiceException, InterruptedException {
        BlobReference<String> stringBlob = new DefaultBlobReference<String>(ctx.getContext(), new S3Resource(BUCKET_NAME, TEXT_KEY), String.class, new StringContentConverter(createStringConversionInfo()));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/plain");
        stringBlob.setContent(TEST_STRING, metadata);
    }

    @Then("it must be regeneratable by the other BlobReference of same key and bucket.")
    public void readTheStringFromS3() throws BlobRestoreException {
        BlobReference<String> inputBlob = new DefaultBlobReference<String>(ctx.getContext(), new S3Resource(BUCKET_NAME, TEXT_KEY), String.class, new StringContentConverter(createStringConversionInfo()));
        String content = inputBlob.getContent();

        assertThat(content, is(TEST_STRING));
    }

    private Map<String,Object> createStringConversionInfo() {
        Map<String,Object> info = new HashMap<String, Object>();
        info.put(StringContentConverter.BUFFER_SIZE_KEY, 1024*1000);
        return info;
    }
}
