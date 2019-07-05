# kmdp-models
##Release Instructions

Affected variables:
* project.parent.version
* project.version (SELF)

Affected sub-modules
* kmdp-core
  * maven-dependency-plugin (plugin)
    * unpack-xmi (execution)
      * configuration.artifactItems.artifactItem.version
        

### Release Branch
1. Set root POM's version and parent.version to desired fixed version
  * The version MUST match the ${kmdp.impl.version} variable in the BOM
2. Set the plugin execution version to a fixed version
  * The version MUST match the ${api4kp.version} variable in the BOM

### Nex Dev Branch
1. Set parent and project to the next desired version
  * Use mvn versions:set and update-child-modules to ensure all children are updated
2. Set the 'unpack-xmi' version to ${org.omg.spec:API4KP:jar.version}