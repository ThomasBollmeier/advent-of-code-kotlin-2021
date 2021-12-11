package de.tbollmeier.aoc2021.day11

import readInput

fun main() {

    val input = readInput("input11")

    part1(input)
    part2(input)
}

fun part1(input: List<String>) {

    val octopuses = parse(input)
    var cntFlashes = 0

    repeat(100) {
        cntFlashes += step(octopuses)
    }

    println(cntFlashes)
}

fun part2(input: List<String>) {

    val octopuses = parse(input)
    val (nRows, nCols) = dimensions(octopuses)
    val allPos = allPositions(nRows, nCols)
    var cntSteps = 0

    while (true) {
        step(octopuses)
        cntSteps++
        if (allPos.all { (row, col) -> octopuses[row][col] == 0 }) {
            break
        }
    }

    println(cntSteps)
}

typealias Position = Pair<Int, Int>
typealias Octopuses = Array<Array<Int>>

const val MAX_ENERGY = 9

fun parse(input: List<String>): Octopuses {
    val nRows = input.size
    val nCols = input[0].length
    val ret = Array(nRows) { Array(nCols) { 0 } }

    for ((row, line) in input.withIndex()) {
        for ((col, ch) in line.withIndex()) {
            ret[row][col] = ch.code - '0'.code
        }
    }

    return ret
}

fun step(octos: Octopuses): Int {

    val (nRows, nCols) = dimensions(octos)
    val allPos = allPositions(nRows, nCols)

    increase(octos, allPos)

    val flashers = findFlashers(
        octos,
        allPos.filter { (row, col) -> octos[row][col] > MAX_ENERGY }.toSet()
    )

    flashers.forEach { (row, col) -> octos[row][col] = 0 }

    return flashers.size
}

fun dimensions(octos: Octopuses): Pair<Int, Int> {
    val nRows = octos.size
    val nCols = octos[0].size
    return Pair(nRows, nCols)
}

fun neighbors(pos: Position, nRows: Int, nCols: Int): Set<Position> {
    val (row, col) = pos
    return listOf(
        row - 1 to col - 1,
        row - 1 to col,
        row - 1 to col + 1,
        row to col - 1,
        row to col + 1,
        row + 1 to col - 1,
        row + 1 to col,
        row + 1 to col + 1
    ).filter {
        val (r, c) = it
        r in 0 until nRows && c in 0 until nCols
    }.toSet()
}

fun allPositions(nRows: Int, nCols: Int) =
    (0 until nRows).flatMap { row ->
        (0 until nCols).map { col -> Pair(row, col)}
    }.toSet()

fun increase(octos: Octopuses, positions: Set<Position>) {
    positions.forEach { (row, col) -> octos[row][col]++ }
}

tailrec fun findFlashers(
    octos: Octopuses,
    flashers: Set<Position>,
    allFlashers: Set<Position> = emptySet()
): Set<Position> {

    if (flashers.isEmpty()) {
        return allFlashers
    }

    val (nRows, nCols) = dimensions(octos)
    val nextFlashers = mutableSetOf<Position>()
    val nextAllFlashers = allFlashers + flashers

    for (flasher in flashers) {
        val nbs = neighbors(flasher, nRows, nCols)
            .filter { it !in nextAllFlashers && it !in nextFlashers }
            .toSet()
        for (nb in nbs) {
            val (row, col) = nb
            octos[row][col]++
            if (octos[row][col] > MAX_ENERGY) {
                nextFlashers.add(nb)
            }
        }
    }

    return findFlashers(octos, nextFlashers, nextAllFlashers)
}
