package org.teckhooi.textdraw.feature

import cats.effect.IO
import cats.effect.kernel.Ref
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.teckhooi.textdraw.domain.*
import org.teckhooi.textdraw.feature.BucketFill.BucketFillDrawable
import org.teckhooi.textdraw.{ConsoleScreenUtils, Drawable, FakeScreen}

class BucketFillSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "A drawn canvas is bucket filled with 'o'" - {
    """
      |----------------------
      ||oooooooooooooxxxxxoo|
      ||xxxxxxooooooox   xoo|
      ||     xoooooooxxxxxoo|
      ||     xoooooooooooooo|
      |----------------------""".stripMargin in {
      val outputIO = for {
        (lineSeparator, screen, screenBufferRef) <- envIO
        _                                        <- ConsoleScreenUtils.drawOnScreen(Rectangle(Pos(13, 0), Pos(17, 2)), screenBufferRef, screen)
        _                                        <- ConsoleScreenUtils.drawOnScreen(Line(Pos(5, 2), Pos(5, 3)), screenBufferRef, screen)
        _                                        <- ConsoleScreenUtils.drawOnScreen(Line(Pos(0, 1), Pos(5, 1)), screenBufferRef, screen)
        _                                        <- ConsoleScreenUtils.drawOnScreen(BucketFill(Pos(9, 2), 'o', Dimension(20, 4)), screenBufferRef, screen)
        result <- screenBufferRef.modify(screenBuffer =>
          (screenBuffer, ConsoleScreenUtils.drawBorderAsString(screenBuffer.buffer, screenBuffer.dim, lineSeparator)))
      } yield result

      outputIO.asserting(
        _ `shouldBe`
          """----------------------
            ||oooooooooooooxxxxxoo|
            ||xxxxxxooooooox   xoo|
            ||     xoooooooxxxxxoo|
            ||     xoooooooooooooo|
            |----------------------""".stripMargin)
    }
  }

  "A drawn canvas is bucket filled with 'b' within rectangle 'x'" - {
    """
      |----------------------
      ||                    |
      || xxxxxxxxxxxxxxxxx  |
      || xbbbbbbbbbbbbbbbx  |
      || xxxxxxxxxxxxxxxxx  |
      |----------------------""".stripMargin in {
      val outputIO = for {
        (lineSeparator, screen, screenBufferRef) <- envIO
        _                                        <- ConsoleScreenUtils.drawOnScreen[Rectangle, IO](Rectangle(Pos(1, 1), Pos(17, 3)), screenBufferRef, screen)
        _ <- ConsoleScreenUtils.drawOnScreen[BucketFill, IO](BucketFill(Pos(9, 2), 'b', Dimension(20, 4)),
                                                             screenBufferRef,
                                                             screen)
        result <- screenBufferRef.modify(screenBuffer =>
          (screenBuffer, ConsoleScreenUtils.drawBorderAsString(screenBuffer.buffer, screenBuffer.dim, lineSeparator)))
      } yield result

      outputIO.asserting(
        _ `shouldBe`
          """----------------------
            ||                    |
            || xxxxxxxxxxxxxxxxx  |
            || xbbbbbbbbbbbbbbbx  |
            || xxxxxxxxxxxxxxxxx  |
            |----------------------""".stripMargin)
    }
  }

  "A drawn canvas is bucket filled with 'b' outside of rectangle 'x'" - {
    """
      |----------------------
      ||bbbbbbbbbbbbbbbbbbbb|
      ||bxxxxxxxxxxxxxxxxxbb|
      ||bx               xbb|
      ||bxxxxxxxxxxxxxxxxxbb|
      |----------------------""".stripMargin in {
      val outputIO = for {
        (lineSeparator, screen, screenBufferRef) <- envIO
        _                                        <- ConsoleScreenUtils.drawOnScreen[Rectangle, IO](Rectangle(Pos(1, 1), Pos(17, 3)), screenBufferRef, screen)
        _ <- ConsoleScreenUtils.drawOnScreen[BucketFill, IO](BucketFill(Pos(0, 0), 'b', Dimension(20, 4)),
                                                             screenBufferRef,
                                                             screen)
        result <- screenBufferRef.modify(screenBuffer =>
          (screenBuffer, ConsoleScreenUtils.drawBorderAsString(screenBuffer.buffer, screenBuffer.dim, lineSeparator)))
      } yield result

      outputIO.asserting(
        _ `shouldBe`
          """----------------------
            ||bbbbbbbbbbbbbbbbbbbb|
            ||bxxxxxxxxxxxxxxxxxbb|
            ||bx               xbb|
            ||bxxxxxxxxxxxxxxxxxbb|
            |----------------------""".stripMargin)
    }
  }

  "A drawn canvas is bucket filled on top of rectangle 'x'" - {
    """
      |----------------------
      ||                    |
      || xxxxxxxxxxxxxxxxx  |
      || x               x  |
      || xxxxxxxxxxxxxxxxx  |
      |----------------------""".stripMargin in {
      val outputIO = for {
        (lineSeparator, screen, screenBufferRef) <- envIO
        _                                        <- ConsoleScreenUtils.drawOnScreen[Rectangle, IO](Rectangle(Pos(1, 1), Pos(17, 3)), screenBufferRef, screen)
        _ <- ConsoleScreenUtils.drawOnScreen[BucketFill, IO](BucketFill(Pos(10, 1), 'b', Dimension(20, 4)),
                                                             screenBufferRef,
                                                             screen)
        result <- screenBufferRef.modify(screenBuffer =>
          (screenBuffer, ConsoleScreenUtils.drawBorderAsString(screenBuffer.buffer, screenBuffer.dim, lineSeparator)))
      } yield result

      outputIO.asserting(
        _ `shouldBe`
          """----------------------
            ||                    |
            || xxxxxxxxxxxxxxxxx  |
            || x               x  |
            || xxxxxxxxxxxxxxxxx  |
            |----------------------""".stripMargin)
    }
  }

  val envIO: IO[(String, FakeScreen[IO], Ref[IO, ScreenBuffer])] = for {
    lineSeparator   <- IO.delay(System.getProperty("line.separator"))
    screen          <- IO.pure(new FakeScreen[IO]())
    screenBufferRef <- Ref.of[IO, ScreenBuffer](ScreenBuffer(List.fill(20 * 4)(' '), Dimension(20, 4)))
  } yield (lineSeparator, screen, screenBufferRef)
}
