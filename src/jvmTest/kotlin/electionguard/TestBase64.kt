package electionguard

import electionguard.Base64.fromBase64
import electionguard.Base64.toBase64
import io.kotest.common.runBlocking
import io.kotest.property.Arb
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.byteArray
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.positiveLong
import io.kotest.property.checkAll
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TestBase64 {
    @Test
    fun comparingBase64ToJavaLong() {
        val encoder = java.util.Base64.getEncoder()
        runTest {
            checkAll(Arb.positiveLong()) { x ->
                val bytes = x.toBigInteger().toByteArray()
                val b64lib = bytes.toBase64()
                val j64lib = String(encoder.encode(bytes), StandardCharsets.ISO_8859_1)

                assertEquals(b64lib, j64lib)

                val bytesAgain = b64lib.fromBase64()
                assertContentEquals(bytes, bytesAgain)
            }
        }
    }

    @Test
    fun comparingBase64ToJavaByteArray() {
        val encoder = java.util.Base64.getEncoder()
        runTest {
            checkAll(Arb.byteArray(Arb.int(1, 200), Arb.byte())) { bytes ->
                val b64lib = bytes.toBase64()
                val j64lib = String(encoder.encode(bytes), StandardCharsets.ISO_8859_1)

                assertEquals(b64lib, j64lib)

                val bytesAgain = b64lib.fromBase64()
                assertContentEquals(bytes, bytesAgain)
            }
        }
    }
}