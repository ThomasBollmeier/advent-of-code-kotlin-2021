package de.tbollmeier.aoc2021.day06

import readInput

fun main() {

    val input = readInput("input06")

    part1(input)

    part2(input)
}

fun part1(input: List<String>) {
    simulate(80, parse(input))
}

fun part2(input: List<String>) {
    simulate(256, parse(input))
}

fun simulate(days: Int, counts: LongArray) {
    val numFishes = generateSequence(counts, ::nextGeneration)
        .take(days + 1)
        .last()
        .sum()
    println(numFishes)
}

fun parse(input: List<String>): LongArray {

    val fishStats = input[0]
        .split(',')
        .map { it.toInt() }
        .groupingBy { it }
        .eachCount()

    val counts = LongArray(9)

    fishStats.keys.forEach {
        counts[it] = fishStats[it]!!.toLong()
    }

    return counts
}

fun nextGeneration(counts: LongArray): LongArray {

    val ret = LongArray(counts.size)

    for (i in counts.indices) {
        ret[i] = when (i) {
            6 -> counts[0] + counts[7]
            8 -> counts[0]
            else -> counts[i + 1]
        }
    }

    return ret
}