# Commands

## add-local-repo

Add local source code repository to the knowledge graph

Usage:
```
gitsocratic add-local-repo [-hV] <repoLocation>
```

Param(s):

```
repoLocation
    - String
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
    - String
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
    - String
```

Example(s):
```
gitsocratic config docker_host 192.168.99.100
gitsocratic config use_docker_grakn false
```

## remove-repo

Remove repository from the knowledge graph

Usage:
```
$ gitsocratic remove-repo [repo_name]
```

Param(s):

```
repo_name
    - String
```

Example(s):
```
$ gitsocratic remove-repo bfergerson/myproject
```

## configure

Configure stuff

availble stuff
