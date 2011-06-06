Scenario: a condition with a matcher can create a expression string with a correct operator.

When a condition is initialized with a attribute name 'age' and a matcher 'is(18)'
Then the describe() method of the condition must return a string like "`age` = '18'"

When a condition is initialized with a attribute name 'age' and a matcher 'greaterEqual(18)', and the matcher has a padding of 3
Then the describe() method of the condition must return a string like "`age` >= '018'"

When a condition is initialized with a attribute name 'saving' and a matcher 'greaterThan(-1)', and the matcher has a padding of 10 and a offset of 1000000000,
Then the describe() method of the condition must return a string like "`saving` > '0999999999'"

When a condition with a expression like name = 'yano', and group() method has been called to the condition
Then the describe() method of the condition must return a string like "(`name` = 'yano')"

When a groupd condition and a normal condition is joined with operator 'and'
Then the result must be like (first condition) and normal-condition

When multiple grouped conditions are used and collected conditions have been grouped at last
Then the result must be grouped multiple times like ((first expression) or (second expression)).

When a intersection method is called
Then two conditions will be joined with a intersection operator.

When another condition has been joined after using intersection
Then three conditions will be joined as a series of conditions. Don't grouped automatically.

When two grouped condition has been joined with a intersection operator
Then the grouping will be keeped.
