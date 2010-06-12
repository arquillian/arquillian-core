
                             Arquillian JUnit project

 Source archetype: arquillian-junit-javaee6

 What is it?
 ===========

 This is your Arquillian sandbox project! It's a sample Maven 2 project to help
 you get your foot in the door testing Java EE 6 applications with Arquillian.

 The project is setup to allow you to test your application against and Weld
 embedded container by default and also embedded and remote JBoss AS and
 GlassFish containers using the matching Maven profile. It also includes
 several sample tests for reference and convenient prototyping.

 System requirements
 ===================

 All you need to run this project is Java 5.0 (Java SDK 1.5) or greator and
 Maven 2.0.10 or greater (though Maven 3 is strongly recommended).

 Running the tests
 =================

 This project uses Maven profiles to select the tests and the container on
 which they will be run.

 By default, simple bean tests are run in a Weld embedded container, handled by
 the weld-embedded profile:

 mvn clean test

 Additional tests are added if you run against a real Java EE 6 container. You
 have several options, with the prerequisites listed for each.

 = JBoss AS remote 6.0

 Start a JBoss AS 6.0 instance. Then run:

  mvn clean test -Pjbossas-remote-60

 = JBoss AS managed 6.0

 Set the location to a JBoss AS 6 install directory in the configuration file
 src/test/resources/arquillian.xml.  Then run:

  mvn clean test -Pjbossas-managed-60

 = GlassFish embedded 3.0

 No prerequisites.

  mvn clean test -Pglassfish-embedded-30

 = GlassFish remote 3.0

 Start a GlassFish 3.0 instance and the JavaDB. Then run:

  mvn clean test -Pglassfish-remote-30

 You can safely remove the profile of any container you won't be using.

 Importing the project into an IDE
 =================================

 If you created the project using the Maven 2 archetype wizard in your IDE
 (Eclipse, NetBeans or IntelliJ IDEA), then there is nothing to do. You should
 already have an IDE project.

 If you created the project from the commandline using archetype:generate, then
 you need to bring the project into your IDE. If you are using NetBeans 6.8 or
 IntelliJ IDEA 9, then all you have to do is open the project as an existing
 project. Both of these IDEs recognize Maven 2 projects natively.

 To import into Eclipse, you first need to install the m2eclipse plugin. To get
 started, add the m2eclipse update site (http://m2eclipse.sonatype.org/update/)
 to Eclipse and install the m2eclipse plugin and required dependencies. Once
 that is installed, you'll be ready to import the project into Eclipse.

 Select File > Import... and select "Import... > Maven Projects" and select
 your project directory. m2eclipse should take it from there.

 Once in the IDE, you can execute the Maven commands through the IDE controls
 to active one of the Maven profiles and execute the tests.

 Arquillian resources
 ====================

 Project site:         http://jboss.org/arquillian
 User forums:          http://community.jboss.org/en/arquillian
 Development forums:   http://community.jboss.org/en/arquillian/dev
 JIRA:                 http://jira.jboss.org/browse/ARQ

