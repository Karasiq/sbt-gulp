package com.karasiq.sbtgulp

import sbt.{Def, _}
import sbt.Keys._
import sbt.plugins.JvmPlugin

object GulpPlugin extends AutoPlugin {
  object autoImport {
    val gulpAssets = settingKey[File]("Gulp.js assets directory")
    val gulpTask = settingKey[String]("Gulp.js task name")
    val gulpOutput = settingKey[File]("Gulp.js output directory")
    val gulpDest = settingKey[File]("Gulp.js output resources directory")
    val gulpCompile = taskKey[Seq[File]]("Compile gulp.js project")

    lazy val baseGulpSettings: Seq[Def.Setting[_]] = Seq(
      gulpAssets := file("webapp"),
      gulpTask := "compile",
      gulpOutput := gulpAssets.value / "out",
      gulpDest := resourceManaged.value / "webapp",
      gulpCompile := {
        GulpLauncher.compile(gulpAssets.value, gulpTask.value, gulpOutput.value, gulpDest.value)(streams.value)
      },
      managedResources ++= gulpCompile.value
    )
  }

  import autoImport._

  override def requires: Plugins = JvmPlugin

  override val projectSettings: Seq[Def.Setting[_]] = inConfig(Compile)(baseGulpSettings)
}
