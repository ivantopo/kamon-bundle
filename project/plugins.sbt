lazy val root = project in file(".") dependsOn(RootProject(uri("git://github.com/kamon-io/kamon-sbt-umbrella.git")))
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")