package org.teckhooi.textdraw

import cats.InvariantSemigroupal.map2
import cats.effect.IO
import cats.effect.kernel.Ref
import cats.effect.std.Console
import cats.implicits.*
import cats.{Alternative, Monad}
import org.teckhooi.textdraw.ConsoleScreenUtils.drawOnScreen
import org.teckhooi.textdraw.domain.{Dimension, Line, Pos, Rectangle, ScreenBuffer, TextCanvas}

import scala.util.matching.Regex

object Parsers {
  type DrawOnScreen[F[_]] = Ref[F, ScreenBuffer] => Screen[F] => F[Option[String]]
  type Parser[A]          = String => Either[Throwable, A]

  given Alternative[Parser] with
    def empty[A]: Parser[A] = Function.const((new Exception("Bad command").asLeft[A]))

    def pure[A](x: A): Parser[A] = _ => Right(x)

    def combineK[A](x: Parser[A], y: Parser[A]): Parser[A] =
      z =>
        x(z) match {
          case Left(_) => y(z)
          case parser => parser
        }

    def ap[A, B](ff: Parser[A => B])(fa: Parser[A]): Parser[B] = map2(fa, ff)((a, f) => f(a))

  object BasicParsers {
    val LineRegEx: Regex      = "[Ll] +(\\d{1,2}) +(\\d{1,2}) +(\\d{1,2}) +(\\d{1,2})".r
    val RectangleRegEx: Regex = "[Rr] +(\\d{1,2}) +(\\d{1,2}) +(\\d{1,2}) +(\\d{1,2})".r
    val CanvasRegEx: Regex    = "[Cc] +(\\d{1,2}) +(\\d{1,2})".r
    val QuitRegEx: Regex      = "[Qq]".r

    def read[F[_]: Monad: Console]: Parser[DrawOnScreen[F]] = {
      case LineRegEx(x1, y1, x2, y2) =>
        ((screenBufferRef: Ref[F, ScreenBuffer]) =>
          (screen: Screen[F]) =>
            Monad[F].ifM(screenBufferRef.get.map(_.buffer.isEmpty))(
              Console[F].println("No canvas found"),
              drawOnScreen(Line(Pos(x1.toInt - 1, y1.toInt - 1), Pos(x2.toInt - 1, y2.toInt - 1)),
                           screenBufferRef,
                           screen)) *>
              Monad[F].pure(none[String])).asRight[Throwable]
      case RectangleRegEx(x1, y1, x2, y2) =>
        ((screenBufferRef: Ref[F, ScreenBuffer]) =>
          (screen: Screen[F]) =>
            Monad[F].ifM(screenBufferRef.get.map(_.buffer.isEmpty))(
              Console[F].println("No canvas found"),
              drawOnScreen(Rectangle(Pos(x1.toInt - 1, y1.toInt - 1), Pos(x2.toInt - 1, y2.toInt - 1)),
                           screenBufferRef,
                           screen)) *> Monad[F].pure(none[String])).asRight[Throwable]
      case CanvasRegEx(w, h) =>
        ((screenRef: Ref[F, ScreenBuffer]) =>
          (screen: Screen[F]) => {
            val (width, height) = (w.toInt, h.toInt)
            screenRef.update(_ => ScreenBuffer(List.fill(width * height)(' '), Dimension(width, height))) *>
              drawOnScreen(TextCanvas, screenRef, screen) *>
              Monad[F].pure(none[String])
          }).asRight[Throwable]

      case QuitRegEx() => ((_: Ref[F, ScreenBuffer]) => (_: Screen[F]) => Monad[F].pure("QUIT".some)).asRight[Throwable]
      case _           => new Exception("Not basic commands").asLeft[DrawOnScreen[F]]
    }
  }

  object EmptyStringParser {
    val EmptyStringRegEx: Regex = "^$".r

    def read[F[_]: Monad: Console]: Parser[DrawOnScreen[F]] = {
      case EmptyStringRegEx() =>
        ((_: Ref[F, ScreenBuffer]) => (_: Screen[F]) => Monad[F].pure(none[String])).asRight[Throwable]
      case _ => new Exception("Not blank input").asLeft[DrawOnScreen[F]]
    }
  }

  object CatchAllParser {
    val CatchAllRegEx: Regex = "(.*)".r

    def read[F[_]: Monad: Console]: Parser[DrawOnScreen[F]] = {
      case CatchAllRegEx(args) => new Exception(s"Bad command, $args").asLeft[DrawOnScreen[F]]
      case _                   => new Exception("Not catch all").asLeft[DrawOnScreen[F]]
    }
  }
}
