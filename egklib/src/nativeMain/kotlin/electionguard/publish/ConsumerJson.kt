package electionguard.publish

import com.github.michaelbull.result.Result
import electionguard.ballot.DecryptionResult
import electionguard.ballot.ElectionConfig
import electionguard.ballot.ElectionInitialized
import electionguard.ballot.PlaintextBallot
import electionguard.ballot.DecryptedTallyOrBallot
import electionguard.ballot.EncryptedBallot
import electionguard.ballot.TallyResult
import electionguard.core.GroupContext
import electionguard.decrypt.DecryptingTrusteeIF
// implements the public API
actual class ConsumerJson actual constructor(private val topDir: String, private val group: GroupContext) : Consumer {
    private val path = ElectionRecordProtoPaths(topDir)

    init {
        if (!exists(topDir)) {
            throw RuntimeException("Non existent directory $topDir")
        }
    }

    actual override fun topdir(): String {
        return this.topDir
    }

    actual override fun isJson() = true

    actual override fun readElectionConfig(): Result<ElectionConfig, String> {
        return readElectionConfig(path.electionConfigPath())
    }

    actual override fun readElectionInitialized(): Result<ElectionInitialized, String> {
        return group.readElectionInitialized(path.electionInitializedPath())
    }

    actual override fun readTallyResult(): Result<TallyResult, String> {
        return group.readTallyResult(path.tallyResultPath())
    }

    actual override fun readDecryptionResult(): Result<DecryptionResult, String> {
        return group.readDecryptionResult(path.decryptionResultPath())
    }

    actual override fun hasEncryptedBallots(): Boolean {
        return exists(path.encryptedBallotPath())
    }

    actual override fun iterateEncryptedBallots(filter : ((EncryptedBallot) -> Boolean)?): Iterable<EncryptedBallot> {
        if (!exists(path.encryptedBallotPath())) {
            return emptyList()
        }
        return Iterable { EncryptedBallotIterator(group, path.encryptedBallotPath(), null, filter) }
    }

    actual override fun iterateCastBallots(): Iterable<EncryptedBallot> {
        if (!exists(path.encryptedBallotPath())) {
            return emptyList()
        }
        return Iterable { EncryptedBallotIterator(group, path.encryptedBallotPath(),
            { it.state === electionguard.protogen.EncryptedBallot.BallotState.CAST }, null)
        }
    }

    actual override fun iterateSpoiledBallots(): Iterable<EncryptedBallot> {
        if (!exists(path.encryptedBallotPath())) {
            return emptyList()
        }
        return Iterable { EncryptedBallotIterator(group, path.encryptedBallotPath(),
            { it.state === electionguard.protogen.EncryptedBallot.BallotState.SPOILED }, null)
        }
    }

    actual override fun iterateDecryptedBallots(): Iterable<DecryptedTallyOrBallot> {
        if (!exists(path.spoiledBallotPath())) {
            return emptyList()
        }
        return Iterable { SpoiledBallotTallyIterator(group, path.spoiledBallotPath())}
    }

    actual override fun iteratePlaintextBallots(
        ballotDir : String,
        filter : ((PlaintextBallot) -> Boolean)?
    ): Iterable<PlaintextBallot> {
        return Iterable { PlaintextBallotIterator(path.plaintextBallotPath(ballotDir), filter) }
    }

    actual override fun readTrustee(trusteeDir: String, guardianId: String): DecryptingTrusteeIF {
        val filename = path.decryptingTrusteePath(trusteeDir, guardianId)
        return group.readTrustee(filename)
    }

}