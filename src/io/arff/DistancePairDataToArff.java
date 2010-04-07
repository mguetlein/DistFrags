package io.arff;

import java.util.List;

import util.ListUtil;
import data.DistancePairData;
import data.MoleculeActivityData;

public class DistancePairDataToArff implements DistancePairArffWritable
{
	// public static String ARFF_NAME = "";

	MoleculeActivityData d;
	DistancePairData p;

	@Override
	public void init(MoleculeActivityData data, DistancePairData pairs)
	{
		this.d = data;
		this.p = pairs;
	}

	@Override
	public String getArffName()
	{
		return "";
	}

	@Override
	public List<String> getAdditionalInfo()
	{
		return null;
	}

	@Override
	public String getAttributeName(int attribute)
	{
		if (attribute < p.getNumDistancePairs())
			return p.getDistancePairName(attribute);
		else
			return d.getEndpoint();
	}

	@Override
	public String getAttributeValueSpace(int attribute)
	{
		if (attribute < p.getNumDistancePairs())
			return "numeric";
		else
			return "{0,1}";
	}

	@Override
	public String getAttributeValue(int instance, int attribute)
	{
		if (attribute < p.getNumDistancePairs())
		{
			Double d = ListUtil.getMean(p.getDistances(attribute, instance));
			if (d == null)
				return null;
			else
				return d.toString();
		}
		else
			return d.getMoleculeActivity(instance) + "";
	}

	@Override
	public int getNumAttributes()
	{
		return p.getNumDistancePairs() + 1;
	}

	@Override
	public int getNumInstances()
	{
		return d.getNumMolecules();
	}

	// @Override
	// public String getRelationName()
	// {
	// return d.getDatasetName() + " " + p.getFragmentName();
	// }

	@Override
	public boolean isSparse()
	{
		return false;
	}

	@Override
	public String getMissingValue(int attribute)
	{
		return "-1";
	}

	@Override
	public boolean isInstanceWithoutAttributeValues(int instance)
	{
		// Set<Integer> mols = f.getMoleculesForDistancePair(instance);
		// return mols == null || mols.size() == 0;
		return p.getNumDistancePairsForMolecule(instance) == 0;
	}

	// @Override
	// public File getArffFile()
	// {
	// return DataFileManager.getArffFile(d.getDatasetName(), p.getFragmentName());
	// }
}
