@echo off
java %JAVA_ARGS% -Xnoclassgc -Xshare:off -noverify -cp "%~dp0\jeval.jar";"%CLASSPATH%" id.jeval.Main %*