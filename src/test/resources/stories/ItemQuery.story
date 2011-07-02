Scenario: We can get an particular item by selecting with the item name of the record

Given a SimpleQuery client
Given a initialized test domain

When querying with the item name from a test domain
Then we can get the only one record from the test domain

When there is no record matching the specified item's name
Then the return value must be a null


Scenario: the 'describe' method of an Expression created by the 'whereItemName()' method of DomainExpression must return a correct SimpleDB expression-string.

Given a SimpleQuery client
Given a initialized test domain

When the expression is created with 'is' matcher
Then the result string must be -> select * from `item-test-domain` where itemName() = 'sample'

When the expression is created with 'in' matcher
Then the result string must be -> select * from `item-test-domain` where itemName() in ('red', 'blue')

