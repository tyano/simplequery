Scenario: DateAttributeConveter must be able to convert a Date to String and String to Date.

Given a DateAttributeConverter

Then the converter convert a String <value> to <expected>

Examples:
|value                        |expected|
|2011-09-03T00:00:00.000+09:00|20110903|
|2011-01-01T00:00:00.000+09:00|20110101|
|2000-02-29T00:00:00.000+09:00|20000229|
