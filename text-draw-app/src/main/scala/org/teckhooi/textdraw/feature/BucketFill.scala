package org.teckhooi.textdraw.feature

import org.teckhooi.textdraw.domain.{Dimension, Pos, ScreenBuffer}
import org.teckhooi.textdraw.{ConsoleScreenUtils, Drawable}

import scala.annotation.tailrec

case class BucketFill(pos: Pos, color: Char, dim: Dimension = Dimension(0, 0))

object BucketFill {
  implicit object BucketFillDrawable extends Drawable[BucketFill] {
    override def draw(shape: BucketFill, screenBuffer: ScreenBuffer): (List[Pos], Char) =
      if (screenBuffer.buffer(ConsoleScreenUtils.toLinear(shape.pos, screenBuffer.dim.width)) == ' ')
        (emptySpaces(
           List(Pos(shape.pos.x, shape.pos.y)),
           screenBuffer.buffer,
           screenBuffer.dim,
           Set(shape.pos)
         ).toList,
         shape.color)
      else (List.empty, ' ')

    @tailrec
    private def emptySpaces(positions: List[Pos], buffer: List[Char], dim: Dimension, acc: Set[Pos]): Set[Pos] =
      if (positions.isEmpty) acc
      else {
        val Pos(x, y) = positions.head
        val next = Set(Pos(x + 1, y), Pos(x - 1, y), Pos(x, y + 1), Pos(x, y - 1))
          .filter {
            case Pos(x, y) =>
              y < dim.height &&
                x < dim.width &&
                y >= 0 &&
                x >= 0 &&
                buffer(ConsoleScreenUtils.toLinear(x, y, dim.width)) == ' ' &&
                !acc.contains(Pos(x, y))
          }

        emptySpaces(positions.tail ++ next, buffer, dim, acc ++ next)
      }
  }
}
