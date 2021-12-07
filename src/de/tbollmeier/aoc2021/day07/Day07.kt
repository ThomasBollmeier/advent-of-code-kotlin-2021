package de.tbollmeier.aoc2021.day07

import readInput

fun main() {

    val input = readInput("input07")

    part1(input)

    part2(input)
}

fun part1(input: List<String>) {

    val posCounts = parse(input)

    val fuelMin = determineMinFuel(
        FuelCalculatorFact(),
        posCounts
    )

    println(fuelMin)
}

fun part2(input: List<String>) {

    val posCounts = parse(input)

    val fuelMin = determineMinFuel(
        FuelCalculatorFact2(),
        posCounts
    )

    println(fuelMin)
}

fun determineMinFuel(
    calcFactory: FuelCalculatorFactory,
    posCounts: Map<Int, Int>): Int {

    val posLeft = posCounts.keys.minOrNull() ?: 0
    val posRight = posCounts.keys.maxOrNull() ?: 0
    var calc = calcFactory.create(posLeft, posCounts)

    return (posLeft+1..posRight).fold(calc.fuel(posLeft)) { fuelMin, pos ->
        if (pos in posCounts) {
            calc = calc.next(pos, posCounts[pos]!!)
        }
        val f = calc.fuel(pos)

        if (f < fuelMin) f else fuelMin
    }
}

fun parse(input: List<String>): Map<Int, Int> {
    return input[0]
        .split(',')
        .map { it.toInt() }
        .groupingBy { it }
        .eachCount()
}

interface FuelCalculator {
    fun fuel(targetPos: Int): Int
    fun next(pos:Int, counts: Int): FuelCalculator
}

interface FuelCalculatorFactory {
    fun create(targetPos: Int, posCounts: Map<Int, Int>): FuelCalculator
}

class FuelCalculatorFact: FuelCalculatorFactory {

    override fun create(targetPos: Int, posCounts: Map<Int, Int>): FuelCalculator {

        val (left, right) = posCounts.keys.partition { it <= targetPos }

        val numLeft = left.map { posCounts[it]!! }.sum()
        val sumLeft = left.map { posCounts[it]!! * it }.sum()

        val numRight = right.map { posCounts[it]!! }.sum()
        val sumRight = right.map { posCounts[it]!! * it }.sum()

        return FuelCalc(numLeft, sumLeft, numRight, sumRight)
    }

}

data class FuelCalc(
    val numLeft: Int,
    val sumLeft: Int,
    val numRight: Int,
    val sumRight: Int
): FuelCalculator {

    override fun fuel(targetPos: Int): Int {
        return (sumRight - sumLeft) + (numLeft - numRight) * targetPos
    }

    override fun next(pos:Int, counts: Int): FuelCalculator {
        val (nl, sl, nr, sr) = this
        return FuelCalc(
            nl + counts,
            sl + counts * pos,
            nr - counts,
            sr - counts * pos
        )
    }

}

class FuelCalculatorFact2: FuelCalculatorFactory {

    override fun create(targetPos: Int, posCounts: Map<Int, Int>): FuelCalculator {

        var sum2all = 0
        var sum1Left = 0
        var sum1Right = 0
        var numLeft = 0
        var numRight = 0

        for ((pos, cnt) in posCounts) {
            sum2all += pos * pos * cnt
            if (pos <= targetPos) {
                sum1Left += pos * cnt
                numLeft += cnt
            } else {
                sum1Right += pos * cnt
                numRight += cnt
            }
        }

        return FuelCalc2(
            sum2all,
            sum1Left,
            sum1Right,
            numLeft,
            numRight
        )
    }

}

data class FuelCalc2(
    val sum2all: Int,
    val sum1Left: Int,
    val sum1Right: Int,
    val numLeft: Int,
    val numRight: Int
) : FuelCalculator {

    override fun fuel(targetPos: Int): Int {

        val t = targetPos

        var fuel = sum2all
        fuel += (1 - 2 * t) * sum1Right
        fuel += (-1 - 2 * t) * sum1Left
        fuel += (-t + t * t) * numRight
        fuel += (t + t * t) * numLeft
        fuel /= 2

        return fuel
    }

    override fun next(pos:Int, counts: Int): FuelCalculator {

        return FuelCalc2(
            sum2all,
            sum1Left + pos * counts,
            sum1Right - pos * counts,
            numLeft + counts,
            numRight - counts
        )
    }

}
