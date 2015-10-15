# swrltab-plugin

*The SWRLTab Plugin has not yet been released. This plugin and related libraries are built using the latest 4.1.0 pre-release version of the OWLAPI and the current Protégé 5.0.0 Beta has not yet been updated to use this version. The upcoming Beta 18 release of Protégé will use the latest OWLAPI version. The SWRLTab plugin will be released at the same time.*

*However, a standalone SWRLTab is currently available [here](https://github.com/protegeproject/swrltab). 
It contains all the functionality of the Protégé SWRLTab plugin. 
The [SWRLTab build project](https://github.com/protegeproject/swrltab-project) can also be used to build this
standalone application and and will ensure that the components it depends on are also built.*

This repository contains the SWRLAPI-based SWRLTab plugin for the Protégé 5.0+ Desktop ontology editor. 

Plugin documentation can be found [here](https://github.com/protegeproject/swrltab-plugin/wiki).
Documentation for the SWRLAPI can be found [here](https://github.com/protegeproject/swrlapi/wiki).

#### Building Prerequisites

To build and run this plugin you must have the following items installed:

+ Apache's [Maven](http://maven.apache.org/index.html)
+ A tool for checking out a [Git](http://git-scm.com/) repository
+ [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
+ A Protégé (5.0 beta 18 or higher) distribution. Download [here](http://protege.stanford.edu/download/protege/5.0/snapshots/)

#### Building

Get a copy of the latest code:

    git clone https://github.com/protegeproject/swrltab-plugin.git swrltab-plugin
    
Change into the swrltab-plugin directory:

    cd swrltab-plugin

Build it with Maven:

    mvn clean package  

On build completion the ```target``` directory will contain a swrltab-plugin-${version}.jar file.

This JAR is generated in the OSGi bundle format required by Protégé's plugin-in mechanism.

To install this plug-in into your local Protégé, copy the JAR file from the target directory to the "plugins" subdirectory of your Protégé installation (e.g.,
/Applications/Protege_5.0_beta/plugins/).
 
#### View the Plugin in Protégé

Launch your Protégé distribution. Select About Protégé from the Protégé menu to verify successful installation:

The  bundle contains:

+ Two custom tabs - "SWRLTab" and "SQWRLTab". Enable either tab via the Window | Tabs menu.
 
#### Example plug-in screenshots

SWRLTab:

![SWRLTab](/img/SWRLTab.png?raw=true "SWRLTab")

SQWRLTab:

![SQWRLTab](/img/SQWRLTab.png?raw=true "SQWRLTab")

#### Questions

If you have questions about this plug-in, please go to the main
Protégé website and subscribe to the [Protégé Developer Support
mailing list](http://protege.stanford.edu/support.php#mailingListSupport).
After subscribing, send messages to protege-dev at lists.stanford.edu.

