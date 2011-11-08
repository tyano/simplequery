/*
 * Copyright 2011 Tsutomu YANO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shelfmap.simplequery.domain;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.shelfmap.simplequery.*;
import com.shelfmap.simplequery.annotation.Attribute;
import com.shelfmap.simplequery.annotation.Container;
import com.shelfmap.simplequery.annotation.ItemName;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import static com.shelfmap.simplequery.SimpleDbUtil.attr;

import com.shelfmap.simplequery.annotation.SimpleDbDomain;
import com.shelfmap.simplequery.expression.MultipleResultsExistException;
import com.shelfmap.simplequery.expression.SimpleQueryException;
import com.shelfmap.simplequery.expression.matcher.MatcherFactory;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author Tsutomu YANO
 */
@StoryPath("stories/EnumProperty.story")
public class EnumPropertyTest extends BaseStoryRunner {
    public static final String ENUM_DOMAIN = "enum-domain";

    private Context context;

    @Given("a test context")
    public void createContext() throws IOException {
        this.context = new TokyoContext(new PropertiesCredentials(new File(TestClientFactory.CREDENTIAL_PATH))) {
            @Override
            public <T> DomainInstanceFactory<T> getDomainInstanceFactory(Domain<T> domain) {
                if(Favorites.class.isAssignableFrom(domain.getDomainClass())) {
                    return new DomainInstanceFactory<T>() {
                        @Override
                        public T create() {
                            return (T) new FavoritesImpl();
                        }
                    };
                }
                return super.getDomainInstanceFactory(domain);
            }

        };
    }

    @Given("test-specific domain")
    public void createDomain() {
        AmazonSimpleDB simpleDb = context.getSimpleDB();

        simpleDb.deleteDomain(new DeleteDomainRequest(ENUM_DOMAIN));
        simpleDb.createDomain(new CreateDomainRequest(ENUM_DOMAIN));

        simpleDb.putAttributes(new PutAttributesRequest(
                ENUM_DOMAIN,
                "favorites",
                Arrays.asList(attr("most-favorite", "RED", true),
                              attr("first-selection", "RED", true),
                              attr("first-selection", "BLUE", false),
                              attr("second-selection", "BLUE", true),
                              attr("second-selection", "YELLOW", false))));

    }

    Favorites favs;

    @When("a domain object which have a enum-type property is retrieved")
    public void fetchDomain() throws SimpleQueryException, MultipleResultsExistException {
        favs = context.select().from(Favorites.class).whereItemName(MatcherFactory.is("favorites")).getSingleResult(true);
        assertThat(favs, is(not(nullValue())));
    }

    @Then("the value of the property is converted a correct enum-value.")
    public void assertFetchResult() {
        assertThat(favs.getMostFavoriteColor(), is(FavoriteColor.RED));

        Collection<FavoriteColor> firstSelection = favs.getFirstSelection();
        assertThat(firstSelection.isEmpty(), is(false));
        Collection<FavoriteColor> expected = Arrays.asList(FavoriteColor.RED, FavoriteColor.BLUE);
        for (FavoriteColor color : firstSelection) {
            assertThat(color, isIn(expected));
        }

        FavoriteColor[] secondSelection = favs.getSecondSelection();
        assertThat(secondSelection.length, is(not(0)));
        FavoriteColor[] array = new FavoriteColor[]{FavoriteColor.BLUE, FavoriteColor.YELLOW};
        for (FavoriteColor color : secondSelection) {
            assertThat(color, isIn(array));
        }
    }

    @When("we set another enum-value to the property of the domain object and save it,")
    public void pushToDomain() {
        favs.setMostFavoriteColor(FavoriteColor.YELLOW);
        favs.setFirstSelection(Arrays.asList(FavoriteColor.YELLOW));
        favs.setSecondSelection(new FavoriteColor[]{FavoriteColor.RED});
        context.putObjects(favs);
        context.save();
    }

