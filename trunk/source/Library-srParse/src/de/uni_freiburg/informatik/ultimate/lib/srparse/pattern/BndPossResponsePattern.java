package de.uni_freiburg.informatik.ultimate.lib.srparse.pattern;

import java.util.List;

import de.uni_freiburg.informatik.ultimate.lib.pea.CDD;
import de.uni_freiburg.informatik.ultimate.lib.pea.CounterTrace;
import de.uni_freiburg.informatik.ultimate.lib.srparse.SrParseScope;

/**
 * {scope}, if "R" holds, then there is at least one execution sequence such that "S" holds after at most "c1" time
 * units
 *
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * 
 */
public class BndPossResponsePattern extends PatternType {

	public BndPossResponsePattern(final SrParseScope scope, final String id, final List<CDD> cdds,
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
		sb.append("if \"");
		sb.append(getCdds().get(1).toBoogieString());
		sb.append("\" holds, then there is at least one execution sequence such that \"");
		sb.append(getCdds().get(0).toBoogieString());
		sb.append("\" holds after at most \"");
		sb.append(getDuration().get(0));
		sb.append("\" time units");
		return sb.toString();
	}

	@Override
	public PatternType rename(final String newName) {
		return new BndPossResponsePattern(getScope(), newName, getCdds(), getDuration());
	}
}
