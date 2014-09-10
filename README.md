# swrltab-plugin

*This plugin will be released in late September, 2014.*

This repository contains the SWRLAPI-based SWRLTab plugin for the Protege Desktop ontology 
editor (*version 5.0 and higher*). 

Plugin documentation can be found here [https://github.com/protegeproject/swrltab-plugin/wiki here].
Documentation for the SWRLAPI can be found [https://github.com/protegeproject/swrlapi/wiki here].

#### Building

The Maven POM file in the top-level directory packages the plug-in code into the required OSGi bundle format 
using the [Maven Bundle Plugin](http://felix.apache.org/site/apache-felix-maven-bundle-plugin-bnd.html).

#### Prerequisites

To build and run this plugin, you must have the following items installed:

+ Apache's [Maven](http://maven.apache.org/index.html).
+ A tool for checking out a [Git](http://git-scm.com/) repository.
+ A Protege distribution (5.0 beta or higher)  [Protege 5.0.0 beta SNAPSHOT builds](http://protege.stanford.edu/download/protege/5.0/snapshots/).

#### Build and install the SWRLTab plug-in

1. Get a copy of the example code:

   git clone https://github.com/protegeproject/swrltab-plugin.git swrltab-plugin
    
2. Change into the swrltab-plugin directory.

3. Type mvn clean package.  On build completion, the "target" directory will contain a swrltab-plugin-${version}.jar file.

4. Copy the JAR file from the target directory to the "plugins" subdirectory of your Protege distribution (e.g.,
/Applications/Protege_5.0_beta/plugins/)
 
#### View the Plugin in Protege

Launch your Protege distribution. Select About Protege from the Protege menu to verify successful installation:

The  bundle contains:

+ Two custom tabs - "SWRLTab" and "SQWRLTab". Enable either tab via the Window | Tabs menu.
 
#### Example plug-in screenshots

SWRLTab:

![SWRLTab](/img/SWRLTab.png?raw=true "SWRLTab")

SQWRLTab:

![SQWRLTab](/img/SQWRLTab.png?raw=true "SQWRLTab")

#### Questions

If you have questions about this plug-in, please navigate to the main
Protege website and subscribe to the [Protege Developer Support
mailing list](http://protege.stanford.edu/support.php#mailingListSupport).
After subscribing, send messages to protege-dev at lists.stanford.edu.

