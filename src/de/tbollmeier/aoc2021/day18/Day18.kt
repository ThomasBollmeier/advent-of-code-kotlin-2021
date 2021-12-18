package de.tbollmeier.aoc2021.day18

import readInput

fun main() {

    val input = readInput("input18")

    part1(input)

    part2(input)
}

fun part1(input: List<String>) {

    var sum: Number? = null

    for (line in input) {
        val (n, _) = number(line)
        sum = sum?.add(n) ?: n
    }

    val interpreter = Interpreter()
    val result = interpreter.evaluate(sum!!)

    println(result)
}

fun part2(input: List<String>) {

    val numbers = input.map {
        val (num, _) = number(it)
        num
    }

    val interpreter = Interpreter()

    val sums = mutableListOf<Number>()

    for (i in numbers.indices) {
        val a = numbers[i]
        for (j in numbers.indices) {
            if (i == j) {
                continue
            }
            val b = numbers[j]
            sums.add(a.add(b))
        }
    }

    val result = sums
        .maxOfOrNull {
            interpreter.evaluate(it)
        }

    println(result)
}

sealed class Number {

    private val exploder = Exploder()
    private val splitter = Splitter()

    abstract fun accept(visitor: NumberVisitor)

    fun add(other: Number): Number {
        val ret = this.clone().addInternal(other.clone())
        ret.reduce()
        return ret
    }

    abstract fun clone(): Number

    protected abstract fun addInternal(other: Number): Number

    private fun reduce(): Number {

        var number = this

        while(true) {

            val explodeRes = exploder.explode(number)
            number = explodeRes.number
            if (explodeRes.exploded) {
                continue
            }

            val splitRes = splitter.split(number)
            number = splitRes.number
            if (!splitRes.split) {
                break
            }
        }

        return number
    }
}

class Pair(
    var left: Number,
    var right: Number
) : Number() {

    override fun accept(visitor: NumberVisitor) {
        visitor.enterPair(this)
        left.accept(visitor)
        right.accept(visitor)
        visitor.exitPair(this)
    }

    override fun clone() = Pair(left.clone(), right.clone())

    override fun addInternal(other: Number) = Pair(this, other)

    fun replaceElement(old: Number, new: Number) {
        if (old == left) {
            left = new
        } else if (right == old) {
            right = new
        }
    }

    override fun toString() = "($left $right)"
}

class Regular(var value: Long) : Number() {

    override fun accept(visitor: NumberVisitor) {
        visitor.visitRegular(this)
    }

    override fun clone() = Regular(value)

    override fun addInternal(other: Number) =
        when(other) {
            is Regular -> Regular(this.value + other.value)
            else -> Pair(this, other)
        }

    override fun toString() = "$value"
}

interface NumberVisitor {
    fun enterPair(p: Pair)
    fun exitPair(p: Pair)
    fun visitRegular(r: Regular)
}

class Interpreter: NumberVisitor {

    private val stack = ArrayDeque<Long>()

    fun evaluate(n: Number): Long {

        stack.clear()
        n.accept(this)

        return stack.last()
    }

    override fun enterPair(p: Pair) {
        // Nothing to do
    }

    override fun exitPair(p: Pair) {
        val right = stack.removeLast()
        val left = stack.removeLast()
        stack.addLast(3 * left + 2 * right)
    }

    override fun visitRegular(r: Regular) {
        stack.addLast(r.value)
    }

}


class Exploder: NumberVisitor {

    data class ExplodeResult(val number: Number, val exploded: Boolean)

    private var pairStack = ArrayDeque<Pair>()
    private var exploding: Pair? = null
    private var explodingParent: Pair? = null
    private var leftRegular: Regular? = null
    private var leftParent: Pair? = null
    private var rightRegular: Regular? = null
    private var rightParent: Pair? = null

    fun explode(n: Number): ExplodeResult {
        pairStack.clear()
        exploding = null
        explodingParent = null
        leftRegular = null
        leftParent = null
        rightRegular = null
        rightParent = null

        n.accept(this)

        if (exploding != null) {
            explodingParent?.replaceElement(exploding!!, Regular(0))
            if (leftRegular != null) {
                leftParent?.replaceElement(
                    leftRegular!!,
                    leftRegular!!.add(exploding!!.left)
                )
            }
            if (rightRegular != null) {
                rightParent?.replaceElement(
                    rightRegular!!,
                    exploding!!.right.add(rightRegular!!)
                )
            }
        }

        return ExplodeResult(n, exploding != null)
    }

    override fun enterPair(p: Pair) {

        if (pairStack.size == 4 && exploding == null) {
            exploding = p
            explodingParent = pairStack.last()
        }
        pairStack.addLast(p)
    }

    override fun exitPair(p: Pair) {
        pairStack.removeLast()
    }

    override fun visitRegular(r: Regular) {

        if (pairStack.isEmpty()) {
            return
        }

        val currentPair = pairStack.last()
        if (currentPair == exploding) {
            return
        }

        if (exploding == null) {
            leftRegular = r
            leftParent = currentPair
        } else if (rightRegular == null) {
            rightRegular = r
            rightParent = currentPair
        }
    }

}

class Splitter: NumberVisitor {

    data class SplitResult(val number: Number, val split: Boolean)

    private var pairStack = ArrayDeque<Pair>()
    private var toSplit: Regular? = null
    private var splitPair: Pair? = null
    private var splittingParent: Pair? = null

    fun split(n: Number): SplitResult {

        pairStack.clear()
        toSplit = null
        splitPair = null
        splittingParent = null

        n.accept(this)

        if (toSplit == null) {
            return SplitResult(n, false)
        }

        return if (splittingParent != null) {
            splittingParent!!.replaceElement(toSplit!!, splitPair!!)
            SplitResult(n, true)
        } else {
            SplitResult(splitPair!!, true)
        }
    }

    override fun enterPair(p: Pair) {
        pairStack.addLast(p)
    }

    override fun exitPair(p: Pair) {
        pairStack.removeLast()
    }

    override fun visitRegular(r: Regular) {
        if (r.value < 10) {
            return
        }

        if (toSplit != null) {
            return
        }

        toSplit = r

        val left = Regular(r.value / 2)
        val right = Regular(r.value - r.value / 2)
        splitPair = Pair(left, right)

        if (pairStack.isNotEmpty()) {
            splittingParent = pairStack.last()
        }
    }

}

data class ParseResult(
    val number: Number,
    val parsedLen: Int
)

fun number(s: String, offset: Int=0): ParseResult {
    val ch = s[offset + 0]
    return if (ch == '[') {
        val(num, len) = pair(s, offset + 1)
        ParseResult(num, 1 + len)
    } else {
        regular(s, offset)
    }
}

fun pair(s: String, offset: Int): ParseResult {
    val (leftNum, leftLen) = number(s, offset)
    val (rightNum, rightLen) = number(s, offset + leftLen + 1)
    val parsedLen = leftLen + 1 + rightLen + 1

    return ParseResult(Pair(leftNum, rightNum), parsedLen)
}

fun regular(s: String, offset: Int): ParseResult {
    var ch: Char
    var digits = ""

    while(offset + digits.length < s.length) {
        ch = s[offset + digits.length]
        if (ch in '0'..'9') {
            digits += ch
        } else {
            break
        }
    }

    return ParseResult(Regular(digits.toLong()), digits.length)
}
