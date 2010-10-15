package eval;

import io.DataFileManager;
import io.Status;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import launch.DatasetSizeSettings;
import launch.Settings;
import util.MinMaxAvg;
import data.DistancePairData;
import data.FragmentData;
import data.MoleculeActivityData;
import datamining.RemoveResultSetFilter;
import datamining.ResultSet;
import datamining.ResultSetFilter;
import datamining.ResultSetIO;

public class ResultHandler
{

	public static ResultHandler instance = new ResultHandler();

	public static ResultHandler getInstance()
	{
		return instance;
	}

	// ---------------------------------------------------------------------------------------------------------

	public static final int PROPERTY_DATASET_NAME = 0;
	public static final int PROPERTY_FEATURE_TYPE = 1;
	public static final int PROPERTY_CV_FOLD = 2;
	public static final int PROPERTY_ALGORITHM = 3;
	public static final int PROPERTY_EVAL_ACCURACY = 4;
	public static final int PROPERTY_NUM_TEST_INSTANCES = 5;
	public static final int PROPERTY_NUM_TRAIN_INSTANCES = 6;
	public static final int PROPERTY_NUM_FEATURES = 7;
	public static final int PROPERTY_EVAL_SENSITIVITY = 8;
	public static final int PROPERTY_EVAL_SPECIFITIY = 9;
	public static final int PROPERTY_EVAL_UNCLASSIFIED = 10;
	public static final int PROPERTY_CV_SEED = 11;
	public static final int PROPERTY_CV_NUM_FOLDS = 12;
	public static final int PROPERTY_CV_STRATIFIED = 13;
	public static final int PROPERTY_DATASET_NUM_INSTANCES = 14;
	public static final int PROPERTY_DATASET_NUM_ACTIVES = 15;
	public static final int PROPERTY_DATASET_ENDPOINT = 16;
	public static final int PROPERTY_EVAL_AUC = 17;
	public static final int PROPERTY_EVAL_F_MEASURE = 18;
	public static final int PROPERTY_INFO = 19;
	public static final int PROPERTY_MODEL_BUILD_TIME = 20;
	public static final int PROPERTY_MODEL_PREDICT_TIME = 21;
	public static final int PROPERTY_NUM_TP = 22;
	public static final int PROPERTY_NUM_FP = 23;
	public static final int PROPERTY_NUM_TN = 24;
	public static final int PROPERTY_NUM_FN = 25;
	public static final int PROPERTY_CLASS_VALUES = 26;
	public static final int PROPERTY_PREDICTION_VALUES = 27;

	private static final String[] PROPS = { "Dataset", "Features", "Fold", "Algorithm", "Acc", "#Test", "#Train", "#Attrib", "Sens",
			"Spec", "Unclass", "Seed", "#Folds", "Strat", "#Instances", "#Actives", "Endpoint", "AUC", "fMeasure", "Info", "BuildT",
			"PredictT", "#TruePos", "#FalsePos", "#TrueNeg", "#FalseNeg", "Class", "Prediction" };

	public static String getPropertyString(int prop)
	{
		return PROPS[prop];
	}

	private static final String[] NICE_PROPS = { "Dataset Name", "Feature Type", "Fold", "Algorithm", "Accuracy", "Num Test Instances",
			"Num Train Instances", "Num Features", "Sensitivity", "Specificity", "Unclassified", "CV Seed", "CV Folds", "CV Stratified",
			"Num Instances", "Num Actives", "Endpoint", "Area Under Roc Curve", "F-Measure", "Additional Info", "Model Build Time",
			"Model Predict Time", "Num True Positives", "Num True Negatives", "Num False Positives", "Num False Negatives",
			"Testdata Class Values", "Testdata Prediction Values" };

	public void setNiceProps()
	{
		for (int i = 0; i < PROPS.length; i++)
			set.setNicePropery(PROPS[i], NICE_PROPS[i]);
	}

	private static final String MERGE_PROPERTY = "orig-eval-filename";

