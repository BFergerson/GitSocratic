## Queries

### Omnilingual (Semantic)
#### Get all functions named "main"
```graql 
match
($function) isa FUNCTION;
($function) isa DECLARATION;
(is_parent: $function, is_child: $functionName);
($functionName) isa FUNCTION;
($functionName) isa NAME;
($functionName) isa IDENTIFIER;
$functionName has token "main";
get $function;
```

### Java (AST)
#### Get all Java functions named "main"
```graql
match
$function isa JavaMethodDeclarationArtifact;
(has_java_name_relation: $function, is_java_name_relation: $functionName);
$functionName has token "main";
get $function;
```

### Go (AST)
#### Get all Go functions named "main"
```graql
match
$function isa GoFuncDeclArtifact;
(has_go_name_relation: $function, is_go_name_relation: $functionName);
$functionName has token "main";
get $function;
```

### Java + Go (AST)
#### Get all Java and Go functions named "main"
```graql
match
{$function isa JavaMethodDeclarationArtifact;} or { $function isa GoFuncDeclArtifact; };
(has_name_relation: $function, is_name_relation: $functionName);
$functionName has token "main";
get $function;
```
