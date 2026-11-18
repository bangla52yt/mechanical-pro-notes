@rem Gradle wrapper launcher for Windows
@echo off
setlocal
set DIRNAME=%~dp0
set WRAPPER_JAR=%DIRNAME%gradle\wrapper\gradle-wrapper.jar
if not exist "%WRAPPER_JAR%" (
    where gradle >nul 2>nul
    if %ERRORLEVEL%==0 (
        gradle %*
        exit /b %ERRORLEVEL%
    )
    echo ERROR: gradle-wrapper.jar not found. Run "gradle wrapper" or open android\ in Android Studio. 1>&2
    exit /b 1
)
java -Xmx2048m -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
