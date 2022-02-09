package pvs;

import mnkgame.MNKCell;
import mnkgame.MNKPlayer;

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

        long timeStart = System.currentTimeMillis();
        long timeEnd = timeStart + time;
        int depth = game.maxDepth();

        MnkGameSearcher.Result result = null;

        for (int i = 1; i <= depth; i++) {
            System.out.println("Depth: " + depth);
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
            System.out.println("mh");
            move = generateRandomMove();
        }
        System.out.println("row:" + game.getRow(move) + " column: "+ game.getCol(move));
        return new MNKCell(game.getRow(move), game.getCol(move));
    }

    private int generateRandomMove() {
        return 0;
    }
}
