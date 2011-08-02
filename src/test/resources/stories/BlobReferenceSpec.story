Scenario: we can get a BufferedImage object from a BlobReference object through S3.

Given a SimpleQuery client
Given a S3 bucket
Given a S3 resource

When retrieve the content of a BlobReference, which describes a given s3 resource
Then we can get a deserialized java object.

Scenario: we can write a BufferedImage object to S3 storage through BlobReference object.

Given a SimpleQuery client
Given a S3 bucket
Given a S3 resource

When putting a object as the content of a BlobReference
Then the put object must immediately be uploaded to S3 storage

Scenario: we can read and write large text object through BlobReference

Given a SimpleQuery client
Given a S3 bucket

When putting a string object through BlobReference
Then it must be regeneratable by the other BlobReference of same key and bucket.