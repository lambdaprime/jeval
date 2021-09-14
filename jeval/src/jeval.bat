@echo off
java %JAVA_ARGS% -Xnoclassgc -Xshare:off -noverify -cp "%~dp0\libs\*";"%CLASSPATH%" id.jeval.Main %*