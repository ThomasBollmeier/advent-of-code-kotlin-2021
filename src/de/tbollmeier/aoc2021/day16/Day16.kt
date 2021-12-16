package de.tbollmeier.aoc2021.day16

import readInput
import java.math.BigInteger

fun main() {

    val input = readInput("input16")

    part1(input)

    part2(input)
}

fun part1(input: List<String>) {

    val bin = input[0].hexToBinStr()
    val (packet, _) = parsePacket(bin)

    println(packet.sumVersions())
}

fun part2(input: List<String>) {

    val bin = input[0].hexToBinStr()

    val (packet, _) = parsePacket(bin)

    println(packet.evaluate())
}

sealed class Packet(
    val version: Int,
    val typeId: Int
) {
    open fun sumVersions() = version
    abstract fun evaluate(): Long
}

class Literal(
    version: Int,
    private val number: Long): Packet(version, TYPE_ID) {

    companion object {
        const val TYPE_ID = 4
    }

    override fun evaluate() = number
}

class Operator(
    version: Int,
    typeId: Int,
    private val subPackets: List<Packet>): Packet(version, typeId) {

    override fun sumVersions(): Int {
        return version + subPackets.sumOf { it.sumVersions() }
    }

    override fun evaluate(): Long {
        return when (typeId) {
            0 -> subPackets.sumOf { it.evaluate() }
            1 -> subPackets.fold(1L) { prod, sp -> prod * sp.evaluate() }
            2 -> subPackets.minOfOrNull { it.evaluate() } ?: 0L
            3 -> subPackets.maxOfOrNull { it.evaluate() } ?: 0L
            5 -> {
                val (a, b) = subPackets.map { it.evaluate() }
                if (a > b) 1L else 0L
            }
            6 -> {
                val (a, b) = subPackets.map { it.evaluate() }
                if (a < b) 1L else 0L
            }
            7 -> {
                val (a, b) = subPackets.map { it.evaluate() }
                if (a == b) 1L else 0L
            }
            else -> throw RuntimeException("Illegal type ID $typeId")
        }
    }
}

data class PacketParseResult(
    val packet: Packet,
    val parsedLength: Int
)

data class LiteralParseResult(
    val number: Long,
    val parsedLength: Int
)

data class OperatorParseResult(
    val subPackets: List<Packet>,
    val parsedLength: Int
)

fun parsePacket(bin: String): PacketParseResult {

    val version = bin.substring(0 until 3).binStrToInt()
    val headerLen = 6

    return when (val typeId = bin.substring(3 until headerLen).binStrToInt()) {
        Literal.TYPE_ID -> {
            val (number, parsedLen) = parseLiteralData(bin.substring(headerLen))
            PacketParseResult(Literal(version, number), parsedLen + headerLen)
        }
        else -> {
            val (subPackets, parsedLen) = parseOperatorData(bin.substring(headerLen))
            PacketParseResult(Operator(version, typeId, subPackets), parsedLen + headerLen)
        }
    }
}

fun parseOperatorData(bin: String): OperatorParseResult {

    val lengthType = bin[0]
    val subPackets = mutableListOf<Packet>()
    var parsedLength = 1

    if (lengthType == '0') {

        val totalLenBits = bin.substring(1..15).binStrToInt()
        parsedLength += 15 + totalLenBits
        var subsLen = 0

        while (subsLen < totalLenBits) {
            val (subPacket, len) = parsePacket(bin.substring(16 + subsLen))
            subPackets.add(subPacket)
            subsLen += len
        }

    } else {

        val numSubPackets = bin.substring(1..11).binStrToInt()
        parsedLength += 11
        var subsLen = 0

        repeat(numSubPackets) {
            val (subPacket, len) = parsePacket(bin.substring(12 + subsLen))
            subPackets.add(subPacket)
            subsLen += len
        }

        parsedLength += subsLen
    }

    return OperatorParseResult(subPackets, parsedLength)
}

fun parseLiteralData(bin: String): LiteralParseResult {

    var offset = 0
    var dataStr = ""

    while (offset + 5 <= bin.length) {
        val segment = bin.substring(offset until offset + 5)
        dataStr += segment.substring(1)
        offset += 5
        if (segment[0] == '0') {
            break
        }
    }

    val number = dataStr.binStrToLong()

    return LiteralParseResult(number, offset)
}

fun String.binStrToInt() =
    this.fold(0) { acc, ch ->
        when (ch) {
            '1' -> acc * 2 + 1
            else -> acc * 2
        }
    }

fun String.binStrToLong() =
    this.fold(0L) { acc, ch ->
        when (ch) {
            '1' -> acc * 2L + 1L
            else -> acc * 2L
        }
    }

fun String.hexToBinStr() = this
    .map { hexCharToBinStr(it) }
    .joinToString(separator = "")

fun hexCharToBinStr(hex: Char): String {
    var ret = BigInteger("$hex", 16).toString(2)
    while (ret.length < 4) {
        ret = "0$ret"
    }
    return ret
}