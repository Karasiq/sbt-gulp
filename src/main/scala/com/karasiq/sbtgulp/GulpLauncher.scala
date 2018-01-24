package com.karasiq.sbtgulp

import java.io.IOException
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import scala.collection.mutable.ListBuffer
import scala.sys.process.Process

import sbt._
import sbt.std.TaskStreams

/**
  * SBT helper object for gulp.js
  */
object GulpLauncher {
  /**
    * Checks if current OS is Windows
    * @return Is SBT running on Windows
    */
  private def isWindows: Boolean = {
    Option(System.getProperty("os.name"))
      .map(_.toLowerCase)
      .exists(_.startsWith("windows"))
  }

  /**
    * Installs required Node.JS packages
    * @param dir Destination directory
    * @note Requires NPM executable in path
    */
  def npmInstall(dir: File)(implicit streams: TaskStreams[ScopedKey[_]]): Unit = {
    require((dir / "package.json").exists(), "NPM package file not found")
    streams.log.info("Installing required node.js packages...")
    val process = if (isWindows) {
      Process(Seq("cmd", "/c", "npm install"), dir.getAbsoluteFile)
    } else {
      Process(Seq("npm", "install"), dir.getAbsoluteFile)
    }
    assert(process.!< == 0, "NPM packages installation failed")
  }

  /**
    * Gulp launcher path
    * @param dir Gulp project directory
    * @return Path to gulp launcher executable
    */
  def gulpScript(dir: File)(implicit streams: TaskStreams[ScopedKey[_]]): String = {
    require((dir / "gulpfile.js").exists(), "Gulp file not found")

    if (!(dir / "node_modules").exists()) {
      // Install Node.JS packages first
      npmInstall(dir)
    }

    val gulpScript = dir / "node_modules" / ".bin" / (if (isWindows) "gulp.cmd" else "gulp")
    require(gulpScript.exists(), "Gulp executable not found")
    gulpScript.getAbsolutePath
  }

  /**
    * Compiles Gulp.js project
    * @param source Gulp assets directory
    * @param task Gulp task name
    * @param out Gulp output directory
    * @param dest Output directory
    * @param streams SBT IO streams
    * @return List of produced files
    */
  def compile(source: File, task: String, out: File, dest: File)(implicit streams: TaskStreams[ScopedKey[_]]): Seq[File] = {
    // Launch compilation process
    streams.log.info(s"Compiling gulp assets in ${source.getAbsolutePath}...")
    val process = Process(Seq(gulpScript(source), task), source.getAbsoluteFile)
    assert(process.!< == 0, "Gulp.js project compilation failed")

    // Move directory
    val outDir = out.getAbsoluteFile.toPath
    val destDir = dest.getAbsoluteFile.toPath

    streams.log.info(s"Moving files from $outDir to $destDir...")
    if (Files.isSymbolicLink(destDir) && Files.isRegularFile(destDir)) {
      Files.delete(destDir)
    } else if (Files.isDirectory(destDir)) {
      Files.walkFileTree(destDir, new SimpleFileVisitor[Path] {
        override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
          Files.delete(file)
          FileVisitResult.CONTINUE
        }

        override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
          Files.delete(dir)
          FileVisitResult.CONTINUE
        }
      })
    }
    Files.createDirectories(destDir.getParent)
    Files.move(outDir, destDir)

    // Create file list
    val buffer = new ListBuffer[File]()
    Files.walkFileTree(destDir, new SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        buffer += file.toFile
        FileVisitResult.CONTINUE
      }
    })
    buffer.result()
  }
}