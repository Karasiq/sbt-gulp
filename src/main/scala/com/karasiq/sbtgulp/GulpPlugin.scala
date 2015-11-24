package com.karasiq.sbtgulp

import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object GulpPlugin extends AutoPlugin {
  object autoImport {
    val gulpAssets = settingKey[File]("Gulp.js assets directory")
    val gulpOutput = settingKey[File]("Gulp.js output directory")
    val gulpDest = settingKey[File]("Gulp.js output resources directory")
    val gulpCompile = taskKey[Seq[File]]("Compile gulp.js project")

    lazy val baseGulpSettings: Seq[Def.Setting[_]] = Seq(
      gulpAssets := file("webapp"),
      gulpOutput := gulpAssets.value / "out",
      gulpDest := resourceManaged.value / "webapp",
      gulpCompile <<= (gulpAssets, gulpOutput, gulpDest, streams).map { (src, out, dest, streams) ⇒
        Gulp.compile(src, out, dest)(streams)
      },
      managedResources ++= gulpCompile.value
    )
  }

  import autoImport._

  override def requires: Plugins = JvmPlugin

  override val projectSettings = inConfig(Compile)(baseGulpSettings)
}
