/*
 * Copyright (C) 2017 Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 * Copyright (C) 2017 University of Freiburg
 *
 * This file is part of the ULTIMATE ModelCheckerUtilsTest Library.
 *
 * The ULTIMATE ModelCheckerUtilsTest Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE ModelCheckerUtilsTest Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE ModelCheckerUtilsTest Library. If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE ModelCheckerUtilsTest Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP),
 * containing parts covered by the terms of the Eclipse Public License, the
 * licensors of the ULTIMATE ModelCheckerUtilsTest Library grant you additional permission
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger;
import de.uni_freiburg.informatik.ultimate.core.model.services.ILogger.LogLevel;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.SmtSortUtils;
import de.uni_freiburg.informatik.ultimate.lib.modelcheckerutils.smt.managedscript.ManagedScript;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.LoggingScript;
import de.uni_freiburg.informatik.ultimate.logic.Logics;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.logic.Sort;
import de.uni_freiburg.informatik.ultimate.test.mocks.UltimateMocks;

/**
 *
 * @author Matthias Heizmann (heizmann@informatik.uni-freiburg.de)
 *
 */
public class MuensterbergBenchmark {

	/**
	 * Warning: each test will overwrite the SMT script of the preceding test.
	 */
	private static final boolean WRITE_SMT_SCRIPTS_TO_FILE = false;
	private static final boolean WRITE_BENCHMARK_RESULTS_TO_WORKING_DIRECTORY = false;
	private static final long TEST_TIMEOUT_MILLISECONDS = 10_000;
	private static final LogLevel LOG_LEVEL = LogLevel.INFO;
	private static final String SOLVER_COMMAND = "z3 SMTLIB2_COMPLIANT=true -t:1000 -memory:2024 -smt2 -in";

	private IUltimateServiceProvider mServices;
	private Script mScript;
	private ManagedScript mMgdScript;
	private ILogger mLogger;
	private static QuantifierEliminationTestCsvWriter mCsvWriter;

	@BeforeClass
	public static void beforeAllTests() {
		mCsvWriter = new QuantifierEliminationTestCsvWriter(MuensterbergBenchmark.class.getSimpleName());
	}

	@AfterClass
	public static void afterAllTests() {
		if (WRITE_BENCHMARK_RESULTS_TO_WORKING_DIRECTORY) {
			try {
				mCsvWriter.writeCsv();
			} catch (final IOException e) {
				throw new AssertionError(e);
			}
		}
	}

	@Before
	public void setUp() throws FileNotFoundException {
		mServices = UltimateMocks.createUltimateServiceProviderMock(LOG_LEVEL);
		mServices.getProgressMonitorService().setDeadline(System.currentTimeMillis() + TEST_TIMEOUT_MILLISECONDS);
		mLogger = mServices.getLoggingService().getLogger("lol");

		final Script solverInstance = UltimateMocks.createSolver(SOLVER_COMMAND, LOG_LEVEL);
		if (WRITE_SMT_SCRIPTS_TO_FILE) {
			mScript = new LoggingScript(solverInstance, "QuantifierEliminationTest.smt2", true);
		} else {
			mScript = solverInstance;
		}

		mMgdScript = new ManagedScript(mServices, mScript);
		mScript.setLogic(Logics.ALL);
	}

	@After
	public void tearDown() {
		mScript.exit();
		mCsvWriter.reportTestFinished();
	}

