# vexrun
Opinionated CLI Test Runner

### Caveat
This CLI test runner was built for one application and one application only. It was built to run end-to-end tests for [Titan](https://titan-data.io/). If you decide to use it, it comes AS-IS with no promise the docs are up to date or it will work for your use case. *You have been warned*. 

### Install
*   Download the latest jar from the [releases](https://github.com/mcred/vexrun/releases). 
*   Put the jar in a convient location and run with: `java -jar vexrun.jar`

### Usage
vexrun will traverse the current directory or a specified directory for all yml or yaml files that contain tests. Each test will pass or fail depending on the provided acceptance criteria. Any failures will exit the process with code 1. If all tests pass, the exit code will be 0. 

```bash
$ java -jar vexrun.jar -d ./path/to/tests`
```

### Sample
The following sample tests are used to test the [Getting Started](https://titan-data.io/getting-started) examples for titan and represent the different methods for commands and validation. 

```yml
tests:
  - "can install titan":
      command: titan install
      exitValue: 0
      wait: 30
      stdout:
        contains: Titan cli successfully installed, happy data versioning :)
  - "can clone hello-world/posrgres":
      command: titan clone s3web://demo.titan-data.io/hello-world/postgres hello-world
      exitValue: 0
      stdout:
        contains:
          - Running controlled container hello-world
          - Starting container hello-world
  - "hello-world/postgres already exists":
      command: titan clone s3web://demo.titan-data.io/hello-world/postgres hello-world
      exitValue: 1
      stdout: repository 'hello-world' already exists
  - "can list hello-world/postgres":
      command: titan ls
      exitValue: 0
      stdout: |-
        REPOSITORY            STATUS
        hello-world           running
  - "can get contents of hello-world/postgres":
      command: [docker, exec, hello-world, psql, postgres://postgres:postgres@localhost/postgres, -t, -c, SELECT * FROM messages;]
      exitValue: 0
      stdout: Hello, World!
  - "can remove hello-world/postgres":
      command: titan rm -f hello-world
      exitValue: 0
      stdout: |-
        Removing container hello-world
        Deleting volume hello-world/v0
        hello-world removed
  - "can run mongo-test":
      command: titan run -- --name mongo-test -p 27017:27017 -d mongo:latest
      exitValue: 0
      wait: 30
      stdout: |-
        Creating repository mongo-test
        Creating docker volume mongo-test/v0 with path /data/configdb
        Creating docker volume mongo-test/v1 with path /data/db
        Running controlled container mongo-test
  - "can insert mongo-test Ada Lovelace":
      command: [docker, exec, mongo-test, mongo, --quiet, --eval, 'db.employees.insert({firstName:"Ada",lastName:"Lovelace"})']
      exitValue: 0
      stdout: |-
        WriteResult({ "nInserted" : 1 })
  - "can commit mongo-test":
      command: [titan, commit, -m, First Employee, mongo-test]
      exitValue: 0
      stdout:
        contains: Commit
      env:
        set:
          - COMMIT_GUID:
              replace:
                find: "Commit "
                replace: ""
  - "can insert mongo-test Grace Hopper":
      command: [docker, exec, mongo-test, mongo, --quiet, --eval, 'db.employees.insert({firstName:"Grace",lastName:"Hopper"})']
      exitValue: 0
      stdout: |-
        WriteResult({ "nInserted" : 1 })
  - "can select employees from mongo-test":
      command: [docker, exec, mongo-test, mongo, --quiet, --eval, 'db.employees.find()']
      exitValue: 0
      stout:
        contains:
          - '"firstName" : "Ada", "lastName" : "Lovelace"'
          - '"firstName" : "Grace", "lastName" : "Hopper"'
  - "can checkout commit mongo-test":
      command: [titan, checkout, --commit, $COMMIT_GUID, mongo-test]
      exitValue: 0
      wait: 10
      stdout: |-
        Stopping container mongo-test
        Checkout $COMMIT_GUID
        Starting container mongo-test
        $COMMIT_GUID checked out
      env:
        get: COMMIT_GUID
  - "mongo-test checkout was successful":
      command: [docker, exec, mongo-test, mongo, --quiet, --eval, 'db.employees.find()']
      exitValue: 0
      stdout:
        excludes:
          - '"firstName" : "Grace", "lastName" : "Hopper"'
  - "can remove mongo-test":
      command: titan rm -f mongo-test
      exitValue: 0
      stdout: |-
        Removing container mongo-test
        Deleting volume mongo-test/v0
        Deleting volume mongo-test/v1
        mongo-test removed
  - "can uninstall titan":
      command: titan uninstall
      exitValue: 0
      stdout: Uninstalled titan infrastructure
```