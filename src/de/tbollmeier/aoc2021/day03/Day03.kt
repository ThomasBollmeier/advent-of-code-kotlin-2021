package de.tbollmeier.aoc2021.day03

import java.io.File

fun main() {

    val path = "src/de/tbollmeier/aoc2021/day03/input03.txt"
    val binStrings = File(path).readLines()

    part1(binStrings)
    part2(binStrings)
}

fun part1(binStrings: List<String>) {

    val totalCount = binStrings.size
    val size = binStrings[0].length
    val counts = IntArray(size)

    binStrings.forEach {
        updateCounts(it, counts)
    }

    val gamma = gammaRate(counts, totalCount).ToDecimal()
    val epsilon = epsilonRate(counts, totalCount).ToDecimal()

    println(gamma * epsilon)

}

fun part2(binStrings: List<String>) {

    val oxygen = oxygenGeneratorRating(binStrings)
    val co2 = co2ScrubberRating(binStrings)

    println(oxygen * co2)
}

fun updateCounts(binStr: String, counts: IntArray) {
    binStr.toList().withIndex().forEach {
        val (i, digit) = it
        if (digit == '1') {
            counts[i]++
        }
    }
}

fun gammaRate(counts: IntArray, totalCount: Int) =
    counts.map { if (it * 2 >= totalCount) 1 else 0 }

fun epsilonRate(counts: IntArray, totalCount: Int) =
    counts.map { if (it * 2 < totalCount) 1 else 0 }

fun List<Int>.ToDecimal() =
    this.fold(0) { acc, digit ->
        2 * acc + digit
    }

fun countOnesAt(binStrings: List<String>, pos: Int): Int {
    return binStrings.count { it[pos] == '1' }
}

tailrec fun findRatingValue(
    binStrings: List<String>,
    pos: Int,
    rating: (Int, Int) -> Char): Int
{

    val totalCount = binStrings.size

    if (totalCount == 1) {
        val digits = binStrings[0].map {
            if (it == '1') 1 else 0
        }
        return digits.ToDecimal()
    }

    if (pos >= binStrings[0].length) {
        throw RuntimeException("Not found")
    }

    val countOnes = countOnesAt(binStrings, pos)
    val matchChar = rating(countOnes, totalCount)

    return findRatingValue(
        binStrings.filter { it[pos] == matchChar },
        pos + 1,
        rating
    )
}

fun oxygenGeneratorRating(binStrings: List<String>) =
    findRatingValue(binStrings, 0) { countOnes, totalCount ->
        if (countOnes * 2 >= totalCount)
            '1'
        else
            '0'
    }

fun co2ScrubberRating(binStrings: List<String>) =
    findRatingValue(binStrings, 0) { countOnes, totalCount ->
        if (countOnes * 2 < totalCount)
            '1'
        else
            '0'
    }