Scenario: We can change the meaning of an expression to a reversed expression by not() expression

When using .not() medhod against an expression object
Then the result must be like 'not (a expression)'

When using not() static method of the Conditions class
Then the result must be like 'not (a expression)'
