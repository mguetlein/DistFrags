package filter;

import data.DistancePairData;
import data.MoleculeActivityData;

public interface DistancePairFilter extends Filter
{
	public DistancePairData apply(String distancePairName, DistancePairData p, MoleculeActivityData d);
}
