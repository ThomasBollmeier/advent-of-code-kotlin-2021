package de.tbollmeier.aoc2021.day22

import readInput
import java.lang.Long.max
import java.lang.Long.min

fun main() {

    val input = readInput("input22")

    part1(input)

    part2(input)
}

fun part1(input: List<String>) {
    solve(input, withRestriction = true)
}

fun part2(input: List<String>) {
    solve(input, withRestriction = false)
}

fun solve(input: List<String>, withRestriction: Boolean) {

    val cuboids = if (withRestriction) {

        val initRegion = Cuboid(
            -50L..50L,
            -50L..50L,
            -50L..50L,
            State.None)

        parse(input).mapNotNull { cuboid ->
            val (_, _, ab) = cuboid overlappedBy initRegion
            ab
        }
    } else {
        parse(input)
    }

    val result = cuboids
        .overlap()
        .sumOf { it.countOn() }

    println(result)
}

fun parse(input: List<String>): List<Cuboid> {
    return input.mapNotNull { parseLine(it) }
}

fun parseLine(line: String): Cuboid? {

    val regex = "(on|off) x=([^.]*)\\.\\.([^.]*),y=([^.]*)\\.\\.([^.]*),z=([^.]*)\\.\\.([^.]*)".toRegex()

    val match = regex.find(line)

    return if (match != null) {
        val state = if (match.groupValues[1] == "on") State.On else State.Off
        val xMin = match.groupValues[2].toLong()
        val xMax = match.groupValues[3].toLong()
        val yMin = match.groupValues[4].toLong()
        val yMax = match.groupValues[5].toLong()
        val zMin = match.groupValues[6].toLong()
        val zMax = match.groupValues[7].toLong()
        Cuboid(xMin..xMax, yMin..yMax, zMin..zMax, state)
    } else {
        null
    }
}


enum class State {
    On,
    Off,
    None
}

class Cuboid(
    private val xRange: LongRange,
    private val yRange: LongRange,
    private val zRange: LongRange,
    val state: State = State.Off
) {

    fun countOn(): Long {
        return if (state == State.On) {
            (xRange.last - xRange.first + 1L) * (yRange.last - yRange.first + 1) *
                    (zRange.last - zRange.first + 1L)
        } else {
            0
        }
    }

    data class CuboidOverlap(
        val a: List<Cuboid>,
        val b: List<Cuboid>,
        val ab: Cuboid?
    )

    infix fun overlappedBy(other: Cuboid): CuboidOverlap {
        val xOver = calcAxisOverlap(xRange, other.xRange)
        val yOver = calcAxisOverlap(yRange, other.yRange)
        val zOver = calcAxisOverlap(zRange, other.zRange)

        if (xOver.abRange == null || yOver.abRange == null || zOver.abRange == null) {
            return CuboidOverlap(listOf(this), listOf(other), null)
        }

        val ab = Cuboid(
            xOver.abRange,
            yOver.abRange,
            zOver.abRange,
            newState(state, other.state))

        val a = mutableListOf<Cuboid>()
        val b = mutableListOf<Cuboid>()

        for (xa in xOver.aRanges)
            for (ya in yOver.aRanges)
                for (za in zOver.aRanges) {
                    a.add(Cuboid(xa, ya, za, state))
                }

        for (xa in xOver.aRanges)
            for (ya in yOver.aRanges) {
                a.add(Cuboid(xa, ya, zOver.abRange, state))
            }

        for (xa in xOver.aRanges)
            for (za in zOver.aRanges) {
                a.add(Cuboid(xa, yOver.abRange, za, state))
            }

        for (ya in yOver.aRanges)
            for (za in zOver.aRanges) {
                a.add(Cuboid(xOver.abRange, ya, za, state))
            }

        for (xa in xOver.aRanges)
            a.add(Cuboid(xa, yOver.abRange, zOver.abRange, state))

        for (ya in yOver.aRanges)
            a.add(Cuboid(xOver.abRange, ya, zOver.abRange, state))

        for (za in zOver.aRanges)
            a.add(Cuboid(xOver.abRange, yOver.abRange, za, state))

        for (xb in xOver.bRanges)
            for (yb in yOver.bRanges)
                for (zb in zOver.bRanges) {
                    b.add(Cuboid(xb, yb, zb, other.state))
                }

        for (xb in xOver.bRanges)
            for (yb in yOver.bRanges) {
                b.add(Cuboid(xb, yb, zOver.abRange, other.state))
            }

        for (xb in xOver.bRanges)
            for (zb in zOver.bRanges) {
                b.add(Cuboid(xb, zOver.abRange, zb, other.state))
            }

        for (yb in yOver.bRanges)
            for (zb in zOver.bRanges) {
                b.add(Cuboid(xOver.abRange, yb, zb, other.state))
            }

        for (xb in xOver.bRanges)
            b.add(Cuboid(xb, yOver.abRange, zOver.abRange, other.state))

        for (yb in yOver.bRanges)
            b.add(Cuboid(xOver.abRange, yb, zOver.abRange, other.state))

        for (zb in zOver.bRanges)
            b.add(Cuboid(xOver.abRange, yOver.abRange, zb, other.state))

        return CuboidOverlap(a, b, ab)
    }

    private fun newState(a: State, b: State) =
        when (b) {
            State.None -> a
            else -> b
        }

    private data class AxisOverlap(
        val aRanges: List<LongRange>,
        val bRanges: List<LongRange>,
        val abRange: LongRange?
    )

    private fun calcAxisOverlap(a: LongRange, b: LongRange): AxisOverlap {

        if (a.last < b.first || a.first > b.last) {
            return AxisOverlap(listOf(a), listOf(b), null)
        }

        val left = max(a.first, b.first)
        val right = min(a.last, b.last)
        val abRange = left..right

        val aRanges = mutableListOf<LongRange>()
        val bRanges = mutableListOf<LongRange>()

        if (a.first != left){
            aRanges.add(a.first until left)
        } else if (b.first != left) {
            bRanges.add(b.first until left)
        }

        if (a.last != right){
            aRanges.add(right + 1..a.last)
        } else if (b.last != right) {
            bRanges.add(right + 1..b.last)
        }

        return AxisOverlap(aRanges, bRanges, abRange)
    }

    override fun toString(): String {
        return "(x: $xRange, y: $yRange, z: $zRange, state: $state)"
    }

}

fun List<Cuboid>.overlap(): List<Cuboid> {

    return this.fold(listOf()) { cuboidsOn, cuboid ->
        val previousOverlapped = cuboidsOn.flatMap { cuboidOn ->
            val (a, _, _) = cuboidOn overlappedBy cuboid
            a
        }
        if (cuboid.state == State.On) {
            previousOverlapped + listOf(cuboid)
        } else {
            previousOverlapped
        }
    }
}