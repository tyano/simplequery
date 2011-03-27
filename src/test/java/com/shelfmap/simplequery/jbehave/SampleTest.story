Scenario: A SimpleDB Client

Given A SimpleDB Client

When I retrieve all of the domains from sample db,
Then the count of the domains should be 2.

When I put a new item named as 'new' into our sample db
Then I can get the 'new' item from the db

When I put a new item named as 'new2' into our sample db
Then I can get the 'new2' item from the db

