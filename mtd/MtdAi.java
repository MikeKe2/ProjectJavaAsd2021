package mtd;

import mnkgame.MNKCell;
import mnkgame.MNKGame;
import mnkgame.MNKPlayer;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class MtdAi implements MNKPlayer {

    public static final int MIN_TIME = 10;
    public static final int MAX_TIME = Integer.MAX_VALUE;

    private long time, timeEnd;
    private Game game;

    private MnkGameSearcher searcher;
    private MnkGameEvaluator evaluator;

    private HashMap<Integer, TwoDeepTT> transpositionMap;

    @Override
    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        time = (long) timeout_in_secs * 1000;
        game = new Game(first ? 0 : 1, M, N, K);
        searcher = new MnkGameSearcher(game, evaluator);
    }

    @Override
    public String playerName() {
        return "hi :)";
    }

    public Game getGame() {
        return searcher.getGame();
    }

    /**
     * public void setMaxDepth(int depth) {
     * if (depth < MIN_DEPTH || depth > MAX_DEPTH)
     * throw new IllegalArgumentException("Invalid search depth: " + depth);
     * this.depth = depth;
     * }
     * <p>
     * public void setMaxTime(int time) {
     * if (time < MIN_TIME || time > MAX_TIME)
     * throw new IllegalArgumentException("Invalid search time: " + time);
     * this.time = time;
     * }
     */
    private boolean hasTime() {
        return System.currentTimeMillis() < timeEnd;
    }

    @Override
    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) throws ExecutionException {
        //Iterative deepening

        Game game1 = game;

        MNKCell lastMove = new MNKCell(MC[MC.length - 1].i, MC[MC.length - 1].j);
        game.update(lastMove);

        MNKCell result = null;
        int depth = getGame().maxDepth();

        timeEnd = System.currentTimeMillis() + time;

        for (int i = 0; hasTime() && (i < depth); i++) {
            try {
                result = MTDF(game, result, i);
            } catch (TimeoutException e) {
                //return generateRandomMove();
            }

        }
        return result;

        /*
        //firstguess := 0;
        MNKCell bestMove, lastMove;
        lastMove = new MNKCell(MC[MC.length - 1].i, MC[MC.length - 1].j);
        game.update(lastMove);

        int depth = game.maxDepth();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        long timeEnd = System.currentTimeMillis() + time;

        MnkGameSearcher.Result result = null;
        //for d = 1 to MAX_SEARCH_DEPTH do
        for (int i = 0; i < depth; i++) {
            //firstguess := MTDF(root, firstguess, d);
            MnkGameSearcher.Task task = new MnkGameSearcher.Task(searcher, i);

            //Start the task in another thread
            //activate MTD(f)
            Future<MnkGameSearcher.Result> future = executor.submit(task);

            long timeRemaining = timeEnd - System.currentTimeMillis();
            //if times_up() then break;
            try {
                //Waits if necessary for at most the given time for the computation to complete,
                //and then retrieves its result, if available.
                result = future.get(timeRemaining, TimeUnit.MILLISECONDS);
            } catch (TimeoutException | InterruptedException e) {
                break;
            } catch (ExecutionException e) {
                executor.shutdown();
                throw e;
            } finally {
                future.cancel(true);
            }

            if (result.isProvenResult())
                break;

        }
        executor.shutdown();
        //if times_up() then break;
        if (result != null) {
            bestMove = result.getPrincipleVariationMove();
            return bestMove;
        }

        private int generateRandomMove () {
            Random rand = new Random();

            int i = 0;
            int totalMoves = getGame().getLegalMoves();
            for (int move : getGame().generateLegalMoves()) {
                if (rand.nextInt(totalMoves - i) == 0)
                    return move;
                i++;
            }

            throw new IllegalStateException("Failed to generate move.");
        }
*/
    }

    private int MTDF(MNKGame game, MNKCell firstGuess, int depth) {
        //g := f;
        //upperbound := +INFINITY;
        //lowerbound := -INFINITY;
        //repeat
        //
        //    if g == lowerbound then beta := g + 1 else beta := g;
        //    g := AlphaBetaWithMemory(root, beta - 1, beta, d);
        //    if g < beta then upperbound := g else lowerbound := g;
        //
        //until lowerbound >= upperbound;
        //return g;
        int g = firstGuess.MtdAi;
        transpositionMap.clear();
        int[] bound = new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE};
        do {
            int beta = (g == bound[0]) ? g + 1 : g;
            g = AlphaBetaWithMemory(root, beta - 1, beta, depth);
            bound[g < beta ? 0 : 1] = firstGuess;
        } while (bound[0] < bound[1]);

        return g;
    }

    private int AlphaBetaWithMemory(MNKCell root, int i, int beta, int depth) {
    }
}
