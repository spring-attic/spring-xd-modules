XSLT Transformer Module
=============================

This is an example of a custom module project that is built and packaged for installation in a Spring XD runtime environment using maven or gradle. The project includes sample unit and integration tests, including the ability to test the module in an embedded single node container. 

## Requirements

In order to install the module and run it in your Spring XD installation, you will need to have installed:

* Spring XD version 1.1.x ([Instructions](https://docs.spring.io/spring-xd/docs/current/reference/html/#getting-started))

## Code Tour

The module uses the XsltPayloadTransformer to transform incoming, supposedly XML, message using XSLT. The XSLT file to be used must be provided during the Stream definition. The module uses the same code structure and tecniques describe in spring-xd-samples si-Dsl-module.


## Building with Maven

	$ mvn clean package

The project's [pom][] declares `spring-xd-module-parent` as its parent. This adds the dependencies needed to compile and test the module and also configures the [Spring Boot Maven Plugin][] to package the module as an uber-jar, packaging any dependencies that are not already provided by the Spring XD container.

## Building with Gradle

	$./gradlew clean test bootRepackage

The project's [build.gradle][] applies the `spring-xd-module` plugin, providing analagous build and packaging support for gradle. This plugin also applies the [Spring Boot Gradle Plugin][] as well as the [propdeps plugin][]. 

In this case, `spring-integration-java-dsl`, `spring-integration-xml` and `spring-xml` is a module dependency that must be packaged with the module to be loaded by the module's class loader. This component has transitive dependencies, including Spring Integration and Spring Framework libraries that are already in the Spring XD classpath. To avoid potential version conflicts and other class loader issues, the Spring Boot Maven Plugin is configured to exclude these from the from the uber-jar. See the [Modules][] section in the Spring XD Reference for instructions on how to override such exclusions.   


## Using the Custom Module

The uber-jar will be in `[project build dir]/xslt-transformer-1.0.0.BUILD-SNAPSHOT.jar`. To install and register the module to your Spring XD distribution, use the `module upload` Spring XD shell command. Start Spring XD and the shell:


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
	xd:>module upload --file [path-to]/xslt-transformer-1.0.0.BUILD-SNAPSHOT.jar --name xslt-transformer --type processor
	Successfully uploaded module 'processor:xslt-transformer'
	xd:>


You can also get information about the available module options:

	xd:>module info processor:xslt-transformer
	Information about processor module 'xslt-transformer':

  	Option Name  Description                                                     Default  Type
  	-----------  -----------------------------------------------------           -------  --------
  	xslt         XSLT File that will be processing the incoming message data.    <none>   String
  	outputType   how this module should emit messages it produces       <none>   MimeType
  	inputType    how this module should interpret messages it consumes  <none>   MimeType


Now copy the sample test.xsl XSLT file under `xslt-transformer/src/test/resources` and copy it under 
    $XD_HOME/modules/processors/scripts/<your-xslt-file>


Now create and deploy the stream:

	xd:>stream create test --definition "http |xslt-transformer --xslt='test.xsl' | log" --deploy

***Assuming your test file in the ```scripts``` folder is called test.xslt***

Post some data:

	xd:>http post --target http://localhost:9000 --data <Records><ID>1002</ID><NAME>OLV80UJS7YO</NAME></Records>
	> POST (text/plain;Charset=UTF-8) http://localhost:9000 hello
	> 200 OK


Since the sample XSLT file transforms the XML record passed in the message to a CSV format, you should see the stream output in the Spring XD log:


	14:11:19,513 1.1.0.SNAP  INFO DeploymentSupervisor-0 server.StreamDeploymentListener - Stream Stream{name='test'} deployment attempt complete
	14:11:22,582 1.1.0.SNAP  INFO pool-10-thread-4 sink.test - 1002,OLV80UJS7YO

[pom]: https://github.com/spring-projects/spring-xd-modules/blob/master/xslt-transformer/pom.xml
[build.gradle]: https://github.com/spring-projects/spring-xd-modules/blob/master/xslt-transformer/build.gradle
[Spring Integration Java DSL]: https://github.com/spring-projects/spring-integration-java-dsl
[Spring Boot Maven Plugin]: https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html
[Spring Boot Gradle Plugin]: https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/html/build-tool-plugins-gradle-plugin.html
[propdeps plugin]: https://github.com/spring-projects/gradle-plugins/tree/master/propdeps-plugin
[Modules]: https://docs.spring.io/spring-xd/docs/current/reference/html/#modules