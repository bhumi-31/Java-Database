@echo off
REM Simple build script for the database project

echo Building Custom In-Memory Database Engine...

REM Create output directories
if not exist "target\classes" mkdir "target\classes"
if not exist "target\test-classes" mkdir "target\test-classes"

echo Compiling main classes...
javac -d target\classes -sourcepath src\main\java src\main\java\com\database\types\*.java
javac -d target\classes -sourcepath src\main\java -cp target\classes src\main\java\com\database\core\*.java
javac -d target\classes -sourcepath src\main\java -cp target\classes src\main\java\com\database\storage\*.java
javac -d target\classes -sourcepath src\main\java -cp target\classes src\main\java\com\database\persistence\*.java
javac -d target\classes -sourcepath src\main\java -cp target\classes src\main\java\com\database\index\*.java
javac -d target\classes -sourcepath src\main\java -cp target\classes src\main\java\com\database\query\*.java
javac -d target\classes -sourcepath src\main\java -cp target\classes src\main\java\com\database\query\ast\*.java
javac -d target\classes -sourcepath src\main\java -cp target\classes src\main\java\com\database\demo\*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    exit /b 1
)

echo Compilation successful!

echo Compiling test classes...
if exist "src\test\java\com\database\index\*.java" (
    javac -d target\test-classes -sourcepath src\test\java -cp target\classes src\test\java\com\database\index\*.java
    if %ERRORLEVEL% NEQ 0 (
        echo Test compilation failed!
        exit /b 1
    )
)

echo Running demo application...
java -cp target\classes com.database.demo.DatabaseDemo

echo.
echo Running indexing demo...
java -cp target\classes com.database.demo.IndexingDemo

echo.
echo Running query processing demo...
java -cp target\classes com.database.demo.QueryProcessingDemo

echo.
echo Running index tests...
if exist "target\test-classes\com\database\index\IndexTest.class" (
    java -cp "target\classes;target\test-classes" com.database.index.IndexTest
) else (
    echo Index tests not compiled - skipping
)

echo Build completed!
pause
