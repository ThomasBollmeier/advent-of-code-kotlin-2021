package de.tbollmeier.aoc2021.day17

import readInput

fun main() {

    val input = readInput("input17")

    part1(input)

    part2(input)
}

fun part1(input: List<String>) {

    val targetArea = parseInput(input)
    val vxMin = calcMinVx(targetArea)
    val vxMax = targetArea.xRange.last
    var yMax = 0

    for (vx in vxMin..vxMax) {

        val vyMax = vx + 300

        for (vy in vx..vyMax) {
            val s = State(Position(0, 0), Velocity(vx, vy))
            if (s.hitsTarget(targetArea)) {
                if (maxHeight(vy) > yMax) {
                    yMax = maxHeight(vy)
                }
            }
        }
    }

    println(yMax)
}

fun part2(input: List<String>) {

    val targetArea = parseInput(input)
    val xtMin = targetArea.xRange.first
    val ytMin = targetArea.yRange.first

    val vxMin = calcMinVx(targetArea)
    val vxMax = targetArea.xRange.last
    var numHits = 0

    for (vx in vxMin..vxMax) {

        val vyMin = (ytMin * vx) / xtMin
        val vyMax = vx + 300

        for (vy in vyMin..vyMax) {
            val v = Velocity(vx, vy)
            val s = State(Position(0, 0), v)
            if (s.hitsTarget(targetArea)) {
                numHits++
            }
        }
    }

    println(numHits)
}

fun parseInput(input: List<String>): TargetArea {

    val line = input[0]
    val regexRange = "([xy])=(-?\\d+)..(-?\\d+)".toRegex()
    var xRange = 0..0
    var yRange = 0..0

    for (match in regexRange.findAll(line)) {

        val minVal = match.groupValues[2].toInt()
        val maxVal = match.groupValues[3].toInt()

        if (match.groupValues[1] == "x") {
            xRange = minVal..maxVal
        } else if (match.groupValues[1] == "y") {
            yRange = minVal..maxVal
        }

    }

    return TargetArea(xRange, yRange)
    //return TargetArea(88..125, -157..-103)
}

fun maxHeight(vy: Int): Int = vy * (vy + 1) / 2

fun calcMinVx(t: TargetArea): Int {
    val xMin = t.xRange.first
    var vx = 0

    while (vx * (vx + 1) / 2 < xMin) {
        vx++
    }

    return vx
}

data class TargetArea(
    val xRange: IntRange,
    val yRange: IntRange
)

data class State(val r: Position, val v: Velocity) {

    fun next() = State(r.next(v), v.next())

    private fun isInTarget(t: TargetArea) =
        r.x in t.xRange && r.y in t.yRange

    private fun canReachTarget(t: TargetArea): Boolean {

        if (r.x < t.xRange.first && v.x <= 0) {
            return false
        }

        if (r.x > t.xRange.last && v.x >= 0) {
            return false
        }

        if (r.y < t.yRange.first && v.y <= 0) {
            return false
        }

        return true
    }

    fun hitsTarget(t: TargetArea): Boolean {

        var state = this

        while (state.canReachTarget(t) && !state.isInTarget(t)) {
            state = state.next()
        }

        return state.isInTarget(t)
    }

}

data class Position(val x: Int, val y: Int) {
    fun next(v: Velocity) =
        Position(x + v.x, y + v.y)
}

data class Velocity(val x: Int, val y: Int) {
    fun next(): Velocity =
        Velocity(
            x = when {
                x > 0 -> x - 1
                x < 0 -> x + 1
                else -> 0
            },
            y = y - 1
        )
}