	/**
	 * Old quantifier elimination needs seconds, new quantifier elimination needs minutes and produces more than 500
	 * conjuncts
	 */
	@Test
	public void rajdeepIteration5wp() {

		final Sort bv8 = SmtSortUtils.getBitvectorSort(mScript, 8);
		final Sort bv32 = SmtSortUtils.getBitvectorSort(mScript, 32);
		final Sort array = SmtSortUtils.getArraySort(mScript, bv32, bv8);

		mScript.declareFun("ULTIMATE.start_design_~nack.base", new Sort[0], bv32);
		mScript.declareFun("ULTIMATE.start_design_~nack.offset", new Sort[0], bv32);
		mScript.declareFun("ULTIMATE.start_design_~alloc_addr.base", new Sort[0], bv32);
		mScript.declareFun("ULTIMATE.start_main_~#nack~7.base", new Sort[0], bv32);
		mScript.declareFun("ULTIMATE.start_main_~#nack~7.offset", new Sort[0], bv32);

		mScript.declareFun("~smain.count", new Sort[0], bv8);
		mScript.declareFun("~smain.busy", new Sort[0], array);

		final String formulaAsString =
				"(forall ((|#memory_INTTYPE1| (Array (_ BitVec 32) (Array (_ BitVec 32) (_ BitVec 8)))) (v_bitvector_28 (_ BitVec 32)) (v_bitvector_27 (_ BitVec 32)) (~smain.free_addr (_ BitVec 8)) (v_bitvector_20 (_ BitVec 8)) (~smain.alloc (_ BitVec 8)) (v_bitvector_32 (_ BitVec 32)) (v_bitvector_31 (_ BitVec 32)) (v_bitvector_30 (_ BitVec 32)) (v_bitvector_29 (_ BitVec 32)) (v_bitvector_36 (Array (_ BitVec 32) (Array (_ BitVec 32) (_ BitVec 8)))) (ULTIMATE.start_design_~alloc_addr.offset (_ BitVec 32)) (|ULTIMATE.start_design_#t~ite18| (_ BitVec 32)) (v_bitvector_24 (_ BitVec 8)) (v_bitvector_19 (_ BitVec 8)) (v_bitvector_35 (_ BitVec 8)) (v_bitvector_25 (_ BitVec 32)) (v_bitvector_26 (_ BitVec 32)) (v_bitvector_33 (_ BitVec 32)) (v_bitvector_34 (_ BitVec 32))) (and (or (= (_ bv0 32) (bvand ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvsub (bvadd (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (bvand ((_ zero_extend 24) ~smain.alloc) (_ bv0 32))) ((_ zero_extend 24) (bvand v_bitvector_20 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) ~smain.free_addr) (_ bv15 32)))))))) (_ bv31 32)))) (_ bv15 32))) (= (select (select (store (store |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base (store (select |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base) ULTIMATE.start_design_~nack.offset (_ bv1 8))) ULTIMATE.start_design_~alloc_addr.base (store (select (store |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base (store (select |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base) ULTIMATE.start_design_~nack.offset (_ bv1 8))) ULTIMATE.start_design_~alloc_addr.base) v_bitvector_28 ((_ extract 7 0) v_bitvector_27))) |ULTIMATE.start_main_~#nack~7.base|) |ULTIMATE.start_main_~#nack~7.offset|) (_ bv0 8)) (= (_ bv0 32) (bvand (bvashr ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvsub (bvadd (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (bvand ((_ zero_extend 24) ~smain.alloc) (_ bv0 32))) ((_ zero_extend 24) (bvand v_bitvector_20 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) ~smain.free_addr) (_ bv15 32)))))))) (_ bv31 32)))) (_ bv4 32)) (_ bv1 32))) (= (select (select (store (store |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base (store (select |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base) ULTIMATE.start_design_~nack.offset (_ bv0 8))) ULTIMATE.start_design_~alloc_addr.base (store (select (store |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base (store (select |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base) ULTIMATE.start_design_~nack.offset (_ bv0 8))) ULTIMATE.start_design_~alloc_addr.base) v_bitvector_28 ((_ extract 7 0) v_bitvector_27))) |ULTIMATE.start_main_~#nack~7.base|) |ULTIMATE.start_main_~#nack~7.offset|) (_ bv0 8))) (or (= (_ bv0 32) (bvand ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvsub (bvadd (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (bvand ((_ zero_extend 24) ~smain.alloc) (_ bv0 32))) ((_ zero_extend 24) (bvand v_bitvector_20 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) ~smain.free_addr) (_ bv15 32)))))))) (_ bv31 32)))) (_ bv15 32))) (= (_ bv0 32) (bvand (bvashr ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvsub (bvadd (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (bvand ((_ zero_extend 24) ~smain.alloc) (_ bv0 32))) ((_ zero_extend 24) (bvand v_bitvector_20 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) ~smain.free_addr) (_ bv15 32)))))))) (_ bv31 32)))) (_ bv4 32)) (_ bv1 32))) (= (select (select (store (store |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base (store (select |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base) ULTIMATE.start_design_~nack.offset (_ bv0 8))) ULTIMATE.start_design_~alloc_addr.base (store (select (store |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base (store (select |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base) ULTIMATE.start_design_~nack.offset (_ bv0 8))) ULTIMATE.start_design_~alloc_addr.base) v_bitvector_32 ((_ extract 7 0) v_bitvector_31))) |ULTIMATE.start_main_~#nack~7.base|) |ULTIMATE.start_main_~#nack~7.offset|) (_ bv0 8)) (= (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (_ bv16 32))) (or (= (_ bv0 32) (bvand ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvsub (bvadd (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (_ bv0 32)) ((_ zero_extend 24) (bvand v_bitvector_20 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) ~smain.free_addr) (_ bv15 32)))))))) (_ bv31 32)))) (_ bv15 32))) (= (_ bv0 32) (bvand (bvashr ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvsub (bvadd (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (_ bv0 32)) ((_ zero_extend 24) (bvand v_bitvector_20 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) ~smain.free_addr) (_ bv15 32)))))))) (_ bv31 32)))) (_ bv4 32)) (_ bv1 32))) (= (select (select (store (store |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base (store (select |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base) ULTIMATE.start_design_~nack.offset (_ bv0 8))) ULTIMATE.start_design_~alloc_addr.base (store (select (store |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base (store (select |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base) ULTIMATE.start_design_~nack.offset (_ bv0 8))) ULTIMATE.start_design_~alloc_addr.base) v_bitvector_30 ((_ extract 7 0) v_bitvector_29))) |ULTIMATE.start_main_~#nack~7.base|) |ULTIMATE.start_main_~#nack~7.offset|) (_ bv0 8))) (or (not (= (select (select (store (store v_bitvector_36 ULTIMATE.start_design_~nack.base (store (select v_bitvector_36 ULTIMATE.start_design_~nack.base) ULTIMATE.start_design_~nack.offset (_ bv0 8))) ULTIMATE.start_design_~alloc_addr.base (store (select (store v_bitvector_36 ULTIMATE.start_design_~nack.base (store (select v_bitvector_36 ULTIMATE.start_design_~nack.base) ULTIMATE.start_design_~nack.offset (_ bv0 8))) ULTIMATE.start_design_~alloc_addr.base) ULTIMATE.start_design_~alloc_addr.offset ((_ extract 7 0) |ULTIMATE.start_design_#t~ite18|))) |ULTIMATE.start_main_~#nack~7.base|) |ULTIMATE.start_main_~#nack~7.offset|) (_ bv0 8))) (= (bvand (bvashr ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvsub (bvadd (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (bvand ((_ zero_extend 24) v_bitvector_35) (_ bv1 32))) ((_ zero_extend 24) (bvand v_bitvector_19 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) v_bitvector_24) (_ bv15 32)))))))) (_ bv31 32)))) (_ bv4 32)) (_ bv1 32)) (_ bv0 32)) (= (bvand ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvsub (bvadd (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (bvand ((_ zero_extend 24) v_bitvector_35) (_ bv1 32))) ((_ zero_extend 24) (bvand v_bitvector_19 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) v_bitvector_24) (_ bv15 32)))))))) (_ bv31 32)))) (_ bv15 32)) (_ bv0 32)) (= (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (_ bv16 32))) (or (not (= (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (_ bv16 32))) (= (_ bv0 32) (bvand ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvsub (bvadd (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (bvand ((_ zero_extend 24) ~smain.alloc) (_ bv0 32))) ((_ zero_extend 24) (bvand v_bitvector_20 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) ~smain.free_addr) (_ bv15 32)))))))) (_ bv31 32)))) (_ bv15 32))) (= (_ bv0 32) (bvand (bvashr ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvsub (bvadd (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (bvand ((_ zero_extend 24) ~smain.alloc) (_ bv0 32))) ((_ zero_extend 24) (bvand v_bitvector_20 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) ~smain.free_addr) (_ bv15 32)))))))) (_ bv31 32)))) (_ bv4 32)) (_ bv1 32))) (= (_ bv0 8) ~smain.alloc) (= (select (select (store (store |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base (store (select |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base) ULTIMATE.start_design_~nack.offset (_ bv1 8))) ULTIMATE.start_design_~alloc_addr.base (store (select (store |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base (store (select |#memory_INTTYPE1| ULTIMATE.start_design_~nack.base) ULTIMATE.start_design_~nack.offset (_ bv1 8))) ULTIMATE.start_design_~alloc_addr.base) v_bitvector_25 ((_ extract 7 0) v_bitvector_26))) |ULTIMATE.start_main_~#nack~7.base|) |ULTIMATE.start_main_~#nack~7.offset|) (_ bv0 8))) (or (not (= (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (_ bv16 32))) (not (= (select (select (store (store v_bitvector_36 ULTIMATE.start_design_~nack.base (store (select v_bitvector_36 ULTIMATE.start_design_~nack.base) ULTIMATE.start_design_~nack.offset (_ bv1 8))) ULTIMATE.start_design_~alloc_addr.base (store (select (store v_bitvector_36 ULTIMATE.start_design_~nack.base (store (select v_bitvector_36 ULTIMATE.start_design_~nack.base) ULTIMATE.start_design_~nack.offset (_ bv1 8))) ULTIMATE.start_design_~alloc_addr.base) v_bitvector_33 ((_ extract 7 0) v_bitvector_34))) |ULTIMATE.start_main_~#nack~7.base|) |ULTIMATE.start_main_~#nack~7.offset|) (_ bv0 8))) (= (bvand (bvashr ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvsub (bvadd (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (bvand ((_ zero_extend 24) v_bitvector_35) (_ bv1 32))) ((_ zero_extend 24) (bvand v_bitvector_19 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) v_bitvector_24) (_ bv15 32)))))))) (_ bv31 32)))) (_ bv4 32)) (_ bv1 32)) (_ bv0 32)) (= (bvand ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvsub (bvadd (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (bvand ((_ zero_extend 24) v_bitvector_35) (_ bv1 32))) ((_ zero_extend 24) (bvand v_bitvector_19 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) v_bitvector_24) (_ bv15 32)))))))) (_ bv31 32)))) (_ bv15 32)) (_ bv0 32)) (= (_ bv0 8) v_bitvector_35)) (or (not (= (select (select (store (store v_bitvector_36 ULTIMATE.start_design_~nack.base (store (select v_bitvector_36 ULTIMATE.start_design_~nack.base) ULTIMATE.start_design_~nack.offset (_ bv0 8))) ULTIMATE.start_design_~alloc_addr.base (store (select (store v_bitvector_36 ULTIMATE.start_design_~nack.base (store (select v_bitvector_36 ULTIMATE.start_design_~nack.base) ULTIMATE.start_design_~nack.offset (_ bv0 8))) ULTIMATE.start_design_~alloc_addr.base) ULTIMATE.start_design_~alloc_addr.offset ((_ extract 7 0) |ULTIMATE.start_design_#t~ite18|))) |ULTIMATE.start_main_~#nack~7.base|) |ULTIMATE.start_main_~#nack~7.offset|) (_ bv0 8))) (= (bvand (bvashr ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvsub (bvadd (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (_ bv0 32)) ((_ zero_extend 24) (bvand v_bitvector_19 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) v_bitvector_24) (_ bv15 32)))))))) (_ bv31 32)))) (_ bv4 32)) (_ bv1 32)) (_ bv0 32)) (= (_ bv0 32) (bvand ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvsub (bvadd (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (_ bv0 32)) ((_ zero_extend 24) (bvand v_bitvector_19 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) v_bitvector_24) (_ bv15 32)))))))) (_ bv31 32)))) (_ bv15 32))))))";
		final String expectedResultAsString =
				"(let ((.cse14 (= ULTIMATE.start_design_~alloc_addr.base |ULTIMATE.start_main_~#nack~7.base|))) (let ((.cse4 (not .cse14)) (.cse1 (= ULTIMATE.start_design_~nack.offset |ULTIMATE.start_main_~#nack~7.offset|)) (.cse2 (= ULTIMATE.start_design_~nack.base |ULTIMATE.start_main_~#nack~7.base|))) (let ((.cse7 (= (_ bv16 32) (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)))) (.cse8 (and .cse4 .cse1 .cse2))) (let ((.cse3 (or .cse14 .cse8)) (.cse10 (not .cse7))) (and (or (forall ((v_prenex_9 (_ BitVec 8)) (v_prenex_8 (_ BitVec 8))) (let ((.cse0 ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvadd (bvneg ((_ zero_extend 24) (bvand v_prenex_8 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) v_prenex_9) (_ bv15 32)))))))) (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32))) (_ bv31 32)))))) (or (= (_ bv0 32) (bvand (bvashr .cse0 (_ bv4 32)) (_ bv1 32))) (= (bvand .cse0 (_ bv15 32)) (_ bv0 32))))) (and (or (and .cse1 .cse2) (forall ((v_bitvector_28 (_ BitVec 32))) (= |ULTIMATE.start_main_~#nack~7.offset| v_bitvector_28))) .cse3 .cse4)) (forall ((v_prenex_17 (_ BitVec 8)) (v_prenex_18 (_ BitVec 8))) (let ((.cse5 ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvadd (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (bvneg ((_ zero_extend 24) (bvand v_prenex_18 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) v_prenex_17) (_ bv15 32))))))))) (_ bv31 32)))))) (or (= (_ bv0 32) (bvand .cse5 (_ bv15 32))) (= (bvand (bvashr .cse5 (_ bv4 32)) (_ bv1 32)) (_ bv0 32))))) (or (and .cse3 .cse4 .cse1 .cse2) (forall ((v_prenex_6 (_ BitVec 8)) (v_prenex_5 (_ BitVec 8))) (let ((.cse6 ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvadd (bvneg ((_ zero_extend 24) (bvand v_prenex_5 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) v_prenex_6) (_ bv15 32)))))))) (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32))) (_ bv31 32)))))) (or (= (_ bv0 32) (bvand (bvashr .cse6 (_ bv4 32)) (_ bv1 32))) (= (bvand .cse6 (_ bv15 32)) (_ bv0 32))))) .cse7) (or .cse8 (forall ((v_bitvector_20 (_ BitVec 8)) (~smain.free_addr (_ BitVec 8))) (let ((.cse9 ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvadd (bvneg ((_ zero_extend 24) (bvand v_bitvector_20 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) ~smain.free_addr) (_ bv15 32)))))))) (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32))) (_ bv31 32)))))) (or (= (_ bv0 32) (bvand (bvashr .cse9 (_ bv4 32)) (_ bv1 32))) (= (bvand .cse9 (_ bv15 32)) (_ bv0 32)))))) (or .cse8 .cse10 (forall ((v_bitvector_19 (_ BitVec 8)) (v_bitvector_24 (_ BitVec 8)) (v_bitvector_35 (_ BitVec 8))) (let ((.cse11 ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvadd (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (bvand ((_ zero_extend 24) v_bitvector_35) (_ bv1 32)) (bvneg ((_ zero_extend 24) (bvand v_bitvector_19 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) v_bitvector_24) (_ bv15 32))))))))) (_ bv31 32)))))) (or (= (bvand .cse11 (_ bv15 32)) (_ bv0 32)) (= (_ bv0 8) v_bitvector_35) (= (bvand (bvashr .cse11 (_ bv4 32)) (_ bv1 32)) (_ bv0 32)))))) (or (forall ((v_prenex_11 (_ BitVec 8)) (v_prenex_12 (_ BitVec 8))) (let ((.cse12 ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvadd (bvneg ((_ zero_extend 24) (bvand v_prenex_12 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) v_prenex_11) (_ bv15 32)))))))) (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32))) (_ bv31 32)))))) (or (= (_ bv0 32) (bvand (bvashr .cse12 (_ bv4 32)) (_ bv1 32))) (= (bvand .cse12 (_ bv15 32)) (_ bv0 32))))) .cse10) (or (forall ((v_prenex_4 (_ BitVec 8)) (v_prenex_3 (_ BitVec 8)) (v_prenex_1 (_ BitVec 8))) (let ((.cse13 ((_ zero_extend 24) ((_ extract 7 0) (bvand (bvadd (bvand ((_ zero_extend 24) ~smain.count) (_ bv31 32)) (bvand ((_ zero_extend 24) v_prenex_4) (_ bv1 32)) (bvneg ((_ zero_extend 24) (bvand v_prenex_1 (select ~smain.busy ((_ zero_extend 24) ((_ extract 7 0) (bvand ((_ zero_extend 24) v_prenex_3) (_ bv15 32))))))))) (_ bv31 32)))))) (or (= (bvand (bvashr .cse13 (_ bv4 32)) (_ bv1 32)) (_ bv0 32)) (= (bvand .cse13 (_ bv15 32)) (_ bv0 32))))) .cse7))))))";
		QuantifierEliminationTest.runQuantifierEliminationTest(formulaAsString, expectedResultAsString, true, mServices, mLogger, mMgdScript, mCsvWriter);
	}


