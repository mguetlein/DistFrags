package eval.xval;

import java.io.File;

import data.util.CrossValidationData;

public interface CrossValidatable
{
	public void init(CrossValidationData data);

	public String getDatasetBaseName();

	public String getFeatureArffName();

	// public void loadAllData();

	public void clearData();

	public int getNumFolds();

	public File getTestArffFile(int fold);

	public File getTrainingArffFile(int fold);

	public CrossValidationData getCVData();

}
