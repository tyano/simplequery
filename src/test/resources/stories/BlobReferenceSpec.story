Scenario: we can get an serializable java object from a BlobReference object.

Given a S3 resource

When retrieve the content of a BlobReference, which describes a given s3 resource
Then we can get a deserialized java object.
