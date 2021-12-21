package de.tbollmeier.aoc2021.day21

import readInput

fun main() {

    val input = readInput("input21")

    part1(input)

    part2(input)
}

fun part1(input: List<String>) {

    val (posOne, posTwo) = parse(input)

    val die = DeterministicDie()
    val playerOne = Player(posOne)
    val playerTwo = Player(posTwo)

    val limit = 1000
    val loserScore: Int

    while(true) {
        if (playerOne.turn(die) >= limit) {
            loserScore = playerTwo.totalScore
            break
        } else if (playerTwo.turn(die) >= limit) {
            loserScore = playerOne.totalScore
            break
        }
    }

    println(loserScore * die.countRolls())
}

fun part2(input: List<String>) {

    val (posOne, posTwo) = parse(input)

    val turnEyeSums = calcTurnEyeSums()
    val startGame = Game(
        PlayerData(posOne),
        PlayerData(posTwo),
        true)
    var games = mutableMapOf(startGame to 1L)
    var wins1 = 0L
    var wins2 = 0L

    while (games.isNotEmpty()) {

        val nextGames = mutableMapOf<Game, Long>()

        for ((game, cnt) in games) {
            if (game.oneWins()) {
                wins1 += cnt
            } else if (game.twoWins()) {
                wins2 += cnt
            } else {
                for ((g, c) in game.turn(turnEyeSums)) {
                    nextGames[g] = nextGames.getOrDefault(g, 0) + cnt * c
                }
            }
        }

        games = nextGames
    }

    println(listOf(wins1, wins2).maxOrNull())
}

fun parse(input: List<String>): Pair<Int, Int> {
    val posPlayerOne = input[0].split(":")[1].trim().toInt()
    val posPlayerTwo = input[1].split(":")[1].trim().toInt()

    return Pair(posPlayerOne, posPlayerTwo)
}

data class Game(
    val player1: PlayerData,
    val player2: PlayerData,
    val turnOne: Boolean
) {

    private val limit = 21

    fun oneWins() = player1.score >= limit

    fun twoWins() = player2.score >= limit

    fun turn(turnEyeSums: Map<Int, Int>): Map<Game, Long> {
        val ret = mutableMapOf<Game, Long>()
        if (turnOne) {
            for ((player, cnt) in player1.turn(turnEyeSums)) {
                ret[Game(player, player2, false)] = cnt
            }
        } else {
            for ((player, cnt) in player2.turn(turnEyeSums)) {
                ret[Game(player1, player, true)] = cnt
            }
        }
        return ret
    }

}

data class PlayerData(val pos: Int, val score: Int = 0) {

    fun turn(turnEyeSums: Map<Int, Int>): Map<PlayerData, Long> {
        val ret = mutableMapOf<PlayerData, Long>()

        for ((eyeSums, cnt) in turnEyeSums) {
            var newPos =  (pos + eyeSums) % 10
            if (newPos == 0) {
                newPos = 10
            }
            val newPlayer = PlayerData(newPos, score + newPos)
            ret[newPlayer] = ret.getOrDefault(newPlayer, 0) + cnt
        }

        return ret
    }

}

fun calcTurnEyeSums(): Map<Int, Int> {
    val ret = mutableMapOf<Int, Int>()
    for (a in 1..3)
        for (b in 1..3)
            for(c in 1..3) {
                val eyesSum = a + b + c
                ret[eyesSum] = ret.getOrDefault(eyesSum, 0) + 1
            }
    return ret
}

class DeterministicDie(private val numEyes: Int = 100) {

    private var _score = 1
    private var _cntRolls = 0

    fun countRolls() = _cntRolls

    fun throwIt(): Int {
        val ret = _score++
        if (_score > numEyes) {
            _score = 1
        }
        _cntRolls++

        return ret
    }
}

class Player(private var _pos: Int) {

    private var _totalScore = 0
    val totalScore: Int
        get() = _totalScore

    fun turn(die: DeterministicDie): Int {
        val cntEyes = (1..3).fold(0) { acc, _ -> acc + die.throwIt() }
        _pos += cntEyes
        _pos %= 10
        if (_pos == 0) {
            _pos = 10
        }
        _totalScore += _pos
        return _totalScore
    }
}

