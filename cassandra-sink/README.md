Cassandra Sink Module
=====================

## Introduction
The Cassandra a popular `Column Family` NoSQL Database that complements the big data ingest use-cases that Spring XD was designed to address.

The Cassandra Sink Module sink will store data in the Cassandra cluster when used in an XD Stream definition.  It is based on the `CassandraMessageHandler` from
[Spring Integration Cassandra](https://github.com/spring-projects/spring-integration-extensions/tree/master/spring-integration-cassandra) and the Cassandra Connection configuration from [Spring Data Cassandra](http://projects.spring.io/spring-data-cassandra).  Please, refer to those projects regarding more information about supported Cassandra Sink options.

## Requirements

In order to install the module and run it in your Spring XD installation, you will need to have installed:

* Spring XD version 1.2.x. [Instructions](http://docs.spring.io/spring-xd/docs/current/reference/html/#getting-started)

## Building with Maven

```
$ mvn package
```

The project's pom.xml declares `spring-xd-module-parent` as its parent. This adds the dependencies needed to test the module and also configures the [Spring Boot Maven Plugin](http://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) to package the module as an uber-jar, packaging any dependencies that are not already provided by the Spring XD container.


## Building with Gradle

```
$./gradlew clean build
```

The project's build.gradle file applies the `spring-xd-module` plugin,
providing analagous build and packaging support for Gradle.
This plugin also applies the [Spring Boot Gradle Plugin](http://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-gradle-plugin.html) as well as the [propdeps plugin](https://github.com/spring-projects/gradle-plugins/tree/master/propdeps-plugin).


## Using this Custom Module

The _uber-jar_ will be in `[project-build-dir]/dist/deploy-to-xd/modules.sink.cassandra-1.0.0.BUILD-SNAPSHOT-uber.jar`.  To install and register the module to your Spring XD distribution, use the `module upload` Spring XD shell command.
Start Spring XD and the shell:


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
	xd:>module upload --file [project-build-dir]/dist/deploy-to-xd/modules.sink.cassandra-1.0.0.BUILD-SNAPSHOT-uber.jar	--name cassandra --type sink
	Successfully uploaded module 'sink:cassandra'
	xd:>


You can also get information about the available module options:

```
xd:>module info --name sink:cassandra
Information about sink module 'cassandra':

Option Name          Description                                                                         Default                       Type
-------------------  ----------------------------------------------------------------------------------  ----------------------------  ----------------
compressionType      the compression to use for the transport                                            NONE                          CompressionType
contactPoints        the comma-delimited string of the hosts to connect to Cassandra                     localhost                     String
entityBasePackages   the base packages to scan for entities annotated with Table annotations             [Ljava.lang.String;@60380fb6  String[]
initScript           the path to file with CQL scripts (delimited by ';') to initialize keyspace schema  <none>                        String
keyspace             the keyspace name to connect to                                                     ${xd.stream.name}             String
metricsEnabled       enable/disable metrics collection for the created cluster                           true                          boolean
password             the password for connection                                                         <none>                        String
port                 the port to use to connect to the Cassandra host                                    9042                          int
username             the username for connection                                                         <none>                        String
consistencyLevel     the consistencyLevel option of WriteOptions                                         <none>                        ConsistencyLevel
ingestQuery          the ingest Cassandra query                                                          <none>                        String
queryType            the queryType for Cassandra Sink                                                    INSERT                        Type
retryPolicy          the retryPolicy  option of WriteOptions                                             <none>                        RetryPolicy
statementExpression  the expression in Cassandra query DSL style                                         <none>                        String
ttl                  the time-to-live option of WriteOptions                                             0                             int
inputType            how this module should interpret messages it consumes                               <none>                        MimeType
```


Now create and deploy a stream with Cassandra schema population on start up:

```
xd:>stream create cassandraTest --definition "http | cassandra --initScript=int-db.cql --ingestQuery='insert into book (isbn, title, author) values (uuid(), ?, ?)'" --deploy
```

where `int-db.cql` looks like (for our case):

```sql
DROP TABLE IF EXISTS book;

CREATE TABLE book  (
	isbn    	uuid PRIMARY KEY,
	author  	text,
	instock 	boolean,
	pages   	int,
	saledate	timestamp,
	title   	text
);
```

This file is located in the `[project-build-dir]/dist/put-into-xd-config` must be placed to the `xd/config`.
Any other resource pattern (`file:`, `http:` etc.) is valid to specify the path to the file.

And now let's send a data:

```
xd:>http post --data "{\"title\": \"The Art of War\", \"author\": \"Sun Tzu\"}" --target http://localhost:9000
```

and check it in our local Cassandra with a simple CQL statement:
```sql
SELECT * FROM cassandratest.book;
```

Another more tricky `INSERT` sample based on the domain entity:

```
xd:>stream create cassandraTest2 --definition "http | transform --expression='new org.springframework.xd.test.domain.Book(T(java.util.UUID).randomUUID(), #jsonPath(payload, \"$.title\"), #jsonPath(payload, \"$.author\"))' | cassandra --keyspace=cassandraTest" --deploy

http post --data "{\"title\": \"The Art of War\", \"author\": \"Sun Tzu\"}" --target http://localhost:9000

```

To achieve this goal you must place everything from the `[project-build-dir]/dist/put-into-xd-lib/` directory into `xd/lib` and restart Spring XD.  This is required since some Spring Data commons classes are in the xd/lib directory and loaded into root classpath , not the module's classpath  Now deploy to the XD the module jar as `[project-build-dir]/dist/deploy-to-xd/modules.sink.cassandra-1.0.0.BUILD-SNAPSHOT-original.jar`
