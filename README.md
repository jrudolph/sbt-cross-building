# Sbt Cross Building Plugin

Building plugins for multiple versions of sbt is often cumbersome because just to build a version
of your plugin for another version of sbt you have to change the sbt version of your plugin _build_.
This hinders fast adoption of new sbt versions because just to release a backwards compatible version
of your plugin all the build plugin dependencies have to be available for every version sbt you want
to build for.

This plugin tries to ease the building of plugins for older versions of sbt.

## Usage

Add

    addSbtPlugin("net.virtual-void" % "sbt-cross-building" % "0.8.0")

to your ``project/plugins.sbt`` and

    crossBuildingSettings

to your ``build.sbt`` or if you are using full configuration (with `Build.scala`):

```scala
.settings(net.virtualvoid.sbt.cross.CrossPlugin.crossBuildingSettings: _*)
```

## Features

 * `sbtVersion in sbtPlugin` sets the target sbt version for your plugin
 * Use the `^^` command in the sbt shell to set the sbt version you want to build against.
   For example, sbt version 0.11.0 is selected by running

       > ^^0.11.0

   in the sbt console. That's basically equivalent to running

       > set sbtVersion in sbtPlugin := "0.11.0"

 * Set the `CrossBuilding.crossSbtVersions` setting to a list of sbt versions to build against and then use
   `^ <command>` to execute an sbt command for each of the sbt versions listed.
 * You can have custom (scala) source directories for particular versions of sbt. Currently, both
   `src/main/scala-sbt-0.x` or `sbt/main/scala-sbt-0.x.y` are supported. E.g. a source
   directory `src/main/scala-sbt-0.11.2` will only be used when building against sbt 0.11.2, a source
   directory `src/main/scala-sbt-0.11` will be used for any version of sbt 0.11.x.
 * You can build 0.12 plugins from sbt 0.11.x. Starting with sbt 0.12, use "0.12" as target sbt version and the
   plugin will choose the latest compatible sbt 0.12.x version and the right scala version. Similarly use "0.13" to
   target sbt 0.13.
 * The scripted plugin shipping with sbt is incompatible with sbt-cross-building because
   it uses the wrong sbt launcher. This plugin contains a fixed version of the scripted plugin. To make
   it work
     * remove the `plugins.sbt` dependency on the scripted plugin
     * in your build replace `scriptedSettings` with `CrossBuilding.scriptedSettings`
 * The plugin itself works for sbt 0.11.2, 0.11.3 and 0.12.x.

## Known Issues

  - To allow building against other versions of sbt we have to rewrite a bunch of settings which are already
    defined in sbt itself. This may lead to issues, however, we've not experienced any such yet.

## License

Copyright (c) 2012 Johannes Rudolph

Published under the [BSD 2-Clause License](http://www.opensource.org/licenses/BSD-2-Clause).
