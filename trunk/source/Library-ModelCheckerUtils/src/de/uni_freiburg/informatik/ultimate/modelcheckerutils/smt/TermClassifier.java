/*
 * Copyright (C) 2017 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2017 University of Freiburg
 *
 * This file is part of the ULTIMATE ModelCheckerUtils Library.
 *
 * The ULTIMATE ModelCheckerUtils Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE ModelCheckerUtils Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE ModelCheckerUtils Library. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE ModelCheckerUtils Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE ModelCheckerUtils Library grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt;

import java.util.HashSet;
import java.util.Set;

import de.uni_freiburg.informatik.ultimate.logic.AnnotatedTerm;
import de.uni_freiburg.informatik.ultimate.logic.ApplicationTerm;
import de.uni_freiburg.informatik.ultimate.logic.ConstantTerm;
import de.uni_freiburg.informatik.ultimate.logic.LetTerm;
import de.uni_freiburg.informatik.ultimate.logic.NonRecursive;
import de.uni_freiburg.informatik.ultimate.logic.QuantifiedFormula;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.logic.TermVariable;
import de.uni_freiburg.informatik.ultimate.smtinterpol.util.DAGSize;

/**
 * Check for a bunch of criteria if they are satisfied by a given {@link Term}.
 *
 * @author Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 *
 */
public class TermClassifier extends NonRecursive {

	private Set<Term> mTermsInWhichWeAlreadyDescended;

	private final Set<String> mOccuringSortNames;
	private final Set<String> mOccuringFunctionNames;
	private final Set<Integer> mOccuringQuantifiers;
	private boolean mHasArrays;

	private int mNumberOfVariables;
	private int mNumberOfFunctions;
	private int mNumberOfQuantifiers;
	private int mSize;

	private String mTerm;

	public TermClassifier() {
		super();
		mOccuringSortNames = new HashSet<>();
		mOccuringFunctionNames = new HashSet<>();
		mOccuringQuantifiers = new HashSet<>();
		mHasArrays = false;
		mNumberOfVariables = 0;
		mNumberOfFunctions = 0;
		mNumberOfQuantifiers = 0;
		mSize = 0;
		mTerm = "";
	}

	public Set<String> getOccuringSortNames() {
		return mOccuringSortNames;
	}

	public Set<String> getOccuringFunctionNames() {
		return mOccuringFunctionNames;
	}

	public Set<Integer> getOccuringQuantifiers() {
		return mOccuringQuantifiers;
	}

	public int getNumberOfVariables() {
		return mNumberOfVariables;
	}

	public int getNumberOfFunctions() {
		return mNumberOfFunctions;
	}

	public int getNumberOfQuantifiers() {
		return mNumberOfQuantifiers;
	}

	public boolean hasArrays() {
		return mHasArrays;
	}

	public String getTerm() {
		return mTerm;
	}
	public int getSize() {
		return mSize;
	}

	public String getStats() {
		StringBuilder sb = new StringBuilder();
		sb.append("Formula ").append(mTerm).append("\n");
		sb.append("Occuring sorts ").append(mOccuringSortNames.toString()).append("\n");
		sb.append("Occuring functions  ").append(mOccuringFunctionNames.toString()).append("\n");
		sb.append("Occuring Quantifiers  ").append(mOccuringQuantifiers.toString()).append("\n");
		sb.append("Size  ").append(mSize).append("\n");
		sb.append("Number of functions ").append(mNumberOfFunctions).append("\n");
		sb.append("Number of quantifiers ").append(mNumberOfQuantifiers).append("\n");
		sb.append("Number of variables ").append(mNumberOfVariables).append("\n");
		return sb.toString();
	}

	/**
	 * Check a/another Term and add the result to the existing classification results.
	 */
	public void checkTerm(final Term term) {
		mTermsInWhichWeAlreadyDescended = new HashSet<>();
		mTerm = term.toString();
		mSize = new DAGSize().size(term);
		run(new MyWalker(term));
		mTermsInWhichWeAlreadyDescended = null;
	}

	private class MyWalker extends TermWalker {
		MyWalker(final Term term) {
			super(term);
		}

		@Override
		public void walk(final NonRecursive walker) {
			if (mTermsInWhichWeAlreadyDescended.contains(getTerm())) {
				// do nothing
			} else {
				final Sort currentSort = getTerm().getSort();
				mOccuringSortNames.add(currentSort.getName());
				if (currentSort.isArraySort()) {
					mHasArrays = true;
				}
				super.walk(walker);
			}
		}

		@Override
		public void walk(final NonRecursive walker, final ConstantTerm term) {
			// cannot descend
		}

		@Override
		public void walk(final NonRecursive walker, final AnnotatedTerm term) {
			mTermsInWhichWeAlreadyDescended.add(term);
			walker.enqueueWalker(new MyWalker(term.getSubterm()));
		}

		@Override
		public void walk(final NonRecursive walker, final ApplicationTerm term) {
			mOccuringFunctionNames.add(term.getFunction().getName());
			mNumberOfFunctions += 1;
			mNumberOfVariables += term.getFreeVars().length;
			mTermsInWhichWeAlreadyDescended.add(term);
			for (final Term t : term.getParameters()) {
				walker.enqueueWalker(new MyWalker(t));
			}
		}

		@Override
		public void walk(final NonRecursive walker, final LetTerm term) {
			mTermsInWhichWeAlreadyDescended.add(term);
			walker.enqueueWalker(new MyWalker(term.getSubTerm()));
			for (final Term v : term.getValues()) {
				walker.enqueueWalker(new MyWalker(v));
			}

		}

		@Override
		public void walk(final NonRecursive walker, final QuantifiedFormula term) {
			mOccuringQuantifiers.add(term.getQuantifier());
			mNumberOfQuantifiers += 1;
			walker.enqueueWalker(new MyWalker(term.getSubformula()));
		}

		@Override
		public void walk(final NonRecursive walker, final TermVariable term) {
			// cannot descend
		}
	}
}
