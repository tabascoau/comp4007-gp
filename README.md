# COMP4007 Term Project - Smart Elevator System (SES)

This project using java to implement the server of the Smart Elevator System (SES)

## Getting Started

These README file will show you the structure of location of the directories and file.
Instructions for compiling, starting and stopping of our system.
Additional information.

## Prerequisites

Java JDK, IntelliJ IDE

Website to download Java JDK:
```
https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
```
Website to download IntelliJ IDE:
```
https://www.jetbrains.com/idea/
```
## Installing

### A step by step approach to help you compiling, start and shopping our system.

After you installed all prerequisites. and downloaded our source code file. -> Upzip it

Open Intellij IDE and open the project. 

Open Command Prompt and change current directory to comp4007-gp/PassengerStream_v3/

Input the command java -jar PSKickstarter.jar PassengerStream.ejb 

Click the run button on Intellij IDE, DONE.

To close the project, click the exit button on Central Control Panel or Stop button on IDE.

## Program structure

```bash
├── src
│   ├── AppKickstarter
│   │   ├──  gui
        │      ├──────────────────CentralControlPanel
        │      │                           ├───────────────CentralControlPanel
        │      │                           ├───────────────CentralControlPanel.form
        │      ├──────────────────ElevatorPanel
        │      │                           ├───────────────ElevatorPanel
        │      │                           ├───────────────ElevatorPanel.form
        │      ├──────────────────KisokPanel
        │                                  ├───────────────KioskPanel
        │                                  ├───────────────KioskPanel.form
        │                                 
        ├── misc
        │      ├──────────────────AppThread
        │      ├──────────────────GreetingServer
        │      ├──────────────────LogFormatter
        │      ├──────────────────MBox
        │      ├──────────────────Msg
        │
        ├── myThreads
        │      ├──────────────────Elevator
        │      ├──────────────────Floor
        │
        │
        ├── timer
        │      ├──────────────────Time
        │
        ├── AppKickstarter
        ├── AppKickstarter
        ├── READ.md
```

```
Finished
```

│   ├── css
│   │   ├── **/*.css
│   ├── favicon.ico
│   ├── images
│   ├── index.html
│   ├── js
│   │   ├── **/*.js
│   └── partials/template
├── dist (or build)
├── node_modules
├── bower_components (if using bower)
├── test
├── Gruntfile.js/gulpfile.js
├── README.md
├── package.json
├── bower.json (if using bower)
└── .gitignore
                      


