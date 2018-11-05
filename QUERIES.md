# Omnilingual Queries

#### Find all for loops
```graql
match
($forLoop) isa IS_FOR;
($forLoop) isa IS_STATEMENT;
get $forLoop; #todo: fix this query

match
($forLoop) isa IS_FOR;
($forLoop) isa IS_STATEMENT;
($varDecl) isa IS_DECLARATION;
($numLiteral) isa IS_NUMBER;
($numLiteral) isa IS_LITERAL;
$numLiteral has token $token;
aggregate sum $token;

match
($forLoop) isa IS_FOR;
($forLoop) isa IS_STATEMENT;
{
  (is_parent: $forLoop, is_child: $varDecl);
  (is_parent: $varDecl, is_child: $numLiteral);
} or
{
  (is_parent: $forLoop, is_child: $thing);
  (is_parent: $thing, is_child: $varDecl);
  (is_parent: $varDecl, is_child: $numLiteral);
  ($thing) isa IS_DECLARATION;
};
($varDecl) isa IS_DECLARATION;
($numLiteral) isa IS_NUMBER;
($numLiteral) isa IS_LITERAL;
$numLiteral has token $token;
aggregate count $token;
```

# Java Queries

#### Find all classes
```graql
match
$c isa JavaTypeDeclarationArtifact;
get $c;
```

#### Find all methods named "bar"
```graql
match
$method isa JavaMethodDeclarationArtifact;
$name isa JavaSimpleNameArtifact
  has token "bar";
($method, $name);
get $method;
```
