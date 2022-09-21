package org.ladinu

import cats.effect.{ExitCode, IO, IOApp}
import fs2._
import cats.implicits._

object Tests extends IOApp with PhraseCount with Utils {

  def byteStream(strs: String*): Stream[IO, Byte] =
    Stream
      .fromIterator[IO](
        strs.map { str =>
          Stream.fromIterator[IO](s"$str\n".getBytes.iterator, 100)
        }.iterator,
        100
      )
      .parJoinUnbounded

  val removePunctuations: IO[Boolean] =
    byteStream("I love\nsandwiches.", "(I LOVE SANDWICHES!?!?)")
      .through(utf8Lines)
      .through(phraseCountStream)
      .map(_.headOption.map(_._2).getOrElse(0))
      .compile
      .toList
      .map(_.headOption.exists(_ == 2))

  val handleUnicodeQuotes: IO[Boolean] =
    byteStream("I love \"sandwiches\"", "I love “sandwiches“")
      .through(utf8Lines)
      .through(phraseCountStream)
      .map(_.headOption.map(_._2).getOrElse(0))
      .compile
      .toList
      .map(_.headOption.exists(_ == 2))

  val properlyFormatOutput: IO[Boolean] = byteStream(
    "I like cats & dogs."
  )
    .through(utf8Lines)
    .through(phraseCountStream)
    .through(fmtCount)
    .compile
    .toList
    .map(_.headOption.exists { actual =>
      actual ==
        """like cats dogs - 1
        |i like cats - 1""".stripMargin
    })

  val handleEmptyStream: IO[Boolean] = byteStream()
    .through(utf8Lines)
    .through(phraseCountStream)
    .compile
    .toList
    .map(_.headOption.exists(_.isEmpty))

  override def run(args: List[String]): IO[ExitCode] = {

    def test(name: String, op: IO[Boolean]): IO[Unit] = op
      .flatMap {
        case true => IO.println(s"$name -> ✅")
        case _    => IO.println(s"$name -> ❌")
      }

    List(
      "Ignore punctuations" -> removePunctuations,
      "Handle unicode quotes" -> handleUnicodeQuotes,
      "Properly format output" -> properlyFormatOutput,
      "Handle empty streams/files" -> handleEmptyStream
    )
      .map(a => test(a._1, a._2))
      .parSequence
      .as(ExitCode.Success)
  }
}
