# Sbt Cross Building Plugin

Building plugins for multiple versions of sbt is often cumbersome because just to build a version
of your plugin for another version of sbt you have to change the sbt version of your plugin _build_.
This hinders fast adoption of new sbt versions because just to release a backwards compatible version
of your plugin all the build plugin dependencies have to be available for every version sbt you want
to build for.

This plugin tries to ease the building of plugins for older versions of sbt.

## Usage

Add

    addSbtPlugin("net.virtual-void" % "sbt-cross-building" % "0.5.0")

to your ``project/plugins.sbt`` and you are ready to go.

Set `sbtVersion in sbtPlugin` to the sbt version you want to build against.for example by
running

   set sbtVersion in sbtPlugin := "0.11.0"

in the sbt console.

## Known Issues

  - To allow building against other versions of sbt we have to rewrite a bunch of settings which are already
    defined in sbt itself. This may lead to issues, however, we've not experienced any such yet.

## TODO

  - Make it possible to build 0.12.x plugins from a plugin 0.11.x build.

## License

Copyright (c) 2012 Johannes Rudolph
