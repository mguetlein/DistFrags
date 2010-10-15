package eval.xval;

import io.Status;
import launch.Settings;
import util.MemoryUtil;
import util.StringUtil;
import data.MoleculeActivityData;
import data.factories.MoleculeFactory;
import data.util.CrossValidationData;
import eval.ResultHandler;

public abstract class AbstractCrossValidator
{
	private String datasets[];

	public AbstractCrossValidator(String datasets[])
	{
		this.datasets = datasets;
	}

	abstract protected void initializeDataset(CrossValidationData data);

	abstract protected void crossValidateFold(int fold);

	abstract protected void storeResults();

	protected boolean resultsExist()
	{
		return false;
	}

	public void crossValidate()
	{
		int startDataset = 0;
		if (Settings.DEBUG_START_CROSSVALIDATION_AT_DATASET > 0)
			startDataset = Settings.DEBUG_START_CROSSVALIDATION_AT_DATASET;

		for (int n = startDataset; n < datasets.length; n++)
		{
			Status.INFO.println("\ndataset " + (n + 1) + "/" + datasets.length + ": " + datasets[n] + "\n"
					+ StringUtil.getTimeStamp(Settings.START_TIME) + "\n");

			MoleculeActivityData d = MoleculeFactory.getMoleculeActivityData(datasets[n]);
			CrossValidationData xval = new CrossValidationData(d, Settings.CV_NUM_FOLDS, Settings.CV_RANDOM_SEED, Settings.CV_STRATIFIED);

			ResultHandler.getInstance().set(ResultHandler.PROPERTY_DATASET_NAME, datasets[n]);
			ResultHandler.getInstance().set(ResultHandler.PROPERTY_CV_SEED, Settings.CV_RANDOM_SEED);
			ResultHandler.getInstance().set(ResultHandler.PROPERTY_CV_NUM_FOLDS, Settings.CV_NUM_FOLDS);
			ResultHandler.getInstance().set(ResultHandler.PROPERTY_CV_STRATIFIED, Settings.CV_STRATIFIED);

			initializeDataset(xval);

			if (!Settings.DEBUG_SKIP_RESULT_EXIST_CHECK && resultsExist())
			{
				Status.INFO.println("Results exist, skipping crossvalidation!");
				continue;
			}

			int startFold = 0;
			if (Settings.DEBUG_START_CROSSVALIDATION_AT_FOLD > 0)
				startFold = Settings.DEBUG_START_CROSSVALIDATION_AT_FOLD;

			for (int i = startFold; i < Settings.CV_NUM_FOLDS; i++)
			{
				ResultHandler.getInstance().set(ResultHandler.PROPERTY_CV_FOLD, (i + 1));

				System.gc();
				Status.INFO.println("\ndataset " + (n + 1) + "/" + datasets.length + ": " + datasets[n] + "\nfold    " + (i + 1) + "/"
						+ Settings.CV_NUM_FOLDS + "\n" + StringUtil.getTimeStamp(Settings.START_TIME) + ", mem-usage: "
						+ MemoryUtil.getUsedMemoryString() + "\n");

				crossValidateFold(i);

				ResultHandler.getInstance().printResultsForDataset(false, datasets[n]);

				if (Settings.DEBUG_ABORT_CROSSVALIDATION_AFTER_FIRST_FOLD || Settings.DEBUG_ABORT_EVALUATION_AFTER_FIRST_ALGORITHM)
					break;
			}

			// ResultHandler.getInstance().printResultsAccrossFolds(false);

			if (Settings.DEBUG_ABORT_EVALUATION_AFTER_FIRST_ALGORITHM)
				break;

			storeResults();

			if (Settings.DEBUG_ABORT_CROSSVALIDATION_AFTER_FIRST_FOLD)
				break;
		}
	}
}
