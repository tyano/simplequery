Scenario: A domain-object can have relationships to other domains

Given a test-specific context
Given domains which refer each other
Given an instance of a domain-object which have a relationship.

Then we can get a domain-object of parent from the reference of a child object.
Then we can get all children from parent's reverse-reference.

Scenario: ReverseToOneReference handle only 1 object if the relationship have more than 2 objects.

Given a test-specific context
Given domains which refer each other
Given an instance of master-object which have multiple children but handle them with ReverseToOneDomainReference
Then the master object can get only 1 child from the reference.


Scenario: if changed the content of an ReverseDomainReference, the target objects of the reference automatically pushed into the current context.

Given a test-specific context
Given domains which refer each other
Given an instance which have a ReverseToManyDomainReference and a ReverseToOneDomainReference

When the content of the two references,
Then objects previously referenced by ReverseToOneDomainReference and all new targets exist in the current context.
