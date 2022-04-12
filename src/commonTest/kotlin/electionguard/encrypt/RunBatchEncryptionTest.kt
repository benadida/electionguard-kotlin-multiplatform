package electionguard.encrypt

import kotlin.test.Test

class RunBatchEncryptionTest {

    @Test
    fun testRunBatchEncryptionTest() {
        main(
            arrayOf(
                "-in",
                "src/commonTest/data/testJava/kickstart/encryptor",
                "-ballots",
                "src/commonTest/data/testJava/kickstart/encryptor/election_private_data/plaintext_ballots",
                "-out",
                "/home/snake/tmp/electionguard/native/runBatchEncryption",
                "-invalidBallots",
                "/home/snake/tmp/electionguard/native/runBatchEncryption/election_private_data/invalid_ballots",
                "-device",
                "CountyCook-precinct079-device24358"
            )
        )
    }
}