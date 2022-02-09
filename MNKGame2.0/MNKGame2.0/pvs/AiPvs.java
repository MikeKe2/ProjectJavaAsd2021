package pvs;

import mnkgame.MNKCell;
import mnkgame.MNKPlayer;

import java.util.Random;
import java.util.concurrent.*;

public class AiPvs implements MNKPlayer {

    private long time;
    private Game game;
    private boolean first;

    private MnkGameSearcher searcher;
    private MnkGameEvaluator evaluator;


    @Override
    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        game = new Game(M, N, K);

        this.time = (long) timeout_in_secs * 1000;
        this.first = first;

        evaluator = new MnkGameEvaluator(game);
        searcher = new MnkGameSearcher(game, evaluator);
    }

    @Override
    public String playerName() {
        return "hi :)";
    }

    @Override
    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) throws ExecutionException {
        if (MC.length > 1)
            game.doMove(MC[MC.length - 2]);
        if (MC.length > 0)
            game.doMove(MC[MC.length - 1]);

        if (searcher.getGame().isGameOver())
            throw new IllegalStateException("Game over. No legal moves.");

        final int NUM_THREADS = Runtime.getRuntime().availableProcessors() + 1;
        final ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        long nodesStart = searcher.getNodeCount();

        long timeStart = System.currentTimeMillis();
        long timeEnd = timeStart + time + 10000;

        int depth = game.maxDepth();

        MnkGameSearcher.Result result = null;
        printSearchResultHeader();
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
            printSearchResult(result, i, System.currentTimeMillis() - timeStart, searcher.getNodeCount() - nodesStart);
            if (result.isProvenResult())
                break;
        }
        executor.shutdown();

        int move;
        if (result != null) {
            move = result.getPrincipleVariationMove();

        } else {
            move = generateRandomMove(FC.length);
        }
        return new MNKCell(game.getRow(move), game.getCol(move));
    }

    private void printSearchResultHeader() {
        System.out.println("Depth\tTime\tNodes\tScore\tVariation");
    }

    private void printSearchResult(MnkGameSearcher.Result r, int d, long t, long n) {
        System.out.printf("%d\t\t", d);
        System.out.printf("%.3f\t\t", t / 1000.0);
        System.out.printf("%d\t\t", n);
        if (r.isProvenResult()) {
            String result;
            int distance;
            if (r.getScore() != 0) {
                result = "win";
                distance = Math.abs(Math.abs(r.getScore()));
            } else {
                result = "draw";
                distance = game.getPseudoLegalMoves(); // remaining turns left
            }
            System.out.printf("%s-%d\t\t", result, distance);
        } else {
            System.out.printf("%d\t\t", r.getScore());
        }
        for (int move : r.getPrincipleVariation()) {
            int row = game.getRow(move);
            int col = game.getCol(move);
            System.out.print(row + "," + col + " ");
        }
        System.out.println();
    }

    private int generateRandomMove(int length) {
        Random rand = new Random();

        int i = 0;
        for (int move : game.generateMoves()) {
            if (rand.nextInt(length - i) == 0)
                return move;
            i++;
        }

        throw new IllegalStateException("Failed to generate move.");
    }
}
