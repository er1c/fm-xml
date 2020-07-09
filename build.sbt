import BuildKeys._
import Boilerplate._

// ---------------------------------------------------------------------------
// Commands

/* We have no other way to target only JVM or JS projects in tests. */
lazy val aggregatorIDs = Seq("core")

addCommandAlias("ci-jvm",     ";" + aggregatorIDs.map(id => s"${id}/clean ;${id}/test:compile ;${id}/test").mkString(";"))
addCommandAlias("ci-package", ";scalafmtCheckAll ;package")
addCommandAlias("ci-doc",     ";unidoc ;site/makeMicrosite")
addCommandAlias("ci",         ";project root ;reload ;+scalafmtCheckAll ;+ci-jvm ;+package ;ci-doc")
addCommandAlias("release",    ";+clean ;ci-release ;unidoc ;site/publishMicrosite")

// ---------------------------------------------------------------------------
// Dependencies

/** Library for unit-testing:
  *  - [[https://github.com/scalatest/scalatest]]
  */
val ScalaTestVersion = "3.2.0"

/** Used for publishing the microsite:
  * [[https://github.com/47degrees/github4s]]
  */
val GitHub4sVersion = "0.24.1"


/**
  * Defines common plugins between all projects.
  */
def defaultPlugins: Project ⇒ Project = pr => {
  val withCoverage = sys.env.getOrElse("SBT_PROFILE", "") match {
    case "coverage" => pr
    case _ => pr.disablePlugins(scoverage.ScoverageSbtPlugin)
  }
  withCoverage
    .enablePlugins(AutomateHeaderPlugin)
    .enablePlugins(GitBranchPrompt)
}

lazy val sharedSettings = Seq(
  projectTitle := "Scala FasterXML/woodstox XML Streaming Library",
  projectWebsiteRootURL := "https://er1c.github.io/",
  projectWebsiteBasePath := "/scala-woodstox/",
  githubOwnerID := "er1c",
  githubRelativeRepositoryID := "scala-woodstox",

  organization := "io.github.er1c",
  scalaVersion := "2.13.3",
  crossScalaVersions := Seq("2.11.12", "2.12.11", "2.13.3"),

  scalacOptions += "-language:implicitConversions",

  // Turning off fatal warnings for doc generation
  scalacOptions.in(Compile, doc) ~= filterConsoleScalacOptions,

  // ScalaDoc settings
  autoAPIMappings := true,
  scalacOptions in ThisBuild ++= Seq(
    // Note, this is used by the doc-source-url feature to determine the
    // relative path of a given source file. If it's not a prefix of a the
    // absolute path of the source file, the absolute path of that file
    // will be put into the FILE_SOURCE variable, which is
    // definitely not what we want.
    "-sourcepath", file(".").getAbsolutePath.replaceAll("[.]$", "")
  ),

  // https://github.com/sbt/sbt/issues/2654
  incOptions := incOptions.value.withLogRecompileOnMacro(false),

  // ---------------------------------------------------------------------------
  // Options for testing

  logBuffered in Test := false,
  logBuffered in IntegrationTest := false,

  // ---------------------------------------------------------------------------
  // Options meant for publishing on Maven Central

  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false }, // removes optional dependencies

  licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  homepage := Some(url(projectWebsiteFullURL.value)),
  headerLicense := Some(HeaderLicense.Custom(
    s"""|Copyright (c) 2019 Frugal Mechanic (http://frugalmechanic.com)
        |Copyright (c) 2020 the ${projectTitle.value} contributors.
        |See the project homepage at: ${projectWebsiteFullURL.value}
        |
        |Licensed under the Apache License, Version 2.0 (the "License");
        |you may not use this file except in compliance with the License.
        |You may obtain a copy of the License at
        |
        |    http://www.apache.org/licenses/LICENSE-2.0
        |
        |Unless required by applicable law or agreed to in writing, software
        |distributed under the License is distributed on an "AS IS" BASIS,
        |WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        |See the License for the specific language governing permissions and
        |limitations under the License."""
      .stripMargin)),

  scmInfo := Some(
    ScmInfo(
      url(s"https://github.com/${githubFullRepositoryID.value}"),
      s"scm:git@github.com:${githubFullRepositoryID.value}.git"
    )),

  developers := List(
    Developer(
      id="ericpeters",
      name="Eric Peters",
      email="eric@peters.org",
      url=url("https://github.com/er1c")
    ),
    Developer(
      id="tpunder",
      name="Tim Underwood",
      email="timunderwood@gmail.com",
      url=url("http://github.com/tpunder")
    )
  ),

  // -- Settings meant for deployment on oss.sonatype.org
  sonatypeProfileName := organization.value,
)

/**
  * Shared configuration across all sub-projects with actual code to be published.
  */
def defaultProjectConfiguration(pr: Project) = {
  pr.configure(defaultPlugins)
    .settings(sharedSettings)
    .settings(doctestTestSettings(DoctestTestFramework.ScalaTest))
    .settings(crossVersionSharedSources)
    .settings(filterOutMultipleDependenciesFromGeneratedPomXml(
      "groupId" -> "org.scoverage".r :: Nil,
      "groupId" -> "org.typelevel".r :: "artifactId" -> "simulacrum".r :: Nil,
    ))
}

