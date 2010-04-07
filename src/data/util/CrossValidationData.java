package data.util;

import io.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import util.ArrayUtil;
import data.MoleculeActivityData;
import data.MoleculeActivityDataImpl;
import data.MoleculeData;
import data.MoleculeDataImpl;

public class CrossValidationData
{
	MoleculeData data;
	int numFolds;
	long randomSeed;
	MoleculeActivityData activityData;

	boolean stratified;

	MoleculeData trainingData[];
	MoleculeData testData[];

	String trainingDataNames[];
	String testDataNames[];

	public CrossValidationData(MoleculeData data, int numFolds, long randomSeed)
	{
		this(data, numFolds, randomSeed, false);
	}

	public CrossValidationData(MoleculeData data, int numFolds, long randomSeed, boolean stratified)
	{
		this.data = data;
		this.numFolds = numFolds;
		this.randomSeed = randomSeed;
		if (data instanceof MoleculeActivityData)
			activityData = (MoleculeActivityData) data;
		this.stratified = stratified;

		generateNames();
		performCV();
	}

	public void clearData()
	{
		for (int j = 0; j < numFolds; j++)
		{
			trainingData[j] = null;
			testData[j] = null;
		}
	}

	public int getNumFolds()
	{
		return numFolds;
	}

	public String getDatasetBaseName()
	{
		return data.getDatasetBaseName();
	}

	public String getDatasetNames(int fold, boolean test)
	{
		if (test)
			return testDataNames[fold];
		else
			return trainingDataNames[fold];
	}

	public MoleculeData getOrigMoleculeData()
	{
		return data;
	}

	public MoleculeActivityData getOrigMoleculeActivityData()
	{
		if (activityData == null)
			throw new Error("Input was no activity data!");
		return activityData;
	}

	public MoleculeData getMoleculeData(int fold, boolean test)
	{
		if (trainingData == null)
			performCV();

		if (test)
			return testData[fold];
		else
			return trainingData[fold];
	}

	public MoleculeActivityData getMoleculeActivityData(int fold, boolean test)
	{
		if (trainingData == null)
			performCV();
		if (activityData == null)
			throw new Error("Input was no activity data!");

		if (test)
			return (MoleculeActivityData) testData[fold];
		else
			return (MoleculeActivityData) trainingData[fold];
	}

	private void generateNames()
	{
		trainingDataNames = new String[numFolds];
		testDataNames = new String[numFolds];

		for (int j = 0; j < numFolds; j++)
		{
			trainingDataNames[j] = data.getDatasetName() + getNameSuffix(j, false);
			testDataNames[j] = data.getDatasetName() + getNameSuffix(j, true);
		}
	}

	private void stratifiedCV()
	{
		Status.INFO.print("Perform CV, numFolds: " + numFolds + ", seed: " + randomSeed + " on dataset: '" + data
				+ "' ...\n ... folds: ");

		Integer[] actives = new Integer[activityData.getNumActives()];
		Integer[] inactives = new Integer[activityData.getNumInactives()];
		int aCount = 0;
		int iCount = 0;
		for (int i = 0; i < activityData.getNumMolecules(); i++)
		{
			if (activityData.getMoleculeActivity(i) == 1)
			{
				actives[aCount] = i;
				aCount++;
			}
			else
			{
				inactives[iCount] = i;
				iCount++;
			}
		}
		assert (actives[actives.length - 1] != 0 && aCount == activityData.getNumActives());
		assert (inactives[inactives.length - 1] != 0 && iCount == activityData.getNumInactives());

		Random r = new Random(randomSeed);
		ArrayUtil.scramble(actives, r);
		ArrayUtil.scramble(inactives, r);

		List<Integer[]> activeFolds = ArrayUtil.split(actives, numFolds);
		List<Integer[]> inactiveFolds = ArrayUtil.split(inactives, numFolds);

		List<Integer[]> mergedFolds = new ArrayList<Integer[]>();

		for (int i = 0; i < numFolds; i++)
		{
			Integer[] merged = ArrayUtil.concat(activeFolds.get(i), inactiveFolds.get(numFolds - (i + 1)));
			ArrayUtil.scramble(merged, r);
			mergedFolds.add(merged);
		}

		trainingData = new MoleculeData[numFolds];
		testData = new MoleculeData[numFolds];

		for (int i = 0; i < numFolds; i++)
		{
			List<String> trainingSmiles = new ArrayList<String>();
			List<String> testSmiles = new ArrayList<String>();

			List<Integer> trainingActivity = new ArrayList<Integer>();
			List<Integer> testActivity = new ArrayList<Integer>();

			for (int j = 0; j < numFolds; j++)
			{
				for (Integer mol : mergedFolds.get(j))
				{
					if (i == j)// test-fold
					{
						testSmiles.add(activityData.getMoleculeSmiles(mol));
						testActivity.add(activityData.getMoleculeActivity(mol));
					}
					else
					{
						trainingSmiles.add(activityData.getMoleculeSmiles(mol));
						trainingActivity.add(activityData.getMoleculeActivity(mol));
					}
				}

			}

			trainingData[i] = new MoleculeActivityDataImpl(data.getDatasetName(), data.getDatasetName()
					+ getNameSuffix(i, false), data.getDatasetName(), trainingSmiles, trainingActivity);
			testData[i] = new MoleculeActivityDataImpl(data.getDatasetName(),
					data.getDatasetName() + getNameSuffix(i, true), data.getDatasetName(), testSmiles, testActivity);

			System.out.print((i > 0 ? ", " : " ") + trainingData[i].getNumMolecules() + "/" + testData[i].getNumMolecules());
		}

		Status.INFO.println(" done");
	}