	private ResultSet set;

	public List<String> properties = new ArrayList<String>();

	public HashMap<String, Object> values = new HashMap<String, Object>();

	public ResultHandler()
	{
		set = new ResultSet();
	}

	public void set(int property, Object value)
	{
		set(PROPS[property], value);
	}

	private void set(String prop, Object value)
	{
		if (!properties.contains(prop))
			properties.add(prop);
		values.put(prop, value);
	}

	public void removeProperty(int property)
	{
		String prop = PROPS[property];
		if (properties.contains(prop))
		{
			properties.remove(prop);
			values.remove(prop);
			set.removePropery(prop);
		}
	}

	public void push()
	{
		int index = set.addResult();
		for (String prop : properties)
			set.setResultValue(index, prop, values.get(prop));
	}

	public void printResults()
	{
		printResults(false);
	}

	public void printResults(boolean mediaWikiFormat)
	{
		Status.INFO.println();
		if (mediaWikiFormat)
			Status.INFO.println(set.toMediaWikiString());
		else
			Status.INFO.print(set.toNiceString());
	}

	public void printResultsForDataset(boolean mediaWikiFormat, String dataset)
	{
		RemoveResultSetFilter filter = new RemoveResultSetFilter();
		filter.addFilterValue(PROPS[PROPERTY_DATASET_NAME], dataset, false);
		ResultSet filtered = set.filter(filter);

		Status.INFO.println();
		if (mediaWikiFormat)
			Status.INFO.println(filtered.toMediaWikiString());
		else
			Status.INFO.print(filtered.toNiceString());
	}

	public void printResultsAccrossFolds()
	{
		printResultsAccrossFolds(false);
	}

	public void printResultsAccrossFolds(boolean mediaWikiFormat)
	{
		List<String> equal = new ArrayList<String>();
		equal.add(PROPS[PROPERTY_DATASET_NAME]);
		equal.add(PROPS[PROPERTY_FEATURE_TYPE]);
		equal.add(PROPS[PROPERTY_ALGORITHM]);
		equal.add(PROPS[PROPERTY_CV_NUM_FOLDS]);
		equal.add(PROPS[PROPERTY_CV_STRATIFIED]);
		equal.add(PROPS[PROPERTY_INFO]);

		List<String> ommit = new ArrayList<String>();
		ommit.add(PROPS[PROPERTY_CV_FOLD]);
		ommit.add(MERGE_PROPERTY);

		List<String> variance = new ArrayList<String>();
		variance.add(PROPS[PROPERTY_EVAL_ACCURACY]);

		ResultSet joined = set.join(equal, ommit, variance);

		joined.sortResults(PROPS[PROPERTY_EVAL_AUC], false, true, 1);
		// joined.sortResults(PROPS[PROPERTY_ALGORITHM]);
		joined.sortResults(PROPS[PROPERTY_DATASET_NAME]);

		Status.INFO.println();
		if (mediaWikiFormat)
			Status.INFO.println(joined.toMediaWikiString());
		else
			Status.INFO.print(joined.toNiceString());
	}

	private ResultSet getFeatureDiff()
	{
		List<String> equal = new ArrayList<String>();
		equal.add(PROPS[PROPERTY_DATASET_NAME]);
		equal.add(PROPS[PROPERTY_ALGORITHM]);
		// equal.add(PROPS[PROPERTY_CV_NUM_FOLDS]);

		List<String> ommit = new ArrayList<String>();
		ommit.add(PROPS[PROPERTY_CV_NUM_FOLDS]);
		ommit.add(PROPS[PROPERTY_CV_FOLD]);
		ommit.add(PROPS[PROPERTY_CV_SEED]);
		ommit.add(PROPS[PROPERTY_NUM_TEST_INSTANCES]);
		ommit.add(PROPS[PROPERTY_NUM_TRAIN_INSTANCES]);
		ommit.add(MERGE_PROPERTY);

		ResultSet diffed = set.diff(PROPS[PROPERTY_FEATURE_TYPE], equal, ommit);

		// diffed.sortResults(PROPS[PROPERTY_ALGORITHM]);
		// diffed.sortResults(PROPS[PROPERTY_DATASET]);

		RemoveResultSetFilter filter = new RemoveResultSetFilter();
		filter.addFilterValue(PROPS[PROPERTY_ALGORITHM], "ZeroR");
		diffed = diffed.filter(filter);

		return diffed;
	}

