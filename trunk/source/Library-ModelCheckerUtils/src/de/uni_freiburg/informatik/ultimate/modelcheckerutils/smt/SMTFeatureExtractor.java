package de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt;

import java.util.ArrayList;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.Term;

public class SMTFeatureExtractor {

	// Members
	private final IUltimateServiceProvider mServices;
	private final ILogger mLogger;
	private final List<SMTFeature> mFeatures;

	public SMTFeatureExtractor(ILogger logger, IUltimateServiceProvider services) {
		mLogger = logger;
		mServices = services;
		mFeatures = new ArrayList<>();
	}

	public void extractFeature(Term[] terms, double time) throws IllegalAccessException {
		mLogger.warn("Extracting feature..");
		TermClassifier tc = new TermClassifier();
		for (Term term : terms) {
			tc.checkTerm(term);
		}
		SMTFeature feature = new SMTFeature();
		feature.mFormula = tc.getTerm();
		feature.mContainsArrays = tc.hasArrays();
		feature.mOccuringFunctions = tc.getOccuringFunctionNames();
		feature.mOccuringQuantifiers = tc.getOccuringQuantifiers();
		feature.mOccuringSorts = tc.getOccuringSortNames();
		feature.mNumberOfFunctions = tc.getNumberOfFunctions();
		feature.mNumberOfQuantifiers = tc.getNumberOfQuantifiers();
		feature.mSolverTime = time;
		mFeatures.add(feature);
		mLogger.warn(feature.toCsv(";"));
		
	}
	

}
