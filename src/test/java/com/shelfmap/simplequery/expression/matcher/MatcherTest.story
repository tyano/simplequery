Scenario: matcher will return a quoted string for string.

When the value is "<targetValue>",
Then the converted value must be "<resultValue>".

Examples:
|targetValue|resultValue|
|test       |= 'test'   |
|it's       |= 'it''s'  |
|1          |= '1'      |
|-1         |= '-1'     |

Scenario: matcher will return a quoted string for simple integer value without padding and offset.

When the int value is <targetValue>,
Then the converted value must be "<resultValue>".

Examples:
|targetValue|resultValue|
|1          |= '1'      |
|-1         |= '-1'     |


Scenario: matcher with padding info will return a quoted string with zero-padding for a integer value.

When the padding size is <paddingSize> and the int value is <targetValue>,
Then the converted value must be "<resultValue>".

Examples:
|paddingSize|targetValue|resultValue    |
|10         |1          |= '0000000001' |
|10         |123456789  |= '0123456789' |
|5          |123456     |= '123456'     |


Scenario: matcher with padding and offset info will return a quoted string of offsetted integer value with padding.

When the padding size is <paddingSize>, the offset is <offset> and the int value is <targetValue>,
Then the converted value must be "<resultValue>".

Examples:
|paddingSize|offset    |targetValue|resultValue    |
|10         |300000000 |1          |= '0300000001' |
|10         |300000000 |-1         |= '0299999999' |


Scenario: IsNullMatcher always generate the expression 'is null'.

When a IsNullMatcher is described
Then the result string must be 'is null'

Scenario: IsNotNullMatcher always generate the expression 'is not null'.

When a IsNotNullMatcher is described
Then the result string must be 'is not null'

Scenario: A InMatcher with multiple parameters will generate a string like in ('value', 'value')

When a InMatcher with parameters 'a' and 'b' is described
Then the result string must be like 'in ('a', 'b')'

When a InMatcher's parameters are 100 and -100, and padding = 4, offset = 200
Then the result string must be like 'in ('0300', '0100')'
