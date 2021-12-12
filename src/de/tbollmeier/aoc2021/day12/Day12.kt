package de.tbollmeier.aoc2021.day12

import readInput

fun main() {

    val input = readInput("input12")

    part1(input)

    part2(input)
}

fun part1(input: List<String>) {

    val caves = parse(input)
    val paths = caves.findStartEndPaths { path ->
        val counters = path.smallCaveStats().values
        counters.all { it == 1 }
    }

    println(paths.size)
}

fun part2(input: List<String>) {

    val caves = parse(input)
    val paths = caves.findStartEndPaths validation@ { path ->

        val stats = path.smallCaveStats()
        if (stats.isEmpty()) {
            return@validation true
        }

        if (stats.any { it.value > 2 }) {
            return@validation false
        }

        val cavesTwice = stats.filter { it.value == 2 }.keys

        return@validation when (cavesTwice.size) {
            0 -> true
            1 -> Caves.START !in cavesTwice && Caves.END !in cavesTwice
            else -> false
        }
    }

    println(paths.size)
}

class Cave(val name: String) {

    private val _neighbors = mutableListOf<Cave>()
    val neighbors: List<Cave>
        get() = _neighbors

    fun addNeighbor(nb: Cave) {
        _neighbors.add(nb)
    }

    fun isSmall() = name[0].isLowerCase()

    fun isEnd() = name == Caves.END
}

class Caves {

    companion object {
        const val START = "start"
        const val END = "end"
    }

    private val caves = mutableMapOf(
        START to Cave(START),
        END to Cave(END)
    )

    private fun getCave(name: String) = caves.getOrPut(name) { Cave(name) }

    fun connect(nameA: String, nameB: String) {
        val a = getCave(nameA)
        val b = getCave(nameB)
        a.addNeighbor(b)
        b.addNeighbor(a)
    }

    fun findStartEndPaths(validate: (Path) -> Boolean): List<Path> {

        val ret = mutableListOf<Path>()
        val start = Path(listOf(getCave(START)))
        val todo = ArrayDeque(listOf(start))

        while (todo.isNotEmpty()) {
            val path = todo.removeFirst()
            val newPaths = path.head.neighbors
                .map { path + it }
                .filter(validate)

            for (np in newPaths) {
                if (np.head.isEnd()) {
                    ret.add(np)
                } else {
                    todo.addFirst(np)
                }
            }
        }

        return ret
    }

}

class Path(private val elements: List<Cave>) {

    val head: Cave
        get() = elements.last()

    operator fun contains(cave: Cave) = cave in elements

    operator fun plus(cave: Cave) = Path(elements + cave)

    override fun toString(): String {
        var ret = ""
        elements.forEach {
            if (ret.isNotEmpty()) {
                ret += ","
            }
            ret += it.name
        }
        return ret
    }

    fun smallCaveStats() =
        elements
            .filter { it.isSmall() }
            .groupBy { it.name }
            .mapValues { it.value.size }
}

fun parse(input: List<String>): Caves {
    val ret = Caves()

    input.forEach {
        val (nameA, nameB) = it.split('-')
        ret.connect(nameA, nameB)
    }

    return ret
}