	/**
	 * Old quantifier elimination produces smaller result (approx factor 2)
	 */
	@Test
	public void Memsafety20020406_1_false_valid_memtrack() {
		final Sort intSort = SmtSortUtils.getIntSort(mMgdScript);
		final Sort intintArraySort = SmtSortUtils.getArraySort(mScript, intSort, intSort);
		final Sort intintintArraySort =
				SmtSortUtils.getArraySort(mScript, intSort, SmtSortUtils.getArraySort(mScript, intSort, intSort));

		mScript.declareFun("#memory_$Pointer$.base", new Sort[0], intintintArraySort);
		mScript.declareFun("#valid", new Sort[0], intintArraySort);
		mScript.declareFun("DUPFFnew_~ans~6.base", new Sort[0], intSort);
		mScript.declareFun("DUPFFnew_~ans~6.offset", new Sort[0], intSort);

		final String formulaAsString =
				"(forall ((|v_#memory_$Pointer$.base_279| (Array Int (Array Int Int)))) (or (= (select |#valid| (select (select |v_#memory_$Pointer$.base_279| DUPFFnew_~ans~6.base) (+ DUPFFnew_~ans~6.offset 8))) 1) (not (= (store |#memory_$Pointer$.base| DUPFFnew_~ans~6.base (store (select |#memory_$Pointer$.base| DUPFFnew_~ans~6.base) (+ DUPFFnew_~ans~6.offset 4) (select (select |v_#memory_$Pointer$.base_279| DUPFFnew_~ans~6.base) (+ DUPFFnew_~ans~6.offset 4)))) |v_#memory_$Pointer$.base_279|))))";
		final String expectedResultAsString =
				"(forall ((v_DerPreprocessor_1 Int)) (= 1 (select |#valid| (select (store (select |#memory_$Pointer$.base| DUPFFnew_~ans~6.base) (+ DUPFFnew_~ans~6.offset 4) v_DerPreprocessor_1) (+ DUPFFnew_~ans~6.offset 8)))))";
		QuantifierEliminationTest.runQuantifierEliminationTest(formulaAsString, expectedResultAsString, false, mServices, mLogger, mMgdScript, mCsvWriter);
	}

