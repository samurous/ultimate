package de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
	private final String mDumpPath;

	public SMTFeatureExtractor(ILogger logger, IUltimateServiceProvider services, String dump_path) {
		mLogger = logger;
		mServices = services;
		mFeatures = new ArrayList<>();
		mDumpPath = dump_path;
	}

	public void extractFeature(Term[] terms, double time) throws IllegalAccessException, IOException {
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
		dumpFeature(feature);
	}
	
	public void dumpFeature(SMTFeature feature) throws IllegalAccessException, IOException {
		mLogger.warn("Writing to file");
		File file = new File(mDumpPath + "smtfeatures.csv");
		FileWriter writer = new FileWriter(file);
		writer.write("Test data");
		writer.close();
	}

}
