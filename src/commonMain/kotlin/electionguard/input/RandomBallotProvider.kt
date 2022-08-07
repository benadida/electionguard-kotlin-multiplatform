package electionguard.input

import electionguard.ballot.Manifest
import electionguard.ballot.PlaintextBallot
import kotlin.random.Random

/** Create nballots randomly generated fake Ballots, used for testing.  */
class RandomBallotProvider(election: Manifest, nballots: Int?) {
    private val nballots: Int
    private val election: Manifest

    init {
        this.election = election
        this.nballots = if (nballots != null && nballots > 0) nballots else 11
    }

    fun ballots(): List<PlaintextBallot> {
        val ballotFactory = BallotFactory()
        val ballots: MutableList<PlaintextBallot> = ArrayList()
        for (i in 0 until nballots) {
            val ballot_id = "ballot-id-" + Random.nextInt()
            ballots.add(ballotFactory.getFakeBallot(election, ballot_id))
        }
        return ballots
    }

    private class BallotFactory {
        fun getFakeBallot(manifest: Manifest, ballot_id: String): PlaintextBallot {
            val contests: MutableList<PlaintextBallot.Contest> = ArrayList()
            for (contestp in manifest.contests) {
                contests.add(getRandomContestFrom(contestp))
            }
            return PlaintextBallot(ballot_id, "styling", contests)
        }

        fun getRandomContestFrom(contest: Manifest.ContestDescription): PlaintextBallot.Contest {
            var voted = 0
            val selections: MutableList<PlaintextBallot.Selection> = ArrayList()
            for (selection_description in contest.selections) {
                val selection: PlaintextBallot.Selection = getRandomSelectionFrom(selection_description)
                voted += selection.vote
                if (voted <= contest.votesAllowed) {
                    selections.add(selection)
                }
            }
            return PlaintextBallot.Contest(
                contest.contestId,
                contest.sequenceOrder,
                selections
            )
        }

        companion object {
            fun getRandomSelectionFrom(description: Manifest.SelectionDescription): PlaintextBallot.Selection {
                val choice: Boolean = Random.nextBoolean()
                return PlaintextBallot.Selection(
                    description.selectionId, description.sequenceOrder,
                    if (choice) 1 else 0, null
                )
            }
        }
    }
}