	/**
	 * Old PQE size 43, new PQE size 96
	 */
	@Test
	public void memsafet_test_0232_false_valid_free_ias() {
		final Sort intSort = SmtSortUtils.getIntSort(mMgdScript);
		final Sort intintintArraySort =
				SmtSortUtils.getArraySort(mScript, intSort, SmtSortUtils.getArraySort(mScript, intSort, intSort));
		mScript.declareFun("ULTIMATE.start_append_~item~4.offset", new Sort[0], intSort);
		mScript.declareFun("ULTIMATE.start_append_~plist.offset", new Sort[0], intSort);
		mScript.declareFun("ULTIMATE.start_append_~plist.base", new Sort[0], intSort);
		mScript.declareFun("ULTIMATE.start_main_~#list~5.base", new Sort[0], intSort);
		mScript.declareFun("#memory_$Pointer$.offset", new Sort[0], intintintArraySort);
		mScript.declareFun("ULTIMATE.start_main_~#list~5.offset", new Sort[0], intSort);
		mScript.declareFun("ULTIMATE.start_append_~item~4.base", new Sort[0], intSort);

		final String formulaAsString =
				"(forall ((|#memory_$Pointer$.base| (Array Int (Array Int Int)))) (= (select (select (store |#memory_$Pointer$.offset| ULTIMATE.start_append_~plist.base (store (select |#memory_$Pointer$.offset| ULTIMATE.start_append_~plist.base) ULTIMATE.start_append_~plist.offset ULTIMATE.start_append_~item~4.offset)) (select (select (store |#memory_$Pointer$.base| ULTIMATE.start_append_~plist.base (store (select |#memory_$Pointer$.base| ULTIMATE.start_append_~plist.base) ULTIMATE.start_append_~plist.offset ULTIMATE.start_append_~item~4.base)) |ULTIMATE.start_main_~#list~5.base|) |ULTIMATE.start_main_~#list~5.offset|)) (+ (select (select (store |#memory_$Pointer$.offset| ULTIMATE.start_append_~plist.base (store (select |#memory_$Pointer$.offset| ULTIMATE.start_append_~plist.base) ULTIMATE.start_append_~plist.offset ULTIMATE.start_append_~item~4.offset)) |ULTIMATE.start_main_~#list~5.base|) |ULTIMATE.start_main_~#list~5.offset|) 4)) 0))";

		QuantifierEliminationTest.runQuantifierEliminationTest(formulaAsString, null, false, mServices, mLogger, mMgdScript, mCsvWriter);
	}

	/**
	 * Too many recursive steps in new PQE
	 */
	@Test
	public void dll_queue_false_unreach_call_false_valid_memcleanup() {
		final Sort intSort = SmtSortUtils.getIntSort(mMgdScript);
		mScript.declareFun("main_#t~malloc5.offset", new Sort[0], intSort);
		mScript.declareFun("main_~item~5.base", new Sort[0], intSort);
		mScript.declareFun("main_~head~5.base", new Sort[0], intSort);
		mScript.declareFun("main_~item~5.offset", new Sort[0], intSort);
		mScript.declareFun("main_~head~5.offset", new Sort[0], intSort);
		mScript.declareFun("main_#t~malloc5.base", new Sort[0], intSort);
		final String formulaAsString =
				"(forall ((v_prenex_8 (Array Int (Array Int Int))) (v_prenex_6 Int) (v_prenex_7 (Array Int (Array Int Int))) (v_DerPreprocessor_4 Int) (|#memory_$Pointer$.offset| (Array Int (Array Int Int))) (|main_#t~mem7.offset| Int) (|#memory_$Pointer$.base| (Array Int (Array Int Int))) (v_DerPreprocessor_6 Int)) (and (= (select (select (store (store v_prenex_7 main_~item~5.base (store (select v_prenex_7 main_~item~5.base) main_~item~5.offset |main_#t~malloc5.base|)) |main_#t~malloc5.base| (store (select (store v_prenex_7 main_~item~5.base (store (select v_prenex_7 main_~item~5.base) main_~item~5.offset |main_#t~malloc5.base|)) |main_#t~malloc5.base|) (+ v_prenex_6 4) main_~item~5.base)) main_~item~5.base) main_~item~5.offset) (select (select (store (store (store v_prenex_7 main_~item~5.base (store (select v_prenex_7 main_~item~5.base) main_~item~5.offset |main_#t~malloc5.base|)) |main_#t~malloc5.base| (store (select (store v_prenex_7 main_~item~5.base (store (select v_prenex_7 main_~item~5.base) main_~item~5.offset |main_#t~malloc5.base|)) |main_#t~malloc5.base|) (+ v_prenex_6 4) main_~item~5.base)) (select (select (store (store v_prenex_7 main_~item~5.base (store (select v_prenex_7 main_~item~5.base) main_~item~5.offset |main_#t~malloc5.base|)) |main_#t~malloc5.base| (store (select (store v_prenex_7 main_~item~5.base (store (select v_prenex_7 main_~item~5.base) main_~item~5.offset |main_#t~malloc5.base|)) |main_#t~malloc5.base|) (+ v_prenex_6 4) main_~item~5.base)) main_~item~5.base) main_~item~5.offset) (store (store (select (store (store v_prenex_7 main_~item~5.base (store (select v_prenex_7 main_~item~5.base) main_~item~5.offset |main_#t~malloc5.base|)) |main_#t~malloc5.base| (store (select (store v_prenex_7 main_~item~5.base (store (select v_prenex_7 main_~item~5.base) main_~item~5.offset |main_#t~malloc5.base|)) |main_#t~malloc5.base|) (+ v_prenex_6 4) main_~item~5.base)) (select (select (store (store v_prenex_7 main_~item~5.base (store (select v_prenex_7 main_~item~5.base) main_~item~5.offset |main_#t~malloc5.base|)) |main_#t~malloc5.base| (store (select (store v_prenex_7 main_~item~5.base (store (select v_prenex_7 main_~item~5.base) main_~item~5.offset |main_#t~malloc5.base|)) |main_#t~malloc5.base|) (+ v_prenex_6 4) main_~item~5.base)) main_~item~5.base) main_~item~5.offset)) (select (select (store (store v_prenex_8 main_~item~5.base (store (select v_prenex_8 main_~item~5.base) main_~item~5.offset |main_#t~malloc5.offset|)) |main_#t~malloc5.base| (store (select (store v_prenex_8 main_~item~5.base (store (select v_prenex_8 main_~item~5.base) main_~item~5.offset |main_#t~malloc5.offset|)) |main_#t~malloc5.base|) (+ v_prenex_6 4) main_~item~5.offset)) main_~item~5.base) main_~item~5.offset) 0) (+ (select (select (store (store v_prenex_8 main_~item~5.base (store (select v_prenex_8 main_~item~5.base) main_~item~5.offset |main_#t~malloc5.offset|)) |main_#t~malloc5.base| (store (select (store v_prenex_8 main_~item~5.base (store (select v_prenex_8 main_~item~5.base) main_~item~5.offset |main_#t~malloc5.offset|)) |main_#t~malloc5.base|) (+ v_prenex_6 4) main_~item~5.offset)) main_~item~5.base) main_~item~5.offset) 8) v_DerPreprocessor_4)) main_~head~5.base) main_~head~5.offset)) (= (select (select (store (store |#memory_$Pointer$.offset| main_~item~5.base (store (select |#memory_$Pointer$.offset| main_~item~5.base) main_~item~5.offset |main_#t~malloc5.offset|)) |main_#t~malloc5.base| (store (select (store |#memory_$Pointer$.offset| main_~item~5.base (store (select |#memory_$Pointer$.offset| main_~item~5.base) main_~item~5.offset |main_#t~malloc5.offset|)) |main_#t~malloc5.base|) (+ |main_#t~mem7.offset| 4) main_~item~5.offset)) main_~item~5.base) main_~item~5.offset) (select (select (store (store (store |#memory_$Pointer$.offset| main_~item~5.base (store (select |#memory_$Pointer$.offset| main_~item~5.base) main_~item~5.offset |main_#t~malloc5.offset|)) |main_#t~malloc5.base| (store (select (store |#memory_$Pointer$.offset| main_~item~5.base (store (select |#memory_$Pointer$.offset| main_~item~5.base) main_~item~5.offset |main_#t~malloc5.offset|)) |main_#t~malloc5.base|) (+ |main_#t~mem7.offset| 4) main_~item~5.offset)) (select (select (store (store |#memory_$Pointer$.base| main_~item~5.base (store (select |#memory_$Pointer$.base| main_~item~5.base) main_~item~5.offset |main_#t~malloc5.base|)) |main_#t~malloc5.base| (store (select (store |#memory_$Pointer$.base| main_~item~5.base (store (select |#memory_$Pointer$.base| main_~item~5.base) main_~item~5.offset |main_#t~malloc5.base|)) |main_#t~malloc5.base|) (+ |main_#t~mem7.offset| 4) main_~item~5.base)) main_~item~5.base) main_~item~5.offset) (store (store (select (store (store |#memory_$Pointer$.offset| main_~item~5.base (store (select |#memory_$Pointer$.offset| main_~item~5.base) main_~item~5.offset |main_#t~malloc5.offset|)) |main_#t~malloc5.base| (store (select (store |#memory_$Pointer$.offset| main_~item~5.base (store (select |#memory_$Pointer$.offset| main_~item~5.base) main_~item~5.offset |main_#t~malloc5.offset|)) |main_#t~malloc5.base|) (+ |main_#t~mem7.offset| 4) main_~item~5.offset)) (select (select (store (store |#memory_$Pointer$.base| main_~item~5.base (store (select |#memory_$Pointer$.base| main_~item~5.base) main_~item~5.offset |main_#t~malloc5.base|)) |main_#t~malloc5.base| (store (select (store |#memory_$Pointer$.base| main_~item~5.base (store (select |#memory_$Pointer$.base| main_~item~5.base) main_~item~5.offset |main_#t~malloc5.base|)) |main_#t~malloc5.base|) (+ |main_#t~mem7.offset| 4) main_~item~5.base)) main_~item~5.base) main_~item~5.offset)) (select (select (store (store |#memory_$Pointer$.offset| main_~item~5.base (store (select |#memory_$Pointer$.offset| main_~item~5.base) main_~item~5.offset |main_#t~malloc5.offset|)) |main_#t~malloc5.base| (store (select (store |#memory_$Pointer$.offset| main_~item~5.base (store (select |#memory_$Pointer$.offset| main_~item~5.base) main_~item~5.offset |main_#t~malloc5.offset|)) |main_#t~malloc5.base|) (+ |main_#t~mem7.offset| 4) main_~item~5.offset)) main_~item~5.base) main_~item~5.offset) 0) (+ (select (select (store (store |#memory_$Pointer$.offset| main_~item~5.base (store (select |#memory_$Pointer$.offset| main_~item~5.base) main_~item~5.offset |main_#t~malloc5.offset|)) |main_#t~malloc5.base| (store (select (store |#memory_$Pointer$.offset| main_~item~5.base (store (select |#memory_$Pointer$.offset| main_~item~5.base) main_~item~5.offset |main_#t~malloc5.offset|)) |main_#t~malloc5.base|) (+ |main_#t~mem7.offset| 4) main_~item~5.offset)) main_~item~5.base) main_~item~5.offset) 8) v_DerPreprocessor_6)) main_~head~5.base) main_~head~5.offset))))";

		QuantifierEliminationTest.runQuantifierEliminationTest(formulaAsString, null, true, mServices, mLogger, mMgdScript, mCsvWriter);
	}

