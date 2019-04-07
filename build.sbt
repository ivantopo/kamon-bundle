import scala.xml.Node
import scala.xml.transform.{RewriteRule, RuleTransformer}
import BuildTools.excludeFromClasspath

val bundle = (project in file("."))
  .settings(
    libraryDependencies ++= withoutTransitiveDependencies(kamonAndKanela),
    libraryDependencies ++= withoutTransitiveDependencies(instrumentationModules),
    assemblyExcludedJars in assembly := excludeFromClasspath((fullClasspath in assembly).value, Seq(
      "kamon-core",
      "slf4j-api",
      "config")),
    packageBin in Compile := assembly.value,
    assemblyJarName in assembly := s"${moduleName.value}_${scalaBinaryVersion.value}-${version.value}.jar",
    assembleArtifact in assemblyPackageScala := false,
    assemblyMergeStrategy in assembly := {
      case "reference.conf" => MergeStrategy.concat
      case anyOther         => (assemblyMergeStrategy in assembly).value(anyOther)
    },
    pomPostProcess := { originalPom => {
      val shadedGroups = Seq("io.kamon")
      val filterShadedDependencies = new RuleTransformer(new RewriteRule {
        override def transform(n: Node): Seq[Node] = {
          if(n.label == "dependency") {
            val group = (n \ "groupId").text
            val artifact = (n \ "artifactId").text
            if (shadedGroups.find(eo => eo.equalsIgnoreCase(group)).nonEmpty && !artifact.startsWith("kamon-core")) Seq.empty else n
          } else n
        }
      })

      filterShadedDependencies(originalPom)
    }}
  )

lazy val kamonAndKanela = Seq(
  "io.kamon" %% "kamon-core"              % "1.2.0-b5a79846fc249b16c7bf4fd4219846e266ca1d40",
//  "io.kamon" %% "kamon-status-page"       % "1.2.0-b5a79846fc249b16c7bf4fd4219846e266ca1d40",
  "io.kamon" %  "kanela-agent"            % "0.0.16",
  "io.kamon" %% "kanela-kamon-extension"  % "0.1.0-f2ca8f6a26268fc72bc7d6624cc0a3df26926a53"
)

lazy val instrumentationModules = Seq(
  "io.kamon" %% "kamon-scala-future"      % "1.1.0-a1361a191c188b23485edda5da76e1c665c6b7fb",
  "io.kamon" %% "kamon-twitter-future"    % "1.1.0-a1361a191c188b23485edda5da76e1c665c6b7fb",
  "io.kamon" %% "kamon-scalaz-future"     % "1.1.0-a1361a191c188b23485edda5da76e1c665c6b7fb",
)



def withoutTransitiveDependencies(modules: Seq[ModuleID]): Seq[ModuleID] =
  modules.map(_.notTransitive())

val commonSettings = Seq(
  assemblyExcludedJars in assembly := {
    val cp = (fullClasspath in assembly).value
    val excludedPackages = Seq("slf4j-api", "config")
    cp filter { file => excludedPackages.exists(file.data.getName.startsWith(_)) }
  },
  packageBin in Compile := assembly.value,
  assemblyJarName in assembly := s"${moduleName.value}_${scalaBinaryVersion.value}-${version.value}.jar",
  pomPostProcess := { originalPom => {
    val shadedGroups = Seq("org.hdrhistogram", "org.jctools", "org.nanohttpd", "com.grack", "org.eclipse.collections")
    val filterShadedDependencies = new RuleTransformer(new RewriteRule {
      override def transform(n: Node): Seq[Node] = {
        if(n.label == "dependency") {
          val group = (n \ "groupId").text
          if (shadedGroups.find(eo => eo.equalsIgnoreCase(group)).nonEmpty) Seq.empty else n
        } else n
      }
    })

    filterShadedDependencies(originalPom)
  }}
)