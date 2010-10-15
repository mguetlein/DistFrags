package io.arff;

import java.util.List;

import data.DistancePairData;
import data.MoleculeActivityData;

public class DistancePairSetValuedToArff implements DistancePairArffWritable
{
	MoleculeActivityData data;
	DistancePairData pairs;

	@Override
	public void init(MoleculeActivityData data, DistancePairData pairs)
	{
		this.data = data;
		this.pairs = pairs;
	}

	@Override
	public String getArffName()
	{
		return "setvalued";
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
			return "\"" + pairs.getDistancePairName(attribute) + "\"";
		else
			return data.getEndpoint();
	}

	@Override
	public String getAttributeValueSpace(int attribute)
	{
		if (attribute < pairs.getNumDistancePairs())
			return "string";
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
				StringBuffer res = new StringBuffer("'{");
				boolean first = true;
				for (double d : dist)
				{
					if (first)
						first = false;
					else
						res.append(";");
					if ((int) d == d)
						res.append(d);
					else
						res.append(d);
				}
				res.append("}'");
				return res.toString();
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
		return false;
	}

	@Override
	public String getMissingValue(int attribute)
	{
		return "'{}'";
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
