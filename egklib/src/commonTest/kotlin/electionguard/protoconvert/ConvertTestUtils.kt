package electionguard.protoconvert

import electionguard.core.*
import io.ktor.utils.io.core.*
import kotlin.random.Random
import kotlin.random.nextUInt

fun generateRangeChaumPedersenProofKnownNonce(
    context: GroupContext
): RangeChaumPedersenProofKnownNonce {
    return RangeChaumPedersenProofKnownNonce(
        listOf(generateGenericChaumPedersenProof(context)),
    )
}

fun generateGenericChaumPedersenProof(context: GroupContext): GenericChaumPedersenProof {
    return GenericChaumPedersenProof(generateElementModQ(context), generateElementModQ(context),)
}

fun generateSchnorrProof(context: GroupContext): SchnorrProof {
    return SchnorrProof(
        generateElementModP(context),
        generateElementModQ(context),
        generateElementModQ(context),
    )
}

fun generateCiphertext(context: GroupContext): ElGamalCiphertext {
    return ElGamalCiphertext(generateElementModP(context), generateElementModP(context))
}

fun generateHashedCiphertext(context: GroupContext): HashedElGamalCiphertext {
    return HashedElGamalCiphertext(generateElementModP(context), "what".toByteArray(), generateUInt256(context), 42)
}

fun generateElementModQ(context: GroupContext): ElementModQ {
    return context.uIntToElementModQ(Random.nextUInt(134217689.toUInt()))
}

fun generateUInt256(context: GroupContext): UInt256 {
    return generateElementModQ(context).toUInt256();
}

fun generateElementModP(context: GroupContext): ElementModP {
    return context.uIntToElementModP(Random.nextUInt(1879047647.toUInt()))
}