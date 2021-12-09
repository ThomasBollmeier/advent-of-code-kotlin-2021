package de.tbollmeier.aoc2021.day09

import readInput

fun main() {

    val input = readInput("input09")

    part1(input)

    part2(input)
}

fun part1(input: List<String>) {

    val heights = parse(input)
    val numRows = heights.size
    val numCols = heights[0].size

    val result = allPositions(numRows, numCols)
        .sumOf { (row, col) ->
            riskLevel(row, col, numRows, numCols, heights)
        }

    println(result)
}

fun part2(input: List<String>) {

    val heights = parse(input)
    val numRows = heights.size
    val numCols = heights[0].size

    val basinSizes = allPositions(numRows, numCols)
        .filter { (row, col) ->
            riskLevel(row, col, numRows, numCols, heights) > 0
        }
        .map {
            determineBasin(it, numRows, numCols, heights)
        }
        .map {
            it.size
        }
        .sortedDescending()

    val result = basinSizes.take(3).fold(1) { prod, it -> prod * it }

    println(result)
}

fun parse(input: List<String>): Array<Array<Int>> {
    return input
        .map { it.toList().map { ch -> ch.code - '0'.code }.toTypedArray() }
        .toTypedArray()
}

typealias Position = Pair<Int, Int>

fun riskLevel(row: Int, col: Int, numRows: Int, numCols: Int, heights: Array<Array<Int>>): Int {
    val height = heights[row][col]

    return if (neighbors(row, col, numRows, numCols).all { (r, c) -> height < heights[r][c] })
        height + 1
    else
        0
}

fun allPositions(numRows: Int, numCols: Int): List<Position> {
    return (0 until numRows)
        .flatMap { row ->
            (0 until numCols).map { col -> Pair(row, col) }
        }
}

fun neighbors(row: Int, col: Int, numRows: Int, numCols: Int): List<Position> {
    val ret = mutableListOf<Position>()

    if (row > 0) {
        ret.add(Pair(row - 1, col))
    }
    if (row < numRows - 1) {
        ret.add(Pair(row + 1, col))
    }
    if (col > 0) {
        ret.add(Pair(row, col - 1))
    }
    if (col < numCols - 1) {
        ret.add(Pair(row, col + 1))
    }

    return ret
}

fun determineBasin(
    start: Position,
    numRows: Int,
    numCols: Int,
    heights: Array<Array<Int>>
): Set<Position> {

    val ret = mutableSetOf<Position>()

    var todo = mutableListOf<Position>()
    todo.add(start)
    val visited = mutableSetOf<Position>()

    while (todo.isNotEmpty()) {

        val current = todo[0]
        val (row, col) = current
        val height = heights[row][col]
        todo = todo.drop(1).toMutableList()

        ret.add(current)
        visited.add(current)

        val nbs = neighbors(row, col, numRows, numCols)
            .filter { it !in visited }
            .filter { (r, c) ->
                heights[r][c] in (height + 1)..8
            }
        todo.addAll(nbs)
    }

    return ret
}