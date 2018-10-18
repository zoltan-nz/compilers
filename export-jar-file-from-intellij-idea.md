# Export JAR file from Intellij IDEA

## Setup Artifacts

1. File -> Project Structure...
2. Select "Artifacts" in Project Settings group.
3. If there is some option, you can remove it and click on "+" icon.
4. Add JAR -> From modules with dependencies...
5. "Create JAR from Modules" popup would show. Select your Main Class with clicking on folder icon.
6. Keep "extract to the target JAR" option selected.
7. Important! Change the Directory for `META-INF/MANIFEST.MF` to your project root folder. Basically, you should delete the `src/main/java` part of the suggested path.
8. Check the "Include tests" option also.
9. In "Output Layout", click on "+" and select "Directory Content", select your main project folder. (You probably just have to accept the default.) In this case, all the file what you have in your project will be added to the Jar file. Documents, README.md, etc...
10. Select "Include in project build"
11. Optional: You can change the output directory also.

## Build

Menu: Build -> Build Artifacts... Actions: Build

## Check it out

Run your code:

```
java -jar /path/your/jar/your.jar
```

List the content of your jar file:

```
jar tf your-jar-file.jar
```

"Unzip" your jar file. (Move in an empty folder first.)

```
jar xf your-jar-file.jar
```