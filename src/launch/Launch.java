package launch;

import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;

import data.MoleculeActivityData;
import data.factories.MoleculeFactory;

public class Launch
{
	public static void main(String args[])
	{

		if (true == true)
			DistancePairTest.main(null);

		String usage = "distance fragments based machine learning tool\n" + "usage:\n"
				+ "-d <dataset-name>   \tperform 10-fold cv on dataset\n";
		// + "-x <experiment-name>\texperiment name (default: dist_cmp2)\n"
		// + "-m                  \tmerge and print results for all datasets\n";

		GetOpt opt = new GetOpt(args, "d:x:m");

		String dataset = null;
		// String experimentName = "dist_cmp2";
		boolean mergeAndPrintResults = false;

		try
		{
			if (args.length == 0)
				throw new IllegalStateException("param missing");

			int o = -1;
			while ((o = opt.getNextOption()) != -1)
			{
				if (o == 'd')
				{
					dataset = opt.getOptionArg();
				}
				// else if (o == 'x')
				// {
				// experimentName = opt.getOptionArg();
				// if (experimentName.length() < 1 || experimentName.contains("."))
				// throw new IllegalStateException("illegal experiment name: " + experimentName);
				// }
				// else if (o == 'm')
				// {
				// mergeAndPrintResults = true;
				// }
				else
					throw new IllegalStateException("illegal param: " + o);
			}

			if (!mergeAndPrintResults && (dataset == null || dataset.length() < 1))
				throw new IllegalStateException("illegal params");

			if (mergeAndPrintResults && (dataset != null && dataset.length() > 0))
				throw new IllegalStateException("illegal param combination");
		}
		catch (Exception e)
		{
			// e.printStackTrace();
			System.err.println(e.getMessage());
			System.err.println(usage);
			System.exit(1);
		}

		if (dataset != null && dataset.length() > 0)
		{
			MoleculeActivityData d = MoleculeFactory.getMoleculeActivityData(dataset);

			// CrossValidationFactory.performVariousCrossValidations(d, Settings.NUM_FOLDS, Settings.RANDOM_SEED);
			// CrossValidationFactory
			// .performFStatisticsDistancePairCrossValidation(d, Settings.NUM_FOLDS, Settings.RANDOM_SEED);

			// CrossValidationFactory.performChiSquareFragmentCrossValidation(d, Settings.CV_NUM_FOLDS,
			// Settings.CV_RANDOM_SEED, Settings.CV_STRATIFIED, DataFileManager.FRAGMENT_TYPE_LINFRAG, false);

			// CrossValidationFactory.performDistancePairCrossValidation(d, Settings.CV_NUM_FOLDS, Settings.CV_RANDOM_SEED,
			// Settings.CV_STRATIFIED, DataFileManager.FRAGMENT_TYPE_LINFRAG, false);

			// CrossValidationFactory.performDistancePairComparison(d, Settings.NUM_FOLDS, Settings.RANDOM_SEED);
			// CrossValidationFactory.performDistancePairNominalMissingCrossValidation(d, Settings.NUM_FOLDS,
			// Settings.RANDOM_SEED);

			// Status.INFO.println();
			// ResultHandler.getInstance().printToFile(dataset, experimentName);
			// Status.INFO.println();
			// Status.INFO.println(ResultHandler.getInstance().getResultsAccrossFolds(true));
		}
		// else if (mergeAndPrintResults)
		// {
		// //ResultHandler.getInstance().readCpdbResults("not_running_right_now");
		// ResultHandler.getInstance().printResultOverview(false);
		// }
	}
}
