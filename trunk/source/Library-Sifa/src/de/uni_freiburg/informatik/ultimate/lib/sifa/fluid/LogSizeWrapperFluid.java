/*
 * Copyright (C) 2019 Claus Schätzle (schaetzc@tf.uni-freiburg.de)
 * Copyright (C) 2019 University of Freiburg
 *
 * This file is part of the ULTIMATE Library-Sifa plug-in.
 *
 * The ULTIMATE Library-Sifa plug-in is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE Library-Sifa plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Library-Sifa plug-in. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Library-Sifa plug-in, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE Library-Sifa plug-in grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.lib.sifa.fluid;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.DagSizePrinter;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.predicates.IPredicate;

/**
 * Logs the size of a formula before asking another fluid whether to abstract or not.
 *
 * @author schaetzc@tf.uni-freiburg.de
 */
public class LogSizeWrapperFluid implements IFluid {

	private final IFluid mFluid;
	private final ILogger mLogger;

	public LogSizeWrapperFluid(final ILogger logger, final IFluid fluid) {
		mFluid = fluid;
		mLogger = logger;
	}

	@Override
	public boolean shallBeAbstracted(final IPredicate predicate) {
		final boolean applyAlpha = mFluid.shallBeAbstracted(predicate);
		if (mLogger.isDebugEnabled()) {
			mLogger.debug("Predicate has dag size %s and %d disjunct(s). Abstraction %s be applied.",
					new DagSizePrinter(predicate.getFormula()),
					SizeLimitFluid.numberOfDisjuncts(predicate.getFormula()),
					applyAlpha ? "will" : "won't");
		}
		return applyAlpha;
	}

}
