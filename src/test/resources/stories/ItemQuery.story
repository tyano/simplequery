Scenario: We can get an particular item by selecting with the item name of the record

Given a SimpleQuery client
Given a initialized test domain

When querying with the item name from a test domain
Then we can get the only one record from the test domain

When there is no record matching the specified item's name
Then the return value must be a null
