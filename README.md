# GitSocratic
> Source code query command line interface

[![Build Status](https://travis-ci.com/CodeBrig/GitSocratic.svg?branch=master)](https://travis-ci.com/CodeBrig/GitSocratic)

![gitsocratic architecture](https://user-images.githubusercontent.com/3278877/49333927-57f61280-f585-11e8-9547-b3e10fe54c2d.jpg)

GitSocratic provides a pain-free interface for querying source code. GitSocratic uses the omnilingual parser [Babelfish](https://github.com/bblfsh/bblfshd) to extract universal abstract syntax trees (which are additionally annotated with their semantic meanings) and imports that data into a knowledge graph powered by [Grakn](https://github.com/graknlabs/grakn). This process is fully facilitated by GitSocratic's integration with [Phenomena](https://github.com/CodeBrig/Phenomena).

### Features
 - [Supports question/answer source code queries](https://github.com/CodeBrig/GitSocratic/blob/v0.2-alpha/docs/source_code_questions.md)
 - [Cross-language source code querying](https://github.com/CodeBrig/GitSocratic/blob/v0.2-alpha/docs/cross_langauage_query.md)
 - [Semantic role querying](https://github.com/CodeBrig/GitSocratic/blob/v0.2-alpha/docs/semantic_querying.md)
 - [UAST structure querying](https://github.com/CodeBrig/GitSocratic/blob/v0.2-alpha/docs/uast_querying.md)

## Setup

GitSocratic requires access to two services:
 - [Babelfish](https://github.com/bblfsh/bblfshd)
 - [Grakn](https://github.com/graknlabs/grakn)

GitSocratic is able to install these services automatically using [Docker](https://www.docker.com/).
If you do no wish to use Docker you may supply the host and ports for these services through the config file or command.

To automatically setup these services with Docker simply use the command:
```
gitsocratic init babelfish && gitsocratic init grakn
```

Note: This command may take several minutes on the first use.

### Bare-bones installation
```
apt-get update
apt install openjdk-8-jre-headless
apt install unzip
apt install docker.io
wget https://github.com/CodeBrig/GitSocratic/releases/download/v0.2-alpha/gitsocratic-0.2.zip
unzip gitsocratic-0.2.zip
cd gitsocratic-0.2/bin/
./gitsocratic init
```

## Usage
```
gitsocratic [-hV] [-c=<configFile>] [COMMAND]
```

[![asciicast](https://asciinema.org/a/4uCMnG7FcG89XE01RyFxg2Pnh.svg)](https://asciinema.org/a/4uCMnG7FcG89XE01RyFxg2Pnh)

### Commands

```
add-local-repo   Add local source code repository to the knowledge graph
add-remote-repo  Add remote source code repository to the knowledge graph
config           Configure GitSocratic
console          Open interactive source code query console
init             Initialize services necessary to use GitSocratic
logs             View logs for initialized services
query            Execute a single source code query
question         Execute a single source code question
```

More information: [COMMANDS](https://github.com/CodeBrig/GitSocratic/blob/master/COMMANDS.md)

## License
[Apache 2.0](https://github.com/CodeBrig/GitSocratic/LICENSE)
