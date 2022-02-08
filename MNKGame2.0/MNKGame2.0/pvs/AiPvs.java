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
        time = (long) timeout_in_secs * 1000;
        game = new Game(M, N, K);
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
            game.update(MC[MC.length - 2]);
        if (MC.length > 0)
            game.update(MC[MC.length - 1]);

        int alpha = game.initialAlpha(first);
        int beta = game.initialBeta(first);
        if (searcher.getGame().isGameOver())
            throw new IllegalStateException("Game over. No legal moves.");

        final int NUM_THREADS = Runtime.getRuntime().availableProcessors() + 1;
        final ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        long timeStart = System.currentTimeMillis();
        long timeEnd = timeStart + time;
        MnkGameSearcher.Result result = null;
        int depth = game.maxDepth();

        for (int i = 1; i <= depth / 2; i++) {
            MnkGameSearcher.Task task = new MnkGameSearcher.Task(searcher, FC, i, alpha, beta);
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

        MNKCell move;
        if (result != null) {
            move = result.getPrincipleVariationMove();
        } else {
            System.out.println("mh");
            move = generateRandomMove();
        }
        return move;
    }

    private MNKCell generateRandomMove() {
        return null;
    }
}