	/**
	 * Too many recursive steps in new PQE
	 */
	@Test
	public void dll_queue_false_unreach_call_false_valid_memcleanup_2() {
		final Sort intSort = SmtSortUtils.getIntSort(mMgdScript);
		final Sort intintintArraySort =
				SmtSortUtils.getArraySort(mScript, intSort, SmtSortUtils.getArraySort(mScript, intSort, intSort));
		mScript.declareFun("main_~item~5.offset", new Sort[0], intSort);
		mScript.declareFun("main_~head~5.offset", new Sort[0], intSort);
		mScript.declareFun("#memory_$Pointer$.base", new Sort[0], intintintArraySort);
		mScript.declareFun("main_~item~5.base", new Sort[0], intSort);
		mScript.declareFun("main_~head~5.base", new Sort[0], intSort);
		final String formulaAsString =
				"(forall ((|#memory_$Pointer$.offset| (Array Int (Array Int Int))) (v_DerPreprocessor_28 Int) (v_DerPreprocessor_22 Int) (v_prenex_65 (Array Int (Array Int Int))) (v_DerPreprocessor_24 Int) (v_DerPreprocessor_26 Int)) (and (= 0 (select (select (store |#memory_$Pointer$.offset| (select (select |#memory_$Pointer$.base| main_~item~5.base) main_~item~5.offset) (store (store (select |#memory_$Pointer$.offset| (select (select |#memory_$Pointer$.base| main_~item~5.base) main_~item~5.offset)) (select (select |#memory_$Pointer$.offset| main_~item~5.base) main_~item~5.offset) 0) (+ (select (select |#memory_$Pointer$.offset| main_~item~5.base) main_~item~5.offset) 8) v_DerPreprocessor_22)) (select (select (store |#memory_$Pointer$.base| (select (select |#memory_$Pointer$.base| main_~item~5.base) main_~item~5.offset) (store (store (select |#memory_$Pointer$.base| (select (select |#memory_$Pointer$.base| main_~item~5.base) main_~item~5.offset)) (select (select |#memory_$Pointer$.offset| main_~item~5.base) main_~item~5.offset) 0) (+ (select (select |#memory_$Pointer$.offset| main_~item~5.base) main_~item~5.offset) 8) v_DerPreprocessor_28)) main_~head~5.base) main_~head~5.offset)) (select (select (store |#memory_$Pointer$.offset| (select (select |#memory_$Pointer$.base| main_~item~5.base) main_~item~5.offset) (store (store (select |#memory_$Pointer$.offset| (select (select |#memory_$Pointer$.base| main_~item~5.base) main_~item~5.offset)) (select (select |#memory_$Pointer$.offset| main_~item~5.base) main_~item~5.offset) 0) (+ (select (select |#memory_$Pointer$.offset| main_~item~5.base) main_~item~5.offset) 8) v_DerPreprocessor_22)) main_~head~5.base) main_~head~5.offset))) (= 0 (select (select (store |#memory_$Pointer$.base| (select (select |#memory_$Pointer$.base| main_~item~5.base) main_~item~5.offset) (store (store (select |#memory_$Pointer$.base| (select (select |#memory_$Pointer$.base| main_~item~5.base) main_~item~5.offset)) (select (select v_prenex_65 main_~item~5.base) main_~item~5.offset) 0) (+ (select (select v_prenex_65 main_~item~5.base) main_~item~5.offset) 8) v_DerPreprocessor_24)) (select (select (store |#memory_$Pointer$.base| (select (select |#memory_$Pointer$.base| main_~item~5.base) main_~item~5.offset) (store (store (select |#memory_$Pointer$.base| (select (select |#memory_$Pointer$.base| main_~item~5.base) main_~item~5.offset)) (select (select v_prenex_65 main_~item~5.base) main_~item~5.offset) 0) (+ (select (select v_prenex_65 main_~item~5.base) main_~item~5.offset) 8) v_DerPreprocessor_24)) main_~head~5.base) main_~head~5.offset)) (select (select (store v_prenex_65 (select (select |#memory_$Pointer$.base| main_~item~5.base) main_~item~5.offset) (store (store (select v_prenex_65 (select (select |#memory_$Pointer$.base| main_~item~5.base) main_~item~5.offset)) (select (select v_prenex_65 main_~item~5.base) main_~item~5.offset) 0) (+ (select (select v_prenex_65 main_~item~5.base) main_~item~5.offset) 8) v_DerPreprocessor_26)) main_~head~5.base) main_~head~5.offset)))))";

		QuantifierEliminationTest.runQuantifierEliminationTest(formulaAsString, null, true, mServices, mLogger, mMgdScript, mCsvWriter);
	}


