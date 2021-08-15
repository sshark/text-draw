package org.teckhooi.textdraw

import cats.effect.kernel.Ref
import cats.effect.{IO, IOApp}
import cats.implicits.*
import org.teckhooi.textdraw.Parsers.{given, *}
import org.teckhooi.textdraw.domain.ScreenBuffer
import org.teckhooi.textdraw.feature.BucketFillParser
import org.teckhooi.textdraw.feature.BucketFillParser.given
import org.teckhooi.textdraw.syntax.Program

object Main extends IOApp.Simple {
  override def run: IO[Unit] =
    for {
      bufferRef     <- Ref.of[IO, ScreenBuffer](ScreenBuffer())
      lineSeparator <- IO(System.getProperty("line.separator"))
      // to remove grouping of 2 Alternatives restriction,
      // declare Parser as trait instead of type
      _ <- Program.run[IO](
        EmptyStringParser.read[IO] <+>
          (BucketFillParser.read[IO] <+>
            (BasicParsers.read[IO] <+> CatchAllParser.read[IO])),
        bufferRef,
        new ConsoleScreen[IO](lineSeparator)
      )
    } yield ()
}