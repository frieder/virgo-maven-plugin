A Maven plugin that provides a convenient way to start Eclipse Virgo and to deploy OSGi bundles to it.

## Installation

Unfortunately there is currently no artifact available in any Maven repository so you have to create your own artifact. To do so execute the following command from within the project root.

`mvn clean install` - This will install a ready-to-use plugin artifact to your local Maven repository. Once this has been done you're ready to use this plugin in any of your Maven scripts. In case that you need to share this plugin accross a development team I suggest you install the plugin in a repository server like Nexus or Artifactory first.

## Goals

Currently the following goals are available

* mvn start - This will start a Virgo instance
* mvn shutdown - This will shutdown a Virgo instance
* mvn immediateShutdown - This will instantly shutdown a Virgo instance
* mvn deploy - This will deploy an OSGi bundle (hopefully in the future it will support plan and par as well)
* mvn undeploy - This will undeploy an OSGi bundle
* mvn update - This will update an previously installed OSGi bundle

## Examples

A couple of example pom.xml files can be found in the example folder.
