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
import java.io.OutputStream;
import javax.imageio.ImageIO;

/**
 *
 * @author Tsutomu YANO
 */
public class ImageContentConverter implements BlobContentConverter<BufferedImage> {
    @Override
    public BufferedImage restoreObject(ObjectMetadata metadata, InputStream stream) throws BlobRestoreException {
        try {
            return ImageIO.read(stream);
        } catch (IOException ex) {
            throw new BlobRestoreException("Could not read image data from stream.", ex);
        }
    }

    @Override
    public void writeObject(OutputStream stream, BufferedImage object) throws BlobOutputException {
        try {
            ImageIO.write(object, "jpeg", stream);
        } catch (IOException ex) {
            throw new BlobOutputException("Could not write image data to stream.", ex);
        }
    }
}
