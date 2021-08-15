package org.teckhooi.textdraw.feature

import cats.Monad
import cats.effect.kernel.Ref
import cats.effect.std.Console
import cats.syntax.all.*
import org.teckhooi.textdraw.ConsoleScreenUtils.drawOnScreen
import org.teckhooi.textdraw.Parsers.{DrawOnScreen, Parser}
import org.teckhooi.textdraw.Screen
import org.teckhooi.textdraw.domain.{Pos, ScreenBuffer}
import org.teckhooi.textdraw.feature.BucketFill.*

import scala.util.matching.Regex

object BucketFillParser {
  val BucketFillRegEx: Regex = "[Bb] +(\\d{1,2}) +(\\d{1,2}) +([a-z])".r

  def read[F[_]: Monad: Console]: Parser[DrawOnScreen[F]] = {
    case BucketFillRegEx(x1, y1, color) =>
      ((screenBufferRef: Ref[F, ScreenBuffer]) =>
        (screen: Screen[F]) =>
          Monad[F].ifM(screenBufferRef.get.map(_.buffer.isEmpty))(
            Console[F].println("No canvas found"),
            drawOnScreen(BucketFill(Pos(x1.toInt - 1, y1.toInt - 1), color.head),
                         screenBufferRef,
                         screen)) *> Monad[F].pure(none[String])).asRight[Throwable]

    case _ => new Exception("Incorrect parameters for bucket fill").asLeft[DrawOnScreen[F]]
  }
}