	/**
	 * 30s elimination time
	 */
	@Test
	public void forester_heap_dll_optional() {
		final Sort bv1 = SmtSortUtils.getBitvectorSort(mScript, 1);
		final Sort bv32 = SmtSortUtils.getBitvectorSort(mScript, 32);
		final Sort array = SmtSortUtils.getArraySort(mScript, bv32, bv1);
		mScript.declareFun("main_~head~0.offset", new Sort[0], bv32);
		mScript.declareFun("main_~head~0.base", new Sort[0], bv32);
		mScript.declareFun("#valid", new Sort[0], array);
		final String formulaAsString =
				"(forall ((|#memory_INTTYPE4| (Array (_ BitVec 32) (Array (_ BitVec 32) (_ BitVec 32)))) (v_DerPreprocessor_10 (_ BitVec 32)) (v_DerPreprocessor_9 (_ BitVec 32)) (|v_main_#t~malloc6.base_4| (_ BitVec 32)) (|main_#t~mem7.offset| (_ BitVec 32)) (v_DerPreprocessor_8 (_ BitVec 32)) (v_subst_6 (_ BitVec 32)) (v_DerPreprocessor_7 (_ BitVec 32)) (v_DerPreprocessor_6 (_ BitVec 32))) (or (not (= (select |#valid| |v_main_#t~malloc6.base_4|) (_ bv0 1))) (not (= (select (select (store (store |#memory_INTTYPE4| main_~head~0.base (store (store (store (select |#memory_INTTYPE4| main_~head~0.base) (bvadd main_~head~0.offset (_ bv12 32)) (_ bv0 32)) (bvadd main_~head~0.offset (_ bv8 32)) v_DerPreprocessor_10) main_~head~0.offset v_DerPreprocessor_9)) |v_main_#t~malloc6.base_4| (store (store (store (store (select (store |#memory_INTTYPE4| main_~head~0.base (store (store (store (select |#memory_INTTYPE4| main_~head~0.base) (bvadd main_~head~0.offset (_ bv12 32)) (_ bv0 32)) (bvadd main_~head~0.offset (_ bv8 32)) v_DerPreprocessor_10) main_~head~0.offset v_DerPreprocessor_9)) |v_main_#t~malloc6.base_4|) (bvadd |main_#t~mem7.offset| (_ bv4 32)) v_DerPreprocessor_8) v_subst_6 v_DerPreprocessor_7) (bvadd v_subst_6 (_ bv12 32)) (_ bv0 32)) (bvadd v_subst_6 (_ bv8 32)) v_DerPreprocessor_6)) main_~head~0.base) (bvadd main_~head~0.offset (_ bv12 32))) (_ bv2 32)))))";
		final String expectedResultAsString =
				"(forall ((v_subst_6 (_ BitVec 32)) (v_DerPreprocessor_8 (_ BitVec 32)) (|main_#t~mem7.offset| (_ BitVec 32)) (v_DerPreprocessor_7 (_ BitVec 32)) (|v_main_#t~malloc6.base_4| (_ BitVec 32)) (v_DerPreprocessor_6 (_ BitVec 32))) (or (not (= (select |#valid| |v_main_#t~malloc6.base_4|) (_ bv0 1))) (not (and (or (and (not (= (bvadd main_~head~0.offset (_ bv12 32)) (bvadd v_subst_6 (_ bv8 32)))) (= main_~head~0.base |v_main_#t~malloc6.base_4|) (or (and (= (bvadd main_~head~0.offset (_ bv12 32)) v_subst_6) (= (_ bv2 32) v_DerPreprocessor_7)) (and (not (= (bvadd main_~head~0.offset (_ bv12 32)) v_subst_6)) (= (_ bv2 32) v_DerPreprocessor_8) (= (bvadd main_~head~0.offset (_ bv12 32)) (bvadd |main_#t~mem7.offset| (_ bv4 32)))))) (and (= (bvadd main_~head~0.offset (_ bv12 32)) (bvadd v_subst_6 (_ bv8 32))) (= (_ bv2 32) v_DerPreprocessor_6) (= main_~head~0.base |v_main_#t~malloc6.base_4|))) (not (= (bvadd v_subst_6 (_ bv12 32)) (bvadd main_~head~0.offset (_ bv12 32))))))))";
		QuantifierEliminationTest.runQuantifierEliminationTest(formulaAsString, expectedResultAsString, false, mServices, mLogger, mMgdScript, mCsvWriter);
	}


	@Test
	public void forester_heap_dll_01() {
		final Sort bv1 = SmtSortUtils.getBitvectorSort(mScript, 1);
		final Sort bv32 = SmtSortUtils.getBitvectorSort(mScript, 32);
		final Sort array = SmtSortUtils.getArraySort(mScript, bv32, bv1);
		mScript.declareFun("main_#t~malloc9.base", new Sort[0], bv32);
		mScript.declareFun("main_~end~0.offset", new Sort[0], bv32);
		mScript.declareFun("main_~list~0.base", new Sort[0], bv32);
		mScript.declareFun("main_~end~0.base", new Sort[0], bv32);
		mScript.declareFun("main_~list~0.offset", new Sort[0], bv32);
		mScript.declareFun("#valid", new Sort[0], array);
		final String formulaAsString = "(forall ((|#memory_$Pointer$.base| (Array (_ BitVec 32) (Array (_ BitVec 32) (_ BitVec 32)))) (|main_#t~mem10.offset| (_ BitVec 32)) (v_subst_7 (_ BitVec 32))) (= (_ bv0 1) (bvadd (select |#valid| (select (select (store (store (store |#memory_$Pointer$.base| main_~end~0.base (store (select |#memory_$Pointer$.base| main_~end~0.base) main_~end~0.offset |main_#t~malloc9.base|)) |main_#t~malloc9.base| (store (select (store |#memory_$Pointer$.base| main_~end~0.base (store (select |#memory_$Pointer$.base| main_~end~0.base) main_~end~0.offset |main_#t~malloc9.base|)) |main_#t~malloc9.base|) (bvadd |main_#t~mem10.offset| (_ bv4 32)) main_~end~0.base)) (select (select (store (store |#memory_$Pointer$.base| main_~end~0.base (store (select |#memory_$Pointer$.base| main_~end~0.base) main_~end~0.offset |main_#t~malloc9.base|)) |main_#t~malloc9.base| (store (select (store |#memory_$Pointer$.base| main_~end~0.base (store (select |#memory_$Pointer$.base| main_~end~0.base) main_~end~0.offset |main_#t~malloc9.base|)) |main_#t~malloc9.base|) (bvadd |main_#t~mem10.offset| (_ bv4 32)) main_~end~0.base)) main_~end~0.base) main_~end~0.offset) (store (store (select (store (store |#memory_$Pointer$.base| main_~end~0.base (store (select |#memory_$Pointer$.base| main_~end~0.base) main_~end~0.offset |main_#t~malloc9.base|)) |main_#t~malloc9.base| (store (select (store |#memory_$Pointer$.base| main_~end~0.base (store (select |#memory_$Pointer$.base| main_~end~0.base) main_~end~0.offset |main_#t~malloc9.base|)) |main_#t~malloc9.base|) (bvadd |main_#t~mem10.offset| (_ bv4 32)) main_~end~0.base)) (select (select (store (store |#memory_$Pointer$.base| main_~end~0.base (store (select |#memory_$Pointer$.base| main_~end~0.base) main_~end~0.offset |main_#t~malloc9.base|)) |main_#t~malloc9.base| (store (select (store |#memory_$Pointer$.base| main_~end~0.base (store (select |#memory_$Pointer$.base| main_~end~0.base) main_~end~0.offset |main_#t~malloc9.base|)) |main_#t~malloc9.base|) (bvadd |main_#t~mem10.offset| (_ bv4 32)) main_~end~0.base)) main_~end~0.base) main_~end~0.offset)) v_subst_7 (_ bv0 32)) (bvadd v_subst_7 (_ bv8 32)) (_ bv0 32))) main_~list~0.base) main_~list~0.offset)) (_ bv1 1))))";
		final String expectedResultAsString =
				"(forall ((|main_#t~mem10.offset| (_ BitVec 32)) (v_arrayElimCell_156 (_ BitVec 32)) (v_subst_7 (_ BitVec 32))) (or (and (= main_~list~0.offset v_subst_7) (= (_ bv0 1) (bvadd (select |#valid| (_ bv0 32)) (_ bv1 1))) (= |main_#t~malloc9.base| main_~list~0.base)) (and (or (not (= |main_#t~malloc9.base| main_~list~0.base)) (not (= main_~list~0.offset v_subst_7))) (or (and (or (not (= |main_#t~malloc9.base| main_~list~0.base)) (not (= main_~list~0.offset (bvadd v_subst_7 (_ bv8 32))))) (or (and (= main_~list~0.base main_~end~0.base) (= (_ bv0 1) (bvadd (select |#valid| |main_#t~malloc9.base|) (_ bv1 1))) (= main_~list~0.offset main_~end~0.offset)) (and (or (and (= main_~list~0.offset (bvadd |main_#t~mem10.offset| (_ bv4 32))) (= (_ bv0 1) (bvadd (select |#valid| main_~end~0.base) (_ bv1 1))) (= |main_#t~malloc9.base| main_~list~0.base)) (and (= (bvadd (select |#valid| v_arrayElimCell_156) (_ bv1 1)) (_ bv0 1)) (or (not (= |main_#t~malloc9.base| main_~list~0.base)) (not (= main_~list~0.offset (bvadd |main_#t~mem10.offset| (_ bv4 32))))))) (or (not (= main_~list~0.base main_~end~0.base)) (not (= main_~list~0.offset main_~end~0.offset)))))) (and (= main_~list~0.offset (bvadd v_subst_7 (_ bv8 32))) (= (_ bv0 1) (bvadd (select |#valid| (_ bv0 32)) (_ bv1 1))) (= |main_#t~malloc9.base| main_~list~0.base))))))";
		QuantifierEliminationTest.runQuantifierEliminationTest(formulaAsString, expectedResultAsString, false, mServices, mLogger, mMgdScript, mCsvWriter);
	}

