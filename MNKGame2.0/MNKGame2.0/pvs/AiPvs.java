package pvs;

import mnkgame.MNKCell;
import mnkgame.MNKPlayer;

import java.util.concurrent.*;

public class AiPvs implements MNKPlayer {

    public static final int MIN_TIME = 10;
    public static final int MAX_TIME = Integer.MAX_VALUE;

    private long time, timeEnd;
    private Game game;

    private MnkGameSearcher searcher;
    private MnkGameEvaluator evaluator;


    @Override
    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        time = (long) timeout_in_secs * 1000;
        game = new Game(first ? 0 : 1, M, N, K);

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


        if (searcher.getGame().isGameOver())
            throw new IllegalStateException("Game over. No legal moves.");


        ExecutorService executor = Executors.newSingleThreadExecutor();
        long timeStart = System.currentTimeMillis();
        long timeEnd = timeStart + time;
        MnkGameSearcher.Result result = null;
        int depth = game.maxDepth();


        for (int i = 1; i <= depth; i++) {
            MnkGameSearcher.Task task = new MnkGameSearcher.Task(searcher, FC, i);
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
