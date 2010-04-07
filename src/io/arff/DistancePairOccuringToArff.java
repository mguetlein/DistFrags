package io.arff;

import java.util.List;

import data.DistancePairData;
import data.MoleculeActivityData;
import data.util.DistancePairSplitPoints;

public class DistancePairOccuringToArff implements DistancePairSplitPointArffWritable
{
	MoleculeActivityData data;
	DistancePairData pairs;
	DistancePairSplitPoints split;

	@Override
	public void init(MoleculeActivityData data, DistancePairData pairs)
	{
		this.data = data;
		this.pairs = pairs;
	}

	@Override
	public void initSplitPoints(DistancePairSplitPoints split)
	{
		this.split = split;
	}

	@Override
	public String getArffName()
	{
		return "occurence";
	}

	@Override
	public List<String> getAdditionalInfo()
	{
		return null;
	}

	@Override
	public String getAttributeName(int attribute)
	{
		if (attribute < pairs.getNumDistancePairs())
			return "\"" + split.getDistancePairSplitPointName(attribute) + "\"";
		else
			return data.getEndpoint();
	}

	@Override
	public String getAttributeValueSpace(int attribute)
	{
		if (attribute < pairs.getNumDistancePairs())
			return "{0,leq,g,b}";
		else
			return "{0,1}";
	}

	@Override
	public String getAttributeValue(int instance, int attribute)
	{
		if (attribute < pairs.getNumDistancePairs())
		{
			List<Double> dist = pairs.getDistances(attribute, instance);

			if (dist == null)
				return null;
			else
			{
				boolean leq = false;
				boolean g = false;

				double splitPoint = split.getSplitPoint(attribute);
				for (Double d : dist)
				{
					if (d <= splitPoint)
						leq = true;
					else
						g = true;
				}
				if (leq && g)
					return "b";
				else if (leq)
					return "leq";
				else if (g)
					return "g";
				else
					throw new IllegalStateException("WTF");
			}
		}
		else
			return data.getMoleculeActivity(instance) + "";
	}

	@Override
	public int getNumAttributes()
	{
		return pairs.getNumDistancePairs() + 1;
	}

	@Override
	public int getNumInstances()
	{
		return data.getNumMolecules();
	}

	// @Override
	// public String getRelationName()
	// {
	// return d.getDatasetName() + " " + p.getFragmentName();
	// }

	@Override
	public boolean isSparse()
	{
		return true;
	}

	@Override
	public String getMissingValue(int attribute)
	{
		throw new Error("data is sparse, missing values are 0");
	}

	@Override
	public boolean isInstanceWithoutAttributeValues(int instance)
	{
		// Set<Integer> mols = f.getMoleculesForDistancePair(instance);
		// return mols == null || mols.size() == 0;
		return pairs.getNumDistancePairsForMolecule(instance) == 0;
	}

	// @Override
	// public File getArffFile()
	// {
	// return DataFileManager.getArffFile(d.getDatasetName(), p.getFragmentName());
	// }
}
