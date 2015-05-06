# Spring Data Key Value #

The primary goal of the [Spring Data](http://projects.spring.io/spring-data) project is to make it easier to build Spring-powered applications that use data access technologies. This module provides infrastructure components to build repository abstractions for stores dealing with Key/Value pairs and ships with a default `java.util.Map` based implementation.

## Features ##

* Infrastructure for building repositories on top of key/value implementations.
* Dynamic SpEL query generation from query method names.
* Possibility to integrate custom repository code.


## Quick Start ##

Download the jar though Maven:

```xml
<dependency>
  <groupId>org.springframework.data</groupId>
  <artifactId>spring-data-keyvalue</artifactId>
  <version>0.1.0.BUILD-SNAPSHOT</version>
</dependency>
```

For snapshot versions, make sure you include the spring snapshots repository:

```xml
<repositories>
  <repository>
    <id>spring-libs-snapshot</id>
    <url>http://repo.spring.io/libs-snapshot</url>
  </repository>
</repositories>
```

The `ConcurrentHashMap` based default configuration looks like this: 
```java
@Configuration
@EnableMapRepositories("com.acme.repositories")
class AppConfig {

}
```

**Note** it is possible to change the used `java.util.Map` implementation via `@EnableMapRepositories(mapType=... .class)`.


Create the domain type and use `@KeySpace` to explicitly group values into one region. By default the types class name is used.
```java
@KeySpace("user")
class User {
  @Id String uuid;
  String firstname;
}
```

Create a repository interface in `com.acme.repositories`:

```java
public interface UserRepository extends CrudRepository<User, String> {
  List<String> findByLastname(String lastname);
}
```

## QueryDSL ##

To use Querydsl with Spring Data KeyValue repositories `com.mysema.querydsl:querydsl-collections:3.6.0` (or better) needs to be present.
Just add `QueryDslPredicateExecutor` to the repository definition and use `com.mysema.query.types.Predicate` to define the query.


## Tips ##
To have the maximum performance we encourage you to use the latest Spring 4.1.x or better release with compiled SpEL expressions enabled.
```
-Dspring.expression.compiler.mode=IMMEDIATE
```


## Contributing to Spring Data Key Value##

Here are some ways for you to get involved in the community:

* Get involved with the Spring community by helping out on [stackoverflow](http://stackoverflow.com/questions/tagged/spring-data-keyvalue) by responding to questions and joining the debate.
* Create [JIRA](https://jira.springsource.org/browse/DATAKV) tickets for bugs and new features and comment and vote on the ones that you are interested in.  
* Github is for social coding: if you want to write code, we encourage contributions through pull requests from [forks of this repository](http://help.github.com/forking/). If you want to contribute code this way, please reference a JIRA ticket as well covering the specific issue you are addressing.
* Watch for upcoming articles on Spring by [subscribing](http://spring.io/blog) to spring.io.

Before we accept a non-trivial patch or pull request we will need you to sign the [contributor's agreement](https://support.springsource.com/spring_committer_signup).  Signing the contributor's agreement does not grant anyone commit rights to the main repository, but it does mean that we can accept your contributions, and you will get an author credit if we do.  Active contributors might be asked to join the core team, and given the ability to merge pull requests.