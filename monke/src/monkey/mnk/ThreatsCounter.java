package monkey.mnk;

import monkey.ai.Player;

/**
 * A <code>ThreatsCounter</code> keeps track of the number of {@link Threat}s of
 * a certain type.
 */
public class ThreatsCounter implements Cloneable {

	/** The kind of {@link Threat} to keep track of. */
	final public Threat THREAT;

	/**
	 * Constructs a new {@link ThreatsCounter}.
	 */
	public ThreatsCounter(Threat threat) {
		THREAT = threat;
	}

	/**
	 * Creates a clone of this {@link ThreatsCounter}.
	 */
	public ThreatsCounter clone() {
		try {
			return (ThreatsCounter) super.clone();
		} catch (CloneNotSupportedException e) {
			// Should never happen: we support clone
			throw new InternalError(e.toString());
		}
	}

	/**
	 * Increments a given {@link monkey.ai.Player Player}'s counter. Does not
	 * increment anything if such {@link monkey.ai.Player Player} is <code>
	 * null</code>.
	 * @param player Specifies whose counter is to be incremented.
	 */
	public void increment(Player player) {
		// if (player == null)
		// throw new NullPointerException("The player can't be null.");
		if (Player.P1 == player)
			p1counter++;
		else
			p2counter++;
	}

	/**
	 * Decrements a given {@link monkey.ai.Player Player}'s counter. Does not
	 * increment anything if such {@link monkey.ai.Player Player} is <code>
	 * null</code>.
	 * @param player Specifies whose counter is to be decremented.
	 */
	public void decrement(Player player) {
		// if (player == null)
		// throw new NullPointerException("The player can't be null.");
		if (Player.P1 == player) {
			// if (p1counter == 0)
			// throw new IllegalCallerException("The counter can't be negative.");
			--p1counter;
		} else {
			// if (p2counter == 0)
			// throw new IllegalCallerException("The counter can't be negative.");
			--p2counter;
		}
	}

	/**
	 * Returns the current value of a given {@link monkey.ai.Player Player}'s
	 * counter.
	 * @param player Specifies whose counter is to be decremented. Cannot be
	 *               <code>null</code>.
	 */
	public int get(Player player) {
		// if (player == null)
		// throw new NullPointerException("The player can't be null.");
		return Player.P1 == player ? p1counter : p2counter;
	}

	/**
	 * Returns a string representation of the object.
	 */
	@Override
	public String toString() {
		return THREAT + ": " + p1counter + " - " + p2counter;
	}

	/** Number of {@link Threat}s by the first {@link monkey.ai.Player Player}. */
	private int p1counter = 0;
	/** Number of {@link Threat}s by the second {@link monkey.ai.Player Player}. */
	private int p2counter = 0;

}
