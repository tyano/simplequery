Scenario: a simple attribute which have only the name must be an instance of QueryAttribute interface.

When an attribute of name '<name>' is a instance of DefaultAttribute
Then the describe() method of the instance must return '<quoted>'

Examples:
|name             |quoted             |
|name             |`name`             |
|need`escape      |`need``escape`     |
|usingwherekeyword|`usingwherekeyword`|


Scenario: we can use the 'every()' expression of SimpleDB through very similar way like every("attribute-name") in Java code.

When using Condisions.every("attribute-name")
Then it must return a Attribute object and the describe() method of the object must return a string 'every(`attribute-name`)'

