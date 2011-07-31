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
import com.shelfmap.simplequery.Client;
import java.io.Serializable;

/**
 *
 * @author Tsutomu YANO
 */
public interface BlobReference<T> extends Serializable {
    T getContent(Client client) throws BlobRestoreException;
    void setContent(Client client, T object, ObjectMetadata metadata) throws BlobOutputException;
    S3ResourceInfo getResourceInfo();
    BlobContentConverter<T> getContentConverter();
}
