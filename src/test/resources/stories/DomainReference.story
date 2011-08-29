Scenario: ToOne reference

Given two domains where one have a reference to another domain

When selecting a domain object which have a reference to another domain,
Then we can get the another object of the another domain from the property of the first domain object

Scenario: ToMany reference

Given two domains where one is a master and the other is the slave domain

When selecting a domain object from the master domain
Then we can get a QueryResult object which hold the child objects of the master domain-object.




