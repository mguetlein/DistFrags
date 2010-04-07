package eval.xval;

import io.arff.DistancePairOccuringToArff;
import io.arff.DistancePairSetValuedToArff;

import java.util.ArrayList;
import java.util.List;

import launch.Settings;
import util.MinMaxAvg;
import data.util.CrossValidationData;
import eval.ResultHandler;
import filter.ChiSquareFragmentFilter;
import filter.DistancePairFilter;
import filter.FragmentFilter;
import filter.TTestFilter;

public class CrossValidationFactory
{

	public static void printInfo(final String[] datasets)
	{
		AbstractCrossValidator cv = new AbstractCrossValidator(datasets)
		{
			CrossValidationData data;
			MinMaxAvg molSizeInfo;

			@Override
			protected void crossValidateFold(int fold)
			{
				ResultHandler.getInstance().setDataset(data.getOrigMoleculeActivityData(),
						data.getMoleculeActivityData(fold, false), molSizeInfo);
			}

			@Override
			protected void finalizeDataset()
			{}

			@Override
			protected void initializeDataset(CrossValidationData data)
			{
				this.data = data;
				if (Settings.isModeCDK() || Settings.isModeOpenBabel())
					molSizeInfo = data.getOrigMoleculeData().getMoleculeSizeInfo();
			}
		};
		cv.crossValidate();

		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_SEED);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_NUM_FOLDS);
		ResultHandler.getInstance().removeProperty(ResultHandler.PROPERTY_CV_STRATIFIED);

		ResultHandler.getInstance().printResultsAccrossFolds();
	}

	public static void mineFragments(final String[] datasets, String fragmentType, FragmentFilter filter)
	{
		MineFragmentsCrossValidator cv = new MineFragmentsCrossValidator(datasets, fragmentType, filter);
		cv.crossValidate();
	}

	public static void mineDistances(final String[] datasets, String fragmentType, DistancePairFilter filter, boolean browse)
	{
		MineDistancePairsCrossValidator cv = new MineDistancePairsCrossValidator(datasets, fragmentType, filter, browse);
		cv.crossValidate();
	}

	public static void performFragmentCrossValidation(String datasets[], String fragmentType)
	{
		EvaluationCrossValidator eval = new EvaluationCrossValidator(datasets, new FragmentCrossValidatable(fragmentType));
		eval.crossValidate();
	}

	public static void performDistancePairCrossValidation(String datasets[], String fragmentType)
	{
		EvaluationCrossValidator eval = new EvaluationCrossValidator(datasets, new DistancePairCrossValidatable(
				fragmentType, new DistancePairSetValuedToArff(), new TTestFilter(10000, 0.01)));// new TTestFilter(1, 500,

		// EvaluationCrossValidator eval = new EvaluationCrossValidator(datasets, new DistancePairCrossValidatable(
		// fragmentType, new DistancePairNominalMissingDataToArff()));
		// // 0.1)));

		eval.crossValidate();
	}

	public static void performFragmentAndDistancePairCrossValidation(String datasets[], String fragmentType,
			boolean skipEvaluation)
	{
		List<EvaluationCrossValidator> evals = new ArrayList<EvaluationCrossValidator>();

		evals.add(new EvaluationCrossValidator(datasets, new CombinedCrossValidatable(new FragmentCrossValidatable(
				fragmentType/* , new ChiSquareFragmentFilter(10000, 0.01) */), new DistancePairCrossValidatable(
				fragmentType, new DistancePairSetValuedToArff(), new TTestFilter(10000, 0.01)))));

		for (EvaluationCrossValidator eval : evals)
			eval.crossValidate();
	}

	public static void performChiSquareFragmentCrossValidation(String datasets[], String fragmentType)
	{
		List<EvaluationCrossValidator> evals = new ArrayList<EvaluationCrossValidator>();

		// evals.add(new EvaluationCrossValidator(datasets, new FragmentCrossValidatable(fragmentType)));
		evals.add(new EvaluationCrossValidator(datasets, new FragmentCrossValidatable(fragmentType,
				new ChiSquareFragmentFilter(100, 0.05))));
		// evals.add(new EvaluationCrossValidator(datasets, new FragmentCrossValidatable(fragmentType,
		// new ChiSquareFragmentFilter(5000, 0.05))));

		for (EvaluationCrossValidator eval : evals)
			eval.crossValidate();
	}

	public static void performTTestDistancePairCrossValidation(String datasets[], String fragmentType)
	{
		List<EvaluationCrossValidator> evals = new ArrayList<EvaluationCrossValidator>();

		evals.add(new EvaluationCrossValidator(datasets, new DistancePairCrossValidatable(fragmentType,
				new DistancePairOccuringToArff())));
		evals.add(new EvaluationCrossValidator(datasets, new DistancePairCrossValidatable(fragmentType,
				new DistancePairOccuringToArff(), new TTestFilter(100, 0.05))));

		for (EvaluationCrossValidator eval : evals)
			eval.crossValidate();
	}

	// public static void printInfo(MoleculeActivityData moleculeData, int numFolds, long randomSeed, boolean stratified,
	// String fragmentType)
	// {
	// setResultValues(moleculeData.getDatasetName(), numFolds, randomSeed, stratified);
	//
	// CrossValidationData xval = new CrossValidationData(moleculeData, numFolds, randomSeed, stratified);
	// DistancePairCrossValidatable cv = new DistancePairCrossValidatable(xval, fragmentType);
	// cv.loadAllData();
	//
	// Status.INFO.println("\n" + moleculeData.getDatasetName() + "\n" + cv.toString());
	// }

}
