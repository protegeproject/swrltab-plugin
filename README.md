# swrltab-plugin

[![Build Status](https://travis-ci.org/protegeproject/swrltab-plugin.svg?branch=master)](https://travis-ci.org/protegeproject/swrltab-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/edu.stanford.swrl/swrltab-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/edu.stanford.swrl/swrltab-plugin)

This repository contains the SWRLAPI-based SWRLTab plugin for the Protégé 5.0 Desktop ontology editor.
The plugin contains SWRLTab and SQWRLTab components for working with SWRL rules and SQWRL queries.
Note that the plugin will work only in Protégé version 5.0.0 RC 2 and later.

Plugin documentation can be found [here](https://github.com/protegeproject/swrltab-plugin/wiki).
Documentation for the SWRLAPI can be found [here](https://github.com/protegeproject/swrlapi/wiki).

#### Installing and Updating

The plugin comes preinstalled with Protégé. 
The plugin can be updated to the latest version via the 'File -> Check for plugins...' menu item in Protégé. 
A popup listing available plugins can be used to select and update the plugin.
The 'Protégé->About Protégé' menu uption can be used to display the current version of the plugin.

#### View the Plugin Tabs in Protégé

The SWRLTab and SQWRLTab can be enabled via the 'Window -> Tabs' menu item.

#### Example Plug-in Screenshots

SWRLTab:

![SWRLTab](/img/SWRLTab.png?raw=true "SWRLTab")

SQWRLTab:

![SQWRLTab](/img/SQWRLTab.png?raw=true "SQWRLTab")

#### Building from Source

To build this plugin you must have the following items installed:

+ [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
+ A tool for checking out a [Git](http://git-scm.com/) repository
+ Apache's [Maven](http://maven.apache.org/index.html)
+ A Protégé (5.0.0 RC2 or higher) distribution. Download [here](http://protege.stanford.edu/products.php#desktop-protege).

Get a copy of the latest code:

    git clone https://github.com/protegeproject/swrltab-plugin.git
    
Change into the swrltab-plugin directory:

    cd swrltab-plugin

Build with Maven:

    mvn clean package  

On build completion the ```target``` directory will contain a swrltab-plugin-${version}.jar file. 
The JAR contains both SWRLTab and SQWRLTab components and is generated in the OSGi bundle format required by Protégé's plugin-in mechanism.

To install in your local Protégé, copy this JAR file to the ```plugins``` subdirectory of your Protégé installation (e.g.,
/Applications/Protege-5.0.0/plugins/).  

#### License

The software is licensed under the [BSD 2-clause License](https://github.com/protegeproject/swrltab-plugin/blob/master/license.txt).

#### Questions

If you have questions about this plug-in, please go to the main
Protégé website and subscribe to the [Protégé Developer Support
mailing list](http://protege.stanford.edu/support.php#mailingListSupport).
After subscribing, send messages to protege-dev at lists.stanford.edu.

