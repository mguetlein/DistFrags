package filter;

import io.Status;

import java.util.ArrayList;
import java.util.List;

import util.StringUtil;
import data.FragmentMoleculeData;
import data.MoleculeActivityData;

public class ChiSquareFragmentFilter implements FragmentFilter
{
	int minNumFragments = 300;
	int maxNumFragments = Integer.MAX_VALUE;
	double confidenceLevel = 0.05;

	int numChosen;
	double minConfidence;

	public ChiSquareFragmentFilter(int minNumFragments, double minConfidence)
	{
		this(minNumFragments, Integer.MAX_VALUE, minConfidence);
	}

	public ChiSquareFragmentFilter(int minNumFragments, int maxNumFragments, double minConfidence)
	{
		this.minConfidence = minConfidence;
		this.maxNumFragments = maxNumFragments;
		this.minNumFragments = minNumFragments;
	}

	// public int getMinNumFragments()
	// {
	// return minNumFragments;
	// }
	//
	// public void setMinNumFragments(int minNumFragments)
	// {
	// this.minNumFragments = minNumFragments;
	// }
	//
	// public double getConfidenceLevel()
	// {
	// return confidenceLevel;
	// }
	//
	// public void setConfidenceLevel(double confidenceLevel)
	// {
	// this.confidenceLevel = confidenceLevel;
	// }

	public String toString()
	{
		return "Chi-Square-Fragment-Filter (#num-fragments: " + numChosen + ", min-confidence: "
				+ StringUtil.formatDouble(minConfidence) + ")";
	}

	@Override
	public FragmentMoleculeData apply(String fragmentName, FragmentMoleculeData f, MoleculeActivityData d)
	{
		Status.INFO.println(Status.INDENT + "Applying fragment filter chi-square");

		double pValues[] = new double[f.getNumFragments()];
		int ordering[] = new int[f.getNumFragments()];

		numChosen = 0;
		minConfidence = 0;

		for (int i = 0; i < pValues.length; i++)
		{
			ordering[i] = i;
			pValues[i] = FilterUtil.getChiSquarePValue(f.getMoleculesForFragment(i), d);
		}

		// bubble sort ordering according to pValues
		for (int i = 0; i < ordering.length - 1; i++)
		{
			for (int j = i + 1; j < ordering.length; j++)
			{
				if (pValues[ordering[i]] > pValues[ordering[j]])
				{
					int tmp = ordering[i];
					ordering[i] = ordering[j];
					ordering[j] = tmp;
				}
			}
		}

		List<Integer> fragments = new ArrayList<Integer>();

		for (int i = 0; i < ordering.length - 1; i++)
		{
			if (i >= minNumFragments && pValues[ordering[i]] > confidenceLevel)
				break;
			if (i >= maxNumFragments)
				break;

			fragments.add(ordering[i]);
		}

		numChosen = fragments.size();
		minConfidence = pValues[ordering[fragments.size() - 1]];

		FragmentMoleculeData res = f.getSubset(fragmentName, fragments);

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
		String res = "chisq_c" + conf + "_m" + minNumFragments;
		if (maxNumFragments < Integer.MAX_VALUE)
			res += "_x" + maxNumFragments;
		return res;
	}

}
