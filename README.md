# sbt-gulp
SBT plugin for auto compilation of Gulp.js assets

# Usage
In `project/plugins.sbt`:
```scala
addSbtPlugin("com.github.karasiq" % "sbt-gulp" % "1.0.1")
```

In `build.sbt`:
```scala
enablePlugins(GulpPlugin)
```

# Settings
Default settings, can be overriden
```scala
gulpAssets in Compile := file("webapp")

gulpTask in Compile := "compile"

gulpOutput in Compile := gulpAssets.value / "out"

gulpDest in Compile := resourceManaged.value / "webapp"
```

# Using with Scala.js
Add dependency in `build.sbt`:
```scala
gulpCompile in Compile <<= (gulpCompile in Compile).dependsOn(fullOptJS in Compile in yourScalaJsProject)
```
