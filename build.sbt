import com.lightbend.sbt.javaagent.Modules
import sbt.Keys.resourceGenerators
import BundleKeys._
onLoad in Global ~= (_ andThen ("project kamonBundle" :: _))



lazy val instrumentationModules: Seq[ModuleID] = Seq(
//  "io.kamon" %% "kamon-scala-future"      % "2.0.0-209f237bcddf4a9c3de2ad91836b6a5d4f6ad3e6",
//  "io.kamon" %% "kamon-twitter-future"    % "2.0.0-209f237bcddf4a9c3de2ad91836b6a5d4f6ad3e6",
//  "io.kamon" %% "kamon-scalaz-future"     % "2.0.0-209f237bcddf4a9c3de2ad91836b6a5d4f6ad3e6",
  "io.kamon" %% "kamon-executors" % "2.0.0-83b17a1a775aa3860432bd1b67788b53b2fdd017" changing()
)

val versionSettings = Seq(
  kamonCoreVersion := "2.0.0-M4",
  kanelaAgentVersion := "1.0.0-M2",
  instrumentationCommonVersion := "2.0.0-3d734de88d883ea580919995b58c12b9755de92d"
)

lazy val kamonBundle = project
   .settings(noPublishing: _*)
   .aggregate(bundle, publishing)

val bundle = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(AssemblyPlugin)
  .settings(versionSettings: _*)
  .settings(
    skip in publish := true,
    name := "kamon-bundle",
    resolvers += Resolver.mavenLocal,
    buildInfoPackage := "kamon.bundle",
    buildInfoKeys := Seq[BuildInfoKey](kanelaAgentJarName),
    kanelaAgentModule := "io.kamon" % "kanela-agent" % kanelaAgentVersion.value % "provided",
    kanelaAgentJar := update.value.matching(Modules.exactFilter(kanelaAgentModule.value)).head,
    kanelaAgentJarName := kanelaAgentJar.value.getName,
    resourceGenerators in Compile += Def.task(Seq(kanelaAgentJar.value)).taskValue,
    kamonCoreExclusion := ExclusionRule(organization = "io.kamon", name = s"kamon-core_${scalaBinaryVersion.value}"),
    bundleDependencies := Seq(
      kanelaAgentModule.value,
      "io.kamon"      %% "kamon-status-page"            % kamonCoreVersion.value excludeAll(kamonCoreExclusion.value) changing(),
      "io.kamon"      %% "kamon-instrumentation-common" % instrumentationCommonVersion.value excludeAll(kamonCoreExclusion.value) changing(),
      "net.bytebuddy" %  "byte-buddy-agent"             % "1.9.12",
    ),
    libraryDependencies ++= bundleDependencies.value ++ instrumentationModules.map(_.excludeAll(kamonCoreExclusion.value)),
    packageBin in Compile := assembly.value,
    assembleArtifact in assemblyPackageScala := false,
    assemblyShadeRules in assembly := Seq(
      ShadeRule.zap("**module-info").inAll,
      ShadeRule.rename("net.bytebuddy.agent.**" -> "kamon.lib.@0").inAll
    ),
    assemblyMergeStrategy in assembly := {
      case "reference.conf" => MergeStrategy.concat
      case anyOther         => (assemblyMergeStrategy in assembly).value(anyOther)
    }
  )

lazy val publishing = project
  .settings(versionSettings: _*)
  .settings(
    name := (name in (bundle, Compile)).value,
    scalaVersion := (scalaVersion in bundle).value,
    crossScalaVersions := (crossScalaVersions in bundle).value,
    packageBin in Compile := (packageBin in (bundle, Compile)).value,
    packageSrc in Compile := (packageSrc in (bundle, Compile)).value,
    bintrayPackage := "kamon-bundle",
    libraryDependencies ++= Seq(
      "io.kamon" %% "kamon-core" % kamonCoreVersion.value
    )
  )
