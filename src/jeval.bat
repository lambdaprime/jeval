java  -Xmn64m -Xms128m -Xmx512m -Xss1024k -XX:+UseParallelGC -Xnoclassgc -Xshare:off -noverify -cp "%~dp0\jeval.jar";%CLASSPATH% id.jeval.Main %*
