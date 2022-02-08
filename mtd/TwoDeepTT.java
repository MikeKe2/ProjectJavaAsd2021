package mtd;

import java.util.Random;

/**
 * Replacement schemes and two-level tables
 * Breuker, D.M.; Uiterwijk, J.W.H.M.; van den Herik, H.J.
 * Published in:  ICCA Journal
 */
/*
* The main transposition table is an array of hash elements.  Each hash element looks something like this:
The hash array is indexed via a Zobrist key.  You take the key for the position, modulo it by the number of elements in your table, and that's the hash element that corresponds to this position.  Since many positions are apt to map to the same element in the hash table, the table elements contain a verification value, which can be used to make sure that the element in the table is the one that you are trying to find.  An obvious verification value is the full 64-bit key, so that is the first field in the example above.
When you get a result from a search, and you want to store an element in the table, it is important to record how deep the search went, if you plan to use the hash table to avoid doing redundant work.  If you searched this position with a 3-ply search, and you come along later planning to do a 10-ply search, you can't assume that the information in this hash element is accurate.  So the search depth for this sub-tree is also recorded.
In an alpha-beta search, rarely do you get an exact value when you search a node.  "Alpha" and "beta" exist to help you prune out useless sub-trees, but the minor disadvantage to using alpha-beta is that you don't often know exactly how bad or good a node is, you just know that it is bad enough or good enough that you don't need to waste any more time on it.
Of course, this raises the question as to what value you store in the hash element, and what you can do with it when you retrieve it.  The answer is to store a value, and a flag that indicates what the value means.  In my example above, if you store, let's say, a 16 in the value field, and "hashfEXACT" in the flags field, this means that the value of the node was exactly 16.  If you store "hashfALPHA" in the flags field, the value of the node was at most 16.  If you store "hashfBETA", the value is at least 16.
It is pretty easy to figure out which value and flags to store given any particular circumstance you might encounter when searching, but it is important to avoid bugs.  Hashing bugs are easy to make, and once you make one they are very difficult to track down.
The final field in my hash element is the "best" move encountered last time I searched this position.  Sometimes I don't get a best move, like if everything failed low (returned a score <= alpha), but other times there is a definite best move, like when something fails high (returns a score >= beta).
If  a best move is found, it will be searched first.
Some sample code, overlaid onto an alpha-beta function, with changes highlighted:

* int AlphaBeta(int depth, int alpha, int beta)
{
    int hashf = hashfALPHA;
    if ((val = ProbeHash(depth, alpha, beta)) != valUNKNOWN)
        return val;
    if (depth == 0) {
        val = Evaluate();
        RecordHash(depth, val, hashfEXACT);
        return val;
    }
    GenerateLegalMoves();
    while (MovesLeft()) {
        MakeNextMove();
        val = -AlphaBeta(depth - 1, -beta, -alpha);
        UnmakeMove();
        if (val >= beta) {
            RecordHash(depth, beta, hashfBETA);
            return beta;
        }
        if (val > alpha) {
            hashf = hashfEXACT;
            alpha = val;
        }
    }
    RecordHash(depth, alpha, hashf);
    return alpha;
}
Here is the source for the two new functions::
*
As you can see, this isn't exactly rocket science, but it's possible to have bugs, and there are some nuances I haven't discussed.  If you do have bugs, they will be really, really bad bugs.
Replacement schemes
The most major nuance involves when to over-write a hash element.  In the example above, I use the scheme "always replace", which simply over-writes anything that was already there.  This may not be the best scheme, and in fact there has been a lot of work trying to figure out what the best scheme is.
Another scheme is "replace if same depth or deeper".  This leaves a currently existing node alone unless the depth attached the new one is greater than or equal to the depth of the one in the table.
There is lots of room for experimentation here.  In 1994 I asked a question about replacement scheme in the Usenet group rec.games.chess (now rec.games.chess.computer), and I received an answer from Ken Thompson.
His answer was to use two tables.  One uses the "replace always" scheme, and the other uses "replace if same depth or deeper".  When you probe, you probe both tables, and if one of them lets you cut off, you do it.  If neither of them let you cut off, you might at least get a best move from one of them, and in fact you might get two different ones, both of which should be tried first (or second).
When recording, you simply use the appropriate replacement scheme.
If you use the "replace if deeper or same depth" scheme, your table might eventually fill up with outdated deep nodes.  The solution to this is either to clear the table each time you move, or add a "sequence" field to your element, so the replacement scheme becomes, "replace if same depth, deeper, or the element pertains to an ancient search".*/



public class TwoDeepTT {
    /**
     * #define    hashfEXACT   0
     * #define    hashfALPHA   1
     * #define    hashfBETA    2
     * <p>
     * typedef struct tagHASHE {
     * U64 key;
     * int depth;
     * int flags;
     * int value;
     * MOVE best;
     * }   HASHE;
     */
    public class Element<Move> {
        private final long key;
        private final int depth;
        private final int score;
        private final Type flags;
        private final int i, j;

        private ZobristHashing zobristHashing;

        private enum Type {
            EXACT,
            LOWER,
            UPPER
        }

        protected Element(int depth, int score, Type flags, int i, int j) {
            this.key = ZobristKey();
            this.depth = depth;
            this.score = score;
            this.flags = flags;
            this.i = i;
            this.j = j;
        }

        private long ZobristKey() {
            return new Random().nextLong();
        }

        protected boolean lessDeepThan(Element element) {
            return depth < element.depth;
        }
    }

    /**
     * int ProbeHash(int depth, int alpha, int beta)
     * {
     * HASHE * phashe = &hash_table[ZobristKey() % TableSize()];
     * if (phashe->key == ZobristKey()) {
     * if (phashe->depth >= depth) {
     * if (phashe->flags == hashfEXACT)
     * return phashe->val;
     * if ((phashe->flags == hashfALPHA) &&
     * (phashe->val <= alpha))
     * return alpha;
     * if ((phashe->flags == hashfBETA) &&
     * (phashe->val >= beta))
     * return beta;
     * }
     * RememberBestMove();
     * }
     * return valUNKNOWN;
     * }
     */
    private Element firstElement, secondPosition;
    private ZobristHashing zobristHashing;

    public TwoDeepTT(Element element) {
        firstElement = element;
    }

    public Element retrieve(int depth, int alpha, int beta) {
        if (zobristHashing.hash())
    }

    public void store(Element element) {
        if (!element.lessDeepThan(firstElement)) {
            secondPosition = firstElement;
            firstElement = element;
        } else {
            secondPosition = element;
        }
    }
}

