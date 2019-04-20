# Commands

 - [add-local-repo](#add-local-repo)
 - [add-remote-repo](#add-remote-repo)
 - [config](#config)
   - [Display configuration](#display-configuration)
   - [Get config option](#get-config-option)
   - [Set config option](#set-config-option)
 - [console](#console)
 - [init](#init)
 - [logs](#logs)
 - [query](#query)
   - [graql](#query-graql)
 - [question](#question)

## add-local-repo

Add local source code repository to the knowledge graph

Usage:
```
gitsocratic add-local-repo [-hV] <repoLocation>
```

Param(s):
```
repoLocation
    - string
```

Example(s):
```
gitsocratic add-local-repo /home/brandon/IdeaProjects/myproject
gitsocratic add-local-repo C:\Projects\MyProject
```

## add-remote-repo

Add remote source code repository to the knowledge graph

Usage:
```
gitsocratic add-remote-repo [-hV] <repoName>
```

Param(s):
```
repoName
    - string
```

Example(s):
```
gitsocratic add-remote-repo google/guava
gitsocratic add-remote-repo graknlabs/grakn
```

## config

Configure GitSocratic

### Display configuration

Usage:
```
gitsocratic config
```

Output:
```
Configuration:
 use_docker_grakn: true
 docker_host: localhost
 docker_port: 2376
 grakn_host: localhost
 grakn_port: 48555
 grakn_keyspace: grakn
 use_docker_babelfish: true
 babelfish_host: localhost
 babelfish_port: 9432
```

### Get config option

Usage:
```
gitsocratic config [-hV] [<option>]
```

Param(s):
```
option
    - enum (
        use_docker_grakn, docker_host, docker_port,
        grakn_host, grakn_port, grakn_keyspace,
        use_docker_babelfish, babelfish_host, babelfish_port
    )
```

Example(s):
```
gitsocratic config docker_host
gitsocratic config use_docker_grakn
```

### Set config option

Usage:
```
gitsocratic config [-hV] [<option>] [<value>]
```

Param(s):
```
option
    - enum (
        use_docker_grakn, docker_host, docker_port,
        grakn_host, grakn_port, grakn_keyspace,
        use_docker_babelfish, babelfish_host, babelfish_port
    )

value
    - string
```

Example(s):
```
gitsocratic config docker_host 192.168.99.100
gitsocratic config use_docker_grakn false
```

## console

Open interactive source code query console

Usage:
```
gitsocratic console [-hV] <console>
```

Param(s):
```
console
    - enum (graql)
```

Example(s):
```
gitsocratic console graql
```

## init

Initialize services necessary to use GitSocratic

Usage:
```
gitsocratic init [-bghvV] [-bv=<babelfishVersion>] [-gv=<graknVersion>]
```

Options:
```
-b, --babelfish         Initialize Babelfish (default: true)
-bv,--babelfish-version Babelfish version to initialize (default: v2.12.0-drivers)
-g, --grakn             Initialize Grakn (default: true)
-gv,--grakn-version     Grakn version to initialize (default: 1.5.0)
-v, --verbose           Verbose logging (default: false)
```

Example(s):
```
gitsocratic init
gitsocratic init -v
gitsocratic init -gv 1.5.0 -bv v2.12.0-drivers
```

## logs

View logs for initialized services

Usage:
```
gitsocratic logs [-htV] <service>
```

Param(s):
```
service
    - enum (babelfish, grakn)
```

Options:
```
-t, -f, --tail   Tail logs
```

Example(s):
```
gitsocratic logs grakn
gitsocratic logs babelfish -f
```

## query

Execute a single source code query

Usage:
```
gitsocratic query [-hV] [COMMAND]
```

Param(s):
```
COMMAND
    - enum (graql)
```

### query (Graql)

Usage:
```
gitsocratic query graql [-hV] <query>
```

Param(s):
```
query
    - string
```

Example(s):
```
gitsocratic query graql 'match $x; get;'
```

## question

Execute a single source code question

Usage:
```
gitsocratic question [-hV] <question>
```

Param(s):
```
question
    - enum (
            how many [language] methods are named [name]
            how many [language] methods total
            how many methods are named [name]
            how many methods are named like [name]
            how many methods total
    )
```

Example(s):
```
gitsocratic question "how many java methods are named main"
gitsocratic question "how many methods are named like closeBuffer"
```
