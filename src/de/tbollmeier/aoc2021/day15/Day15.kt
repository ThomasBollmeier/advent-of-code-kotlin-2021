package de.tbollmeier.aoc2021.day15

import readInput

fun main() {

    val input = readInput("input15")

    part1(input)

    part2(input)
}

fun part1(input: List<String>) {
    solve(Cavern(parse(input)))
}

fun part2(input: List<String>) {
    solve(Cavern(inflate(parse(input), 5)))
}

fun solve(cavern: Cavern) {
    val from = Position(0, 0)
    val to = Position(cavern.nRows -1, cavern.nCols - 1)
    val result = cavern.calcTotalRisk(from, to)

    println(result)
}

typealias RiskLevels = Array<Array<Int>>

data class Position(val row: Int, val col: Int)

class Cavern(private val riskLevels: RiskLevels) {

    data class PositionInfo(var totalRisk: Int, var pred: Position?)

    val nRows = riskLevels.size
    val nCols = riskLevels[0].size

    fun calcTotalRisk(from: Position, to: Position): Int {
        // Dijkstra algorithm

        val posInfoMap = initPositionInfo(from)
        val todo = mutableSetOf<Position>()
        val done = mutableSetOf<Position>()
        var ret = 0

        todo.add(from)

        while (todo.isNotEmpty()) {

            val current = getNextPos(todo, posInfoMap)
            val (row, col) = current
            val posInfo = posInfoMap[row][col]!!

            if (current == to) {
                ret = posInfo.totalRisk
                break
            }

            val newPositions = getNeighbors(current).filter { it !in done }

            updatePosInfoMap(
                current,
                posInfo.totalRisk,
                newPositions,
                posInfoMap
            )

            done.add(current)
            todo.addAll(newPositions)
        }

        return ret
    }

    private fun updatePosInfoMap(
        current: Position,
        totalRisk: Int,
        positions: List<Position>,
        posInfoMap: Array<Array<PositionInfo?>>) {

        for (pos in positions) {

            val (row, col) = pos
            val posInfo = posInfoMap[row][col] ?: PositionInfo(Int.MAX_VALUE, null)
            val newTotalRisk = totalRisk + riskLevels[row][col]

            if (newTotalRisk < posInfo.totalRisk) {
                posInfoMap[row][col] = PositionInfo(newTotalRisk, current)
            }
        }
    }

    private fun getNeighbors(pos: Position): List<Position> {

        val (row, col) = pos

        return listOf(
            Position(row - 1, col),
            Position(row + 1, col),
            Position(row, col - 1),
            Position(row, col + 1)
        ).filter {
            it.row in 0 until nRows &&
                    it.col in 0 until nCols
        }

    }

    private fun getNextPos(
        todo: MutableSet<Position>,
        posInfoMap: Array<Array<PositionInfo?>>): Position {

        val ret = todo.minByOrNull { pos ->
            val (row, col) = pos
            posInfoMap[row][col]?.totalRisk ?: Int.MAX_VALUE
        }!!

        todo.remove(ret)

        return ret
    }

    private fun initPositionInfo(start: Position): Array<Array<PositionInfo?>> {

        val noPositionInfo: PositionInfo? = null
        val ret = Array(nRows) { Array(nCols) { noPositionInfo } }
        ret[start.row][start.col] = PositionInfo(0, null)

        return ret
    }

}

fun parse(input: List<String>): RiskLevels {

    val riskLevels = mutableListOf<Array<Int>>()

    for (line in input) {
        val row = line.map { it.code - '0'.code }
        riskLevels.add(row.toTypedArray())
    }

    return riskLevels.toTypedArray()
}

fun inflate(riskLevels: RiskLevels, n: Int): RiskLevels {

    val transform = { i: Int, j: Int ->
        val ret = (i + j) % 9
        if (ret != 0) ret else 9
    }

    val nRows = riskLevels.size
    val nCols = riskLevels[0].size

    val ret = Array(nRows * n) { Array(nCols * n) { 0 } }

    for (row in 0 until nRows) {
        for (col in 0 until nCols) {
            for (i in 0 until n) {
                for (j in 0 until n) {
                    ret[row + i * nRows][col + j * nCols] =
                        transform(riskLevels[row][col], i + j)
                }
            }
        }
    }

    return ret
}
