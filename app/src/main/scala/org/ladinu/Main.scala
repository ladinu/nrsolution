package org.ladinu

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import fs2._
import fs2.io.file.{Files, Path}

object Main extends IOApp {

  def isHyphen(chr: Char): Boolean = chr === '-'

  def lines =
    Files[IO]
      .readAll(Path("/Users/ladinu/Downloads/brothers-karamazov.txt"))
      .through(text.utf8.decode)
      .through(text.lines)
      // Remove non-unicode line-ending hyphens
      .map(line => if (line.lastOption.exists(isHyphen)) line.dropRight(1) else line)
      // Split line into words
      .flatMap(line => Stream.fromIterator[IO](line.split(" ").iterator, 100))
      // Remove punctuations
      .map(w => w.replaceAll("\\p{Punct}", ""))
      // Remove unicode quotation
      .map(w => w.replaceAll("“", "").replace("”", ""))
      // Ignore whitespaces
      .filterNot(_.isBlank)
      // Lowercase & trim
      .map(_.toLowerCase.trim)
      // Sliding window of 3 words
      .sliding(3)
      .fold(Map.empty[String, Int]) { (map, chunk) =>
        val key = chunk.mkString_("")

      }
      .evalMap(a => IO.println(a.mkString_("")))
      .take(100)

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- lines.compile.drain
    } yield ExitCode.Success

}
