# Freifunk Firmware auto deployer #

The software can be used to automatically install (Freifunk) firmware
replacing the manufacturer firmware. It can also be used to configure
the installed firmware on the device.


## Dependencies ##

The auto deployer only depends on Java 7 at the moment. Make sure to have
either OpenJDK 7 or Oracle's JDK 7 installed.


## Building ##

Windows and MacOS X support is untested. At least building the software
should work anyways. Simply run:

```
./gradlew clean build
```

If you run into problems make sure the `java` in your `PATH` is the one
of a JDK 7 installation by calling `java -version`. Also make sure your
`JAVA_HOME` variable points to the JDK 7 installation.


## Preperations ##

Make sure your (to be flashed) router is connected to your computer via
ethernet and your ethernet port is configured to having IPs 192.168.0.2/24
and 192.168.1.2/24. This is required as the router most probably will change
its IP during deployment from 192.168.0.1 to 192.168.1.1.

*Attention:* Switching ports during deployment (as needed e.g. for the
WR841N) is currently not supported. You can circumvent this by invoking
the deploy and configure actions seperately.

## Running ##

The build will create a `.jar` file containing all required libraries and
resources:

```
ui-commandline/build/libs/ui-commandline-*.jar
```

Run the program as follows:

```
java -jar ui-commandline/build/libs/ui-commandline-*.jar --help
```


## Debugging ##

### Increasing log output ###

You can get debugging output by setting the log level to `DEBUG` or even
`TRACE`:

```
java -Dlog.level=DEBUG -jar ui-commandline/build/libs/ui-commandline-*.jar
```

or

```
java -Dlog.level=TRACE -jar ui-commandline/build/libs/ui-commandline-*.jar
```


### Running deployment in Firefox ###

Also you can enable running in Firefox instead of headless. For that first
build the software as follows:

```
./gradlew clean build -Dwebdriver.firefox.allow=true
```

Then run the program:

```
java -Dwebdriver.firefox.enable=true -jar ui-commandline/build/libs/ui-commandline-*.jar
```