	public void printFeatureComparison(boolean mediaWikiFormat)
	{
		ResultSet diffed = getFeatureDiff();

		Status.INFO.println();
		if (mediaWikiFormat)
			Status.INFO.println(diffed.toMediaWikiString());
		else
			Status.INFO.print(diffed.toNiceString());
	}

	public void printFeatureWinLoss(boolean mediaWikiFormat)
	{
		ResultSet diffed = getFeatureDiff();

		List<String> equal = new ArrayList<String>();
		// equal.add(PROPS[PROPERTY_FEATURE_TYPE]);
		// equal.add(PROPS[PROPERTY_ALGORITHM]);
		equal.add(PROPS[PROPERTY_DATASET_NAME]);

		List<String> ommit = new ArrayList<String>();
		ommit.add(PROPS[PROPERTY_EVAL_UNCLASSIFIED]);
		ommit.add(PROPS[PROPERTY_NUM_FEATURES]);

		ResultSet winLoss = diffed.winLoss(equal, ommit);

		winLoss.sortResults(PROPS[PROPERTY_FEATURE_TYPE]);

		Status.INFO.println();
		if (mediaWikiFormat)
			Status.INFO.println(winLoss.toMediaWikiString());
		else
			Status.INFO.print(winLoss.toNiceString());
	}

	public void clear()
	{
		set = new ResultSet();
	}

	public boolean evalFileExists(String datasetBaseName, String experimentName)
	{
		File f = DataFileManager.getEvalFile(datasetBaseName, experimentName);
		return f.exists();
	}

	public void printToFile(String datasetBaseName, String experimentName)
	{
		File f = DataFileManager.getEvalFile(datasetBaseName, experimentName);
		Status.INFO.print("Printing results to file '" + f.getName() + "' ... ");

		if (f.exists())
		{
			Status.INFO.flush();
			if (Settings.OVERWRITE_EVAL_FILES)
				Status.WARN.println("Overwriting already exitsing result file: '" + f + "'");
			else
				Status.WARN.println("Append to already exitsing result file: '" + f + "'");
			Status.WARN.flush();
		}
		else
			DataFileManager.createParentFolders(f);

		ResultSetIO.printToFile(f, set, Settings.OVERWRITE_EVAL_FILES);

		Status.INFO.println("done");
	}

	public void readFromFile(String datasetBaseName, String experimentName)
	{
		File f = DataFileManager.getEvalFile(datasetBaseName, experimentName);
		if (!f.exists())
			throw new Error("no result found: '" + f + "'");

		Status.INFO.print("Reading results from file '" + f.getName() + "' ... ");
		set = ResultSetIO.parseFromFile(f);
		Status.INFO.println("done");
	}

	public void readResults(String datasetNames[], String experimentNames[])
	{
		List<File> res = DataFileManager.getExistingEvalFiles(datasetNames, experimentNames);

		Status.INFO.print("Reading '" + res.size() + "' results  ... ");

		String firstFilename = "";
		for (File file : res)
		{
			if (set.getNumResults() == 0)
			{
				set = ResultSetIO.parseFromFile(file);
				firstFilename = file.toString();
			}
			else
			{
				set = set.merge(MERGE_PROPERTY, firstFilename, ResultSetIO.parseFromFile(file), file.toString());
			}
		}
		set.sortResults(PROPS[PROPERTY_FEATURE_TYPE], false, false, -1);

		Status.INFO.println("done");
	}

	public void readResults(String datasetNames[], String experimentName)
	{
		readResults(datasetNames, new String[] { experimentName });
	}

