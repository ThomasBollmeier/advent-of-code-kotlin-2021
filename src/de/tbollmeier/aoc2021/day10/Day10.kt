package de.tbollmeier.aoc2021.day10

import readInput

fun main() {

    val input = readInput("input10")

    part1(input)

    part2(input)
}

fun part1(input: List<String>) {

    val totalScore = input
        .mapNotNull {
            (checkLine(it) as? Corrupted)?.score()
        }
        .sum()

    println(totalScore)
}

fun part2(input: List<String>) {

    val scores = input
        .mapNotNull {
            (checkLine(it) as? Incomplete)?.score()
        }
        .sorted()

    println(scores[scores.size / 2])
}

val parens = mapOf(
    '(' to ')',
    '[' to ']',
    '{' to '}',
    '<' to '>'
)

sealed class CheckResult

object Ok : CheckResult()

class Incomplete(private val stack: ArrayDeque<Char>) : CheckResult() {

    private val charScores = mapOf(
        ')' to 1,
        ']' to 2,
        '}' to 3,
        '>' to 4
    )

    fun score(): Long {
        val st = ArrayDeque(stack)
        var ret = 0L

        while (st.isNotEmpty()) {
            val openParen = st.removeLast()
            ret = 5 * ret + charScores[parens[openParen]]!!.toLong()
        }

        return ret
    }

}

class Corrupted(private val invalid: Char) : CheckResult() {

    private val scores = mapOf(
        ')' to 3,
        ']' to 57,
        '}' to 1197,
        '>' to 25137
    )

    fun score() = scores[invalid]!!

}

fun checkLine(inputLine: String): CheckResult {

    val stack = ArrayDeque<Char>()

    for (ch in inputLine) {
        if (ch in parens) {
            stack.addLast(ch)
        } else {
            val openParen = stack.removeLastOrNull()
            if (openParen == null || parens[openParen] != ch) {
                return Corrupted(ch)
            }
        }
    }

    return if (stack.isEmpty())
        Ok
    else
        Incomplete(stack)
}
