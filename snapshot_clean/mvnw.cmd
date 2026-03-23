@REM Maven Wrapper startup script for Windows
@echo off
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot
set WRAPPER_JAR="%~dp0.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_URL="https://repo1.maven.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
"%JAVA_HOME%\bin\java.exe" %MAVEN_OPTS% -jar %WRAPPER_JAR% %*
