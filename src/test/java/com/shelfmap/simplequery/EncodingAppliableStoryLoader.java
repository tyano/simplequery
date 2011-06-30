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
package com.shelfmap.simplequery;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.jbehave.core.io.InvalidStoryResource;
import org.jbehave.core.io.LoadFromClasspath;

/**
 *
 * @author Tsutomu YANO
 */
public class EncodingAppliableStoryLoader extends LoadFromClasspath {

    String encoding = System.getProperty("file.encoding");

    public EncodingAppliableStoryLoader(ClassLoader classLoader) {
        super(classLoader);
    }

    public EncodingAppliableStoryLoader(Class<?> loadFromClass) {
        super(loadFromClass);
    }

    public EncodingAppliableStoryLoader() {
    }

    public EncodingAppliableStoryLoader(ClassLoader classLoader, String encoding) {
        this(classLoader);
        this.encoding = encoding;
    }

    public EncodingAppliableStoryLoader(Class<?> loadFromClass, String encoding) {
        this(loadFromClass);
        this.encoding = encoding;
    }

    public EncodingAppliableStoryLoader(String encoding) {
        this();
        this.encoding = encoding;
    }

    @Override
    public String loadResourceAsText(String resourcePath) {
        InputStream stream = resourceAsStream(resourcePath);
        try {
            return IOUtils.toString(stream, encoding);
        } catch (IOException e) {
            throw new InvalidStoryResource(resourcePath, stream, e);
        }
    }
}