	public void applyFilter(ResultSetFilter filter)
	{
		set = set.filter(filter);
	}

	public void printResultOverview(boolean mediaWikiFormat)
	{
		set.sortResults(PROPS[PROPERTY_CV_FOLD], true, true, 1);

		// if (!mediaWikiFormat)
		// printResults(mediaWikiFormat);
		//
		// if (!mediaWikiFormat)
		printResultsAccrossFolds(mediaWikiFormat);
		//
		// if (!mediaWikiFormat)
		// printSignificances(mediaWikiFormat);
		//
		// printFeatureComparison(mediaWikiFormat);

		// printFeatureWinLoss(mediaWikiFormat);
	}

	public void printSignificances(boolean mediaWikiFormat)
	{
		double CONFIDENCE_LEVEL = 0.05;

		List<String> algorithms = new ArrayList<String>();
		List<String> datasets = new ArrayList<String>();

		for (int i = 0; i < set.getNumResults(); i++)
		{
			String a = (String) set.getResultValue(i, PROPS[PROPERTY_ALGORITHM]);
			String d = (String) set.getResultValue(i, PROPS[PROPERTY_DATASET_NAME]);

			if (!algorithms.contains(a))
				algorithms.add(a);
			if (!datasets.contains(d))
				datasets.add(d);
		}

		List<String> equal = new ArrayList<String>();
		equal.add(PROPS[PROPERTY_CV_FOLD]);
		equal.add(PROPS[PROPERTY_CV_NUM_FOLDS]);
		equal.add(PROPS[PROPERTY_CV_SEED]);

		ResultSet finalSet = new ResultSet();

		for (String a : algorithms)
		{
			RemoveResultSetFilter aFilter = new RemoveResultSetFilter();
			aFilter.addFilterValue(PROPS[PROPERTY_ALGORITHM], a, false);
			ResultSet s = set.filter(aFilter);

			for (String d : datasets)
			{
				RemoveResultSetFilter dFilter = new RemoveResultSetFilter();
				dFilter.addFilterValue(PROPS[PROPERTY_DATASET_NAME], d, false);
				ResultSet s2 = s.filter(dFilter);

				ResultSet signi = s2.pairedTTest(PROPS[PROPERTY_FEATURE_TYPE], equal, PROPS[PROPERTY_EVAL_ACCURACY], CONFIDENCE_LEVEL);

				for (int i = 0; i < signi.getNumResults(); i++)
				{
					signi.setResultValue(i, PROPS[PROPERTY_ALGORITHM], a);
					signi.setResultValue(i, PROPS[PROPERTY_DATASET_NAME], d);
				}
				finalSet.concat(signi);
			}
		}

		List<String> joinEqual = new ArrayList<String>();
		joinEqual.add(PROPS[PROPERTY_FEATURE_TYPE] + "_1");
		joinEqual.add(PROPS[PROPERTY_FEATURE_TYPE] + "_2");

		List<String> ommit = new ArrayList<String>();
		ommit.add(PROPS[PROPERTY_DATASET_NAME]);
		ommit.add(PROPS[PROPERTY_ALGORITHM]);

		ResultSet winLoss = finalSet.winLoss(joinEqual, ommit);

		Status.INFO.println();
		if (mediaWikiFormat)
			Status.INFO.println(winLoss.toMediaWikiString());
		else
			Status.INFO.print(winLoss.toNiceString());

	}

