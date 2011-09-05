Scenario: A domain-object can have relationships to other domains

Given a SimpleQuery client
Given domains where one-side have a relationship to another one.
Given an instance of a domain-object which have a relationship.

Then we can get another domain-object from the relationship.