package filter;

import data.DistancePairData;
import data.MoleculeActivityData;

public class FStatisticsFilter extends AbstractDistancePairFilter
{

	public FStatisticsFilter(int minNumPairs, double confidenceLevel)
	{
		super(minNumPairs, confidenceLevel);
	}

	@Override
	public String getNiceFilterName()
	{
		return "F-Statistics-Filter";
	}

	@Override
	public String getShortFilterName()
	{
		return "fstats";
	}

	@Override
	public double getTestConfidenceValue(DistancePairData p, MoleculeActivityData d, int pairIndex)
	{
		return p.getFStatistic(d, pairIndex);
	}
}
