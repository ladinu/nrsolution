package org.ladinu

import cats.effect.IO
import cats.implicits.{catsSyntaxEq, toFunctorFilterOps}
import fs2._

trait PhraseCount {

  def isHyphen(chr: Char): Boolean = chr === '-'

  def phraseCountStream: Pipe[IO, String, List[((String, String, String), Int)]] = in =>
    in.map(line => if (line.lastOption.exists(isHyphen)) line.dropRight(1) else line)
      // Split line into words
      .flatMap(line => Stream.fromIterator[IO](line.split(" ").iterator, 100))
      // Remove punctuations
      .map(w => w.replaceAll("\\p{Punct}", ""))
      // Remove unicode quotation
      .map(w => w.replaceAll("“", "").replace("”", ""))
      // Ignore whitespaces
      .filter(a => !a.isBlank)
      // Lowercase & trim
      .map(_.toLowerCase.trim)
      // Sliding window of 3 words
      .sliding(3)
      .map { chunk =>
        chunk.toList match {
          case a :: b :: c :: Nil => Some((a, b, c))
          case _                  => None
        }
      }
      .flattenOption
      .fold(Map.empty[(String, String, String), Int]) { (map, wordTriple) =>
        map + (wordTriple -> (map.getOrElse(wordTriple, 0) + 1))
      }
      .map(_.toList.sortBy(_._2).takeRight(100).reverse)

  def fmtCount: Pipe[IO, List[((String, String, String), Int)], String] = in =>
    in.map { counts =>
      counts
        .map { case ((a, b, c), count) => s"$a $b $c - $count" }
        .mkString("\n")
    }
}
