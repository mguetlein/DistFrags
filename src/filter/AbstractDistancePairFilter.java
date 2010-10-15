package filter;

import io.Status;

import java.util.ArrayList;
import java.util.List;

import util.StringUtil;
import data.DistancePairData;
import data.MoleculeActivityData;

public abstract class AbstractDistancePairFilter implements DistancePairFilter
{
	int minNumPairs = 100;
	int maxNumPairs = Integer.MAX_VALUE;
	double confidenceLevel = 0.1;

	int numChosen;
	double minConfidence;

	public AbstractDistancePairFilter(int minNumPairs, double confidenceLevel)
	{
		this(minNumPairs, Integer.MAX_VALUE, confidenceLevel);
	}

	public AbstractDistancePairFilter(int minNumPairs, int maxNumPairs, double confidenceLevel)
	{
		this.confidenceLevel = confidenceLevel;
		this.minNumPairs = minNumPairs;
		this.maxNumPairs = maxNumPairs;
	}

	public abstract String getNiceFilterName();

	public abstract String getShortFilterName();

	public abstract double getTestConfidenceValue(DistancePairData p, MoleculeActivityData d, int pairIndex);

	public String toString()
	{
		return getNiceFilterName() + " (#num-fragments: " + numChosen + ", min-confidence: " + StringUtil.formatDouble(minConfidence) + ")";
	}

	@Override
	public DistancePairData apply(String distancePairName, DistancePairData p, MoleculeActivityData d)
	{
		Status.INFO.println(Status.INDENT + "Applying filter " + getNiceFilterName() + " " + getName());

		double testValues[] = new double[p.getNumDistancePairs()];
		int ordering[] = new int[p.getNumDistancePairs()];

		numChosen = 0;
		minConfidence = 0;

		for (int i = 0; i < testValues.length; i++)
		{
			ordering[i] = i;
			testValues[i] = getTestConfidenceValue(p, d, i);
		}

		// bubble sort ordering according to pValues
		for (int i = 0; i < ordering.length - 1; i++)
		{
			for (int j = i + 1; j < ordering.length; j++)
			{
				if (testValues[ordering[i]] > testValues[ordering[j]])
				{
					int tmp = ordering[i];
					ordering[i] = ordering[j];
					ordering[j] = tmp;
				}
			}
		}

		List<Integer> distancePairs = new ArrayList<Integer>();

		for (int i = 0; i < ordering.length - 1; i++)
		{
			if (i >= minNumPairs && testValues[ordering[i]] > confidenceLevel)
				break;
			if (i >= maxNumPairs)
				break;

			// if (p.getDistancePairName(ordering[i]).matches("N.*-.*O"))
			// {
			distancePairs.add(ordering[i]);
			// }
			//
			// System.out.println(p.getDistancePairName(ordering[i]));
			// System.out.println(ordering[i]);
			// Set<Integer> set = p.getMoleculesForDistancePair(ordering[i]);
			// for (Integer m : set)
			// {
			// List<Double> dis = p.getDistances(ordering[i], m);
			// System.out.print(d.getMoleculeSmiles(m) + " ");
			// for (Double double1 : dis)
			// {
			// System.out.print(double1 + " ");
			// }
			// System.out.println();
			// }
			// System.out.println();
			// }
			// if (p.getDistancePairName(ordering[i]).length() < 15) // startsWith("O<"))
			// distancePairs.add(ordering[i]);
		}

		numChosen = distancePairs.size();
		minConfidence = testValues[ordering[distancePairs.size() - 1]];

		DistancePairData res = p.getSubset(distancePairName, distancePairs);

		// Status.INFO.println("done");

		return res;
	}

	@Override
	public String getName()
	{
		String conf = confidenceLevel + "";
		int index = conf.indexOf(".");
		if (index != -1)
			conf = conf.substring(index + 1);
		String res = getShortFilterName() + "_c" + conf + "_m" + minNumPairs;
		if (maxNumPairs < Integer.MAX_VALUE)
			res += "_x" + maxNumPairs;
		return res;
	}
}
