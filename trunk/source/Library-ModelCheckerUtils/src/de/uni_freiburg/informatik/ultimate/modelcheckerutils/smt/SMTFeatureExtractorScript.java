package de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.SMTLIBException;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Term;

public class SMTFeatureExtractorScript extends WrapperScript {

	SMTFeatureExtractor mFeatureExtractor;
	ILogger mLogger;
	IUltimateServiceProvider mServices;

	/**
	 * Create a new script which can extract properties of SMTterms and measure time of checkSat().
	 *
	 * @param script The wrapped script.
	 */
	public SMTFeatureExtractorScript(Script script, ILogger logger, IUltimateServiceProvider services, String dump_path) {
		super(script);
		mLogger = logger;
		mServices = services;
		mFeatureExtractor = new SMTFeatureExtractor(logger, services, dump_path);
	}

	@Override
	public LBool checkSat() throws SMTLIBException {
		final double start = System.nanoTime();
		final LBool sat = super.mScript.checkSat();
		final double analysisTime = (System.nanoTime() - start) / 1000;
		Term[] assertions = super.mScript.getAssertions();
		try {
			mFeatureExtractor.extractFeature(assertions, analysisTime);
		} catch (IllegalAccessException | IOException e) {
			mLogger.error(e);
		}
		return sat;
	}

}
