package uk.co.thomasc.thealley

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.Base64
import kotlin.test.assertEquals

data class TestPayload(
    val json: String,
    val withoutHeader: String,
    val withHeader: String
)

class CryptoTest {

    val payloads = mapOf(
        Pair(
            "setPowerStateOn",
            TestPayload(
                "{\"system\":{\"set_relay_state\":{\"state\":1}}}",
                "0PKB+Iv/mvfV75S2xaDUi/mc8JHot8Sw0aXA4tijgfKG55P21O7fot+i",
                "AAAAKtDygfiL/5r31e+UtsWg1Iv5nPCR6LfEsNGlwOLYo4HyhueT9tTu36Lfog=="
            )
        ),
        Pair(
            "setPowerStateOff",
            TestPayload(
                "{\"system\":{\"set_relay_state\":{\"state\":0}}}",
                "0PKB+Iv/mvfV75S2xaDUi/mc8JHot8Sw0aXA4tijgfKG55P21O7eo96j",
                "AAAAKtDygfiL/5r31e+UtsWg1Iv5nPCR6LfEsNGlwOLYo4HyhueT9tTu3qPeow=="
            )
        ),
        Pair(
            "getSysInfo",
            TestPayload(
                "{ \"system\":{ \"get_sysinfo\":null } }",
                "0PDSodir37rX9c+0lLbRtMCf7JXmj+GH6MrwnuuH68u2lus=",
                "AAAAI9Dw0qHYq9+61/XPtJS20bTAn+yV5o/hh+jK8J7rh+vLtpbr"
            )
        ),
        Pair(
            "getConsumption",
            TestPayload(
                "{ \"emeter\":{ \"get_realtime\":null } }",
                "0PDSt9q/y67c/sS/n73av8uU5oPijvqT/pu5g+2Y9Ji4xeWY",
                "AAAAJNDw0rfav8uu3P7Ev5+92r/LlOaD4o76k/6buYPtmPSYuMXlmA=="
            )
        ),
        Pair(
            "specialChars",
            TestPayload(
                "right single quotation mark:’ left double quotation mark:“ right double quotation mark:” kissing cat face with closed eyes:😽",
                "2bDXv8vrmPGf+JTx0aDVus6v27Lds5P+n+2GvF7eR2cLbgh8XDhXIkAsSWkYbQJ2F2MKZQsrRidVPgTmZvraqMGmzrqa/pHkhuqPr96rxLDRpcyjze2A4ZP4wiCgPR12H2wfdhh/XzxdKQlvDm0IKF82QioKaQVqGXwYOF0kQTII+Gf/Qg==",
                "AAAAhdmw17/L65jxn/iU8dGg1brOr9uy3bOT/p/thrxe3kdnC24IfFw4VyJALElpGG0CdhdjCmULK0YnVT4E5mb62qjBps66mv6R5Ibqj6/eq8Sw0aXMo83tgOGT+MIgoD0ddh9sH3YYf188XSkJbw5tCChfNkIqCmkFahl8GDhdJEEyCPhn/0I="
            )
        )
    )

    @TestFactory
    fun decrypt(): Collection<DynamicTest> = payloads.map {
        DynamicTest.dynamicTest("it should decrypt ${it.key} payload") {
            val buf = uk.co.thomasc.thealley.decrypt(
                Base64.getDecoder().decode(it.value.withoutHeader)
            )

            val encoded = String(buf)

            assertEquals(it.value.json, encoded)        }
    }

    @TestFactory
    fun decryptWithHeader(): Collection<DynamicTest> = payloads.map {
        DynamicTest.dynamicTest("it should decrypt ${it.key} payload") {
            val buf = uk.co.thomasc.thealley.decryptWithHeader(
                Base64.getDecoder().decode(it.value.withHeader)
            )

            val encoded = String(buf)

            assertEquals(it.value.json, encoded)
        }
    }

    @TestFactory
    fun encrypt(): Collection<DynamicTest> = payloads.map {
        DynamicTest.dynamicTest("it should encrypt ${it.key} payload") {
            val buf = uk.co.thomasc.thealley.encrypt(
                it.value.json.toByteArray()
            )

            val encoded = String(Base64.getEncoder().encode(buf))

            assertEquals(it.value.withoutHeader, encoded)
        }
    }

    @TestFactory
    fun encryptWithHeader(): Collection<DynamicTest> = payloads.map {
        DynamicTest.dynamicTest("it should encrypt ${it.key} payload") {
            val buf = uk.co.thomasc.thealley.encryptWithHeader(
                it.value.json.toByteArray()
            )

            val encoded = String(Base64.getEncoder().encode(buf))

            assertEquals(it.value.withHeader, encoded)
        }
    }

}
