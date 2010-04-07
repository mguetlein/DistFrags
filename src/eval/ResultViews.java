package eval;

import io.DataFileManager;
import datamining.RegexRemoveResultSetFilter;
import datamining.RemoveResultSetFilter;
import datamining.ReplaceValueResultSetFilter;

public class ResultViews
{
	public static void printNumFeatures(String datasets[])
	{
		ResultHandler.getInstance().readResults(datasets,
		// new String[] { "linfrag.dist.nominal_missing",
				// // "linfrag.chisq_c05_m5000",
				// // "linfrag.chisq_c05_m100"
				// });
				new String[] { "linfrag.chisq_c05_m100", "linfrag",
				// "linfrag.dist.occurence",
						// "linfrag.dist.ttest_c05_m5000.nominal_missing",
						"linfrag.dist.ttest_c05_m100.occurence",
						// "linfrag.dist.fstats_c05_m100.nominal_missing",
						"combine(linfrag.chisq_c05_m100;linfrag.dist.ttest_c05_m100.occurence)" });

		RemoveResultSetFilter filter2 = new RemoveResultSetFilter();
		filter2.addFilterValue(ResultHandler.getPropertyString(ResultHandler.PROPERTY_ALGORITHM), "ZeroR", false);
		ResultHandler.getInstance().applyFilter(filter2);

		ReplaceValueResultSetFilter replace = new ReplaceValueResultSetFilter();
		replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE),
				"linfrag.dist.nominal_missing", "All");
		// replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE), "linfrag", "Linear Fragments");
		// replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE), "linfrag", "All");
		// replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE), "linfrag.chisq_c05_m100",
		// "Confidence > 95%");
		replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE),
				"linfrag.dist.ttest_c05_m100.nominal_missing", "Confidence > 95%");

		ResultHandler.getInstance().applyFilter(replace);

		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_SEED);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_NUM_FOLDS);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_STRATIFIED);
		// ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_FEATURES);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_ALGORITHM);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_EVAL_ACCURACY);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_EVAL_UNCLASSIFIED);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_EVAL_SENSITIVITY);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_EVAL_SPECIFITIY);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_EVAL_AUC);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_EVAL_F_MEASURE);

		ResultHandler.getInstance().applyFilter(new DatasetNiceNameResultSetFilter(datasets));
		ResultHandler.getInstance().setNiceProps();

		ResultHandler.getInstance().printResultsAccrossFolds(false);
	}

	public static void printClassificationResults(String datasets[])
	{
		ResultHandler.getInstance().readResults(datasets, new String[] { "linfrag.chisq_c05_m100", "linfrag" });
		// "linfrag.dist.nominal_missing" });
		// "linfrag.chisq_c05_m5000",
		// "linfrag.chisq_c05_m100"
		// });

		ReplaceValueResultSetFilter replace = new ReplaceValueResultSetFilter();
		// replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE), "linfrag", "Linear Fragments");
		replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE),
				"linfrag.dist.nominal_missing", "Linear Distance Fragments");
		ResultHandler.getInstance().applyFilter(replace);

		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_SEED);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_NUM_FOLDS);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_STRATIFIED);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_EVAL_UNCLASSIFIED);

		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_NUM_FEATURES);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_NUM_TRAIN_INSTANCES);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_NUM_TEST_INSTANCES);

		ResultHandler.getInstance().applyFilter(new DatasetNiceNameResultSetFilter(datasets));
		ResultHandler.getInstance().setNiceProps();

		ResultHandler.getInstance().printResultsAccrossFolds(false);
	}

	public static void printChiSquareClassificationComparison(String[] datasets)
	{
		ResultHandler.getInstance().readResults(datasets,
				new String[] { "linfrag", "linfrag.chisq_c05_m5000", "linfrag.chisq_c05_m100" });

		ReplaceValueResultSetFilter replace = new ReplaceValueResultSetFilter();
		replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE), "linfrag", "All");
		replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE), "linfrag.chisq_c05_m5000",
				"Top 5000");
		replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE), "linfrag.chisq_c05_m100",
				"Confidence > 95%");
		ResultHandler.getInstance().applyFilter(replace);

		RemoveResultSetFilter filter2 = new RemoveResultSetFilter();
		filter2.addFilterValue(ResultHandler.getPropertyString(ResultHandler.PROPERTY_ALGORITHM), new String[] { "ZeroR" });
		ResultHandler.getInstance().applyFilter(filter2);

		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_SEED);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_NUM_FOLDS);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_STRATIFIED);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_EVAL_UNCLASSIFIED);

		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_NUM_FEATURES);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_NUM_TRAIN_INSTANCES);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_NUM_TEST_INSTANCES);

		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_EVAL_SENSITIVITY);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_EVAL_SPECIFITIY);

		ResultHandler.getInstance().applyFilter(new DatasetNiceNameResultSetFilter(datasets));
		ResultHandler.getInstance().setNiceProps();

		ResultHandler.getInstance().printResultOverview(false);

	}

	public static void printTTestClassificationComparison()
	{
		ResultHandler.getInstance().readResults(
				DataFileManager.BENCHMARK_DATASETS,
				new String[] { "linfrag.dist.nominal_missing", "linfrag.dist.ttest_c05_m5000.nominal_missing",
						"linfrag.dist.ttest_c05_m100.nominal_missing", "linfrag.dist.ttest_c1_m100.nominal_missing",
						"linfrag.dist.fstats_c05_m5000.nominal_missing", "linfrag.dist.fstats_c05_m100.nominal_missing",
						"linfrag.dist.fstats_c1_m100.nominal_missing", });

		// ReplaceValueResultSetFilter replace = new ReplaceValueResultSetFilter();
		// replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE),
		// "linfrag.dist.nominal_missing", "All");
		// replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE),
		// "linfrag.dist.ttest_c1_m100.nominal_missing", "Confidence > 90%");
		// ResultHandler.getInstance().applyFilter(replace);

		RemoveResultSetFilter filter2 = new RemoveResultSetFilter();
		filter2.addFilterValue(ResultHandler.getPropertyString(ResultHandler.PROPERTY_ALGORITHM), new String[] { "ZeroR" });
		ResultHandler.getInstance().applyFilter(filter2);

		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_SEED);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_NUM_FOLDS);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_STRATIFIED);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_EVAL_UNCLASSIFIED);

		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_NUM_FEATURES);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_NUM_TRAIN_INSTANCES);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_NUM_TEST_INSTANCES);

		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_EVAL_SENSITIVITY);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_EVAL_SPECIFITIY);

		ResultHandler.getInstance().applyFilter(new DatasetNiceNameResultSetFilter(DataFileManager.BENCHMARK_DATASETS));
		ResultHandler.getInstance().setNiceProps();

		ResultHandler.getInstance().printResultOverview(false);

	}

	public static void printCombineClassificationComparison(String datasets[])
	{
		ResultHandler.getInstance().readResults(datasets, new String[] { // "combine(linfrag.chisq_c05_m100;linfrag.dist.ttest_c05_m100.nominal_missing)",
				// "linfrag.dist.ttest_c05_m100.occurence",
						"linfrag.dist.ttest_c01_m10000.setvalued",
						// "linfrag.dist.ttest_c1_m100_x5000.setvalued",
						// "combine(linfrag;linfrag.dist.ttest_c1_m100_x5000.occurence)",
						// "combine(linfrag.chisq_c05_m100;linfrag.dist.ttest_c05_m100.occurence)",
						// "combine(linfrag.chisq_c05_m100;linfrag.dist.ttest_c05_m100.missing)",
						// "combine(linfrag.chisq_c05_m100;linfrag.dist.ttest_c1_m100.nominal_missing)",
						// "combine(linfrag.chisq_c05_m100;linfrag.dist.fstats_c05_m100.nominal_missing)",
						"linfrag" });

		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_FEATURE_TYPE);

		// ReplaceValueResultSetFilter replace = new ReplaceValueResultSetFilter();
		// replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE),
		// "combine(linfrag;linfrag.dist.ttest_c1_m100_x5000.occurence)", "new-combine");// "nominal");
		// replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE),
		// "combine(linfrag.chisq_c05_m100;linfrag.dist.ttest_c1_m100.nominal_missing)", "combine.1");
		// replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE),
		// "combine(linfrag.chisq_c05_m100;linfrag.dist.ttest_c05_m100.nominal_missing)", "combine");// "nominal");
		// replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE),
		// "combine(linfrag.chisq_c05_m100;linfrag.dist.fstats_c05_m100.nominal_missing)", "fcombine");
		// replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE),
		// "combine(linfrag.chisq_c05_m100;linfrag.dist.ttest_c05_m100.missing)", "numeric");
		// replace.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_FEATURE_TYPE), "linfrag.chisq_c05_m100",
		// "linfrag");
		// ResultHandler.getInstance().applyFilter(replace);

		RegexRemoveResultSetFilter filter2 = new RegexRemoveResultSetFilter();
		filter2.addFilterValue(ResultHandler.getPropertyString(ResultHandler.PROPERTY_ALGORITHM), "IBk.*", false);
		ResultHandler.getInstance().applyFilter(filter2);

		// InclusiveResultSetFilter filter2 = new InclusiveResultSetFilter();
		// filter2.addValue(ResultHandler.getPropertyString(ResultHandler.PROPERTY_ALGORITHM), new String[] { "J48" });
		// ResultHandler.getInstance().applyFilter(filter2);

		// InclusiveResultSetFilter filter3 = new InclusiveResultSetFilter();
		// filter3.addValue(ResultHandler.getPropertyString(ResultHandler.PROPERTY_DATASET_NAME),
		// new String[] { "rat_carcinogenicity_alt" });
		// ResultHandler.getInstance().applyFilter(filter3);

		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_SEED);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_NUM_FOLDS);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_STRATIFIED);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_EVAL_UNCLASSIFIED);

		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_NUM_FEATURES);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_NUM_TRAIN_INSTANCES);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_NUM_TEST_INSTANCES);

		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_EVAL_SENSITIVITY);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_EVAL_SPECIFITIY);

		ResultHandler.getInstance().applyFilter(new DatasetNiceNameResultSetFilter(DataFileManager.BENCHMARK_DATASETS));
		ResultHandler.getInstance().setNiceProps();

		ResultHandler.getInstance().printResultOverview(false);

	}
}
