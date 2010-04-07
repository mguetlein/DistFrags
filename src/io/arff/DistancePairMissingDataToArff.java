package io.arff;

public class DistancePairMissingDataToArff extends DistancePairDataToArff
{
	// public static String ARFF_NAME = "missing";

	@Override
	public String getMissingValue(int attribute)
	{
		return "?";
	}

	@Override
	public String getArffName()
	{
		return "missing";
	}
}
