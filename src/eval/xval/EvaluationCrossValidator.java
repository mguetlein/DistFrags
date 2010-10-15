package eval.xval;

import io.Status;

import java.io.File;

import launch.Settings;
import weka.core.old.KnnDebug;
import data.util.CrossValidationData;
import eval.DefaultWekaEvaluation;
import eval.ResultHandler;
import eval.WekaEvaluation;

public class EvaluationCrossValidator extends AbstractCrossValidator
{
	CrossValidatable cv;

	WekaEvaluation eval = new DefaultWekaEvaluation();

	public EvaluationCrossValidator(String datasets[], CrossValidatable cv)
	{
		super(datasets);
		this.cv = cv;
	}

	public String getFeatureArffName()
	{
		return cv.getFeatureArffName();
	}

	@Override
	protected void crossValidateFold(int fold)
	{
		Status.INFO.println("Evaluate fold");
		Status.addIndent();

		File train = ((CrossValidatable) cv).getTrainingArffFile(fold);
		File test = ((CrossValidatable) cv).getTestArffFile(fold);

		if (Settings.DEBUG_KNN_PRINT_TESTSET_PREDICT)
			KnnDebug.TestPredictions.setTestMolecules(cv.getCVData().getMoleculeActivityData(fold, true));
		if (Settings.DEBUG_KNN_PRINT_SINGLE_PREDICT != -1)
			KnnDebug.SinglePrediction.setTrainMolecules(cv.getCVData().getMoleculeActivityData(fold, false));

		if (!Settings.DEBUG_SKIP_EVALUATION)
		{
			eval.evalTrainTest(cv.getDatasetBaseName(), train, test);
		}
		else
		{
			Status.INFO.println(Status.INDENT + "skipping evaluation");
			Status.INFO.println(Status.INDENT + " - '" + train.getName() + "'");
			Status.INFO.println(Status.INDENT + " - '" + test.getName() + "'");
		}

		Status.remIndent();
		cv.clearData();
	}

	@Override
	protected void initializeDataset(CrossValidationData xval)
	{
		cv.init(xval);
		ResultHandler.getInstance().set(ResultHandler.PROPERTY_FEATURE_TYPE, cv.getFeatureArffName());
	}

	@Override
	protected boolean resultsExist()
	{
		return ResultHandler.getInstance().evalFileExists(cv.getDatasetBaseName(), cv.getFeatureArffName());
	}

	@Override
	protected void storeResults()
	{
		if (!Settings.DEBUG_SKIP_EVALUATION)
		{
			ResultHandler.getInstance().printToFile(cv.getDatasetBaseName(), cv.getFeatureArffName());
			ResultHandler.getInstance().clear();
		}
	}
}
