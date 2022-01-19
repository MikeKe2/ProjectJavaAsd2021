package monkey.mnk;

/**
 * A <code>Threat</code> can refer to one of the six types of configurations.
 * See A. Abdoulaye, V. R. Houndji, E. C. Ezin, G. Aglin, <i>Generic Heuristic
 * for the mnk-games</i>, in A. E. Badouel, N. Gmati, B. Watson (eds),
 * <i>Proceedings of CARI 2018 (African Conference on Research in Computer
 * Science and Applied Mathematics). Nabil Gmati; Eric Badouel; Bruce Watson.
 * CARI 2018 - Colloque africain sur la recherche en informatique et
 * mathématiques appliquées</i>, Oct 2018, Stellenbosch, South Africa. 2018, pp.
 * 266-267. hal-01881376f
 *
 * @author Gaia Clerici
 * @version 1.0
 * @since 1.0
 */
public enum Threat {
	/** N consecutive pieces whose extremities are free. */
	ONE,
	/**
	 * N consecutive pieces whose location at one extremity is free while the second
	 * is occupied.
	 */
	TWO,
	/** N consecutive pieces whose extremities are occupied. */
	THREE,
	/**
	 * N aligned pieces with a single jump location and whose extremities are free.
	 */
	FOUR,
	/**
	 * N aligned pieces with a single jump location and whose location at one
	 * extremity is free while the second is occupied.
	 */
	FIVE,
	/**
	 * N aligned pieces with a single jump location and whose extremities are
	 * occupied.
	 */
	SIX;

	/** The number of types of {@link Threat}s. */
	public final static int SIZE = SIX.ordinal() + 1;

	/**
	 * The types of configuration are partitioned so that each of them has its own
	 * <code>Category</code>.
	 *
	 * @author Gaia Clerici
	 * @version 1.0
	 * @since 1.0
	 */
	public enum Category {

		/** The most menacing category. */
		OPEN,
		/** A mildly threatening category. */
		HALFOPEN,
		/** A not-really-dangerous category. */
		CLOSED

	}

	/**
	 * Checks whether this kind of {@link Threat} features a hole or not.
	 *
	 * @return <code>true</code> if there is a hole, <code>false</code> otherwise.
	 * @author Gaia Clerici
	 * @version 1.0
	 * @since 1.0
	 */
	public boolean hasHole() {
		return this == Threat.FOUR || this == Threat.FIVE || this == Threat.SIX;
	}

}
