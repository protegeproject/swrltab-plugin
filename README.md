# swrltab-plugin

This repository contains the SWRLAPI-based SWRLTab plugins for the Protégé 5.0 Desktop ontology editor. 
Note that the plugins will work only in version 5.0.0-Beta-21 and later.

Plugin documentation can be found [here](https://github.com/protegeproject/swrltab-plugin/wiki).
Documentation for the SWRLAPI can be found [here](https://github.com/protegeproject/swrlapi/wiki).

#### Building

To build these plugins you must have the following items installed:

+ [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
+ A tool for checking out a [Git](http://git-scm.com/) repository
+ Apache's [Maven](http://maven.apache.org/index.html)
+ A Protégé (5.0.0 Beta 21 or higher) distribution. Download [here](http://protege.stanford.edu/products.php#desktop-protege).

Get a copy of the latest code:

    git clone https://github.com/protegeproject/swrltab-plugin.git
    
Change into the swrltab-plugin directory:

    cd swrltab-plugin

Build with Maven:

    mvn clean package  

#### Manually Installing Plugin

On build completion the ```target``` directory will contain a swrltab-plugin-${version}.jar file. 
The JAR contains both SWRLTab and SQWRLTab plugins and is generated in the OSGi bundle format required by Protégé's plugin-in mechanism.

To install these plugins into your local Protégé, copy this JAR file to the ```plugins``` subdirectory of your Protégé installation (e.g.,
/Applications/Protege-5.0.0-beta-21/plugins/).
 
#### View the Plugins in Protégé

Enable either plugin via the Window | Tabs menu.
 
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

