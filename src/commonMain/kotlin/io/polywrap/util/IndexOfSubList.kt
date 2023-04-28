package io.polywrap.util

fun <T> List<T>.indexOfSubList(target: List<T>): Int {
    if (target.isEmpty()) return 0
    if (this.size < target.size) return -1

    val kmpTable = kmpTable(target)
    var m = 0
    var i = 0

    while (m + i < this.size) {
        if (target[i] == this[m + i]) {
            if (i == target.size - 1) {
                return m
            }
            i++
        } else {
            if (kmpTable[i] > -1) {
                m += i - kmpTable[i]
                i = kmpTable[i]
            } else {
                m++
                i = 0
            }
        }
    }

    return -1
}

private fun <T> kmpTable(subList: List<T>): IntArray {
    val table = IntArray(subList.size)
    var pos = 1
    var cnd = 0

    table[0] = -1

    while (pos < subList.size) {
        if (subList[pos] == subList[cnd]) {
            table[pos] = table[cnd]
        } else {
            table[pos] = cnd
            cnd = table[cnd]

            while (cnd >= 0 && subList[pos] != subList[cnd]) {
                cnd = table[cnd]
            }
        }
        pos++
        cnd++

        if (pos >= table.size) {
            break
        }
    }

    return table
}
