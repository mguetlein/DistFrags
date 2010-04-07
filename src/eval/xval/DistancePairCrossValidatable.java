package eval.xval;

import io.DataFileManager;
import io.Status;
import io.arff.ArffWriter;
import io.arff.DistancePairArffWritable;
import io.arff.DistancePairSplitPointArffWritable;

import java.io.File;

import util.StringUtil;
import data.DistancePairData;
import data.factories.DistancePairFactory;
import data.util.CrossValidationData;
import data.util.DistancePairSplitPoints;
import filter.DistancePairFilter;

public class DistancePairCrossValidatable extends AbstractCrossValidatable
{
	protected DistancePairData trainPairs[];
	private DistancePairData testPairs[];

	private DistancePairArffWritable arffWritable;

	DistancePairFilter filter;

	public DistancePairCrossValidatable(String fragmentType, DistancePairArffWritable arffWritable)
	{
		this(fragmentType, arffWritable, null);
	}

	public DistancePairCrossValidatable(String fragmentType, DistancePairArffWritable arffWritable, DistancePairFilter filter)
	{
		super(fragmentType);
		fragmentName = DataFileManager.getFragmentNameDistancePair(fragmentName);
		arffName = arffWritable.getArffName();
		this.arffWritable = arffWritable;

		if (filter != null)
		{
			this.filter = filter;
			fragmentName = DataFileManager.getFragmentNameFiltered(fragmentName, filter);
		}
	}

	public void init(CrossValidationData data)
	{
		super.init(data);

		trainPairs = new DistancePairData[data.getNumFolds()];
		testPairs = new DistancePairData[data.getNumFolds()];
	}

	@Override
	public void clearData()
	{
		super.clearData();
		for (int i = 0; i < getNumFolds(); i++)
		{
			trainPairs[i] = null;
			testPairs[i] = null;
		}
	}

	// @Override
	// public void loadAllData()
	// {
	// super.loadAllData();
	// for (int i = 0; i < getNumFolds(); i++)
	// getTestDistancePairs(i);
	// }

	@Override
	public String toString()
	{
		double trainDistancePairs = 0;
		double testDistancePairs = 0;

		for (int i = 0; i < getNumFolds(); i++)
		{
			trainDistancePairs = (trainDistancePairs * i + this.trainPairs[i].getNumDistancePairs()) / (double) (i + 1);
			testDistancePairs = (testDistancePairs * i + this.testPairs[i].getNumDistancePairs()) / (double) (i + 1);
		}
		return super.toString() + "," + StringUtil.formatDouble(trainDistancePairs) + ","
				+ StringUtil.formatDouble(testDistancePairs);
	}

	protected DistancePairData getTrainingDistancePairs(int fold)
	{
		if (trainPairs[fold] == null)
		{
			if (filter == null)
				trainPairs[fold] = DistancePairFactory.mineDistancePairs(getTrainingData(fold), getTrainingFragments(fold));
			else
				trainPairs[fold] = DistancePairFactory.applyFilter_minePairs(getTrainingData(fold),
						getTrainingFragments(fold), filter);
		}
		return trainPairs[fold];
	}

	protected DistancePairData getTestDistancePairs(int fold)
	{
		if (testPairs[fold] == null)
			testPairs[fold] = DistancePairFactory.checkDistancePairs(getTestData(fold), getTestFragments(fold),
					getTrainingDistancePairs(fold));
		return testPairs[fold];
	}

	@Override
	public File getTestArffFile(int fold)
	{
		File file = DataFileManager.getArffFile(getTestDatasetName(fold), getFeatureArffName());
		if (!file.exists())
		{
			Status.INFO.println(Status.INDENT + "Creating test arff file: " + file.getName());
			Status.addIndent();
			writeTestDistancePairsToArffFile(file, fold);
			Status.remIndent();
		}
		return file;
	}

	protected void writeTestDistancePairsToArffFile(File file, int fold)
	{
		arffWritable.init(getTestData(fold), getTestDistancePairs(fold));
		if (arffWritable instanceof DistancePairSplitPointArffWritable)
			((DistancePairSplitPointArffWritable) arffWritable).initSplitPoints(new DistancePairSplitPoints(
					getTrainingDistancePairs(fold), getTrainingData(fold)));
		ArffWriter.writeToArffFile(file, arffWritable);
	}

	@Override
	public File getTrainingArffFile(int fold)
	{
		File file = DataFileManager.getArffFile(getTrainingDatasetName(fold), getFeatureArffName());
		if (!file.exists())
		{
			Status.INFO.println(Status.INDENT + "Creating training arff file: " + file.getName());
			Status.addIndent();
			writeTrainingDistancePairsToArffFile(file, fold);
			Status.remIndent();
		}
		return file;
	}

	protected void writeTrainingDistancePairsToArffFile(File file, int fold)
	{
		arffWritable.init(getTrainingData(fold), getTrainingDistancePairs(fold));
		if (arffWritable instanceof DistancePairSplitPointArffWritable)
			((DistancePairSplitPointArffWritable) arffWritable).initSplitPoints(new DistancePairSplitPoints(
					getTrainingDistancePairs(fold), getTrainingData(fold)));
		ArffWriter.writeToArffFile(file, arffWritable);
	}

}
