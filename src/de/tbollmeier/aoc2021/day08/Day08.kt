package de.tbollmeier.aoc2021.day08

import readInput

fun main() {

    val input = readInput("input08")

    part1(input)

    part2(input)
}

fun part1(input: List<String>) {
    val count1478 = input
        .map(::parse)
        .sumOf { it.determineCountOneFourSevenEight() }

    println(count1478)
}

fun part2(input: List<String>) {
    println(input.map(::parse).sumOf { it.decode() })
}

fun parse(inputLine: String): Display {
    val (left, right) = inputLine.split('|').map { it.trim() }
    val segments = left.toDigits()
    val output = right.toDigits()

    return Display(Wiring(segments), output)
}

typealias Digit = Set<Char>

fun String.toDigits(): List<Digit> {
    return this.split(' ').map { it.toSet() }
}

class Wiring(segments: List<Digit>) {

    private val segmentsMap = segments.groupBy { it.size }
    private val buffer = Array<Digit?>(10) { null }
    private val pattern: MutableMap<Digit, Int> = mutableMapOf()

    init {
        pattern[getZero()] = 0
        pattern[getOne()] = 1
        pattern[getTwo()] = 2
        pattern[getThree()] = 3
        pattern[getFour()] = 4
        pattern[getFive()] = 5
        pattern[getSix()] = 6
        pattern[getSeven()] = 7
        pattern[getEight()] = 8
        pattern[getNine()] = 9
    }

    fun getSignalPattern(): Map<Digit, Int> {
        return pattern
    }

    private fun getDigit(i: Int, fn: () -> Digit): Digit {
        if (buffer[i] == null) {
            buffer[i] = fn()
        }
        return buffer[i]!!
    }

    private fun getZero() = getDigit(0) {
        val six = getSix()
        val nine = getNine()
        segmentsMap[6]!!.find { it != six && it != nine }!!
    }

    private fun getOne() = getDigit(1) {
        segmentsMap[2]!![0]
    }

    private fun getTwo() = getDigit(2) {
        val nine = getNine()
        segmentsMap[5]!!.filter { (it subtract nine).size == 1 }[0]
    }

    private fun getThree() = getDigit(3) {
        val seven = getSeven()
        segmentsMap[5]!!.filter { (it subtract seven).size == 2 }[0]
    }

    private fun getFour() = getDigit(4) {
        segmentsMap[4]!![0]
    }

    private fun getFive() = getDigit(5) {
        val two = getTwo()
        val three = getThree()
        segmentsMap[5]!!.find { it != two && it != three }!!
    }

    private fun getSix() = getDigit(6) {
        val one = getOne()
        segmentsMap[6]!!.filter { (it subtract one).size == 5 }[0]
    }

    private fun getSeven() = getDigit(7) {
        segmentsMap[3]!![0]

    }

    private fun getEight() = getDigit(8) {
        segmentsMap[7]!![0]
    }

    private fun getNine() = getDigit(9) {
        val four = getFour()
        val seven = getSeven()
        segmentsMap[6]!!.filter { (it subtract seven subtract four).size == 1 }[0]
    }

    override fun toString(): String {
        return "$pattern"
    }
}

class Display(
    private val wiring: Wiring,
    private val output: List<Digit>
) {

    private val outputMap = output.groupBy { it.size }

    fun determineCountOneFourSevenEight(): Int {
        return (outputMap[2]?.size ?: 0) +
                (outputMap[4]?.size ?: 0) +
                (outputMap[3]?.size ?: 0) +
                (outputMap[7]?.size ?: 0)
    }

    fun decode(): Int {
        val pattern = wiring.getSignalPattern()
        return output
            .map { pattern[it]!! }
            .fold(0) { acc, n ->
                acc * 10 + n
            }
    }

    override fun toString(): String {
        return "$wiring\n$output"
    }

}
