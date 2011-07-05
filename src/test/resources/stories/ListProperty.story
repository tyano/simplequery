Scenario: SimpleQuery can handle the multi-value column of Amazon SimpleDB with both of List-property and simple Object-property.

Given a SimpleQuery client
Given a initialized domain which have a multi-value column

When selecting an item from the domain which have a multi-value column
Then we can get the values through a property whose type is a kind of Collection

When selecting an item from the same domain, but the properties type is not a kind of Collection
Then we should get a random value from values of the multi-value column

When selecting an item from the domain which have a multi-value column and receive the result with an Array property
Then we can get the values through a property whose type is an Array

Scenario: If the value of multi-value column is null, the result value must be empty list or null.

Given a SimpleQuery client
Given a initialized domain which have a multi-value column without values

When the value of a multi-value column is null and the type of the property associated with the column is a kind of Collection
Then the return value must be an empty collection

When the value of a multi-value column is null and the type of the property associated with the column is not a Collection
Then the return value must be a null

When the value of a multi-value column is null and the type of the property associated with the column is an array
Then the return value must be an array which size is zero

