package de.tbollmeier.aoc2021.day20

import readInput

fun main() {

    val input = readInput("input20")

    part1(input)

    part2(input)
}

fun part1(input: List<String>) {
    enhance(input, 2)
}

fun part2(input: List<String>) {
    enhance(input, 50)
}

fun enhance(input: List<String>, cnt: Int) {
    val result = parse(input)
    val encoding = result.first
    var image = result.second

    repeat(cnt) {
        image = image.enhance(encoding)
    }

    //println(image)
    println(image.countLitPixels())
}

typealias Position = Pair<Int, Int>
typealias Color = Int
typealias ColorEncoding = IntArray

class Image(
    private val pixels: Array<IntArray>,
    private val leftUpper: Position = Position(0, 0),
    private val backGroundColor: Color = 0
) {
    private val width = pixels[0].size
    private val height = pixels.size

    fun countLitPixels() = if (backGroundColor == 0)
        pixels.fold(0) { cnt, pixelLine ->
            cnt + pixelLine.count { it == 1 }
        }
    else
        throw RuntimeException("There is an infinite number of lit pixels.")

    fun enhance(encoding: ColorEncoding): Image {
        val newLeftUpper = Pair(leftUpper.first - 1, leftUpper.second - 1)
        val (x0, y0) = newLeftUpper
        val pixelLines = mutableListOf<IntArray>()

        for (y in y0 until y0 + height + 2) {
            val pixelLine = IntArray(width + 2)
            for (x in x0 until x0 + width + 2) {
                pixelLine[x - x0] = calcOutputColor(x, y, encoding)
            }
            pixelLines.add(pixelLine)
        }

        val newPixels = pixelLines.toTypedArray()
        val newBgColor = if (backGroundColor == 0)
            encoding[0]
        else
            encoding.last()

        return Image(newPixels, newLeftUpper, newBgColor)
    }

    private fun getColor(pos: Position): Color {
        val (x, y) = pos
        val (x0, y0) = leftUpper

        return if (x in x0 until x0 + width && y in y0 until y0 + height) {
            pixels[y - y0][x - x0]
        } else {
            backGroundColor
        }
    }

    private fun calcOutputColor(x: Int, y: Int, encoding: ColorEncoding): Color {
        val positions = listOf(
            x - 1 to y -  1,
            x to y - 1,
            x + 1 to y - 1,
            x - 1 to y,
            x to y,
            x + 1 to y,
            x - 1 to y +  1,
            x to y + 1,
            x + 1 to y + 1
        )

        val colorIdx = positions.fold(0) { code, pos ->
            2 * code + getColor(pos)
        }

        return encoding[colorIdx]
    }

    override fun toString(): String {
        val sb = StringBuilder()

        for (pixelLine in pixels) {
            pixelLine.forEach {
                if (it == 1)
                    sb.append('#')
                else
                    sb.append('.')
            }
            sb.append('\n')
        }

        return sb.toString()
    }
}

fun parse(input: List<String>): Pair<ColorEncoding, Image> {
    val (encoding, remaining) = parseEncoding(input)
    val pixels: Array<IntArray> = parsePixels(remaining)

    return Pair(encoding, Image(pixels))
}

fun parsePixels(input: List<String>): Array<IntArray> {
    val pixelLines = mutableListOf<IntArray>()

    for (line in input) {
        if (line.isNotBlank()) {
            val pixelLine = line
                .map { if (it == '#') 1 else 0 }
                .toIntArray()
            pixelLines.add(pixelLine)
        }
    }

    return pixelLines.toTypedArray()
}

fun parseEncoding(input: List<String>): Pair<ColorEncoding, List<String>> {
    val sb = StringBuilder()
    var numLines = 0

    for ((i, line) in input.withIndex()) {
        if (line.isNotBlank()) {
            sb.append(line)
        } else {
            numLines = i
            break
        }
    }

    val encoding = sb
        .toString()
        .map { ch -> if (ch == '#') 1 else 0 }
        .toIntArray()
    val remaining = input.drop(numLines)

    return Pair(encoding, remaining)
}