Load Generator Source Module
=============================

Is a source that sends generated data and dispatches it to the stream.  This is to provide
a method for users to identify the performance of XD in different environments and deployment
types.

## Requirements

In order to install the module and run it in your Spring XD installation, you will need to have installed:

* Spring XD version 1.1.x ([Instructions](https://docs.spring.io/spring-xd/docs/current/reference/html/#getting-started))

## Building with Maven

	$ mvn package

The project's [pom](pom.xml) declares `spring-xd-module-parent` as its parent. This adds the dependencies needed to test the module and also configures the [Spring Boot Maven Plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) to package the module as an uber-jar, packaging any dependencies that are not already provided by the Spring XD container. See the [Modules](https://docs.spring.io/spring-xd/docs/current/reference/html/#modules) section in the Spring XD Reference for a more detailed explanation of module class loading.

## Building with Gradle

	$./gradlew clean test bootRepackage

The project's [build.gradle](build.gradle) applies the `spring-xd-module` plugin, providing analagous build and packaging support for gradle. This plugin also applies the [Spring Boot Gradle Plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-gradle-plugin.html) as well as the [propdeps plugin](https://github.com/spring-projects/gradle-plugins/tree/master/propdeps-plugin). 

## Using the Custom Module

The uber-jar will be in `[project-build-dir]/load-generator-source-feed-1.0.0.BUILD-SNAPSHOT.jar`. To install and register the module to your Spring XD distribution, use the `module upload` Spring XD shell command. Start Spring XD and the shell:


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
	xd:>module upload --file [path-to]/load-generator-source-1.0.0.BUILD-SNAPSHOT.jar --name load-generator --type source
	Successfully uploaded module 'source:load-generator'
	xd:>


You can also get information about the available module options:

xd:>module info source:load-generator
Information about source module 'load-generator':

	Option Name   Description                                       Default  Type
	------------  ------------------------------------------------  -------  --------
	messageCount  the number of messages to send                    100      int
	messageSize   the size of message to send                       1000     int
	producers     the number of producers                           1        int
	outputType    how this module should emit messages it produces  <none>   MimeType


Now create and deploy a stream:

	xd:>stream create genTest --definition "load-generator --messageCount=10 --producers=5 --messageSize=1000 |log" --deploy


You should see the stream output in the Spring XD log.

Now to output data directly to a Rabbit queue so you can test with perftest as a consumer

	xd:>stream create rabbitTest --definition "load-generator --messageCount=1000 --producers=5 --messageSize=1000 > queue:foo" --deploy

You can view rabbit performance results by running the perfTest client (provided by rabbitmq)

	./runjava.sh com.rabbitmq.examples.PerfTest -p -u xdbus.queue:foo  -x 0 -y 2 -z 18
	
The example above captures statistics from the xdbus.queue:foo using 2 consumers for 18 seconds

