package uk.co.thomasc.thealley

import java.nio.ByteBuffer
import kotlin.experimental.xor

fun encrypt(input: ByteArray, firstKey: Int = 0xAB): ByteArray {
    var key = firstKey
    input.forEachIndexed { index, byte ->
        input[index] = byte.xor(key.toByte())
        key = input[index].toInt()
    }

    return input
}

fun encrypt(input: String, firstKey: Int = 0xAB): ByteArray =
    encrypt(input.toByteArray(), firstKey)

fun encryptWithHeader(input: ByteArray, firstKey: Int = 0xAB): ByteArray {
    val buf = encrypt(input, firstKey)
    val bufLen = ByteBuffer.allocate(4).putInt(buf.size).array()

    return bufLen + buf
}

fun encryptWithHeader(input: String, firstKey: Int = 0xAB): ByteArray =
    encryptWithHeader(input.toByteArray(), firstKey)

fun decrypt(input: ByteArray, firstKey: Int = 0xAB): ByteArray {
    var key = firstKey
    input.forEachIndexed { index, byte ->
        input[index] = byte.xor(key.toByte())
        key = byte.toInt()
    }

    return input
}

fun decryptWithHeader(input: ByteArray, firstKey: Int = 0xAB): ByteArray {
    val newSize = ByteBuffer.wrap(input.copyOfRange(0, 4)).int
    return decrypt(input.copyOfRange(4, newSize + 4), firstKey)
}
