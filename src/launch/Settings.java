package launch;

import io.Status;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Date;

import eval.WekaClassifierFactory;

public class Settings
{
	// OB - CDK switch
	public static final int MODE_OPEN_BABEL = 1;
	public static final int MODE_CDK = 2;
	public static final int MODE_EXTERN_OPEN_BABEL = 3;

	public static final int MODE = MODE_EXTERN_OPEN_BABEL;

	public static boolean isModeOpenBabel()
	{
		return MODE == MODE_OPEN_BABEL;
	}

	public static boolean isModeCDK()
	{
		return MODE == MODE_CDK;
	}

	public static boolean isModeExternOpenBabel()
	{
		return MODE == MODE_EXTERN_OPEN_BABEL;
	}

	// ----------------------------------------------------------

	public static final boolean CACHE_MOLECULES = true;
	public static final boolean CACHE_DISTANCE_MAPS = true;

	public static final boolean WRITE_DISTANCE_PAIRS = true;
	public static final boolean WRITE_ARFF_FILES = true;

	// --------------------------------------------------------

	public static final boolean DEBUG_SWITCH = true;

	public static boolean DEBUG_SKIP_RESULT_EXIST_CHECK;
	public static boolean DEBUG_SKIP_EVALUATION;
	public static boolean DEBUG_ABORT_EVALUATION_AFTER_FIRST_ALGORITHM;
	public static boolean DEBUG_ABORT_CROSSVALIDATION_AFTER_FIRST_FOLD;
	public static int DEBUG_START_CROSSVALIDATION_AT_FOLD;
	public static int DEBUG_START_CROSSVALIDATION_AT_DATASET;
	public static boolean DEBUG_KNN_PRINT_TESTSET_PREDICT;
	/** integer.max value means all */
	public static int DEBUG_KNN_PRINT_SINGLE_PREDICT;
	public static int[] DEBUG_KNN_PRINT_SINGLE_NEIGHBOUR;

	public static boolean DEBUG_KNN_LINE_PLOT;
	public static boolean DEBUG_KNN_BAR_PLOT;
	public static boolean DEBUG_KNN_ANALYZE;

