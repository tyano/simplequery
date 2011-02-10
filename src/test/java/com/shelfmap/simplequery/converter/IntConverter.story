Scenario: IntConverter can generate an appropriate string following the rule of Amazon SimpleDB.

Given a DefaultIntConverter
When the parameter value is <intParam>
Then the converted value must be <convertAnswer>

Examples:
|intParam   |convertAnswer|
|1          |3000000001   |
|-1         |2999999999   |
|2147483647 |5147483647   |
|-2147483648|852516352    |



Scenario: IntConverter can restore a int value from a string.

Given a DefaultIntConverter
When <stringParam> is restored
Then the restored value must be <restoreAnswer>

Examples:
|stringParam|restoreAnswer|
|3000000001 |1            |
|2999999999 |-1           |
|5147483647 |2147483647   |
|852516352  |-2147483648  |


Scenario: IntConverter will throw an exception when restoring a number greater than Integer.MAX.

When a number greater than Integer.MAX (2147483648) is restored
Then ValueGreaterThanIntMaxException will be thrown

