package de.tbollmeier.aoc2021.day02

import readInput

fun main() {

    val commands = readInput("input02").parse()

    // Part 1
    val position = commands.execute(Position(0, 0)) { pos, dir, step ->
        val (horiz, depth) = pos
        when (dir) {
            Direction.FORWARD -> Position(horiz + step, depth)
            Direction.UP -> Position(horiz, depth - step)
            Direction.DOWN -> Position(horiz, depth + step)
        }
    }

    println(position.horiz * position.depth)

    // Part 2
    val uboat = commands.execute(Uboat(0, 0, 0)) { ubt, dir, step ->
        val (x, y, aim) = ubt
        when (dir) {
            Direction.FORWARD -> Uboat(x + step, y + aim * step, aim)
            Direction.UP -> Uboat(x, y, aim - step)
            Direction.DOWN -> Uboat(x, y, aim + step)
        }
    }

    println(uboat.horiz * uboat.depth)
}

enum class Direction {
    FORWARD, UP, DOWN
}

data class Command(val direction: Direction, val step: Int)

data class Position(val horiz: Int, val depth: Int)

data class Uboat(val horiz: Int, val depth: Int, val aim: Int)

fun parseCommand(command: String): Command {
    val regex = "(forward|up|down)\\s+(\\d+)".toRegex()
    val result = regex.find(command) ?: throw IllegalArgumentException("unknown command")
    val directionStr = result.groupValues[1]
    val units = result.groupValues[2].toInt()

    return when(directionStr) {
        "forward" -> Command(Direction.FORWARD, units)
        "up" -> Command(Direction.UP, units)
        "down" -> Command(Direction.DOWN, units)
        else -> throw IllegalArgumentException("unknown command")
    }
}

fun List<String>.parse() = this.map(::parseCommand)

fun<T> List<Command>.execute(
    start: T,
    move: (T, Direction, Int) -> T): T
{
    return this.fold(start) { state, cmd ->
        val (direction, step) = cmd
        move(state, direction, step)
    }
}