	@Test
	public void forester_heap_dll_simple_white_blue() {
		final Sort bv1 = SmtSortUtils.getBitvectorSort(mScript, 1);
		final Sort bv32 = SmtSortUtils.getBitvectorSort(mScript, 32);
		final Sort array = SmtSortUtils.getArraySort(mScript, bv32, bv1);
		mScript.declareFun("main_~x~0.offset", new Sort[0], bv32);
		mScript.declareFun("main_~head~0.offset", new Sort[0], bv32);
		mScript.declareFun("main_~x~0.base", new Sort[0], bv32);
		mScript.declareFun("main_#t~malloc2.base", new Sort[0], bv32);
		mScript.declareFun("main_~head~0.base", new Sort[0], bv32);
		mScript.declareFun("#valid", new Sort[0], array);
		final String formulaAsString = "(forall ((|#memory_$Pointer$.base| (Array (_ BitVec 32) (Array (_ BitVec 32) (_ BitVec 32)))) (|main_#t~mem3.offset| (_ BitVec 32)) (v_subst_5 (_ BitVec 32)) (v_DerPreprocessor_1 (_ BitVec 32))) (= (_ bv1 1) (select |#valid| (select (select (store (store (store |#memory_$Pointer$.base| main_~x~0.base (store (select |#memory_$Pointer$.base| main_~x~0.base) main_~x~0.offset |main_#t~malloc2.base|)) |main_#t~malloc2.base| (store (select (store |#memory_$Pointer$.base| main_~x~0.base (store (select |#memory_$Pointer$.base| main_~x~0.base) main_~x~0.offset |main_#t~malloc2.base|)) |main_#t~malloc2.base|) (bvadd |main_#t~mem3.offset| (_ bv4 32)) main_~x~0.base)) (select (select (store (store |#memory_$Pointer$.base| main_~x~0.base (store (select |#memory_$Pointer$.base| main_~x~0.base) main_~x~0.offset |main_#t~malloc2.base|)) |main_#t~malloc2.base| (store (select (store |#memory_$Pointer$.base| main_~x~0.base (store (select |#memory_$Pointer$.base| main_~x~0.base) main_~x~0.offset |main_#t~malloc2.base|)) |main_#t~malloc2.base|) (bvadd |main_#t~mem3.offset| (_ bv4 32)) main_~x~0.base)) main_~x~0.base) main_~x~0.offset) (store (store (select (store (store |#memory_$Pointer$.base| main_~x~0.base (store (select |#memory_$Pointer$.base| main_~x~0.base) main_~x~0.offset |main_#t~malloc2.base|)) |main_#t~malloc2.base| (store (select (store |#memory_$Pointer$.base| main_~x~0.base (store (select |#memory_$Pointer$.base| main_~x~0.base) main_~x~0.offset |main_#t~malloc2.base|)) |main_#t~malloc2.base|) (bvadd |main_#t~mem3.offset| (_ bv4 32)) main_~x~0.base)) (select (select (store (store |#memory_$Pointer$.base| main_~x~0.base (store (select |#memory_$Pointer$.base| main_~x~0.base) main_~x~0.offset |main_#t~malloc2.base|)) |main_#t~malloc2.base| (store (select (store |#memory_$Pointer$.base| main_~x~0.base (store (select |#memory_$Pointer$.base| main_~x~0.base) main_~x~0.offset |main_#t~malloc2.base|)) |main_#t~malloc2.base|) (bvadd |main_#t~mem3.offset| (_ bv4 32)) main_~x~0.base)) main_~x~0.base) main_~x~0.offset)) (bvadd v_subst_5 (_ bv8 32)) v_DerPreprocessor_1) v_subst_5 (_ bv0 32))) main_~head~0.base) main_~head~0.offset))))";
		final String expectedResultAsString =
				"(forall ((|main_#t~mem3.offset| (_ BitVec 32)) (v_arrayElimCell_62 (_ BitVec 32)) (v_subst_5 (_ BitVec 32)) (v_DerPreprocessor_1 (_ BitVec 32))) (or (and (= (bvadd v_subst_5 (_ bv8 32)) main_~head~0.offset) (= |main_#t~malloc2.base| main_~head~0.base) (= (_ bv1 1) (select |#valid| v_DerPreprocessor_1))) (and (= (select |#valid| (_ bv0 32)) (_ bv1 1)) (= |main_#t~malloc2.base| main_~head~0.base) (= main_~head~0.offset v_subst_5)) (and (or (not (= |main_#t~malloc2.base| main_~head~0.base)) (not (= (bvadd v_subst_5 (_ bv8 32)) main_~head~0.offset))) (or (not (= main_~x~0.offset main_~head~0.offset)) (not (= main_~head~0.base main_~x~0.base))) (or (and (or (not (= main_~head~0.offset (bvadd |main_#t~mem3.offset| (_ bv4 32)))) (not (= |main_#t~malloc2.base| main_~head~0.base))) (= (select |#valid| v_arrayElimCell_62) (_ bv1 1))) (and (= (_ bv1 1) (select |#valid| main_~x~0.base)) (= |main_#t~malloc2.base| main_~head~0.base) (= main_~head~0.offset (bvadd |main_#t~mem3.offset| (_ bv4 32))))) (or (not (= |main_#t~malloc2.base| main_~head~0.base)) (not (= main_~head~0.offset v_subst_5)))) (and (or (not (= |main_#t~malloc2.base| main_~head~0.base)) (not (= (bvadd v_subst_5 (_ bv8 32)) main_~head~0.offset))) (= (_ bv1 1) (select |#valid| |main_#t~malloc2.base|)) (or (not (= |main_#t~malloc2.base| main_~head~0.base)) (not (= main_~head~0.offset v_subst_5))) (= main_~head~0.base main_~x~0.base) (= main_~x~0.offset main_~head~0.offset))))";
		QuantifierEliminationTest.runQuantifierEliminationTest(formulaAsString, expectedResultAsString, false, mServices, mLogger, mMgdScript, mCsvWriter);
	}

