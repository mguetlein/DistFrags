package filter;

import data.DistancePairData;
import data.MoleculeActivityData;

public class KolmogorovSmirnovTest extends AbstractDistancePairFilter
{

	public KolmogorovSmirnovTest(int minNumPairs, double confidenceLevel)
	{
		super(minNumPairs, confidenceLevel);
	}

	public KolmogorovSmirnovTest(int minNumPairs, int maxNumPairs, double confidenceLevel)
	{
		super(minNumPairs, maxNumPairs, confidenceLevel);
	}

	@Override
	public String getNiceFilterName()
	{
		return "Kolmogorov-Smirnov-Filter";
	}

	@Override
	public String getShortFilterName()
	{
		return "kolmo";
	}

	@Override
	public double getTestConfidenceValue(DistancePairData p, MoleculeActivityData d, int pairIndex)
	{
		return p.getKolmogorovSmirnovTest(d, pairIndex);
	}
}
