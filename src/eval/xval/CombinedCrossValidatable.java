package eval.xval;

import io.DataFileManager;
import io.Status;
import io.arff.ArffCombiner;

import java.io.File;

import data.util.CrossValidationData;

public class CombinedCrossValidatable implements CrossValidatable
{
	AbstractCrossValidatable cv1, cv2;

	public CombinedCrossValidatable(AbstractCrossValidatable cv1, AbstractCrossValidatable cv2)
	{
		this.cv1 = cv1;
		this.cv2 = cv2;
	}

	@Override
	public void init(CrossValidationData data)
	{
		cv1.init(data);
		cv2.init(data);

		if (cv1.getNumFolds() != cv2.getNumFolds())
			throw new IllegalStateException("different number of folds");
		if (!cv1.getTrainingDatasetName(0).equals(cv2.getTrainingDatasetName(0)))
			throw new IllegalStateException("different data");
	}

	@Override
	public void clearData()
	{
		cv1.clearData();
		cv2.clearData();
	}

	@Override
	public String getFeatureArffName()
	{
		return "combine(" + cv1.getFeatureArffName() + ";" + cv2.getFeatureArffName() + ")";
	}

	@Override
	public int getNumFolds()
	{
		return cv1.getNumFolds();
	}

	@Override
	public File getTestArffFile(int fold)
	{
		File file = DataFileManager.getArffFile(cv1.getTestDatasetName(fold), getFeatureArffName());
		if (!file.exists())
		{
			Status.INFO.println(Status.INDENT + "Creating test arff file: " + file.getName());
			Status.addIndent();
			File f1 = cv1.getTestArffFile(fold);
			File f2 = cv2.getTestArffFile(fold);
			ArffCombiner.combine(file, f1, f2);
			Status.remIndent();
		}
		return file;
	}

	@Override
	public File getTrainingArffFile(int fold)
	{
		File file = DataFileManager.getArffFile(cv1.getTrainingDatasetName(fold), getFeatureArffName());
		if (!file.exists())
		{
			Status.INFO.println(Status.INDENT + "Creating training arff file: " + file.getName());
			Status.addIndent();
			File f1 = cv1.getTrainingArffFile(fold);
			File f2 = cv2.getTrainingArffFile(fold);
			ArffCombiner.combine(file, f1, f2);
			Status.remIndent();
		}
		return file;
	}

	// @Override
	// public void loadAllData()
	// {
	// cv1.loadAllData();
	// cv2.loadAllData();
	// }

	@Override
	public String getDatasetBaseName()
	{
		String d1 = cv1.getDatasetBaseName();
		String d2 = cv2.getDatasetBaseName();
		if (d1.equals(d2))
			return d1;
		else
			throw new IllegalStateException("different datasets, if happening, what todo about the size?");
	}

	@Override
	public CrossValidationData getCVData()
	{
		CrossValidationData d1 = cv1.getCVData();
		CrossValidationData d2 = cv2.getCVData();
		if (d1 == d2)
			return d1;
		else
			throw new IllegalStateException("different datasets, if happening, what todo about the size?");
	}

}
