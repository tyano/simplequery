Scenario: a simple attribute which have only the name must be an instance of QueryAttribute interface.

When an attribute of name '<name>' is an instance of DefaultAttribute
Then the describe() method of the instance must return '<quoted>'

When an instance of DefaultAttribute is created by Attributes.$() method
Then the describe() method of the instance must return '<quoted>'

When an instance of DefaultAttribute is created by Attributes.attr() method
Then the describe() method of the instance must return '<quoted>'

Examples:
|name             |quoted             |
|name             |`name`             |
|need`escape      |`need``escape`     |
|usingwherekeyword|`usingwherekeyword`|


Scenario: we can use the 'every()' expression of SimpleDB through very similar way like every("attribute-name") in Java code.

When an attribute of name '<name>' is an instance of EveryAttribute
Then the describe() method of the instance must return a string '<quoted>'

When an attribute of name '<name>' is created with Attributes.every() method
Then the describe() method of the instance must return a string '<quoted>'

Examples:
|name             |quoted                    |
|name             |every(`name`)             |
|need`escape      |every(`need``escape`)     |
|usingwherekeyword|every(`usingwherekeyword`)|
