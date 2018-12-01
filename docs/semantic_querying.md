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
