package filter;

import data.DistancePairData;
import data.MoleculeActivityData;

public class TTestFilter extends AbstractDistancePairFilter
{

	public TTestFilter(int minNumPairs, double confidenceLevel)
	{
		super(minNumPairs, confidenceLevel);
	}

	public TTestFilter(int minNumPairs, int maxNumPairs, double confidenceLevel)
	{
		super(minNumPairs, maxNumPairs, confidenceLevel);
	}

	@Override
	public String getNiceFilterName()
	{
		return "T-Test-Filter";
	}

	@Override
	public String getShortFilterName()
	{
		return "ttest";
	}

	@Override
	public double getTestConfidenceValue(DistancePairData p, MoleculeActivityData d, int pairIndex)
	{
		return p.getTTest(d, pairIndex);
	}
}
