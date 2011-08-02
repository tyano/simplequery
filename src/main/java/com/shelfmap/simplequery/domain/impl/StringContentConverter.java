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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tsutomu YANO
 */
public class StringContentConverter implements BlobContentConverter<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringContentConverter.class);
    private static final int BUFFER_SIZE = 1024 * 1000; //1K * 1000 = 1M

    public static final String BUFFER_SIZE_KEY = "BUFFER_SIZE";
    private Map<String,Object> conversionInfo;

    public StringContentConverter(Map<String, Object> conversionInfo) {
        super();
        this.conversionInfo = conversionInfo;
    }

    @Override
    public String restoreObject(ObjectMetadata metadata, InputStream stream) throws BlobRestoreException {
        DataInputStream dataInput = new DataInputStream(stream);
        String result = "";
        try {
            result = dataInput.readUTF();
        } catch (IOException ex) {

        } finally {
            IO.close(dataInput, this);
        }
        return result;
    }

    @Override
    public InputStream objectToStream(String object) throws BlobOutputException {
        Object bufferSizeValue = conversionInfo.get(BUFFER_SIZE_KEY);
        int bufferSize = (bufferSizeValue instanceof Integer) ? ((Integer)bufferSizeValue).intValue() : BUFFER_SIZE;
        PipedInputStream stream = new PipedInputStream(bufferSize);

        Thread t = new Thread(new StringWriter(stream, object));
        t.start();

        return stream;
    }

    private static class StringWriter implements Runnable {
        private static final Logger LOGGER = LoggerFactory.getLogger(StringWriter.class);
        private PipedInputStream input;
        private String data;

        public StringWriter(PipedInputStream input, String data) {
            this.input = input;
            this.data = data;
        }

        @Override
        public void run() {
            PipedOutputStream output = null;
            DataOutputStream dataOutput = null;
            try {
                output = new PipedOutputStream(input);
                dataOutput = new DataOutputStream(output);
                dataOutput.writeUTF(data);
                dataOutput.flush();
            } catch (IOException ex) {
                LOGGER.error("OutputStream for blob-text suddenly is closed for an exception.", ex);
            } finally {
                IO.close(dataOutput, this);
                IO.close(output, this);
            }
            LOGGER.debug("Thread finished.");
        }

    }
}
