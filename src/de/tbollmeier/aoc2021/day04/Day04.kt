package de.tbollmeier.aoc2021.day04

import readInput

fun main() {

    val lines = readInput("input04")

    part1(lines)

    part2(lines)
}

fun part1(lines: List<String>) {

    val (numbers, bingos) = readGameSetup(lines)

    numbers.forEach { number ->
        bingos.forEach { bingo ->
            bingo.mark(number)
            if (bingo.isComplete()) {
                println(bingo.sumOfUnmarked() * number)
                return
            }
        }
    }
}

fun part2(lines: List<String>) {

    val gameSetup = readGameSetup(lines)
    val numbers = gameSetup.numbers
    var bingos = gameSetup.bingos

    numbers.forEach { n ->

        bingos.forEach { it.mark(n) }
        val bingosDone = bingos.filter { it.isComplete() }
        bingos = bingos.filter { !it.isComplete() }

        if (bingos.isEmpty()) {
            if (bingosDone.isNotEmpty()) {
                println(n * bingosDone[0].sumOfUnmarked())
            }
            return
        }
    }
}

fun readGameSetup(lines: List<String>) =
    GameSetup(
        readNumbers(lines[0]),
        readBingos(lines.drop(1)))

fun readBingos(lines: List<String>): List<Bingo> {

    val ret = mutableListOf<Bingo>()
    var bingo: Bingo? = null
    var row = 0

    for (line in lines) {
        val numbersStr = line.trim()
        if (numbersStr.isNotEmpty()) {
            val numbers = numbersStr.split("\\s+".toRegex()).map { it.toInt() }
            if (bingo == null) {
                row = 0
                bingo = Bingo(numbers.size)
            }
            for ((col, value) in numbers.withIndex()) {
                bingo.setValueAt(row, col, value)
            }
            row++
        } else {
            if (bingo != null) {
                ret.add(bingo)
                bingo = null
            }
        }
    }

    if (bingo != null) {
        ret.add(bingo)
    }

    return ret
}

fun readNumbers(line: String): List<Int> {
    return line.split(',').map { it.toInt() }
}

data class Position(val row: Int, val col: Int)

class Bingo(private val size: Int = 5) {

    class Cell(var value: Int, var found: Boolean)

    private val cells: Array<Array<Cell>> = Array(size) {
        Array(size) {
            Cell(0, false)
        }
    }

    private val valuePos: MutableMap<Int, MutableSet<Position>> = mutableMapOf()

    fun setValueAt(row: Int, col: Int, value: Int) {
        cells[row][col].value = value
        if (value !in valuePos) {
            valuePos[value] = mutableSetOf(Position(row, col))
        } else {
            valuePos[value]!!.add(Position(row, col))
        }
    }

    fun mark(value: Int) {
        val positions = valuePos[value] ?: return
        positions.forEach { (row, col) ->
            cells[row][col].found = true
        }
    }

    fun isComplete(): Boolean {
        for (row in 0 until size) {
            if ((0 until size).map { cells[row][it] }.all { it.found }) {
                return true
            }
        }

        for (col in 0 until size) {
            if ((0 until size).map { cells[it][col] }.all { it.found }) {
                return true
            }
        }

        return false
    }

    fun sumOfUnmarked() =
        cells
            .flatten()
            .filter { !it.found }
            .sumOf { it.value }

}

data class GameSetup(val numbers: List<Int>, val bingos: List<Bingo>)