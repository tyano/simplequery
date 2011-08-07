Scenario: We can read bytes in a BlobReference through InputStream.

Given a SimpleQuery client
Given a S3 bucket
Given a S3 resource

When we have a BlobReference object which point to a key in S3 storage
Then we must be able to retrieve the data though an InputStream as a byte stream.

Scenario: We can put an object into S3 storage through OutputStream which is gotten from a BlobReference

Given a SimpleQuery client
Given a S3 bucket

When we have a BlobReference object which have no data but point to a key in S3 storage
Then we must be able to put data into S3 storage through an OutputStream gotten by BlobReference#getOuputStream() method.