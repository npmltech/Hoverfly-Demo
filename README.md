# Hoverfly-Demo

#### ⚙️ Demonstration of Hoverfly Framework - API Simulator and Service Virtualization.
<br />

<img align="center" src="./img/hoverfly_logo.png" alt="Hoverfly Logo"></img>
<br />

## What is the Hoverfly?

Developing and testing interdependent applications is difficult.
Maybe you’re working on a mobile application that needs to talk to a legacy API. Or a microservice that relies on two other services that are still in development.

The problem is the same: how do you develop and test against external dependencies which you cannot control?
You could use mocking libraries as substitutes for external dependencies. But mocks are intrusive, and do not allow  you to test all the way to the architectural boundary of your application.
Stubbed services are better, but they often involve too much configuration or may not be transparent to your application.
Then there is the problem of managing test data. Often, to write proper tests, you need fine-grained control over the data in your mocks or stubs. Managing test data across large projects with multiple teams introduces bottlenecks that impact delivery times.
Integration testing “over the wire” is problematic too.

When stubs or mocks are swapped out for real services (in a continuous integration environment for example) new variables are introduced. Network latency and random outages can cause integration tests to fail unexpectedly.
**Hoverfly** was designed to provide you with the means to create your own “dependency sandbox”: a simulated development and test environment that you control.
**Hoverfly** grew out of an effort to build “the smallest service virtualization tool possible”.

## Introduction:

### What's the Service Virtualization?

**Service Virtualization** is a technique used in software development and testing to simulate the behavior of components or services that are not yet available or are difficult to access for testing purposes.
It allows developers and testers to create virtual representations or mocks of these services or components to emulate their functionalities, behaviors, and responses.

Given the need for applications to integrate with several independent services, such as external components, third-party APIs, as well as different protocols such as SOAP or GRPC, service virtualization has become an interesting tool for resolving issues related to instability, unavailability or the same cost that making constant calls to these services entails.

## Stack:

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

## Explanation (Basics):

Explanation about the project.

## Documentation:

[Documentation](https://linktodocumentation)

## Features:

- Light/dark mode toggle
- Live previews
- Fullscreen mode
- Cross platform

## Installation:

Install my-project with command below:

```bash
  cd my-project
  mvn install my-project
```

## Screenshots:

![App Screenshot](https://via.placeholder.com/468x300?text=App+Screenshot+Here)

## Running Tests:

To run tests, run the following command

```bash
  mvn or gradle
```

## Usage/Examples:

```java
class HelloWorld {
          public static void main (String args[]) {
               System.out.println("Hello World");
          }
     }
}
```

## References:

[Tutorial](https://linktotutorial)

---
