**jeval** - command line Java code evaluator. It is similar to [JShell](https://docs.oracle.com/en/java/javase/17/jshell/introduction-jshell.html) except it provides you additional functionality and better integration with command-line. **jeval** allows you to declare dependencies in Java scripts to any libraries from Maven repository so that it will automatically resolve them and add to the script class path. See [examples](http://portal2.atwebpages.com/jeval/)

# Usage

```bash
jeval [ <JAVA_SCRIPT_FILE> | -e <JAVA_CODE_SNIPPET> | -i ] [ARGS]
```

Where: 

- JAVA_SCRIPT_FILE - Java script file to be executed
- JAVA_CODE_SNIPPET - Java expression
- ARGS - optional user arguments which will be available to you through the global variable "args: String[]" (same as you use in main() function). 

# Download

[Release versions](https://github.com/lambdaprime/jeval/releases)

[Latest snapshot versions](https://github.com/lambdaprime/jeval/tree/master/jeval/release) (may not always be published)

# Documentation

[Documentation](http://portal2.atwebpages.com/jeval/)

[Development](DEVELOPMENT.md)

# Contributors

lambdaprime <intid@protonmail.com>
