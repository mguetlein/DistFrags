package io.arff;

import data.DistancePairData;
import data.MoleculeActivityData;

public interface DistancePairArffWritable extends ArffWritable
{
	public void init(MoleculeActivityData data, DistancePairData pairs);

	public String getArffName();
}
