Scenario: we can save a domain-instance immediately with a Context object.

Given a test-specific context
Given 2 test domains
Given A instance of the test domain which have a reference to another domain

When we change some properties of the instance of the test domain
Then we can apply the changes against the one instance immediately with one method.
Then the instance of another domain do not saved.


Scenario: we can save all changed instances including referenced instances with only one method-call.

Given a test-specific context
Given 2 test domains
Given some instances of the test domain which have a reference to another domain

When we change some properties of instances of test domains and the referenced instances
Then we can save all instances, which put into Context, with one method call