	private void performCV()
	{
		if (stratified)
			stratifiedCV();
		else
			unstratifiedCV();
	}

	private void unstratifiedCV()
	{
		Status.INFO.print("Perform CV numFolds: " + numFolds + " seed: " + randomSeed + " on dataset: '" + data + "' ... ");

		HashMap<Integer, List<String>> trainingSmiles = new HashMap<Integer, List<String>>();
		HashMap<Integer, List<String>> testSmiles = new HashMap<Integer, List<String>>();
		for (int j = 0; j < numFolds; j++)
		{
			trainingSmiles.put(j, new ArrayList<String>());
			testSmiles.put(j, new ArrayList<String>());
		}

		HashMap<Integer, List<Integer>> trainingActivity = null;
		HashMap<Integer, List<Integer>> testActivity = null;
		if (activityData != null)
		{
			trainingActivity = new HashMap<Integer, List<Integer>>();
			testActivity = new HashMap<Integer, List<Integer>>();
			for (int j = 0; j < numFolds; j++)
			{
				trainingActivity.put(j, new ArrayList<Integer>());
				testActivity.put(j, new ArrayList<Integer>());
			}
		}

		int ordering[] = new int[data.getNumMolecules()];
		for (int i = 0; i < ordering.length; i++)
			ordering[i] = i;

		Random r = new Random(randomSeed);
		for (int i = 0; i < ordering.length; i++)
		{
			int rand_i = r.nextInt(ordering.length);
			int tmp = ordering[i];
			ordering[i] = ordering[rand_i];
			ordering[rand_i] = tmp;
		}

		for (int i = 0; i < data.getNumMolecules(); i++)
		{
			int x = i % numFolds;

			for (int j = 0; j < numFolds; j++)
			{
				boolean test = j == x;

				if (test)
					testSmiles.get(j).add(data.getMoleculeSmiles(ordering[i]));
				else
					trainingSmiles.get(j).add(data.getMoleculeSmiles(ordering[i]));

				if (activityData != null)
				{
					if (test)
						testActivity.get(j).add(activityData.getMoleculeActivity(ordering[i]));
					else
						trainingActivity.get(j).add(activityData.getMoleculeActivity(ordering[i]));
				}
			}
		}

		trainingData = new MoleculeData[numFolds];
		testData = new MoleculeData[numFolds];

		for (int j = 0; j < numFolds; j++)
		{
			if (activityData != null)
			{
				trainingData[j] = new MoleculeActivityDataImpl(data.getDatasetName(), data.getDatasetName()
						+ getNameSuffix(j, false), data.getDatasetName(), trainingSmiles.get(j), trainingActivity.get(j));
				testData[j] = new MoleculeActivityDataImpl(data.getDatasetName(), data.getDatasetName()
						+ getNameSuffix(j, true), data.getDatasetName(), testSmiles.get(j), testActivity.get(j));
			}
			else
			{
				trainingData[j] = new MoleculeDataImpl(data.getDatasetName(), data.getDatasetName()
						+ getNameSuffix(j, false), trainingSmiles.get(j));
				testData[j] = new MoleculeDataImpl(data.getDatasetName(), data.getDatasetName() + getNameSuffix(j, true),
						testSmiles.get(j));
			}
		}

		Status.INFO.println("done");
	}

	private String getNameSuffix(int fold, boolean test)
	{
		return "_CV_f" + numFolds + "_s" + randomSeed + "_i" + (fold + 1) + (test ? "_test" : "_training");
	}
}
