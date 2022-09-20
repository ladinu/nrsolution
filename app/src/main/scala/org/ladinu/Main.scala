package org.ladinu

import cats.effect.{ExitCode, IO, IOApp}
import fs2.{io, _}

object Main extends IOApp with PhraseCount with Utils {

  override def run(args: List[String]): IO[ExitCode] = {
    val stream = if (args.nonEmpty) {
      Stream
        .fromIterator[IO](args.map(streamFile).iterator, 5)
        .parJoinUnbounded
        .through(utf8Lines)
    } else {
      io.stdin[IO](100)
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
