# Board Game Borrowing System (Group 6)

## Project Overview

This project is a **Board Game Borrowing System** that allows users to browse, borrow, and manage board games. Game owners can list their games for borrowing, manage incoming requests, and schedule game events. Users can also register for events, submit reviews for borrowed games, and interact within the system.

See the project report here: [Project Report](https://docs.google.com/document/d/1hCBOQyv-SNZ_06jHwz9L2WM6tvUJASui5_qTRZZchs8/edit?usp=sharing) for deliverable 1.

See the project report here: [Project Report](https://github.com/McGill-ECSE321-Winter2025/project-group-6/wiki/Project-Report-Deliverable-3) for deliverable 3

## Core Functionalities:

- **Game Borrowing System:** Users can browse available board games and submit borrowing requests.
- **Game Management:** Game owners can add, edit, and delete games in their collection.
- **Event System:** Users can create, browse, and register for board game events.
- **Review System:** Users can submit reviews for games they have borrowed.
- **Notifications:** Game owners receive notifications about borrowing requests, and users get updates on their request status.

## Deliverables & Task Assignments (Deliverable 3)

| Task                                | Assigned To                                                | Deadline                                       | Effort (Hours) |
| ----------------------------------- | ---------------------------------------------------------- | ---------------------------------------------- |----------------|
| Add games, see games in platform Frontend | Panayiotis                                       | Sunday, April 6                                  | |
| See user's borrowing requests, accept/decline br, create a br Frontend        | Hakkim                                      | Sunday, April 6                              | |
| Browse, register to vents          | Logan      | Sunday, April 6                               |
| Login, settings, authentification, sign up page Frontend  | Kevin                                              | Sunday, April 6                              | |
| See reviews for games, write a review    | Marshall                                   | Sunday, April 6 |
| See user's created events, create an event    | Artiom                                   | Sunday, April 6 |
| Homepage, setup, add game to user's collection    | Andres                                   | Sunday, April 6 | 30+ I lost count |

## Team Members & Roles

| Name                  | Role                                                                      | Effort (Hours) |
| --------------------- | ------------------------------------------------------------------------- | -------------- |
| Panayiotis Saropoulos | UML Class Diagram, Testing Classes for all layers, Service Layer,                                                          | 9%              |
| Kevin Jiang           | UML Class Diagram Design Improvement, model classes and JPA annotations (all classes), CRUD implementation for all classes, and tests for all classes               | 23%             |
| Hakkim Bekkari        | Persistence Layer, Manage Borrowing Requests, Submit Borrowed Game Review | 13%              |
| Artiom Volodin        | Persistence Layer, Create Event                                           | 13%              |
| Andres Gonzalez       | Project Management, UML improvement, Testing, Repo Management, Add Game to Collection                          | 21%              |
| Logan Ma              | Testing, Register to an Event                                             | 10%              |
| Marshall Moussavi     | Report Writing, Testing, Submit Borrowed Game Review                      | 11%              |


## Deliverables & Task Assignments

| Task                                | Assigned To                                                | Deadline                                       |
| ----------------------------------- | ---------------------------------------------------------- | ---------------------------------------------- |
| UML Class Diagram & Code Generation | Panayiotis, Kevin                                          | Sunday, Feb 9                                  |
| Persistence Layer (JPA, DAO)        | Hakkim, Artiom, Kevin                                      | Wednesday, Feb 12                              |
| Testing Persistence Layer           | Andres, Logan (primary), Marshall, Kevin (secondary)       | Saturday, Feb 15                               |
| Build System Configuration (Maven)  | Andres, Kevin                                              | Monday, Feb 10                                 |
| Repo Management & Issue Tracking    | Andres, Hakkim, Marshall                                   | Issues: Sunday, Feb 9 / Report: Sunday, Feb 16 |


## Installation

Running the app locally requires Java, PostgreSQL, and npm (node.js).

```sh
$ java --version
openjdk 21.0.5 2024-10-15
OpenJDK Runtime Environment (build 21.0.5+11-Ubuntu-1ubuntu122.04)
OpenJDK 64-Bit Server VM (build 21.0.5+11-Ubuntu-1ubuntu122.04, mixed mode, sharing)
$ psql --version
psql (PostgreSQL) 14.15 (Ubuntu 14.15-0ubuntu0.22.04.1)
$ node -v
v22.11.0
$ npm -v
v10.9.0
```

Furthermore, there must be a database for this app.
To create one, simply connect to the database and issue the command `create database boardgame;`:
```
$ psql --username postgres
psql (14.15 (Ubuntu 14.15-0ubuntu0.22.04.1))
Type "help" for help.

postgres=# create database boardgame;
CREATE DATABASE
```
If you want to use a different database configuration (different database name, port, or password), then update [application.properties](./BoardGame-Backend/src/main/resources/application.properties).

To run the application on the browser, first move into the Backend folder, then boot the application
```sh
cd ./BoardGame-Backend
./gradlew bootRun
```
Additionnally, you can populate the database with mock data running instead (this will delete any data present in the boardgame db)

```sh
./gradlew bootRun --args='--spring.profiles.active=dev'
```
Then, move on to the Frontend folder, and run npm (by default the Backend only receives data from the 5173 port)
```sh
cd ./BoardGame-Frontend
npm run dev
```
You can view the UI on the browser with the link that has appeared. You will be prompted to create an account before proceeding to the home page.
[IMPORTANT: Please remember your user id]

## Running the Tests

To run the tests, move to the `BoardGame-Backend` directory and then run
```sh
./gradlew clean test
```

## Accessing RESTful API documentation

This project has buil-in RESTful API documentation to try the different services offered.

To access the documentation simply run:
```sh
./gradlew bootRun
```
Then access the following link on yout browser: http://localhost:8080/swagger-ui/index.html#/.
If for some reason the links fail (as if was often the case while testing) we provide manual documentation on the wiki of this project.

## Deliverable 2 Work Division

| Name                  | Role                                                                      | Effort (Hours) |
| --------------------- | ------------------------------------------------------------------------- | -------------- |
| Panayiotis Saropoulos | Implemented most of Game( service, dto, controller, test integration(but all failed, his push did not work and he did not know)) |25 Hours              |
| Kevin Jiang           | Implemented All UserAccount(service, dto, controller, tests, test integration), Ask project questions in Tutorial, Software Quality Assurance Report| Around 35 Hours             |
| Hakkim Bekkari        | Implemented all Borrowing Request classes(service, dto, controller, tests, test integration), Project Report| 27+ Hours              |
| Artiom Volodin        | Implemented All Event classes(service, dto, controller, tests, test integration),  Project Report             | 30+ Hours I lost count              |
| Andres Gonzalez       | Implemented All GameCopy(service, dto, controller, tests, test integration), Software Quality Assurance Report | 30+ Hours              |
| Logan Ma              | Implemented All EventRegistration(service, dto, controller, tests, test integration),Implemented some of Game(test and corrected test integration)| 35+ Hours              |
| Marshall Moussavi     | Implemented All Review(service, dto, controller, tests, test integration)| 25+ Hours              |


See the project report 2 here: [Project Report](https://docs.google.com/document/d/1cK9yB0SHbMPFoHfhHK9CYBijscfwlQioQiMMHNI2jnM/edit?usp=sharing)
