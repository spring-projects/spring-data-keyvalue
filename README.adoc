= Spring Data KeyValue image:https://jenkins.spring.io/buildStatus/icon?job=spring-data-keyvalue%2Fmain&subject=Build[link=https://jenkins.spring.io/view/SpringData/job/spring-data-keyvalue/] https://gitter.im/spring-projects/spring-data[image:https://badges.gitter.im/spring-projects/spring-data.svg[Gitter]]

The primary goal of the https://projects.spring.io/spring-data[Spring Data] project is to make it easier to build Spring-powered applications that use new data access technologies such as non-relational databases, map-reduce frameworks, and cloud based data services.

This module provides infrastructure components to build repository abstractions for stores dealing with Key/Value pairs and ships with a default `java.util.Map` based implementation.

== Features

* Infrastructure for building repositories on top of key/value implementations.
* Dynamic SpEL query generation from query method names.
* Possibility to integrate custom repository code.

== Code of Conduct

This project is governed by the https://github.com/spring-projects/.github/blob/main/CODE_OF_CONDUCT.md[Spring Code of Conduct]. By participating, you are expected to uphold this code of conduct. Please report unacceptable behavior to spring-code-of-conduct@pivotal.io.

== Getting Started

Here is a quick teaser of an application using Spring Data Repositories in Java:

[source,java]
----
public interface PersonRepository extends CrudRepository<Person, Long> {

  List<Person> findByLastname(String lastname);

  List<Person> findByFirstnameLike(String firstname);
}

@Service
public class MyService {

  private final PersonRepository repository;

  public MyService(PersonRepository repository) {
    this.repository = repository;
  }

  public void doWork() {

    repository.deleteAll();

    Person person = new Person();
    person.setFirstname("Oliver");
    person.setLastname("Gierke");
    repository.save(person);

    List<Person> lastNameResults = repository.findByLastname("Gierke");
    List<Person> firstNameResults = repository.findByFirstnameLike("Oli*");
 }
}

@KeySpace("person")
class Person {

  @Id String uuid;
  String firstname;
	String lastname;

	// getters and setters omitted for brevity
}

@Configuration
@EnableMapRepositories("com.acme.repositories")
class AppConfig { … }
----

=== Maven configuration

Add the Maven dependency:

[source,xml]
----
<dependency>
  <groupId>org.springframework.data</groupId>
  <artifactId>spring-data-keyvalue</artifactId>
  <version>${version}.RELEASE</version>
</dependency>
----

If you'd rather like the latest snapshots of the upcoming major version, use our Maven snapshot repository and declare the appropriate dependency version.

[source,xml]
----
<dependency>
  <groupId>org.springframework.data</groupId>
  <artifactId>spring-data-keyvalue</artifactId>
  <version>${version}.BUILD-SNAPSHOT</version>
</dependency>

<repository>
  <id>spring-libs-snapshot</id>
  <name>Spring Snapshot Repository</name>
  <url>https://repo.spring.io/libs-snapshot</url>
</repository>
----

== Getting Help

Having trouble with Spring Data? We’d love to help!

* Check the
https://docs.spring.io/spring-data/keyvalue/docs/current/reference/html/[reference documentation], and https://docs.spring.io/spring-data/keyvalue/docs/current/api/[Javadocs].
* Learn the Spring basics – Spring Data builds on Spring Framework, check the https://spring.io[spring.io] web-site for a wealth of reference documentation.
If you are just starting out with Spring, try one of the https://spring.io/guides[guides].
* If you are upgrading, check out the https://docs.spring.io/spring-data/keyvalue/docs/current/changelog.txt[changelog] for "`new and noteworthy`" features.
* Ask a question - we monitor https://stackoverflow.com[stackoverflow.com] for questions tagged with https://stackoverflow.com/tags/spring-data[`spring-data-keyvalue`].
You can also chat with the community on https://gitter.im/spring-projects/spring-data[Gitter].
* Report bugs with Spring Data KeyValue via https://github.com/spring-projects/spring-data-keyvalue/issues[Github].

== Reporting Issues

Spring Data uses Github as issue tracking system to record bugs and feature requests.
If you want to raise an issue, please follow the recommendations below:

* Before you log a bug, please search the https://github.com/spring-projects/spring-data-keyvalue/issues[issue tracker] to see if someone has already reported the problem.
* If the issue does not already exist, https://github.com/spring-projects/spring-data-keyvalue/issues/new[create a new issue].
* Please provide as much information as possible with the issue report, we like to know the version of Spring Data that you are using, the JVM version, Stacktrace, etc.
* If you need to paste code, or include a stack trace use https://guides.github.com/features/mastering-markdown/[Markdown] code fences +++```+++.
* If possible try to create a test-case or project that replicates the issue. Attach a link to your code or a compressed file containing your code.

== Building from Source

You don’t need to build from source to use Spring Data (binaries in https://repo.spring.io[repo.spring.io]), but if you want to try out the latest and greatest, Spring Data can be easily built with the https://github.com/takari/maven-wrapper[maven wrapper].
You also need JDK 1.8.

[source,bash]
----
 $ ./mvnw clean install
----

If you want to build with the regular `mvn` command, you will need https://maven.apache.org/run-maven/index.html[Maven v3.5.0 or above].

_Also see link:CONTRIBUTING.adoc[CONTRIBUTING.adoc] if you wish to submit pull requests, and in particular please sign the https://cla.pivotal.io/sign/spring[Contributor’s Agreement] before your first change, is trivial._

=== Building reference documentation

Building the documentation builds also the project without running tests.

[source,bash]
----
 $ ./mvnw clean install -Pdistribute
----

The generated documentation is available from `target/site/reference/html/index.html`.

== Examples

* https://github.com/spring-projects/spring-data-examples/[Spring Data Examples] contains example projects that explain specific features in more detail.

== License

Spring Data KeyValue is Open Source software released under the https://www.apache.org/licenses/LICENSE-2.0.html[Apache 2.0 license].