lazy val root = project.in(file("."))
  .enablePlugins(ScalaUnidocPlugin)
  .configure(defaultPlugins)
  .disablePlugins(MimaPlugin)
  .aggregate(core)
  .settings(sharedSettings)
  .settings(doNotPublishArtifact)
  .settings(unidocSettings(core))
  .settings(
    // Try really hard to not execute tasks in parallel ffs
    Global / concurrentRestrictions := Tags.limitAll(1) :: Nil,
  )

lazy val core = project
  .in(file("core"))
  .configure(defaultProjectConfiguration)
  .disablePlugins(MimaPlugin)
  .settings(
    // Try really hard to not execute tasks in parallel ffs
    Global / concurrentRestrictions := Tags.limitAll(1) :: Nil,

    name := "scala-woodstox",
    description := "Scala XML Streaming Reader & Writer",
    scalaVersion := "2.13.3",
    crossScalaVersions := Seq("2.11.12", "2.12.11", "2.13.3"),
    scalacOptions := (if (!scalaVersion.value.startsWith("2.11")) Seq(
      // Scala 2.12 specific compiler flags
      "-opt:l:inline",
      "-opt-inline-from:<sources>"
    ) else Nil),
    libraryDependencies ++= Seq(
      "io.github.er1c" %% "fm-common" % "1.0.1",
      "io.github.er1c" %% "fm-lazyseq" % "1.0.0-RC1",
      //"com.frugalmechanic" %% "scala-optparse" % "1.1.3",
      "com.fasterxml.woodstox" % "woodstox-core" % "5.1.0",
      "com.sun.xml.bind" % "jaxb-core" % "2.3.0.1",          // JAXB - Needed for Java 9+ since it is no longer automatically available
      "com.sun.xml.bind" % "jaxb-impl" % "2.3.1",            // JAXB - Needed for Java 9+ since it is no longer automatically available
      "javax.xml.bind" % "jaxb-api" % "2.3.1",               // JAXB - Needed for Java 9+ since it is no longer automatically available
      "javax.activation" % "javax.activation-api" % "1.2.0", // JAXB - Needed for Java 9+ since it is no longer automatically available
      "org.scalatest" %% "scalatest" %  ScalaTestVersion % Test

    )
  )

lazy val site = project.in(file("site"))
  .disablePlugins(MimaPlugin)
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(MdocPlugin)
  .settings(sharedSettings)
  .settings(doNotPublishArtifact)
  .dependsOn(core)
  .settings {
    import microsites._
    Seq(
      micrositeName := projectTitle.value,
      micrositeDescription := "Scala XML Streaming Reader & Writers",
      micrositeAuthor := "Eric Peters",
      micrositeTwitterCreator := "@ericpeters",
      micrositeGithubOwner := githubOwnerID.value,
      micrositeGithubRepo := githubRelativeRepositoryID.value,
      micrositeUrl := projectWebsiteRootURL.value.replaceAll("[/]+$", ""),
      micrositeBaseUrl := projectWebsiteBasePath.value.replaceAll("[/]+$", ""),
      micrositeDocumentationUrl := s"${projectWebsiteFullURL.value.replaceAll("[/]+$", "")}/${docsMappingsAPIDir.value}/",
      micrositeGitterChannelUrl := githubFullRepositoryID.value,
      micrositeFooterText := None,
      micrositeHighlightTheme := "atom-one-light",
      micrositePalette := Map(
        "brand-primary" -> "#3e5b95",
        "brand-secondary" -> "#294066",
        "brand-tertiary" -> "#2d5799",
        "gray-dark" -> "#49494B",
        "gray" -> "#7B7B7E",
        "gray-light" -> "#E5E5E6",
        "gray-lighter" -> "#F4F3F4",
        "white-color" -> "#FFFFFF"
      ),
      micrositeCompilingDocsTool := WithMdoc,
      fork in mdoc := true,
      scalacOptions.in(Tut) ~= filterConsoleScalacOptions,
      libraryDependencies += "com.47deg" %% "github4s" % GitHub4sVersion,
      micrositePushSiteWith := GitHub4s,
      micrositeGithubToken := sys.env.get("GITHUB_TOKEN"),
      micrositeExtraMdFilesOutput := (resourceManaged in Compile).value / "jekyll",
      micrositeExtraMdFiles := Map(
        file("README.md") -> ExtraMdFileConfig("index.md", "page", Map("title" -> "Home", "section" -> "home", "position" -> "100")),
        file("CHANGELOG.md") -> ExtraMdFileConfig("CHANGELOG.md", "page", Map("title" -> "Change Log", "section" -> "changelog", "position" -> "101")),
        file("CODE_OF_CONDUCT.md") -> ExtraMdFileConfig("CODE_OF_CONDUCT.md", "page", Map("title" -> "Code of Conduct", "section" -> "code of conduct", "position" -> "102")),
        file("LICENSE.md") -> ExtraMdFileConfig("LICENSE.md", "page", Map("title" -> "License", "section" -> "license", "position" -> "103"))
      ),
      docsMappingsAPIDir := s"api",
      addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc) in root, docsMappingsAPIDir),
      sourceDirectory in Compile := baseDirectory.value / "src",
      sourceDirectory in Test := baseDirectory.value / "test",
      mdocIn := (sourceDirectory in Compile).value / "mdoc",

      // Bug in sbt-microsites
      micrositeConfigYaml := microsites.ConfigYml(
        yamlCustomProperties = Map("exclude" -> List.empty[String])
      ),
    )
  }
