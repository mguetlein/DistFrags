package io.arff;

import data.util.DistancePairSplitPoints;

public interface DistancePairSplitPointArffWritable extends DistancePairArffWritable
{
	public void initSplitPoints(DistancePairSplitPoints split);
}
