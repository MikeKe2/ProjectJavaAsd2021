package mtd;

import mnkgame.MNKCell;
import mnkgame.MNKPlayer;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.*;

public class MtdAi implements MNKPlayer {

    public static final int MIN_TIME = 10;
    public static final int MAX_TIME = Integer.MAX_VALUE;

    private int time;
    private Game game;

    private MnkGameSearcher searcher;
    private MnkGameEvaluator evaluator;

    final int capacity;
    private final HashMap<Integer, MNKCell> transpositionMap;


    @Override
    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        /**/
        time = timeout_in_secs * 1000;
        game = new Game(first ? 0 : 1, M, N, K);

        searcher = new MnkGameSearcher(game, evaluator);
        capacity = findCapacity();
        transpositionMap = new HashMap<>(capacity);
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

    @Override
    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) throws ExecutionException {

        searcher.update(MC[MC.length - 1]);

        if (searcher.getGame().isGameOver())
            throw new IllegalStateException("Game over. No legal moves.");

        int depth = game.maxDepth();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        long timeStart = System.currentTimeMillis();
        long timeEnd = timeStart + time;

        MnkGameSearcher.Result result = null;

        for (int i = 1; i <= depth; i++) {

            MnkGameSearcher.Task task = new MnkGameSearcher.Task(searcher, i);
            Future<MnkGameSearcher.Result> future = executor.submit(task);
            long timeRemaining = timeEnd - System.currentTimeMillis();

            try {
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

        int move;
        if (result != null) {
            move = result.getPrincipleVariationMove();
        } else {
            move = generateRandomMove();
        }
        return new MNKCell(getGame().getRow(move), getGame().getCol(move));
    }

    private int generateRandomMove() {
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
}
