package org.teckhooi.textdraw

import cats.effect.IO
import cats.effect.kernel.Ref
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.teckhooi.textdraw.Parsers.*
import org.teckhooi.textdraw.domain.{Dimension, ScreenBuffer}

class ParsersSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {
  val envIO: IO[(FakeScreen[IO], Ref[IO, ScreenBuffer])] = for {
    screen          <- IO.pure(new FakeScreen[IO]())
    screenBufferRef <- Ref.of[IO, ScreenBuffer](ScreenBuffer(List.fill(20 * 4)(' '), Dimension(20, 4)))
  } yield (screen, screenBufferRef)

  """String "L 1 1 10 10"""" - {
    "None" in {
      (for {
        (screen, screenBufferRef) <- envIO
        result <- BasicParsers
          .read[IO]
          .apply("L 1 1 10 10")
          .map(_(screenBufferRef)(screen))
          .fold(_ => IO.pure(none[String]), identity)
      } yield result).asserting(_ `shouldBe` none[String])
    }
  }

  "Foo 1 1 10 10" - {
      """"Bad command, Foo 1 1 10 10" message""" in {
        (for {
          (screen, screenBufferRef) <- envIO
          result <-BasicParsers.read[IO].apply("Foo 1 1 10 10")
            .map(_(screenBufferRef)(screen))
            .fold(t => IO.pure(t.getMessage.some), identity)
        } yield result).asserting(_ `shouldBe` "Not basic commands".some)
    }
  }
}
