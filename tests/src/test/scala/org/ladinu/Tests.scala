package org.ladinu

import cats.effect.{ExitCode, IO, IOApp}
import fs2._
import cats.implicits._

object Tests extends IOApp with PhraseCount with Utils {

  def byteStream(strs: String*): Stream[IO, Byte] =
    Stream
      .fromIterator[IO](
        strs
          .map { str =>
            Stream.fromIterator[IO](s"$str\n".getBytes.iterator, 100)
          }
          .iterator,
        100
      )
      .parJoinUnbounded

  val removePunctuations: IO[Boolean] =
    byteStream("I love\nsandwiches.", "(I LOVE SANDWICHES!!)")
      .through(utf8Lines)
      .through(phraseCountStream)
      .map(_.headOption.map(_._2).getOrElse(0))
      .compile
      .toList
      .map(_.headOption.exists(_ == 2))

  override def run(args: List[String]): IO[ExitCode] = {

    def test(name: String, op: IO[Boolean]): IO[Unit] = op
      .flatMap {
        case true => IO.println(s"$name -> ✅")
        case _    => IO.println(s"$name -> ❌")
      }

    List(
      "Ignore punctuations" -> removePunctuations
    )
      .map(a => test(a._1, a._2))
      .parSequence
      .as(ExitCode.Success)
  }
}
