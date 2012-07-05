resolvers += "less is" at "http://repo.lessis.me"

//addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.1", "0.11.2)
libraryDependencies += Defaults.sbtPluginExtra("me.lessis" % "ls-sbt" % "0.1.1", "0.11.2", "2.9.1")

resolvers += Resolver.url("Typesafe repository", url("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns)

addSbtPlugin("net.virtual-void" % "sbt-cross-building" % "0.6.9")