addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.3")

resolvers ++= Seq(
  Resolver.url("Typesafe repository", url("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns),
  "coda hale's repo" at "http://repo.codahale.com"
)

addSbtPlugin("net.virtual-void" % "sbt-cross-building" % "0.8.1")
