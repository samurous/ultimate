package de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.Term;
import de.uni_freiburg.informatik.ultimate.smtinterpol.util.DAGSize;

public class SMTFeatureExtractor {

	// Members
	private final IUltimateServiceProvider mServices;
	private final ILogger mLogger;
	private final List<SMTFeature> mFeatures;
	private final String mDumpPath;
	private final String mFilename;

	public SMTFeatureExtractor(ILogger logger, IUltimateServiceProvider services, String dump_path) {
		mLogger = logger;
		mServices = services;
		mFeatures = new ArrayList<>();
		mDumpPath = dump_path;
		String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ofPattern( "uuuu-MM-dd-HH-mm" ));
		mFilename = mDumpPath + timestamp + "-smtfeatures.csv"; 
	}

	public void extractFeature(Term[] terms, double time) throws IllegalAccessException, IOException {
		mLogger.warn("Extracting feature..");
		TermClassifier tc = new TermClassifier();
		for (Term term : terms) {
			mLogger.warn(term.toString());
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
		feature.mDAGSize = tc.getDAGSize();
		feature.mSolverTime = time;
		mLogger.warn(tc.getStats());
		mFeatures.add(feature);
		dumpFeature(feature);
		
	}
	
	public void dumpFeature(SMTFeature feature) throws IllegalAccessException, IOException {
		mLogger.warn("Writing to file:" + mFilename);
		try(FileWriter fw = new FileWriter(mFilename, true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw))
			{
			    out.println(feature.toCsv(";"));
			} catch (IOException e) {
				throw new IOException(e);
			}
	}

}
