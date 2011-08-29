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
package com.shelfmap.simplequery.domain.impl;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.shelfmap.simplequery.domain.BlobContentConverter;
import com.shelfmap.simplequery.domain.BlobOutputException;
import com.shelfmap.simplequery.domain.BlobRestoreException;
import com.shelfmap.simplequery.util.IO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tsutomu YANO
 */
public class ImageContentConverter implements BlobContentConverter<BufferedImage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageContentConverter.class);
    private static final int BUFFER_SIZE = 1024 * 500; //1K * 500 = 500K

    public static final String BUFFER_SIZE_KEY = "BUFFER_SIZE";
    public static final String IMAGE_FORMAT_KEY = "IMAGE_FORMAT";

    private Map<String,Object> conversionInfo;

    public ImageContentConverter(Map<String,Object> conversionInfo) {
        super();
        this.conversionInfo = conversionInfo;
    }

    public ImageContentConverter() {
        this(null);
    }

    @Override
    public BufferedImage restoreObject(ObjectMetadata metadata, InputStream stream) throws BlobRestoreException {
        try {
            return ImageIO.read(stream);
        } catch (IOException ex) {
            throw new BlobRestoreException("Could not read image data from stream.", ex);
        }
    }

    @Override
    public InputStream objectToStream(final BufferedImage object) throws BlobOutputException {
        Object bufferSizeValue = conversionInfo.get(BUFFER_SIZE_KEY);
        int bufferSize = (bufferSizeValue instanceof Integer) ? ((Integer)bufferSizeValue).intValue() : BUFFER_SIZE;

        Object formatValue = conversionInfo.get(IMAGE_FORMAT_KEY);
        String format = (formatValue instanceof String) ? (String)formatValue : "jpeg";

        PipedInputStream stream = new PipedInputStream(bufferSize);
        Executors.newSingleThreadExecutor().execute(new ImageWriter(stream, object, format));

        return stream;
    }

    private static class ImageWriter implements Runnable {
        private PipedInputStream stream;
        private BufferedImage image;
        private String format;

        public ImageWriter(PipedInputStream stream, BufferedImage image, String format) {
            super();
            this.stream = stream;
            this.image = image;
            this.format = format;
        }

        @Override
        public void run() {
            PipedOutputStream output = new PipedOutputStream();
            try {
                output.connect(stream);
                ImageIO.write(this.image, this.format, output);
                output.flush();
            } catch(IOException ex) {
                LOGGER.error("the OutputStream for blob-image suddenly is closed for an exception.", ex);
            } finally {
                IO.close(output, this);
            }
            LOGGER.debug("Thread finished.");
        }

    }
}