	static
	{
		if (DEBUG_SWITCH)
		{
			DEBUG_SKIP_RESULT_EXIST_CHECK = true;
			DEBUG_SKIP_EVALUATION = false;
			DEBUG_ABORT_EVALUATION_AFTER_FIRST_ALGORITHM = false;
			DEBUG_ABORT_CROSSVALIDATION_AFTER_FIRST_FOLD = false;
			DEBUG_START_CROSSVALIDATION_AT_FOLD = 0;
			DEBUG_START_CROSSVALIDATION_AT_DATASET = -1;

			DEBUG_KNN_PRINT_TESTSET_PREDICT = false;
			if (DEBUG_KNN_PRINT_TESTSET_PREDICT)
				DEBUG_KNN_PRINT_SINGLE_PREDICT = Integer.MAX_VALUE;
			else
				DEBUG_KNN_PRINT_SINGLE_PREDICT = -1;

			if (DEBUG_KNN_PRINT_SINGLE_PREDICT != -1)
				DEBUG_KNN_PRINT_SINGLE_NEIGHBOUR = new int[] {};// new int[] { 200, 123, 93, 30, 168, 156 };
			else
				DEBUG_KNN_PRINT_SINGLE_NEIGHBOUR = new int[] {};
			DEBUG_KNN_LINE_PLOT = false;
			DEBUG_KNN_BAR_PLOT = false;
			DEBUG_KNN_ANALYZE = false;

			// -------------------------------------------------------

			Status.WARN.println("DEBUG SWITCH IS ON");

			if (DEBUG_SKIP_RESULT_EXIST_CHECK)
				Status.WARN.println(" Skipping result exist check");
			if (DEBUG_SKIP_EVALUATION)
				Status.WARN.println(" Skipping evaluation");
			if (DEBUG_ABORT_EVALUATION_AFTER_FIRST_ALGORITHM)
				Status.WARN.println(" Abort evaluation after first algorithm");
			if (DEBUG_ABORT_CROSSVALIDATION_AFTER_FIRST_FOLD)
				Status.WARN.println(" Abort CV after first fold");
			if (DEBUG_START_CROSSVALIDATION_AT_FOLD > 0)
				Status.WARN.println(" Starting CV at fold " + DEBUG_START_CROSSVALIDATION_AT_FOLD);
			if (DEBUG_START_CROSSVALIDATION_AT_DATASET > 0)
				Status.WARN.println(" Starting CV at dataset " + DEBUG_START_CROSSVALIDATION_AT_DATASET);
			if (DEBUG_KNN_PRINT_TESTSET_PREDICT)
				Status.WARN.println(" Print knn test set prediciton");
			if (DEBUG_KNN_PRINT_SINGLE_PREDICT > 0)
				Status.WARN.println(" Print knn single prediction for test inst " + DEBUG_KNN_PRINT_SINGLE_PREDICT);
			if (DEBUG_KNN_PRINT_SINGLE_NEIGHBOUR.length > 0)
				Status.WARN.println(" Print knn single neighbours for train inst " + Arrays.toString(DEBUG_KNN_PRINT_SINGLE_NEIGHBOUR));
			if (DEBUG_KNN_BAR_PLOT)
				Status.WARN.println(" Show knn bar plot");
			if (DEBUG_KNN_LINE_PLOT)
				Status.WARN.println(" Show knn line plot");
			if (DEBUG_KNN_ANALYZE)
				Status.WARN.println(" Analyze knn");
		}
		else
		{
			DEBUG_SKIP_RESULT_EXIST_CHECK = false;
			DEBUG_SKIP_EVALUATION = false;
			DEBUG_ABORT_EVALUATION_AFTER_FIRST_ALGORITHM = false;
			DEBUG_ABORT_CROSSVALIDATION_AFTER_FIRST_FOLD = false;
			DEBUG_START_CROSSVALIDATION_AT_FOLD = 0;
			DEBUG_START_CROSSVALIDATION_AT_DATASET = -1;
			DEBUG_KNN_PRINT_TESTSET_PREDICT = false;
			DEBUG_KNN_PRINT_SINGLE_PREDICT = -1;
			DEBUG_KNN_PRINT_SINGLE_NEIGHBOUR = new int[] {};
			DEBUG_KNN_LINE_PLOT = false;
			DEBUG_KNN_BAR_PLOT = false;
			DEBUG_KNN_ANALYZE = false;
		}
	}

	// ---------------------------------------------------------

	public static final long START_TIME = new Date().getTime();
	public static final boolean SHOW_PROGRESS_DIALOGS = false;
	public static String USER_HOME = System.getProperty("user.home");

	// settings

	public static final boolean OVERWRITE_EVAL_FILES = false;

	public static final int CV_NUM_FOLDS = 10;
	public static final long CV_RANDOM_SEED = 1;
	public static final boolean CV_STRATIFIED = true;

	// algorithms

	public static final WekaClassifierFactory.NamedClassifier CLASSIFIERS[] = { WekaClassifierFactory.getId3(),
			WekaClassifierFactory.getId3Both(), WekaClassifierFactory.getId3Include(), WekaClassifierFactory.getId3Exclude(),
	// WekaClassifierFactory.getId3(true)
	// WekaClassifierFactory.getIBK(),
	// wekaClassifierFactory.getIBK_tanimoto(), WekaClassifierFactory.getJ48(),
	// WekaClassifierFactory.getNaiveBayes() };
	// WekaClassifierFactory.getIBK_singleLinkage(), WekaClassifierFactory.getIBK_tanimoto(),
	// WekaClassifierFactory.getIBK_completeLinkage(),
	// WekaClassifierFactory.getIBK_hausdoffDistance(), WekaClassifierFactory.getIBK_RIBL()
	//
	};

	// user specific settings

	static
	{
		if (MODE == MODE_OPEN_BABEL)
		{
			System.loadLibrary("openbabel");
		}

		boolean knecht = false;
		try
		{
			knecht = InetAddress.getLocalHost().getHostName().contains("knecht");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (USER_HOME == null || USER_HOME.length() < 2)
		{
			if (knecht)
				USER_HOME = "/home/mguetlein";
			else
				USER_HOME = "/home/martin";
		}
	}

}
