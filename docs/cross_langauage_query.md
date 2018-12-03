This document demonstrates how it's possible to perform cross-language querying. Presented below are several source code snippets (in various languages) as well as the queries and results performed on this collection of source code using GitSocratic.

## Source code

### Go
```go
package main

import "fmt"

func main() {
	for k := 10; k >= 1; k-- {
		fmt.Println(k)
	}
}
```
### Java
```java
public class SameProgram {
  public static void main(String [] args) {
    for (int k = 10; k >= 1; k--) {
      System.out.println(k);
    }
  }
}
```

### JavaScript
```javascript
for (var k = 10; k >= 1; k--) {
    console.info(k);
}
```

## Queries

### Sum of `k` variable declarations

#### Query
```graql
```

#### Result
```
```
