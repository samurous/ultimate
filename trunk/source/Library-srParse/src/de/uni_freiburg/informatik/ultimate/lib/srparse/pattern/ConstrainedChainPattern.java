package de.uni_freiburg.informatik.ultimate.lib.srparse.pattern;

/**
 * {scope}, it is always the case that if "P" holds, then "Q" eventually holds and is succeeded by "R", where "S" does not hold between "T" and "U"
 *
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 *
 */
import java.util.List;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.CounterTrace;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;

public class ConstrainedChainPattern extends PatternType {

	public ConstrainedChainPattern(final SrParseScope scope, final String id, final List<CDD> cdds,
			final List<String> durations) {
		super(scope, id, cdds, durations);
	}

	@Override
	public CounterTrace transform(CDD[] cdds, int[] durations) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if (getId() != null) {
			sb.append(getId());
			sb.append(": ");
		}
		if (getScope() != null) {
			sb.append(getScope());
		}
		sb.append("it is always the case that if \"");
		sb.append(getCdds().get(5).toBoogieString());
		sb.append("\" holds, then \"");
		sb.append(getCdds().get(4).toBoogieString());
		sb.append("\" eventually holds and is succeeded by \"");
		sb.append(getCdds().get(3).toBoogieString());
		sb.append("\", where \"");
		sb.append(getCdds().get(2).toBoogieString());
		sb.append("\" does not hold between \"");
		sb.append(getCdds().get(1).toBoogieString());
		sb.append("\" and \"");
		sb.append(getCdds().get(0).toBoogieString());
		sb.append("\"");
		return sb.toString();
	}

	@Override
	public PatternType rename(final String newName) {
		return new ConstrainedChainPattern(getScope(), newName, getCdds(), getDuration());
	}
}
