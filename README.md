# Meta CSP Tutorial code base
This is the code base for the the Meta-CSP tutorial.

## Installation
After cloning this repository in a directry of your choce, enter the directory with the file build.gradle and issue the command:
```
$ ./gradlew install    #(on Unix-based systems)
$ gradlew.bat install  #(on Windows-based systems)
```

To test the build, issue the following:
```
$ ./gradlew run        #(on Unix-based systems)
$ gradlew.bat run      #(on Windows-based systems)
```

The ```clean``` target will clean up the build directory. The target ```javadoc``` can be used to generate the API documentation (Javadoc), which will be placed in ```build/docs/javadoc```.

## Preparing an Eclipse project

If developing in Eclipse, consider using the eclipse target:
```
$ ./gradlew eclipse    #(on Unix-based systems)
$ gradlew.bat eclipse  #(on Windows-based systems)
```

This will prepare the directory with ```.classpath```, ```.settings``` and ```.project``` files. The directory can then be used as source for a new Eclipse project which will have all dependencies properly set.
