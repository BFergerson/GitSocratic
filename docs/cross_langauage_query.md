This document demonstrates how it's possible to perform cross-language querying. Presented below are several source code snippets as well as the queries and results performed on this collection of various source code languages using GitSocratic.

## Example source code repository

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

## Example source code queries

### Sum of `k` variable declarations

#### Query
```graql
match
($kDeclaration) isa DECLARATION;
$kIdentifier has token "k";
($kIdentifier) isa IDENTIFIER;
$kNumber has numberValue $kNumberValue;
($kNumber) isa NUMBER;
(is_parent: $kDeclaration, is_child: $kIdentifier);
(is_parent: $kDeclaration, is_child: $kNumber);
aggregate sum $kNumberValue;
```

#### Result
```
30.0
```
