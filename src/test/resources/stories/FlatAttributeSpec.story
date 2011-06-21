Given a SimpleQuery client
Given initialized domains

When select an item whose name is 'test-1'
Then the user should have an Address object whose postalCode is 1640011

When selecting an item whose address1 of the address property is 'test2-Address1'
Then it should return the item whose name is 'test-2'

When selecting an item which have a FlatAttribute
Then we should be able to access telNo and faxNo through the address.telInfo property.

When selecting an item with Address class as the domain class
Then we should be able to get a separated Address object


