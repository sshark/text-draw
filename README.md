# Text Draw
A text console drawing application with few simple commands.

The objective for making this application is to show what is the difference of a purely functional style application from an
OOP designed application in terms of implementation. The second objective is sharing an example of using GraalVM `native-image`
to compile a non-trivial Scala project to native code.

The importance of [Cats Effect](https://typelevel.org/cats-effect/#:~:text=Cats%20Effect%20is%20a%20high,style%20within%20the%20Typelevel%20ecosystem.&text=Even%20more%20importantly%2C%20Cats%20Effect,a%20purely%20functional%20runtime%20system) or
other similar libraries cannot be emphasized enough. Without using any one of these libraries, it is extremely tedious if 
not impossible to create a pure functional style application.

## How to Build
This project uses [SBT 1.5.x](http://www.scala-sbt.org/download.html) to build and runs on
[Scala 3.0.x](http://www.scala-lang.org/download/scala3.html). Please install these software before 
proceeding.

1. `sbt` to start the build tool. Steps (2) and (3) must be executed in the SBT console.
2. `app/run` to run the application.
3. `test` to run the all test spec.
4. `nativeImage` to compile project to native code. This might not be straight forward because it requires GraalVM. Please refer to [`sbt-native-image`](https://github.com/scalameta/sbt-native-image) and (GraalVM)[https://www.graalvm.org/reference-manual/native-image/] documentations for details

## Commands

| Command 		    | Description
| ------------------|------------
| C w h             | Should create a new canvas of width w and height h.
| L x1 y1 x2 y2     | Should create a new line from (x1,y1) to (x2,y2). Currently only horizontal or vertical lines are supported. Horizontal and vertical lines will be drawn using the 'x' character.
| R x1 y1 x2 y2     | Should create a new rectangle, whose upper left corner is (x1,y1) and lower right corner is (x2,y2). Horizontal and vertical lines will be drawn using the 'x' character.
| B x1 y1 c         | Should fill the entire area connected to (x,y) with "colour" c. The behaviour of this is the same as that of the "bucket fill" tool in paint programs.
| Q                 | Should quit the program.

## Sample Output

Below is a sample run of the program. User input is prefixed with enter command:

```
enter command: C 20 4
----------------------
|                    |
|                    |
|                    |
|                    |
----------------------

enter command: L 1 2 6 2
----------------------
|                    |
|xxxxxx              |
|                    |
|                    |
----------------------

enter command: L 6 3 6 4
----------------------
|                    |
|xxxxxx              |
|     x              |
|     x              |
----------------------

enter command: R 14 1 18 3
----------------------
|             xxxxx  |
|xxxxxx       x   x  |
|     x       xxxxx  |
|     x              |
----------------------

enter command: B 10 3 o
----------------------
|oooooooooooooxxxxxoo|
|xxxxxxooooooox   xoo|
|     xoooooooxxxxxoo|
|     xoooooooooooooo|
----------------------

enter command: Q
Quit
```
