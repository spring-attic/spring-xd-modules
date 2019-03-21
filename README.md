Spring XD Modules
=================
This repository contains modules - reusable components for building streams or jobs with [Spring XD](https://projects.spring.io/spring-xd/). The Spring XD distribution includes a number of ready to use  [modules out of the box](https://docs.spring.io/spring-xd/docs/current-SNAPSHOT/reference/html/#available-modules). This repository is intended to supplement the pre-packaged modules with contributions from the user community and the Spring XD team. 

Contributions to this repository are subject to the same initial code review and QA processes applied to the main Spring XD code base. However, they are not included in the Spring XD release management and CI processes, as are the pre-packaged modules, and are provided as-is. This means they are manually tested against a specific version of Spring XD and approved updates are merged into the master branch. Additionally, the Spring XD team will manually build and test these against current releases. This policy is subject to change, but at this point this repository should be considered an "incubator". We strongly encourage the Spring XD community to use and contribute to this repository. Furthermore, we envision that this resource will eventually become a vital part of the Spring XD ecosystem. 


## Installing a module

Each module is a self contained component and includes the source code, a general description, and instructions for building the the module with Maven and/or Gradle. In order to use one of these modules, clone this repo, build the module locally, and upload the jar using the spring xd shell `module upload` command. Detailed instructions are included with each project. You will find a good overview of modules and module packaging [here](https://docs.spring.io/spring-xd/docs/current/reference/html/#modules).

## Updating the Spring XD version

Each module project provides a build script (Maven, Gradle, or both) used to package the module as a jar. The build scripts are configured for a specific Spring XD version. If you intend to use a module with a different Spring XD version, rebuild the module configured for the target version. See the section on [module dependency management](https://docs.spring.io/spring-xd/docs/current-SNAPSHOT/reference/html/#module-dependency-management) in the Spring XD reference for details.

## Developing Modules

Read the [Creating a Module](https://docs.spring.io/spring-xd/docs/current-SNAPSHOT/reference/html/#creating-a-module) section in the Spring XD reference guide for a good overview of developing modules. If you want to dive into code, have a look at the example modules in the [spring-xd-samples][Samples] repository as well as the modules here. 

### Should I provide both Maven and Gradle builds?

Note that many of the [Samples][] provide both Maven and Gradle builds, primarily for illustration purposes. Some of the contributions here have followed this pattern. If you feel strongly about offering users a choice, we will respect that, but it's not really necessary. You are free to choose your preferred build tool. If you do decide to provide both, just be aware that you and whoever reviews your project will need to verify both builds are identically configured, complete successfully, and the jar contents produced by each are identical.

## Contributing to Spring XD

If you would like to contribute to this repository, we encourage contributions via pull requests from [forks of this repository](https://help.github.com/forking/). If you want to contribute code this way, please familiarize yourself with the process outlined for contributing to Spring projects here: [Contributor Guidelines](https://github.com/SpringSource/spring-integration/wiki/Contributor-Guidelines).

Before we accept a non-trivial patch or pull request we will need you to sign the [contributor's agreement](https://support.springsource.com/spring_committer_signup). Signing the contributor's agreement does not grant anyone commit rights to the main repository, but it does mean that we can accept your contributions, and you will get an author credit if we do.  Active contributors might be asked to join the core team, and given the ability to merge pull requests.

## Issue Tracking

Report issues via the [Spring XD JIRA](https://jira.springsource.org/browse/XD).

[Samples]:https://github.com/spring-projects/spring-xd-samples)
