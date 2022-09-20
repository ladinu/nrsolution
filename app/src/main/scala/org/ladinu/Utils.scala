package org.ladinu

import cats.effect.IO
import fs2._
import fs2.Pipe
import fs2.io.file.{Files, Path}

trait Utils {

  def streamFile(path: String): Stream[IO, Byte] = Files[IO].readAll(Path(path))

  def utf8Lines: Pipe[IO, Byte, String] = in => in.through(text.utf8.decode).through(text.lines)
}