	// public boolean repair()
	// {
	// HashMap<Integer, Integer> foldRange = new HashMap<Integer, Integer>();
	// for (int i = 0; i < set.getNumResults(); i++)
	// {
	// Object o = set.getResultValue(i, PROPS[PROPERTY_CV_FOLD]);
	// if (o == null)
	// throw new Error("no fold");
	// else if (!(o instanceof Number))
	// throw new Error("fold is no number");
	// else
	// {
	// double d = ((Number) o).doubleValue();
	// Integer n = ((Number) o).intValue();
	//
	// if (d != n.intValue())
	// throw new Error("fold is no int");
	//
	// if (foldRange.containsKey(n))
	// foldRange.put(n, foldRange.get(n) + 1);
	// else
	// foldRange.put(n, 1);
	// }
	// }
	//
	// Set<Integer> folds = foldRange.keySet();
	// int min = Collections.min(folds);
	// if (min != 0)
	// throw new Error("smallest fold != 0");
	// int max = Collections.max(folds);
	// if (max != 9)
	// throw new Error("biggest fold != 9");
	//
	// int count = -1;
	// for (Integer c : foldRange.values())
	// {
	// if (count == -1)
	// count = c;
	// else if (count != c)
	// throw new Error("folds inbalanced");
	// }
	// if (count < 1)
	// throw new Error("no folds");
	//
	// for (int i = 0; i < set.getNumResults(); i++)
	// set.setResultValue(i, PROPS[PROPERTY_CV_FOLD], ((Number)
	// set.getResultValue(i, PROPS[PROPERTY_CV_FOLD]))
	// .intValue() + 1);
	//
	// Status.INFO.println("repaired");
	// return true;
	//
	// }

	public void setDataset(MoleculeActivityData origData, MoleculeActivityData cvData, MinMaxAvg molSizeInfo, FragmentData f,
			DistancePairData d)
	{
		set(PROPERTY_DATASET_NAME, origData.getDatasetBaseName());

		set(PROPERTY_DATASET_NUM_INSTANCES, origData.getNumMolecules());
		set(PROPERTY_DATASET_NUM_ACTIVES, origData.getNumActives());

		if (cvData != null)
		{
			// set("CV-" + PROPS[PROPERTY_DATASET_NUM_INSTANCES],
			// cvData.getNumMolecules());
			// set("CV-" + PROPS[PROPERTY_DATASET_NUM_ACTIVES],
			// cvData.getNumActives());
		}

		if (molSizeInfo != null)
		{
			set("Mol-Size-Min", molSizeInfo.getMin());
			set("Mean", molSizeInfo.getMean());
			set("Max", molSizeInfo.getMax());
		}

		if (f != null)
			set("fragments", f.getNumFragments());
		if (d != null)
			set("pairs", d.getNumDistancePairs());

		DatasetSizeSettings.setCurrentDatasetSize(origData.getDatasetBaseName());
		set(PROPERTY_INFO, "min-freq " + DatasetSizeSettings.MIN_FREQUENCY + " (" + DatasetSizeSettings.MIN_FREQUENCY_PER_CLASS + ")");

		// set(PROPERTY_DATASET_ENDPOINT, d.getEndpoint());
		push();

	}

	public void setDataset(MoleculeActivityData origData, MoleculeActivityData cvData)
	{
		setDataset(origData, cvData, null, null, null);
	}

	public void setDataset(MoleculeActivityData d)
	{
		setDataset(d, null);
	}

	public void setFragments(MoleculeActivityData d, FragmentData f, int fold)
	{
		// set(PROPERTY_DATASET_NAME,
		// DataFileManager.getNiceDatasetName(datasetName));
		set(PROPERTY_DATASET_NAME, d.getDatasetBaseName());

		set(PROPERTY_NUM_TRAIN_INSTANCES, d.getNumMolecules());
		set(PROPERTY_NUM_FEATURES, f.getNumFragments());

		set(PROPERTY_CV_FOLD, fold);

		push();
	}

	public void setDistances(MoleculeActivityData d, DistancePairData p, int fold)
	{
		// set(PROPERTY_DATASET_NAME,
		// DataFileManager.getNiceDatasetName(datasetName));
		set(PROPERTY_DATASET_NAME, d.getDatasetBaseName());

		set(PROPERTY_NUM_TRAIN_INSTANCES, d.getNumMolecules());
		set(PROPERTY_NUM_FEATURES, p.getNumDistancePairs());

		set(PROPERTY_CV_FOLD, fold);

		push();
	}
}
