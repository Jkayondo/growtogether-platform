@echo off
set BASE_DIR=%~dp0
set WRAPPER_JAR=%BASE_DIR%.mvn\wrapper\maven-wrapper.jar
if not exist "%WRAPPER_JAR%" powershell -Command "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.4/maven-wrapper-3.3.4.jar' -OutFile '%WRAPPER_JAR%'"
java -classpath "%WRAPPER_JAR%" -Dmaven.multiModuleProjectDirectory="%BASE_DIR%" org.apache.maven.wrapper.MavenWrapperMain %*
