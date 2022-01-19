import mnkgame.MNKPlayer;

public class yeet implements MNKPlayer{
    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs);
	
	/**
	 * Select a position among those listed in the <code>FC</code> array
	 *
	 * @param FC Free Cells: array of free cells
	 * @param MC Marked Cells: array of already marked cells, ordered with respect
   * to the game moves (first move is in the first position, etc)
   *
   * @return an element of <code>FC</code>
	 */
	public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC);	

  @Override
	public String playerName(){return "yeet";};
}
