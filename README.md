# GitSocratic
> Source code query command line interface

[![asciicast](https://asciinema.org/a/4uCMnG7FcG89XE01RyFxg2Pnh.svg)](https://asciinema.org/a/4uCMnG7FcG89XE01RyFxg2Pnh)

GitSocratic provides a pain-free interface for querying source code. GitSocratic uses the omnilingual parser [Babelfish](https://github.com/bblfsh/bblfshd) to extract universal abstract syntax trees which are annotated with their semantic meanings. This data is then imported into a knowledge graph powered by [Grakn](https://github.com/graknlabs/grakn). This enables source code to be queried using Graql (more query languages coming soon), allowing for a wide range of modern source code analyses.

## Setup

GitSocratic requires access to two services:
 - Grakn
 - Babelfish
 
 GitSocratic is able to install these services automatically using [Docker](https://www.docker.com/).
 If you do no wish to use Docker you may suppy the host and ports for these services through the config file or command.
 
 To automatically setup these services in Docker simply use the command:
 ```
 gitsocratic init
 ```
 
 Notes: This command may take several minutes on the first use.
 
 ### Bare-bones installation
 ```
 apt-get update
apt install openjdk-8-jre-headless
apt install unzip
apt install docker.io
wget https://github.com/CodeBrig/GitSocratic/releases/download/v0.1-alpha/gitsocratic-0.1.zip
unzip gitsocratic-0.1.zip
cd gitsocratic-0.1/bin/
./gitsocratic init
```

## Usage
```
gitsocratic [-hV] [-c=<configFile>] [COMMAND]
```

## Commands

```
add-local-repo   Add local source code repository to the knowledge graph
add-remote-repo  Add remote source code repository to the knowledge graph
config           Configure GitSocratic
console          Open interactive source code query console
init             Initialize services necessary to use GitSocratic
logs             View logs for initialized services
query            Execute single source code query
question         Execute single source code question
```

More information: [COMMANDS](https://github.com/CodeBrig/GitSocratic/blob/master/COMMANDS.md)

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

## License
[Apache 2.0](https://github.com/CodeBrig/GitSocratic/LICENSE)
