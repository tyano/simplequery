package com.shelfmap.simplequery.util;

public interface IntConverter {

    String convert(int value);
    int restore(String value) throws ValueGreaterThanIntMaxException, ValueIsNotNumberException;
}
