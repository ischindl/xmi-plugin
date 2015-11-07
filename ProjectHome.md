A maven plugin to generate an UML dependency model (in XMI) from the dependencies of a maven project.

# Installation #

For now you have to check out the code in order to use the plugin. You'll obviously also require a working maven installation.

  * checkout the code from subversion

> `svn checkout http://xmi-plugin.googlecode.com/svn/trunk/ xmi-plugin`

  * install the xmi-plugin in your local maven repository

> In the checkout directory, run `mvn install`

  * use the plugin on your maven project

> Run `mvn xmi:xmi` in a directory where there is a maven pom.xml file. The resulting xmi model will be generated in the target directory of your project, with the name ${artifactid}-${version}.xmi

  * open the xmi model in your favourite xmi tool (e.g. Magic Draw)