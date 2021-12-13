package de.tbollmeier.aoc2021.day13

import readInput

fun main() {

    val input = readInput("input13")

    part1(input)

    part2(input)
}

fun part1(input: List<String>) {

    val (dots, actions) = parse(input)

    println(actions.first()(dots).size)
}

fun part2(input: List<String>) {

    val (dots, actions) = parse(input)

    val result = actions.fold(dots) {
            acc, action -> action(acc)
    }

    display(result)
}

typealias Dot = Pair<Int, Int>

typealias FoldAction = (Set<Dot>) -> Set<Dot>

fun makeUpAction(y: Int) =
    makeAction { dot ->
        val (_x, _y) = dot
        if (_y <= y) {
            dot
        } else {
            Dot(_x, 2 * y - _y)
        }
    }

fun makeLeftAction(x: Int) =
    makeAction { dot ->
        val (_x, _y) = dot
        if (_x <= x) {
            dot
        } else {
            Dot(2 * x - _x, _y)
        }
    }

fun makeAction(dotTrans: (Dot) -> Dot): FoldAction {
    return { dots ->
        dots.fold(mutableSetOf()) { acc, dot ->
            acc.add(dotTrans(dot))
            acc
        }
    }
}

data class Problem(val dots: Set<Dot>, val actions: List<FoldAction>)

fun parse(input: List<String>): Problem {

    val dots = mutableSetOf<Dot>()
    val actions = mutableListOf<FoldAction>()
    val regexDot = "(\\d+),(\\d+)".toRegex()
    val regexAction = "([xy])=(\\d+)".toRegex()
    var inPositionSection = true

    for (line in input) {

        if (inPositionSection) {
            val match = regexDot.find(line)
            if (match != null) {
                val x = match.groupValues[1].toInt()
                val y = match.groupValues[2].toInt()
                dots.add(Dot(x, y))
            } else {
                inPositionSection = false
            }
        } else {
            val match = regexAction.find(line)
            if (match != null) {
                val direction = match.groupValues[1]
                val n = match.groupValues[2].toInt()
                val action = if (direction == "x") {
                    makeLeftAction(n)
                } else {
                    makeUpAction(n)
                }
                actions.add(action)
            }
        }
    }

    return Problem(dots, actions)
}

fun display(dots: Set<Dot>) {

    val xMax = dots.maxByOrNull { (x, _) -> x }!!.first
    val yMax = dots.maxByOrNull { (_, y) -> y }!!.second

    for (y in 0..yMax) {
        for (x in 0..xMax) {
            print(if (Dot(x, y) in dots) {
                '#'
            } else {
                '.'
            })
        }
        println()
    }

}