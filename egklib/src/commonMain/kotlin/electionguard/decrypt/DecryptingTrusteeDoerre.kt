package electionguard.decrypt

import electionguard.core.ElementModP
import electionguard.core.ElementModQ
import electionguard.core.GroupContext
import electionguard.core.randomElementModQ

/**
 * A Trustee that has a share of the election private key, for the purpose of decryption.
 * DecryptingTrustee must stay private. Guardian is its public info in the election record.
 */
data class DecryptingTrusteeDoerre(
    val id: String,
    val xCoordinate: Int,
    val publicKey: ElementModP,
    val keyShare: ElementModQ, // My share of the election key, P(i)
) : DecryptingTrusteeIF {

    init {
        require(xCoordinate > 0)
    }

    override fun id(): String = id
    override fun xCoordinate(): Int = xCoordinate
    override fun electionPublicKey(): ElementModP = publicKey

    override fun decrypt(
        group: GroupContext,
        texts: List<ElementModP>,
    ): List<PartialDecryption> {
        val results: MutableList<PartialDecryption> = mutableListOf()
        for (text: ElementModP in texts) {
            val u = group.randomElementModQ(2)
            val a = group.gPowP(u)
            val b = text powP u
            val mi = text powP keyShare // M = A ^ P(i), eq 6
            results.add(PartialDecryption(id, mi, u, a, b)) // controversial to send u, could cache it here.
        }
        return results
    }

    override fun challenge(
        group: GroupContext,
        challenges: List<ChallengeRequest>,
    ): List<ChallengeResponse> {
        return challenges.map {
            ChallengeResponse(it.id, it.nonce - it.challenge * keyShare) // eq 12
        }
    }
}