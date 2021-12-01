fun main() {
    val depths = readInput("input01")
        .map { it.toInt() }

    //println(calcNumIncrements(depths))

    val sums = createTriples(depths, depths.drop(1), depths.drop(2))
        .map { it.first + it.second + it.third }

    println(calcNumIncrements(sums))

}

fun calcNumIncrements(values: List<Int>): Int {
    val steps = values zip values.drop(1)
    return steps.filter { it.second > it.first }.size
}

tailrec fun <X, Y, Z> createTriples(
    xs: List<X>,
    ys: List<Y>,
    zs: List<Z>,
    acc: List<Triple<X, Y, Z>> = emptyList()
): List<Triple<X, Y, Z>> {
    return if (xs.isEmpty() || ys.isEmpty() || zs.isEmpty()) {
        acc
    } else {
        val x = xs.first()
        val y = ys.first()
        val z = zs.first()
        createTriples(
            xs.drop(1),
            ys.drop(1),
            zs.drop(1),
            acc + listOf(Triple(x, y, z))
        )
    }
}
