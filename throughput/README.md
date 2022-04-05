Throughput Reporting Sink Module
================================

Is a sink that counts received messages and periodically report the witnessed thoughput. This is to provide
a method for users to identify the performance of XD in different environments and deployment
types.

## Requirements

In order to install the module and run it in your Spring XD installation, you will need to have installed:

* Spring XD version 1.1.x ([Instructions](https://docs.spring.io/spring-xd/docs/current/reference/html/#getting-started))

## Building with Maven

	$ mvn package

The project's [pom](pom.xml) declares `spring-xd-module-parent` as its parent. This adds the dependencies needed to test the module and also configures the [Spring Boot Maven Plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) to package the module as an uber-jar, packaging any dependencies that are not already provided by the Spring XD container. See the [Modules](https://docs.spring.io/spring-xd/docs/current/reference/html/#modules) section in the Spring XD Reference for a more detailed explanation of module class loading.

## Building with Gradle

	$./gradlew build

The project's [build.gradle](build.gradle) applies the `spring-xd-module` plugin, providing analagous build and packaging support for gradle. This plugin also applies the [Spring Boot Gradle Plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-gradle-plugin.html) as well as the [propdeps plugin](https://github.com/spring-projects/gradle-plugins/tree/master/propdeps-plugin). 

## Using the Custom Module

The uber-jar will be in `[project-build-dir]/throughput-1.0.0.BUILD-SNAPSHOT.jar`. To install and register the module to your Spring XD distribution, use the `module upload` Spring XD shell command. Start Spring XD and the shell:


	_____                           __   _______
	/  ___|          (-)             \ \ / /  _  \
	\ `--. _ __  _ __ _ _ __   __ _   \ V /| | | |
 	`--. \ '_ \| '__| | '_ \ / _` |   / ^ \| | | |
	/\__/ / |_) | |  | | | | | (_| | / / \ \ |/ /
	\____/| .__/|_|  |_|_| |_|\__, | \/   \/___/
    	  | |                  __/ |
      	|_|                 |___/
	eXtreme Data
	1.1.0.BUILD-SNAPSHOT | Admin Server Target: http://localhost:9393
	Welcome to the Spring XD shell. For assistance hit TAB or type "help".
	xd:>module upload --file [path-to]/throughput-1.0.0.BUILD-SNAPSHOT.jar --name throughput --type sink
	Successfully uploaded module 'sink:throughput'
	xd:>


You can also get information about the available module options:

```
xd:>module info --name sink:throughput
Information about sink module 'throughput':

  Option Name        Description                                                                             Default              Type
  -----------------  --------------------------------------------------------------------------------------  -------------------  --------
  logger             the name of the logger to use (will use 'xd.sink.throughput.<logger>')                  ${xd.stream.name}    String
  reportEveryBytes   if positive, will report throughput this every bytes received                           9223372036854775807  long
  reportEveryMs      if positive, will report throughput this every milliseconds                             10000                long
  reportEveryNumber  if positive, will report throughput this every received messages                        9223372036854775807  long
  sizeUnit           the size unit to use in reports                                                         MB                   SizeUnit
  timeUnit           the time unit to use in reports                                                         s                    TimeUnit
  totalExpected      if positive, will report throughput once exactly that many messages have been received  0                    long
  inputType          how this module should interpret messages it consumes                                   <none>               MimeType
```

Now create and deploy a stream:

	xd:>stream create genTest --definition "load-generator --messageCount=10 --producers=5 --messageSize=1000 | throughput" --deploy


You should see the throughput reports in the log. You can configure the module to emit a report everytime the following condition is met:
* a certain number of messages have been received
* a message is received and a certain amount of time has passed (1)
* a certain amount of bytes have been received (2)

or any combination of the above (a report is emitted if _any_ condition is met, _i.e._ it's an OR).

(1) Note that a message needs to be received still. That is, if nothing happens, you'll not see the report.

(2) Your messages need to be `byte[]` or `String` for this to work

When a report is emitted, two lines of output are added to the log, like so:
```
Messages +    1000006 in        1,68s =   593827,79/s
Messages =   10070757 in       13,82s =   728550,75/s
```

The line with the `+` sign is the delta since the last report, while the line with the `=` sign displays metrics since the beginning of the collection (the timer starts when the very first message is received).

Additionnally, if the payload of your messages is of type `byte[]` or `String`, you'll see information about the size received, like so:

```
Messages +    1000006 in        1,68s =   593827,79/s    --    Bytes +    1000,01MB in        1,68s =      593,83MB/s
Messages =   10070757 in       13,82s =   728550,75/s    --    Bytes =   10070,76MB in       13,82s =      728,55MB/s
```


