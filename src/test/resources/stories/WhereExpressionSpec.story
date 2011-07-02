Scenario: Select().from(TestDomain.class).where("name", is("yano"))

Given a AmazonSimpleDB client

When TestDomain don't have any @Attribute annotation on it's properties,
Then WhereExpression will generate a simple expression with no padding nor offset.

When TestDomain have a @Attribute on a property whose name is same with the attribute specified in a expression
Then WhereExpression will use a padding and a offset on the annotation

When a property have a @Attribute annotation, but the annotation don't have attributeName,
Then this library will find the annotation through the name of property instead of the attributeName, and uses values of the annotation.
