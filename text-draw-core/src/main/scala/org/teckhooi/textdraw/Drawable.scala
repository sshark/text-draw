package org.teckhooi.textdraw

import org.teckhooi.textdraw.domain.{Line, Pixel, Pos, Rectangle, ScreenBuffer, TextCanvas}

trait Drawable[A] {
  def draw(shape: A, screenBuffer: ScreenBuffer): (List[Pos], Char)
}

object Drawable {
  implicit def apply[F](implicit ev: Drawable[F]): Drawable[F] = ev

  implicit object PixelDrawable extends Drawable[Pixel] {
    override def draw(shape: Pixel, screenBuffer: ScreenBuffer): (List[Pos], Char) = (List(shape.pos), shape.color)
  }

  implicit object LineDrawable extends Drawable[Line] {
    override def draw(shape: Line, screenBuffer: ScreenBuffer): (List[Pos], Char) =
      if (shape.startPos.x == shape.endPos.x)
        (drawVerticalLine(shape.startPos, shape.endPos), shape.color)
      else
        (drawHorizontalLine(shape.startPos, shape.endPos), shape.color)

    def drawHorizontalLine(startPos: Pos, endPos: Pos): List[Pos] =
      (Math.min(startPos.x, endPos.x) to Math.max(startPos.x, endPos.x)).map(Pos(_, startPos.y)).toList

    def drawVerticalLine(startPos: Pos, endPos: Pos): List[Pos] =
      (Math.min(startPos.y, endPos.y) to Math.max(startPos.y, endPos.y)).map(Pos(startPos.x, _)).toList
  }

  implicit object RectangleDrawable extends Drawable[Rectangle] {
    override def draw(rect: Rectangle, screenBuffer: ScreenBuffer): (List[Pos], Char) =
      (drawRectangle(rect), rect.color)

    private val drawTopLine: Rectangle => List[Pos] = (r: Rectangle) =>
      LineDrawable.drawHorizontalLine(r.startPos, r.endPos)

    private val drawLeftBorder: Rectangle => List[Pos] = (r: Rectangle) =>
      LineDrawable.drawVerticalLine(Pos(r.startPos.x, r.startPos.y + 1), Pos(r.startPos.x, r.endPos.y - 1))

    private val drawRightBorder = (r: Rectangle) =>
      LineDrawable.drawVerticalLine(Pos(r.endPos.x, r.startPos.y + 1), Pos(r.endPos.x, r.endPos.y - 1))

    private val drawBottomLine = (r: Rectangle) =>
      LineDrawable.drawHorizontalLine(Pos(r.startPos.x, r.endPos.y), Pos(r.endPos.x, r.endPos.y))

    private def drawRectangle(r: Rectangle): List[Pos] =
      drawTopLine(r) ++ drawLeftBorder(r) ++ drawRightBorder(r) ++ drawBottomLine(r)
  }

  implicit object TextCanvasDrawable extends Drawable[TextCanvas.type] {
    override def draw(shape: TextCanvas.type, screenBuffer: ScreenBuffer): (List[Pos], Char) = (List.empty, ' ')
  }
}
