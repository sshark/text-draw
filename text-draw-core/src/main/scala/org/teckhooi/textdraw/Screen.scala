package org.teckhooi.textdraw

import cats.Monad
import cats.effect.Ref
import cats.effect.std.Console
import cats.syntax.all.*
import org.teckhooi.textdraw.domain.{Dimension, Pos, ScreenBuffer}

import scala.language.{existentials, implicitConversions}

trait Screen[F[_]] {
  def draw(points: List[Pos], color: Char, bufferRef: Ref[F, ScreenBuffer]): F[Unit]
}

object Screen {
  implicit def apply[F[_]](implicit ev: Screen[F]): Screen[F] = ev
}

class ConsoleScreen[F[_]: Console: Monad](lineSeparator: String) extends Screen[F] {
  override def draw(points: List[Pos], color: Char, screenBufferRef: Ref[F, ScreenBuffer]): F[Unit] =
    for {
      screenBuffer <- screenBufferRef.updateAndGet(
        screenBuffer =>
          screenBuffer.copy(
            buffer = points
              .filter(p => withinScreenDim(p, screenBuffer.dim))
              .foldLeft(screenBuffer.buffer)((buffer, pos) =>
                buffer.updated(pos.x + pos.y * screenBuffer.dim.width, color))))
      _ <- Console[F].println(
        ConsoleScreenUtils.drawBorderAsString(screenBuffer.buffer, screenBuffer.dim, lineSeparator))
    } yield ()

  private def withinScreenDim(pos: Pos, dim: Dimension): Boolean =
    pos.x < dim.width && pos.y < dim.height && pos.x >= 0 && pos.y >= 0
}

object ConsoleScreenUtils {
  def drawOnScreen[D: Drawable, S[_]: Monad](d: D, bufferRef: Ref[S, ScreenBuffer], screen: Screen[S]): S[Unit] =
    for {
      screenBuffer <- bufferRef.get
      (points, color) = Drawable[D].draw(d, screenBuffer)
      _ <- screen.draw(points, color, bufferRef)
    } yield ()

  def drawBorder(buffer: List[Char], dim: Dimension): List[Char] = {
    val horizontalBar = ("-" * (dim.width + 2)).toList
    horizontalBar ++ drawVerticalBorder('|', buffer, dim.width) ++ horizontalBar
  }

  def drawBorderAsString(buffer: List[Char], dim: Dimension, lineSeparator: String): String =
    asString(drawBorder(buffer, dim), dim.width + 2, lineSeparator)

  def asString(buffer: List[Char], width: Int, lineSeparator: String): String =
    buffer.grouped(width).map(_.mkString).mkString(lineSeparator)

  def drawVerticalBorder(color: Char, buffer: List[Char], width: Int): List[Char] =
    buffer.grouped(width).flatMap(color +: _ :+ color).toList

  def toLinear(x: Int, y: Int, width: Int): Int = x + y * width
  
  def toLinear(pos: Pos, width: Int): Int = toLinear(pos.x, pos.y, width)
}
