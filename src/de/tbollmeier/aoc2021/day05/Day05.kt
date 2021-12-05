package de.tbollmeier.aoc2021.day05

import readInput

fun main() {
    val input = readInput("input05")
    part1(input)
    part2(input)
}

fun part1(input: List<String>) {

    val numOverlapPositions = findNumOverlapPositions(input
        .map(::parseInputLine)
        .filter { it.isHorizontal() || it.isVertical() })

    println(numOverlapPositions)
}

fun part2(input: List<String>) {
    
    val numOverlapPositions = findNumOverlapPositions(input.map(::parseInputLine))

    println(numOverlapPositions)
}

fun findNumOverlapPositions(lines: List<Line>): Int {

    val numOverlayPositions = lines
        .flatMap { it.positions() }
        .fold(mutableMapOf<Position, Int>()) { counts, pos ->
            counts[pos] = counts.getOrDefault(pos, 0) + 1
            counts }
        .values
        .filter { it > 1 }
        .size

    return numOverlayPositions
}

data class Position(val row: Int, val col: Int)

data class Line(val start: Position, val end: Position) {

    fun isHorizontal() = start.row == end.row

    fun isVertical() = start.col == end.col

    fun positions(): List<Position> {
        val ret = mutableListOf<Position>()
        val drow = calcDelta(start.row, end.row)
        val dcol = calcDelta(start.col, end.col)
        val size1 = kotlin.math.abs(end.row - start.row) + 1
        val size2 = kotlin.math.abs(end.col - start.col) + 1
        val size = if (size1 > size2) size1 else size2
        var row = start.row
        var col = start.col

        repeat(size) {
            ret.add(Position(row, col))
            row += drow
            col += dcol
        }

        return ret
    }

    private fun calcDelta(start: Int, end: Int) =
        when {
            start < end -> 1
            start > end -> -1
            else -> 0
        }
}

fun parseInputLine(s: String): Line {
    val regex = "(\\d+),(\\d+)\\s+->\\s+(\\d+),(\\d+)".toRegex()
    val groups = regex.find(s)?.groupValues ?: throw IllegalArgumentException("invalid input")
    val (startRow, startCol, endRow, endCol) = groups.drop(1).map { it. toInt() }

    return Line(Position(startRow, startCol), Position(endRow, endCol))
}
