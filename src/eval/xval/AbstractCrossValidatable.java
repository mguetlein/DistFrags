package eval.xval;

import io.DataFileManager;
import util.StringUtil;
import data.FragmentMoleculeData;
import data.MoleculeActivityData;
import data.factories.FragmentFactory;
import data.util.CrossValidationData;

public abstract class AbstractCrossValidatable implements CrossValidatable
{
	private CrossValidationData data;
	protected FragmentMoleculeData trainFragments[];
	protected FragmentMoleculeData testFragments[];
	protected final String fragmentType;
	protected String fragmentName;
	protected String arffName;

	public AbstractCrossValidatable(String fragmentType)
	{
		if (!DataFileManager.isLegalFragmentType(fragmentType))
			throw new IllegalArgumentException("illegal fragment type");
		this.fragmentType = fragmentType;
		this.fragmentName = fragmentType;
		arffName = "";
	}

	public void init(CrossValidationData data)
	{
		this.data = data;
		trainFragments = new FragmentMoleculeData[data.getNumFolds()];
		testFragments = new FragmentMoleculeData[data.getNumFolds()];
	}

	// @Override
	// public void loadAllData()
	// {
	// for (int i = 0; i < data.getNumFolds(); i++)
	// getTestFragments(i);
	// }

	@Override
	public void clearData()
	{
		for (int i = 0; i < data.getNumFolds(); i++)
		{
			trainFragments[i] = null;
			testFragments[i] = null;
		}
	}

	@Override
	public String toString()
	{
		double trainInstances = 0;
		double testInstances = 0;
		double trainFragments = 0;
		double testFragments = 0;
		for (int i = 0; i < data.getNumFolds(); i++)
		{
			trainInstances = (trainInstances * i + data.getMoleculeActivityData(i, false).getNumMolecules())
					/ (double) (i + 1);
			testInstances = (testInstances * i + data.getMoleculeActivityData(i, true).getNumMolecules()) / (double) (i + 1);
			trainFragments = (trainFragments * i + this.trainFragments[i].getNumFragments()) / (double) (i + 1);
			testFragments = (testFragments * i + this.testFragments[i].getNumFragments()) / (double) (i + 1);
		}
		return StringUtil.formatDouble(trainInstances) + "," + StringUtil.formatDouble(testInstances) + ","
				+ StringUtil.formatDouble(trainFragments) + "," + StringUtil.formatDouble(testFragments);
	}

	@Override
	public int getNumFolds()
	{
		return data.getNumFolds();
	}

	protected String getTestDatasetName(int fold)
	{
		return data.getDatasetNames(fold, true);
	}

	protected String getTrainingDatasetName(int fold)
	{
		return data.getDatasetNames(fold, false);
	}

	protected MoleculeActivityData getTestData(int fold)
	{
		return data.getMoleculeActivityData(fold, true);
	}

	protected MoleculeActivityData getTrainingData(int fold)
	{
		return data.getMoleculeActivityData(fold, false);
	}

	protected FragmentMoleculeData getTrainingFragments(int fold)
	{
		if (trainFragments[fold] == null)
			trainFragments[fold] = FragmentFactory.mineFragments(fragmentType, getTrainingData(fold));
		return trainFragments[fold];
	}

	protected FragmentMoleculeData getTestFragments(int fold)
	{
		if (testFragments[fold] == null)
			testFragments[fold] = FragmentFactory.checkFragments(getTestData(fold), getTrainingFragments(fold));
		return testFragments[fold];
	}

	@Override
	public String getFeatureArffName()
	{
		String arffString = "";
		if (arffName != null && arffName.length() > 0)
			arffString = "." + arffName;
		return fragmentName + arffString;
	}

	@Override
	public String getDatasetBaseName()
	{
		return data.getDatasetBaseName();
	}

	@Override
	public CrossValidationData getCVData()
	{
		return data;
	}

}
