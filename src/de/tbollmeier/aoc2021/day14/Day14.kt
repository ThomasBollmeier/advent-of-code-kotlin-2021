package de.tbollmeier.aoc2021.day14

import readInput

fun main() {

    val input = readInput("input14")

    part1(input)

    part2(input)
}

fun part1(input: List<String>) {
    buildPolymer(input, 10)
}

fun part2(input: List<String>) {
    buildPolymer(input, 40)
}

fun buildPolymer(input: List<String>, nSteps: Int) {

    val (polymer, rules) = parse(input)

    val finalPolymer = (1..nSteps).fold(polymer) { acc, _ ->
        step(acc, rules)
    }

    val stats = calcStats(finalPolymer)
    val minCount = stats.values.minOrNull()!!
    val maxCount = stats.values.maxOrNull()!!

    println(maxCount - minCount)
}

typealias Polymer = Map<Pair<Char, Char>, Long>

typealias InsertionRules = Map<Pair<Char, Char>, Char>

data class Problem(
    val polymer: Polymer,
    val rules: InsertionRules
)

fun parse(input: List<String>): Problem {

    val polymer = mutableMapOf<Pair<Char, Char>, Long>()
    val s = input[0]

    (s zip s.drop(1)).forEach { (a, b) ->
        val key = Pair(a, b)
        polymer[key] = polymer.getOrDefault(key, 0L) + 1L
    }
    polymer[Pair(s.last(), '_')] = 1L

    val rules = mutableMapOf<Pair<Char, Char>, Char>()
    val regex = "(.+)\\s*->\\s*(.+)".toRegex()

    for (line in input.drop(2)) {
        val match = regex.find(line)
        if (match != null) {
            val pairStr = match.groupValues[1].trim()
            val a = pairStr[0]
            val b = pairStr[1]
            rules[Pair(a, b)] = match.groupValues[2].trim()[0]
        }
    }

    return Problem(polymer, rules)
}

fun step(polymer: Polymer, rules: InsertionRules): Polymer {

    val ret = mutableMapOf<Pair<Char, Char>, Long>()

    for ((key, cnt) in polymer) {
        val c = rules[key]
        if (c != null) {
            val (a, b) = key
            ret[Pair(a, c)] = ret.getOrDefault(Pair(a, c), 0L) + cnt
            ret[Pair(c, b)] = ret.getOrDefault(Pair(c, b), 0L) + cnt
        } else {
            ret[key] = cnt
        }
    }

    return ret
}

fun calcStats(polymer: Polymer): Map<Char, Long> {

    val ret = mutableMapOf<Char, Long>()

    for ((key, cnt) in polymer) {
        val (a, _) = key
        ret[a] = ret.getOrDefault(a, 0L) + cnt
    }

    return ret
}