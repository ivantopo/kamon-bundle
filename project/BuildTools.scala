import sbt.Keys._
import sbtassembly.AssemblyPlugin.autoImport.{assembly, assemblyExcludedJars, assemblyJarName}

import scala.xml.Node
import scala.xml.transform.{RewriteRule, RuleTransformer}

object BuildTools {
  
  def excludeFromClasspath(cp: Classpath, exclusions: Seq[String]): Classpath = {
    cp filter { file => exclusions.exists(file.data.getName.startsWith(_)) }
  }

//  assemblyExcludedJars in assembly := {
//    val cp = (fullClasspath in assembly).value
//    val excludedPackages = Seq("slf4j-api", "config")
//    cp filter { file => excludedPackages.exists(file.data.getName.startsWith(_)) }
//  }
//
//  assemblyJarName in assembly := s"${moduleName.value}_${scalaBinaryVersion.value}-${version.value}.jar",
//  pomPostProcess := { originalPom => {
//    val shadedGroups = Seq("org.hdrhistogram", "org.jctools", "org.nanohttpd", "com.grack", "org.eclipse.collections")
//    val filterShadedDependencies = new RuleTransformer(new RewriteRule {
//      override def transform(n: Node): Seq[Node] = {
//        if(n.label == "dependency") {
//          val group = (n \ "groupId").text
//          if (shadedGroups.find(eo => eo.equalsIgnoreCase(group)).nonEmpty) Seq.empty else n
//        } else n
//      }
//    })
//
//    filterShadedDependencies(originalPom)
//  }}

}
