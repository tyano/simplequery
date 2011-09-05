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

import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import static com.shelfmap.simplequery.util.Dates.date;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import com.shelfmap.simplequery.BaseStoryRunner;
import com.shelfmap.simplequery.StoryPath;
import com.shelfmap.simplequery.domain.impl.DateAttributeConverter;
import java.util.Date;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tsutomu YANO
 */
@StoryPath("stories/DateAttributeConverter.story")
public class DateAttributeConverterTest extends BaseStoryRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DateAttributeConverterTest.class);

    DateAttributeConverter converter;

    @Given("a DateAttributeConverter")
    public void createConveter() {
        LOGGER.debug("simpledb date format: " + SimpleDBUtils.encodeDate(new Date()));
        this.converter = new DateAttributeConverter();
    }

    @Then("the converter convert a String <value> to <expected>")
    public void assertConversionToDate(@Named("value") String value, @Named("expected") String expected) throws Exception {
        Date exp = date(expected, "yyyyMMdd");
        Date converted = converter.restoreValue(value);

        assertThat(converted, is(exp));
    }
}