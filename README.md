**jeval** - command line Java code evaluator. It is based on [JShell](https://docs.oracle.com/en/java/javase/17/jshell/introduction-jshell.html) except it provides you additional functionality and better integration with the command-line. **jeval** allows you to declare dependencies in Java scripts to any libraries from Maven repository so that it will automatically resolve them and add to the script class path. See [examples](http://portal2.atwebpages.com/jeval/)

# Usage

```bash
jeval [ <JAVA_SCRIPT_FILE> | -e <JAVA_CODE_SNIPPET> | -i | -jshell ] [ARGS]
```

Where: 

- JAVA_SCRIPT_FILE - Java script file to be executed
- JAVA_CODE_SNIPPET - Java expression
- ARGS - optional user arguments which will be available to you through the global variable "args: String[]" (same as you use in main() function). 

# Download

[Release versions](jeval/release/CHANGELOG.md)

# Documentation

[Documentation](http://portal2.atwebpages.com/jeval/)

[Development](DEVELOPMENT.md)

# Contacts

lambdaprime <intid@protonmail.com>
