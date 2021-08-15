package org.teckhooi.textdraw.syntax

import cats.Monad
import cats.effect.kernel.Ref
import cats.effect.std.Console
import cats.syntax.all.*
import org.teckhooi.textdraw.Parsers.{DrawOnScreen, Parser}
import org.teckhooi.textdraw.Screen
import org.teckhooi.textdraw.domain.*

object Program {

  def run[F[_]: Monad: Console](parsers: Parser[DrawOnScreen[F]], bufferRef: Ref[F, ScreenBuffer], ev: Screen[F]): F[Unit] =
    for {
      _         <- Console[F].print("Command >> ")
      rawInput  <- Console[F].readLine
      resultOpt <- eval(parsers, rawInput, bufferRef, ev)
      _         <- resultOpt.filter(_ == "QUIT").fold(run(parsers, bufferRef, ev))(_ => Console[F].println("Quit"))
    } yield ()

  private def eval[F[_]: Console: Monad, A](parsers: Parser[DrawOnScreen[F]],
                                            action: String,
                                            screenRef: Ref[F, ScreenBuffer],
                                            screen: Screen[F]): F[Option[String]] =
    parsers(action)
      .fold(ex => Console[F].println(s"${ex.getMessage}") *> Monad[F].pure(None), f => f(screenRef)(screen))
}
