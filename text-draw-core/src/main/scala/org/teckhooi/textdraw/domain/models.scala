package org.teckhooi.textdraw.domain

case class Pos(x: Int, y: Int)
case class Dimension(width: Int, height: Int)
case class Pixel(pos: Pos, color: Char = 'x')
case class ScreenBuffer(buffer: List[Char] = List.empty, dim: Dimension = Dimension(0, 0))

case class Line(startPos: Pos, endPos: Pos, color: Char = 'x')
case class Rectangle(startPos: Pos, endPos: Pos, color: Char = 'x')
case object TextCanvas
case object Quit
case object BlankInput
