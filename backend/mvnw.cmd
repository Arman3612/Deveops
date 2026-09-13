@echo off`rif exist "%~dp0mvn_home\apache-maven-3.9.6\bin\mvn.cmd" (`r    call "%~dp0mvn_home\apache-maven-3.9.6\bin\mvn.cmd" %*`r) else (`r    mvn %*`r)
