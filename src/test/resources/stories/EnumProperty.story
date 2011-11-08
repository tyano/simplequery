Scenario: a property of enum type is able to retrieve from and save to Amazon SimpleDB

Given a test context
Given test-specific domain

When a domain object which have a enum-type property is retrieved
Then the value of the property is converted a correct enum-value.

When we set another enum-value to the property of the domain object and save it,
Then the value of the simpledb domain must be equal to the 'name()' of the enum-value.