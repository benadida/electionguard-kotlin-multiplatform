package electionguard.decrypt

import electionguard.ballot.AvailableGuardian
import electionguard.core.ElementModP
import electionguard.core.GenericChaumPedersenProof
import electionguard.core.compatibleContextOrFail
import electionguard.core.multP

/** Partial decryptions from one DecryptingTrustee, includes both direct and compensated decryptions */
class DecryptionShare(
    val decryptingTrustee: String, // who did these Decryptions?
) {
    val partialDecryptions: MutableMap<String, DirectDecryption> = mutableMapOf()
    val compensatedDecryptions: MutableMap<String, CompensatedDecryption> = mutableMapOf()

    fun addPartialDecryption(contestId: String, selectionId: String, decryption: DirectDecryption): DecryptionShare {
        // LOOK test to see if there are duplicates?
        partialDecryptions["${contestId}#@${selectionId}"] = decryption
        return this
    }

    fun addMissingDecryption(
        contestId: String,
        selectionId: String,
        missingGuardian: String,
        decryption: MissingPartialDecryption
    ): DecryptionShare {
        var existing = compensatedDecryptions["${contestId}#@${selectionId}"]
        if (existing == null) {
            existing = CompensatedDecryption(selectionId)
            compensatedDecryptions["${contestId}#@${selectionId}"] = existing
        }
        // LOOK test to see if there are duplicates?
        existing.missingDecryptions[missingGuardian] = decryption
        return this
    }
}

data class DirectDecryption(
    val selectionId: String,
    val guardianId: String,
    val share: ElementModP,
    val proof: GenericChaumPedersenProof,
)

class CompensatedDecryption(
    val selectionId: String,
) {
    // keyed by missing guardian id
    val missingDecryptions: MutableMap<String, MissingPartialDecryption> = mutableMapOf()
}

data class MissingPartialDecryption(
    val decryptingGuardianId: String,
    val missingGuardianId: String,
    val share: ElementModP,  // M_𝑖,ℓ
    val recoveryKey: ElementModP,
    val proof: GenericChaumPedersenProof
)

// heres where all the direct and compensated decryptions are accumulated
class PartialDecryption(
    val selectionId: String,
    val guardianId: String, // share for this guardian
    var share: ElementModP?, // M_𝑖 set by direct decryption, else computed from missingDecryptions
    val proof: GenericChaumPedersenProof?,
    recovered: List<MissingPartialDecryption>?
) {
    // When guardian is missing there will be quorum of these
    val missingDecryptions: MutableList<MissingPartialDecryption> = mutableListOf()

    init {
        if (recovered != null) {
            missingDecryptions.addAll(recovered)
        }
    }

    constructor(guardianId: String, partial: DirectDecryption) :
            this(partial.selectionId, guardianId, partial.share, partial.proof, null)

    constructor(guardianId: String, partial: CompensatedDecryption) :
            this(partial.selectionId, guardianId, null, null, null)

    fun add(recovered: MissingPartialDecryption): PartialDecryption {
        missingDecryptions.add(recovered)
        return this
    }

    fun lagrangeInterpolation(guardians: List<AvailableGuardian>) {
        if (share == null && missingDecryptions.isEmpty()) {
            throw IllegalStateException("PartialDecryption $selectionId has neither share nor missingDecryptions")
        }
        if (share != null && missingDecryptions.isNotEmpty()) {
            throw IllegalStateException("PartialDecryption $selectionId has both share and missingDecryptions")
        }
        // the quardians and missingDecryptions are sorted by guardianId, so can use index to match with
        // 𝑀_𝑖 = ∏ ℓ∈𝑈 (𝑀_𝑖,ℓ) mod 𝑝, where 𝑀_𝑖,ℓ = 𝐴^𝑃_𝑖(ℓ) mod 𝑝.
        if (missingDecryptions.isNotEmpty()) {
            val shares = missingDecryptions.sortedBy { it.decryptingGuardianId }.mapIndexed { idx, value ->
                value.share powP guardians[idx].lagrangeCoordinate
            }
            val context = compatibleContextOrFail(*shares.toTypedArray())
            shares.toTypedArray()
            share = context.multP(*shares.toTypedArray())
        }
    }

    fun share(): ElementModP {
        return share!!
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PartialDecryption

        if (selectionId != other.selectionId) return false
        if (guardianId != other.guardianId) return false
        if (share != other.share) return false
        if (proof != other.proof) return false
        if (missingDecryptions != other.missingDecryptions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = selectionId.hashCode()
        result = 31 * result + guardianId.hashCode()
        result = 31 * result + (share?.hashCode() ?: 0)
        result = 31 * result + (proof?.hashCode() ?: 0)
        result = 31 * result + missingDecryptions.hashCode()
        return result
    }
}

/** Direct decryption from the Decrypting Trustee */
data class DirectDecryptionAndProof(
    val partialDecryption: ElementModP,
    val proof: GenericChaumPedersenProof)

/** Compensated decryption from the Decrypting Trustee */
data class CompensatedDecryptionAndProof(
    val partialDecryption: ElementModP, // used in the calculation. LOOK encrypt ??
    val proof: GenericChaumPedersenProof,
    val recoveredPublicKeyShare: ElementModP) // g^Pi(ℓ), used in the verification