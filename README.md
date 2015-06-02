Spring XD Modules
=================
This repository contains modules - reusable components for building streams or jobs with [Spring-XD](http://projects.spring.io/spring-xd/). The Spring XD distribution includes a number of ready to use [source](http://docs.spring.io/spring-xd/docs/current/reference/html/#sources),   [processor](http://docs.spring.io/spring-xd/docs/current/reference/html/#processors), [sink](http://docs.spring.io/spring-xd/docs/current/reference/html/#sinks) and [job](http://docs.spring.io/spring-xd/docs/1.2.0.BUILD-SNAPSHOT/reference/html/#_pre_packaged_batch_jobs) modules out of the box. This repository is intended to supplement the out of the box modules with contributions from the user community and the Spring XD team. 

Contributions to this repository are subject to the same code review and QA process applied to the main Spring XD repository. However, they are not included in the Spring XD release management or CI evironment and are provided as-is. This means, they have been tested against a specific version of Spring XD and approved updates are applied to the master branch. This policy is subject to change, but at this point this repository is considered in the 'incubation' phase. We strongly encourage the XD community to use and contribute to this code. Additionally, we envision that this resource will eventually become a vital part of the Spring XD ecosystem. 


## Installing a module

Each module is a self contained component and includes the source code, a general description, and instructions for building the the module with maven and/or gradle. In order to use one of these modules, clone this repo, build the module locally, and upload the jar using the spring xd shell `module upload` command. Detailed instructions are included with each project. You will find a good overview of modules and module packaging [here](http://docs.spring.io/spring-xd/docs/current/reference/html/#modules).

## Updating the Spring XD version

Each module project provides a build script (maven, gradle, or both) used to packag the module as a jar. Typically, the jar layout is a Spring boot `uber jar` which includes nested jars to satisfy any runtime dependency that is not already provided by the Spring XD runtime. Spring XD dependencies are subject to change with each new release which, in turn, will affect which dependencies need to be exported to the module jar. The module builds use tools provided by Spring XD that determine what needs to be exported; a parent pom if using Maven, and a Gradle plugin if using gradle. These tools, and other module dependencies, reference a specific Spring XD Version and the modules have been built and tested against that version. _**For this reason, we strongly recommend that you change the build script to  use the Spring XD version that matches your target installation**_. If this results in any problem with the build, such as a test failure, please report the issue, using the instructions below, or better yet, submit a patch if you are able to resolve it.

## Developing Modules

Read the [Modules](http://docs.spring.io/spring-xd/docs/current/reference/html/#modules) section in the Spring XD reference guide for a good overview. If you want to dive into code, have a look at the example modules in the [spring-xd-samples][Samples] repo as well as the existing modules here. The following sections provide some additional tips.

### Version management

The samples repo is configured with a parent pom to allow us to update the Spring XD version globally. This is done as part of the Spring XD release procedures. We test all the samples against the latest version and tag the repo with the version. When using gradle, the recommended practice is to add `springXdVersion` to gradle.properties and reference this property in the plugin dependency:

````
buildScript {
    dependencies {
        classpath("org.springframework.xd:spring-xd-module-plugin:${springXDVersion}")
    }
}
````

The `springXdVersion` property is also required by the plugin to resolve XD dependencies. This pattern has not been enforced, but will be for future contributions.

With Maven there is no technical requirement for a property definition, but it is a good practice to define versions as properties especially when referenced in more than one place, and `spring.xd.version` is the standard convention. 

As of the 1.2.0 release of XD, the `spring-xd-module-parent` pom provides dependency management for every Spring XD dependency, which covers a lot of territory. Compile time dependencies for module must be declared even if they are provided by Spring XD. Runtime dependencies must be declared only if they are not provided by Spring XD. The latter will be exported to the module jar. This includes any dependencies that do not match the Spring XD version. This behavior is intended to allow the module classloader to load a different version as required, but this is generally not recommended or guaranteed to work. Therefore, the recommendation for building a module with Spring XD 1.2.x is to not specify a version for any compile time dependency required by the module. Chances are it is already in the XD classpath and Maven will complain otherwise. To determine the module's additional runtime dependencies, you can start with a manual inspection of the top level contents of `xd/lib` in your XD installation. Then write a test to run the module in an XD container, using the Spring XD testing framework (there are many good examples [here](https://github.com/spring-projects/spring-xd-samples)). Unfortunately, dependency management is less evolved with Gradle, so versions must be explicitly declared.

### Should I provide both Maven and Gradle builds?

Note that many of the [Samples][] provide both Maven and Gradle builds, primarily for illustration purposes. Some of the contributions here have followed the examples. If you feel strongly about offering users a choice, we will respect that, but it's not really necessary. Just be aware that you and whoever reviews and approves your contribution will need to verify the versions are the same in both builds, run both builds, and verify the jar contents produced by each are identical. If you are on the fence about which way to go, the version management discussion above indicates  Maven. But if you prefer Gradle, so be it.  

## Contributing to Spring XD

If you would like to contribute to this repository, we encourage contributions through pull requests from [forks of this repository](http://help.github.com/forking/). If you want to contribute code this way, please familiarize yourself with the process outlined for contributing to Spring projects here: [Contributor Guidelines](https://github.com/SpringSource/spring-integration/wiki/Contributor-Guidelines).

Before we accept a non-trivial patch or pull request we will need you to sign the [contributor's agreement](https://support.springsource.com/spring_committer_signup). Signing the contributor's agreement does not grant anyone commit rights to the main repository, but it does mean that we can accept your contributions, and you will get an author credit if we do.  Active contributors might be asked to join the core team, and given the ability to merge pull requests.

## Issue Tracking

Report issues via the [Spring XD JIRA](https://jira.springsource.org/browse/XD).

[Samples]:https://github.com/spring-projects/spring-xd-samples)