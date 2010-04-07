package io.arff;

public class DistancePairNominalMissingDataToArff extends DistancePairNominalDataToArff
{
	// public static String ARFF_NAME = "nominal_missing";

	@Override
	public String getArffName()
	{
		return "nominal_missing";
	}

	@Override
	public String getAttributeValueSpace(int attribute)
	{
		if (attribute < pairs.getNumDistancePairs())
			return "{leq,g}";
		else
			return "{0,1}";
	}

	@Override
	public String getMissingValue(int attribute)
	{
		return "?";
	}

	@Override
	public boolean isSparse()
	{
		return false;
	}
}
