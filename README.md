[![GPL LICENSE][license-shield]][license-url]
[![GitHub tag (with filter)][tag-shield]][tag-url]
[![GitHub contributors][contributors-shield]][contributors-url]
[![GitHub Workflow Status (with event)][dev-build-shield]][dev-build-url]
[![GitHub Workflow Status (with event)][release-build-shield]][release-build-url]

# General Computational Model
The General Computational Model (GCM) is a Java based simulation framework for building disease progression models. 

Users of GCM should have a general familiarity with Java and object oriented programming and would benefit from some exposure to event based modeling.

This repository contains the source code, along with a set of lessons that have been created to aide new users with using this simulation framework.

## License
Distributed under the GPLv3 License. See [LICENSE](LICENSE) for more information.

Please read the [HHS vulnerability discloure](https://www.hhs.gov/vulnerability-disclosure-policy/index.html).

## Overview
There are 3 core tenants to GCM.

### Simulation
GCM is an event based simulation framework composed of data managers, actors and an event engine.  
<p>The data managers contain the state of the simulation and generate events when that state changes.  
The actors contain the business logic of your model and act on the data managers.  
<p>The engine transports events generated by the data managers to any data managers and actors that subscribe to those events.

### Plugins
Data managers and actors are organized into plugins. A GCM model is thus composed of the core simulation and a suite of plugins.  
<p>The plugin architecture provides for the scalable reuse of concepts and capabilities between models.  
<p>GCM is provided with a set of existing plugins that define many of the concepts useful to a broad range of models such as the management of people, their properties, and social group structures. 
<p>The modeler is free to compose a model from their choice of plugins.

### Experiment
<p>GCM also provides an experiment management system.  
<p>Each plugin contains zero to many data objects that define the initial state of its actors and data managers. Each such data object may be altered freely.  
<p>The complete set of all combinations (scenarios) of the variant plugin data objects form an experiment and a separate simulation instance is executed for each combination.

## Building from Source

### Requirements
- Maven 3.8.x
- Java 17
- Your favroite IDE for developing Java projects
- Modeling Utilities located [here](https://github.com/HHS/ASPR-ms-util)

*Note that Modeling Utilities is in Maven Central, so there is no need to clone and build it. 

### Building
To build this project:
- Clone the repo
- open a command line terminal
- navigate to the root folder of this project
- run the command: `mvn clean install`

## Documentation
The documentation can be found at [https://hhs.github.io/ASPR-8/](https://hhs.github.io/ASPR-8/)

## Lessons
The documentation contains lessons which the code can be found in [lessons](lessons).

<!-- MARKDOWN LINKS & IMAGES -->
[contributors-shield]: https://img.shields.io/github/contributors/HHS/ASPR-8
[contributors-url]: https://github.com/HHS/ASPR-8/graphs/contributors
[tag-shield]: https://img.shields.io/github/v/tag/HHS/ASPR-8
[tag-url]: https://github.com/HHS/ASPR-8/releases/latest
[license-shield]: https://img.shields.io/github/license/HHS/ASPR-8
[license-url]: LICENSE
[dev-build-shield]: https://img.shields.io/github/actions/workflow/status/HHS/ASPR-8/dev_build.yml?label=dev-build
[dev-build-url]: https://github.com/HHS/ASPR-8/actions/workflows/dev_build.yml
[release-build-shield]: https://img.shields.io/github/actions/workflow/status/HHS/ASPR-8/release_build.yml?label=release-build
[release-build-url]: https://github.com/HHS/ASPR-8/actions/workflows/release_build.yml
