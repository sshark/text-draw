package org.teckhooi.textdraw

import cats.Monad
import cats.effect.std.Console
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Ref}
import cats.syntax.all.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.teckhooi.textdraw.domain.{Dimension, Line, Pixel, Pos, Rectangle, ScreenBuffer}

class FakeScreen[F[_]: Console: Monad]() extends Screen[F] {
  override def draw(points: List[Pos], color: Char, screenBufferRef: Ref[F, ScreenBuffer]): F[Unit] =
    for {
      _ <- screenBufferRef.updateAndGet(
        screenBuffer =>
          screenBuffer.copy(
            buffer = points
              .filter(p => withinScreenDim(p, screenBuffer.dim))
              .foldLeft(screenBuffer.buffer)((buffer, pos) =>
                buffer.updated(pos.x + pos.y * screenBuffer.dim.width, color))))
    } yield ()

  private def withinScreenDim(pos: Pos, dim: Dimension): Boolean =
    pos.x < dim.width && pos.y < dim.height && pos.x >= 0 && pos.y >= 0
}

class CoreShapesDrawOnCanvasSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  val envIO: IO[(String, FakeScreen[IO], Ref[IO, ScreenBuffer])] = for {
    lineSeparator   <- IO.delay(System.getProperty("line.separator"))
    screen          <- IO.pure(new FakeScreen[IO]())
    screenBufferRef <- Ref.of[IO, ScreenBuffer](ScreenBuffer(List.fill(20 * 4)(' '), Dimension(20, 4)))
  } yield (lineSeparator, screen, screenBufferRef)

  "2 '@'s are drawn at (1,1) and (20,4)" - {
    """
      |----------------------
      ||@                   |
      ||                    |
      ||                    |
      ||                   @|
      |----------------------""".stripMargin in {

      val outputIO: IO[String] = for {
        (lineSeparator, screen, screenBufferRef) <- envIO
        _                                        <- ConsoleScreenUtils.drawOnScreen(Pixel(Pos(19, 3), '@'), screenBufferRef, screen)
        _                                        <- ConsoleScreenUtils.drawOnScreen(Pixel(Pos(0, 0), '@'), screenBufferRef, screen)
        result <- screenBufferRef.modify(screenBuffer =>
          (screenBuffer, ConsoleScreenUtils.drawBorderAsString(screenBuffer.buffer, screenBuffer.dim, lineSeparator)))
      } yield result

      outputIO.asserting(
        _ `shouldBe`
          """----------------------
             ||@                   |
             ||                    |
             ||                    |
             ||                   @|
             |----------------------""".stripMargin)
    }
  }

  "A horizontal line is drawn" - {
    """
      |----------------------
      ||                    |
      ||xxxxxx              |
      ||                    |
      ||                    |
      |----------------------""".stripMargin in {
      val outputIO: IO[String] = for {
        (lineSeparator, screen, screenBufferRef) <- envIO
        _                                        <- ConsoleScreenUtils.drawOnScreen(Line(Pos(0, 1), Pos(5, 1)), screenBufferRef, screen)
        result <- screenBufferRef.modify(screenBuffer =>
          (screenBuffer, ConsoleScreenUtils.drawBorderAsString(screenBuffer.buffer, screenBuffer.dim, lineSeparator)))
      } yield result

      outputIO.asserting(
        _ `shouldBe`
          """----------------------
            ||                    |
            ||xxxxxx              |
            ||                    |
            ||                    |
            |----------------------""".stripMargin)
    }
  }

  "A vertical line, a horizontal lines and, a rectangle are drawn" - {
    """
      |----------------------
      ||             xxxxx  |
      ||xxxxxx       x   x  |
      ||     x       xxxxx  |
      ||     x              |
      |----------------------""".stripMargin in {
      val outputIO = for {
        (lineSeparator, screen, screenBufferRef) <- envIO
        _                                        <- ConsoleScreenUtils.drawOnScreen(Rectangle(Pos(13, 0), Pos(17, 2)), screenBufferRef, screen)
        _                                        <- ConsoleScreenUtils.drawOnScreen(Line(Pos(5, 2), Pos(5, 3)), screenBufferRef, screen)
        _                                        <- ConsoleScreenUtils.drawOnScreen(Line(Pos(0, 1), Pos(5, 1)), screenBufferRef, screen)
        result <- screenBufferRef.modify(screenBuffer =>
          (screenBuffer, ConsoleScreenUtils.drawBorderAsString(screenBuffer.buffer, screenBuffer.dim, lineSeparator)))
      } yield result

      outputIO.asserting(
        _ `shouldBe`
          """----------------------
            ||             xxxxx  |
            ||xxxxxx       x   x  |
            ||     x       xxxxx  |
            ||     x              |
            |----------------------""".stripMargin)
    }
  }
}
