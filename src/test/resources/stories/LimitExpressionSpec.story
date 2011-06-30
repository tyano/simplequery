Given a SimpleQuery client

When calling limit(10) for OrderByExpression
Then it should generate 'limit 10'

When limit(10) for WhereExpression
Then it should generate 'limit 10' after where expression


When limit(10) for DomainExpression
Then it should generate 'limit 10' after domain expression

