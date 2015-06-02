Header Enricher
===============

A processor module that provides a basic header enricher to allow a stream to add runtime state in one or more message headers. Message headers are preserved across the entire stream flow and may be referenced by down stream modules using SpEL, e.g., `expression=headers['foo']`

Header expressions are provided using the `headers` module option which expects a JSON string.

## Requirements

In order to install the module and run it in your Spring XD installation, you will need to have installed:

* Spring XD version 1.1.x or higher ([Instructions](http://docs.spring.io/spring-xd/docs/current/reference/html/#getting-started))

## Building with Maven

	$ mvn package

The project's [pom](pom.xml) declares `spring-xd-module-parent` as its parent. This adds the dependencies needed to test the module and also configures the [Spring Boot Maven Plugin](http://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) to package the module as an uber-jar, packaging any dependencies that are not already provided by the Spring XD container. See the [Modules](http://docs.spring.io/spring-xd/docs/current/reference/html/#modules) section in the Spring XD Reference for a more detailed explanation of module class loading.

## Installing the Custom Module

The uber-jar will be in `[project-build-dir]/header-enricher-1.0.0.BUILD-SNAPSHOT.jar`. To install and register the module to your Spring XD distribution, use the `module upload` Spring XD shell command. Start Spring XD and the shell:


	_____                           __   _______
	/  ___|          (-)             \ \ / /  _  \
	\ `--. _ __  _ __ _ _ __   __ _   \ V /| | | |
 	`--. \ '_ \| '__| | '_ \ / _` |   / ^ \| | | |
	/\__/ / |_) | |  | | | | | (_| | / / \ \ |/ /
	\____/| .__/|_|  |_|_| |_|\__, | \/   \/___/
    	  | |                  __/ |
      	|_|                 |___/
	eXtreme Data
	1.2.0.BUILD-SNAPSHOT | Admin Server Target: http://localhost:9393
	Welcome to the Spring XD shell. For assistance hit TAB or type "help".
	xd:>module upload --file [path-to]/header-enricher-1.0.0.BUILD-SNAPSHOT.jar --name header-enricher --type processor
	Successfully uploaded module 'processor:header-enricher'
	xd:>
	
## Examples

### A Simple SpEL expression

    xd:>stream create t1 --definition "http | header-enricher --headers={\"foo\":\"payload.toUpperCase()\"} | log --expression=headers" --deploy
   
    xd:>http post --data hello --target http://localhost:9000 
  
You should see the message headers, including **foo=HELLO** in the container console log:

  
    2015-05-24T11:40:40-0400 1.2.0.SNAP INFO pool-12-thread-4 sink.t1 - {requestMethod=POST, foo=HELLO, User-Agent=Java/1.8.0_25, Host=localhost:9000, id=f6b2c420-cd12-2d5e-e0cb-675f60fb9a63, Content-Length=5, contentType=text/plain;Charset=UTF-8, requestPath=/, timestamp=1432482040939}

### Multiple Headers

    xd:>stream destroy t1

NOTE: Destroy or undeploy each stream before creating the next example to free up the default port 9000 for the HTTP source.

This will add 2 headers, **foo** and **bar**

    xd:> stream create t2 --definition "http | header-enricher --headers={\"foo\":\"payload.toUpperCase()\",\"bar\":\"payload+',world'\"} | log --expression=headers" --deploy
    
    xd:>http post --data hello --target http://localhost:9000
    
The result:

    2015-05-24T11:49:19-0400 1.2.0.SNAP INFO pool-27-thread-4 sink.t2 - {bar=hello,world, requestMethod=POST, foo=HELLO, User-Agent=Java/1.8.0_25, Host=localhost:9000, id=c9220992-f71c-db3f-0f22-b5ab262d4aee, Content-Length=5, contentType=text/plain;Charset=UTF-8, requestPath=/, timestamp=1432482559740}
    
### Using JSON Path Expressions

	xd:>stream destroy t2
	
This example uses a JSON Path expression to extract a field value in a JSON Payload:

    xd:>stream create t3 --definition "http | header-enricher --headers={\"foo\":\"#jsonPath(payload,'$.duration')\"} | log --expression=headers" --deploy
    
    xd:>http post --data {"duration":123.45} --target http://localhost:9000		
		
**foo=123.45** !!

    2015-05-24T11:45:16-0400 1.2.0.SNAP INFO pool-23-thread-4 sink.t3 - {requestMethod=POST, foo=123.45, User-Agent=Java/1.8.0_25, Host=localhost:9000, id=87d229b9-a434-31c2-eb35-b42ca3f8352a, Content-Length=19, contentType=text/plain;Charset=UTF-8, requestPath=/, timestamp=1432482316080}

### Literal Strings with Embedded Spaces

SpEL expects literals to be enclosed in single quotes. This is straight forward when the literal does not contain embedded spaces as in the `Multple Headers` example above. Using embedded spaces requires either 
wrapping the `headers` value in single quotes and escaping the single quote for the literal string

	xd:>stream create t4 --definition "http | header-enricher --headers='{\"foo\":\"''this is a literal string''\"}' | log --expression=headers" --deploy

or you can encode embedded spaces using the Unicode escape sequence (the leading '\' must itself be escaped):

    xd:>stream create t4 --definition "http | header-enricher --headers={\"foo\":\"'this\\u0020is\\u0020a\\u0020literal\\u0020string'\"} | log --expression=headers" --deploy

...    

    2015-05-24T12:26:32-0400 1.2.0.SNAP INFO pool-31-thread-4 sink.t4 - {requestMethod=POST, foo=this is a literal string, User-Agent=Java/1.8.0_25, Host=localhost:9000, id=b8892a1b-57b1-de6e-71de-e30f84cd199a, Content-Length=5, contentType=text/plain;Charset=UTF-8, requestPath=/, timestamp=1432484792949}


  