	@Test
	public void dllqueue01() {
		final Sort bv32 = SmtSortUtils.getBitvectorSort(mScript, 32);
		final Sort bv32Tobv32 = SmtSortUtils.getArraySort(mScript, bv32, bv32);
		final Sort bv32Tobv32Tobv32 = SmtSortUtils.getArraySort(mScript, bv32, bv32Tobv32);

		mScript.declareFun("main_~item~0.base", new Sort[0], bv32);
		mScript.declareFun("#length", new Sort[0], bv32Tobv32);
		mScript.declareFun("main_~head~0.offset", new Sort[0], bv32);
		mScript.declareFun("main_~item~0.offset", new Sort[0], bv32);
		mScript.declareFun("main_#t~malloc2.offset", new Sort[0], bv32);
		mScript.declareFun("main_#t~malloc2.base", new Sort[0], bv32);
		mScript.declareFun("main_~head~0.base", new Sort[0], bv32);
		final String formulaAsString = "(forall ((|v_#memory_$Pointer$.base_64| (Array (_ BitVec 32) (Array (_ BitVec 32) (_ BitVec 32)))) (|v_#memory_$Pointer$.offset_61| (Array (_ BitVec 32) (Array (_ BitVec 32) (_ BitVec 32)))) (|#memory_$Pointer$.offset| (Array (_ BitVec 32) (Array (_ BitVec 32) (_ BitVec 32)))) (|#memory_$Pointer$.base| (Array (_ BitVec 32) (Array (_ BitVec 32) (_ BitVec 32))))) (or (not (and (= |v_#memory_$Pointer$.base_64| (store |#memory_$Pointer$.base| main_~item~0.base (store (select |#memory_$Pointer$.base| main_~item~0.base) main_~item~0.offset |main_#t~malloc2.base|))) (= |v_#memory_$Pointer$.offset_61| (store |#memory_$Pointer$.offset| main_~item~0.base (store (select |#memory_$Pointer$.offset| main_~item~0.base) main_~item~0.offset |main_#t~malloc2.offset|))))) (and (forall ((v_prxprot_4 (_ BitVec 32)) (v_prxprot_1 (_ BitVec 32)) (v_DerPreprocessor_9 (_ BitVec 32))) (bvsle (bvadd (select (select (store (store |v_#memory_$Pointer$.offset_61| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.offset_61| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd v_prxprot_4 (_ bv4 32)) main_~item~0.offset)) (select (select (store |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd v_prxprot_4 (_ bv4 32)) main_~item~0.base)) main_~item~0.base) main_~item~0.offset) (store (store (select (store |v_#memory_$Pointer$.offset_61| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.offset_61| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd v_prxprot_4 (_ bv4 32)) main_~item~0.offset)) (select (select (store |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd v_prxprot_4 (_ bv4 32)) main_~item~0.base)) main_~item~0.base) main_~item~0.offset)) v_prxprot_1 (_ bv0 32)) (bvadd v_prxprot_1 (_ bv8 32)) v_DerPreprocessor_9)) main_~head~0.base) main_~head~0.offset) (_ bv8 32)) (bvadd (select (select (store (store |v_#memory_$Pointer$.offset_61| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.offset_61| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd v_prxprot_4 (_ bv4 32)) main_~item~0.offset)) (select (select (store |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd v_prxprot_4 (_ bv4 32)) main_~item~0.base)) main_~item~0.base) main_~item~0.offset) (store (store (select (store |v_#memory_$Pointer$.offset_61| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.offset_61| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd v_prxprot_4 (_ bv4 32)) main_~item~0.offset)) (select (select (store |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd v_prxprot_4 (_ bv4 32)) main_~item~0.base)) main_~item~0.base) main_~item~0.offset)) v_prxprot_1 (_ bv0 32)) (bvadd v_prxprot_1 (_ bv8 32)) v_DerPreprocessor_9)) main_~head~0.base) main_~head~0.offset) (_ bv12 32)))) (forall ((|main_#t~mem3.offset| (_ BitVec 32)) (v_DerPreprocessor_5 (_ BitVec 32)) (v_DerPreprocessor_10 (_ BitVec 32)) (v_prxprot_2 (_ BitVec 32))) (bvsle (bvadd (select (select (store (store |v_#memory_$Pointer$.offset_61| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.offset_61| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd |main_#t~mem3.offset| (_ bv4 32)) main_~item~0.offset)) (select (select (store |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd |main_#t~mem3.offset| (_ bv4 32)) main_~item~0.base)) main_~item~0.base) main_~item~0.offset) (store (store (select (store |v_#memory_$Pointer$.offset_61| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.offset_61| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd |main_#t~mem3.offset| (_ bv4 32)) main_~item~0.offset)) (select (select (store |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd |main_#t~mem3.offset| (_ bv4 32)) main_~item~0.base)) main_~item~0.base) main_~item~0.offset)) v_prxprot_2 (_ bv0 32)) (bvadd v_prxprot_2 (_ bv8 32)) v_DerPreprocessor_10)) main_~head~0.base) main_~head~0.offset) (_ bv12 32)) (select |#length| (select (select (store (store |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd |main_#t~mem3.offset| (_ bv4 32)) main_~item~0.base)) (select (select (store |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd |main_#t~mem3.offset| (_ bv4 32)) main_~item~0.base)) main_~item~0.base) main_~item~0.offset) (store (store (select (store |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd |main_#t~mem3.offset| (_ bv4 32)) main_~item~0.base)) (select (select (store |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd |main_#t~mem3.offset| (_ bv4 32)) main_~item~0.base)) main_~item~0.base) main_~item~0.offset)) v_prxprot_2 (_ bv0 32)) (bvadd v_prxprot_2 (_ bv8 32)) v_DerPreprocessor_5)) main_~head~0.base) main_~head~0.offset)))) (forall ((v_subst_2 (_ BitVec 32)) (v_prxprot_3 (_ BitVec 32)) (v_DerPreprocessor_8 (_ BitVec 32))) (bvsle (_ bv0 32) (bvadd (select (select (store (store |v_#memory_$Pointer$.offset_61| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.offset_61| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd v_prxprot_3 (_ bv4 32)) main_~item~0.offset)) (select (select (store |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd v_prxprot_3 (_ bv4 32)) main_~item~0.base)) main_~item~0.base) main_~item~0.offset) (store (store (select (store |v_#memory_$Pointer$.offset_61| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.offset_61| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd v_prxprot_3 (_ bv4 32)) main_~item~0.offset)) (select (select (store |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset) (store (select |v_#memory_$Pointer$.base_64| (select (select |v_#memory_$Pointer$.base_64| main_~item~0.base) main_~item~0.offset)) (bvadd v_prxprot_3 (_ bv4 32)) main_~item~0.base)) main_~item~0.base) main_~item~0.offset)) v_subst_2 (_ bv0 32)) (bvadd v_subst_2 (_ bv8 32)) v_DerPreprocessor_8)) main_~head~0.base) main_~head~0.offset) (_ bv8 32)))))))";
		QuantifierEliminationTest.runQuantifierEliminationTest(formulaAsString, null, true, mServices, mLogger, mMgdScript, mCsvWriter);
	}

}
