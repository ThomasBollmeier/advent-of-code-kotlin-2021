package de.tbollmeier.aoc2021.day19

import readInput

fun main() {

    val input = readInput("input19")

    part1(input)

    part2(input)
}

fun part1(input: List<String>) {

    val scanners = parse(input)

    val located = mutableListOf<Scanner>()
    val locatedIndices = mutableSetOf<Int>()
    located.add(scanners[0])
    locatedIndices.add(0)
    var locatedIdx = 0

    while (locatedIdx < located.size) {
        val a = located[locatedIdx]

        for ((i, b) in scanners.withIndex()) {
            if (i in locatedIndices) {
                continue
            }
            val overlap = a.findOverlap(b)
            if (overlap != null) {
                locatedIndices.add(i)
                located.add(overlap.partner)
            }
        }

        locatedIdx++
    }

    val allBeacons = mutableSetOf<Position>()
    for (scanner in located) {
        allBeacons.addAll(scanner.beacons)
    }

    println(allBeacons.size)
}

fun part2(input: List<String>) {

    val scanners = parse(input)

    val located = mutableListOf<Pair<Scanner, Position>>()
    val locatedIndices = mutableSetOf<Int>()
    located.add(Pair(scanners[0], Position(0, 0, 0)))
    locatedIndices.add(0)
    var locatedIdx = 0

    while (locatedIdx < located.size) {
        val (a, _) = located[locatedIdx]

        for ((i, b) in scanners.withIndex()) {
            if (i in locatedIndices) {
                continue
            }
            val overlap = a.findOverlap(b)
            if (overlap != null) {
                locatedIndices.add(i)
                located.add(Pair(overlap.partner, overlap.dr))
            }
        }

        locatedIdx++
    }

    var maxDistance = 0
    for (i in located.indices) {
        val (_, r1) = located[i]
        for (j in i until located.size) {
            val (_, r2) = located[j]
            val distance = r1.manhattanDistance(r2)
            if (distance > maxDistance) {
                maxDistance = distance
            }
        }
    }

    println(maxDistance)
}

val xFacings = arrayOf(
    intArrayOf(1, 0, 0),
    intArrayOf(-1, 0, 0),
    intArrayOf(0, 1, 0),
    intArrayOf(0, -1, 0),
    intArrayOf(0, 0, 1),
    intArrayOf(0, 0, -1)
)

val upDirections = arrayOf(
    intArrayOf(1, 0),
    intArrayOf(0, 1),
    intArrayOf(-1, 0),
    intArrayOf(0, -1)
)

val transformations = xFacings.flatMap { x ->
    upDirections.map { up ->
        transformation(x, up)
    }
}

fun yFacing(x: IntArray, up: IntArray): IntArray {
    val coordinates = mutableListOf<Int>()
    var j = 0
    for (i in 0..2) {
        if (x[i] != 0) {
            coordinates.add(0)
        } else {
            coordinates.add(up[j])
            j++
        }
    }
    return coordinates.toIntArray()
}

fun zFacing(x: IntArray, y: IntArray): IntArray {
    val z = IntArray(3) { 0 }

    z[0] = x[1] * y[2] - x[2] * y[1]
    z[1] = x[2] * y[0] - x[0] * y[2]
    z[2] = x[0] * y[1] - x[1] * y[0]

    return z
}

typealias Transformation = Array<IntArray>

fun transformation(x: IntArray, up: IntArray): Transformation {
    val ret = Array(3) { IntArray(3) }
    val y = yFacing(x, up)
    val z = zFacing(x, y)

    for (i in 0..2) {
        ret[i][0] = x[i]
        ret[i][1] = y[i]
        ret[i][2] = z[i]
    }

    return ret
}

data class Position(
    val x: Int,
    val y: Int,
    val z: Int
) {

    operator fun plus(other: Position) =
        Position(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: Position) =
        Position(x - other.x, y - other.y, z - other.z)

    fun manhattanDistance(other: Position): Int {
        var ret = 0
        val dr = this - other

        ret += if (dr.x >= 0) dr.x else -dr.x
        ret += if (dr.y >= 0) dr.y else -dr.y
        ret += if (dr.z >= 0) dr.z else -dr.z

        return ret
    }
}

class Overlap(
    val partner: Scanner, // other transformed an moved
    val dr: Position, // position of scanner b relative to a
    val positions: Set<Position>
)

class Scanner(val beacons: List<Position>) {

    fun findOverlap(other: Scanner): Overlap? {

        for (t in transformations) {
            for (i in beacons.indices) {
                for (j in other.beacons.indices) {
                    val overlap = calcOverlap(other, t, i, j)
                    if (overlap.positions.size >= 12) {
                        return overlap
                    }
                }
            }
        }

        return null
    }

    private fun calcOverlap(other: Scanner, t: Transformation, i: Int, j: Int): Overlap {
        var s = other.transform(t)
        val dr = beacons[i] - s.beacons[j]
        s = s.move(dr)
        val overlapPositions = beacons.toSet() intersect s.beacons.toSet()

        return Overlap(s, dr, overlapPositions)
    }

    private fun transform(t: Transformation): Scanner {
        val transformed = mutableListOf<Position>()

        for (beacon in beacons) {
            val r = arrayOf(beacon.x, beacon.y, beacon.z)
            val x = (0..2).fold(0) { acc, i ->
                acc + t[0][i] * r[i]
            }
            val y = (0..2).fold(0) { acc, i ->
                acc + t[1][i] * r[i]
            }
            val z = (0..2).fold(0) { acc, i ->
                acc + t[2][i] * r[i]
            }
            transformed.add(Position(x, y, z))
        }

        return Scanner(transformed)
    }

    private fun move(dr: Position) =
        Scanner(beacons.map { it + dr })
}

fun parse(input: List<String>): List<Scanner> {

    val ret = mutableListOf<Scanner>()
    var beacons = mutableListOf<Position>()

    for (line in input) {
        if (line.startsWith("---")) {
            if (beacons.isNotEmpty()) {
                ret.add(Scanner(beacons))
            }
            beacons = mutableListOf()
            continue
        }
        val coordinates = line.split(",")
        if (coordinates.size == 3) {
            val (x, y, z) = coordinates.map { it.toInt() }
            beacons.add(Position(x, y, z))
        }
    }

    if (beacons.isNotEmpty()) {
        ret.add(Scanner(beacons))
    }

    return ret
}
