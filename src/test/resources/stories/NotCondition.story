Scenario: We can change the meaning of an condition to a reversed condition by not() expression

When using .not() medhod against an condition object
Then the result of describe() must be like 'not (a expression)'

When using not() static method of the Conditions class
Then the result of describe() must be like 'not (a expression)'

When using not() method against a GroupCondition object
Then the result of describe() must be like 'not ((a expression and a expression))
