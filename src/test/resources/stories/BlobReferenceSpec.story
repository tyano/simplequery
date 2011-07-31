Scenario: we can get an serializable java object from a BlobReference object.

Given a SimpleQuery client
Given a S3 resource

When retrieve the content of a BlobReference, which describes a given s3 resource
Then we can get a deserialized java object.

Scenario: we can write an object to S3 storage through BlobReference object.

Given a SimpleQuery client
Given a S3 resource

When putting a object as the content of a BlobReference
Then the put object must immediately be uploaded to S3 storage