    @Then("the value of the simpledb domain must be equal to the 'name()' of the enum-value.")
    public void assertPushedValue() throws SimpleQueryException, MultipleResultsExistException {
        Favorites changed = context.select().from(Favorites.class).whereItemName(MatcherFactory.is("favorites")).getSingleResult(true);

        Collection<FavoriteColor> expectedCol = Arrays.asList(FavoriteColor.YELLOW);
        FavoriteColor[] expectedArray = new FavoriteColor[]{FavoriteColor.RED};

        assertThat(changed.getMostFavoriteColor(), is(FavoriteColor.YELLOW));
        Collection<FavoriteColor> firstSelection = changed.getFirstSelection();
        assertThat(firstSelection.size(), is(1));
        for (FavoriteColor color : firstSelection) {
            assertThat(color, isIn(expectedCol));
        }

        FavoriteColor[] secondSelection = changed.getSecondSelection();
        assertThat(secondSelection.length, is(1));
        for (FavoriteColor color : secondSelection) {
            assertThat(color, isIn(expectedArray));
        }
    }


    /**
     * we here override the 'toString()' method so that this enum is not restored through
     * the toString/valueOf pair which DefaultAttributeConverter uses.
     * EnumAttributeConverter can handle enum-values because it uses the name()/valueOf pair,
     * instead of the toString()/valueOf pair.
     */
    public enum FavoriteColor {
        RED {
            @Override
            public String toString() {
                return "red";
            }
        },
        BLUE {
            @Override
            public String toString() {
                return "blue";
            }
        },
        YELLOW {
            @Override
            public String toString() {
                return "yellow";
            }
        };
    }

    @SimpleDbDomain(ENUM_DOMAIN)
    public static interface Favorites {
        @ItemName
        String getItemName();
        void setItemName(String name);

        @Attribute(attributeName="most-favorite")
        FavoriteColor getMostFavoriteColor();
        void setMostFavoriteColor(FavoriteColor color);

        @Attribute(attributeName="first-selection")
        @Container(containerType=ArrayList.class, valueType=FavoriteColor.class)
        Collection<FavoriteColor> getFirstSelection();
        void setFirstSelection(Collection<? extends FavoriteColor> colors);


        @Attribute(attributeName="second-selection")
        FavoriteColor[] getSecondSelection();
        void setSecondSelection(FavoriteColor[] colors);
    }

    public static class FavoritesImpl implements Favorites {
        private FavoriteColor mostFavorite;
        private Collection<FavoriteColor> firstSelection;
        private FavoriteColor[] secondSelection;
        private String itemName;

        public FavoritesImpl() {
            this.firstSelection = new ArrayList<FavoriteColor>();
            this.secondSelection = new FavoriteColor[0];
        }

        @Override
        public String getItemName() {
            return itemName;
        }

        @Override
        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        @Override
        public Collection<FavoriteColor> getFirstSelection() {
            return new ArrayList<FavoriteColor>(firstSelection);
        }

        @Override
        public void setFirstSelection(Collection<? extends FavoriteColor> firstSelection) {
            this.firstSelection = new ArrayList<FavoriteColor>(firstSelection);
        }

        @Override
        public FavoriteColor getMostFavoriteColor() {
            return mostFavorite;
        }

        @Override
        public void setMostFavoriteColor(FavoriteColor mostFavorite) {
            this.mostFavorite = mostFavorite;
        }

        @Override
        public FavoriteColor[] getSecondSelection() {
            FavoriteColor[] color = new FavoriteColor[secondSelection.length];
            System.arraycopy(secondSelection, 0, color, 0, secondSelection.length);
            return color;
        }

        @Override
        public void setSecondSelection(FavoriteColor[] secondSelection) {
            FavoriteColor[] color = new FavoriteColor[secondSelection.length];
            System.arraycopy(secondSelection, 0, color, 0, secondSelection.length);
            this.secondSelection = color;
        }
    }
}
