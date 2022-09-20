package org.ladinu

import cats.effect.{ExitCode, IO, IOApp}
import fs2.{io, _}

object Main extends IOApp with PhraseCount with Utils {

  override def run(args: List[String]): IO[ExitCode] = {
    val stream = if (args.nonEmpty) {
      // Read the files line-by-line
      Stream
        .fromIterator[IO](args.map(streamFile).iterator, 10)
        .parJoinUnbounded
        .through(utf8Lines)
    } else {
      // If there are no args, read from stdin
      io.stdin[IO](1e6.toInt)
        .through(utf8Lines)
    }

    for {
      _ <- stream
        .through(phraseCountStream)
        .through(fmtCount)
        .evalMap(IO.println)
        .compile
        .drain
    } yield ExitCode.Success
  }

}
