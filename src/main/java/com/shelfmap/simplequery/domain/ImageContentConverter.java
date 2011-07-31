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

import com.amazonaws.services.s3.model.ObjectMetadata;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tsutomu YANO
 */
public class ImageContentConverter implements BlobContentConverter<BufferedImage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageContentConverter.class);
    private static final int BUFFER_SIZE = 1024 * 1000; //1K * 1000 = 1M
    
    public static final String BUFFER_SIZE_KEY = "BUFFER_SIZE";
    public static final String IMAGE_FORMAT_KEY = "IMAGE_FORMAT";
    
    private Map<String,Object> metadata;
    
    public ImageContentConverter(Map<String,Object> metadata) {
        super();
        this.metadata = metadata;
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
        Object bufferSizeValue = metadata.get(BUFFER_SIZE_KEY);
        int bufferSize = (bufferSizeValue instanceof Integer) ? ((Integer)bufferSizeValue).intValue() : BUFFER_SIZE;

        Object formatValue = metadata.get(IMAGE_FORMAT_KEY);
        String format = (formatValue instanceof String) ? (String)formatValue : "jpeg";
        
        PipedInputStream stream = new PipedInputStream(bufferSize);
        Thread t = new Thread(new ImageWriter(stream, object, format));
        t.start();

        return stream;
    }
    
    public static class ImageWriter implements Runnable {
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
                stream.connect(output);
                ImageIO.write(this.image, this.format, output);
            } catch(IOException ex) {
                LOGGER.error("the OutputStream for blob-image suddenly is closed for an exception.", ex);
            } finally {
                if(output != null) {
                    try {
                        output.close();
                    } catch (IOException ex) {
                        LOGGER.error("Could not close an output stream.", ex);
                    }
                }
            }
        }
        
    }
}
