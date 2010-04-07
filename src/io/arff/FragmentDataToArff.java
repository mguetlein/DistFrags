package io.arff;

import java.util.List;

import data.FragmentMoleculeData;
import data.MoleculeActivityData;

public class FragmentDataToArff implements ArffWritable
{
	// public static String ARFF_NAME = "";

	MoleculeActivityData d;
	FragmentMoleculeData f;

	public FragmentDataToArff(MoleculeActivityData d, FragmentMoleculeData f)
	{
		this.d = d;
		this.f = f;
	}

	@Override
	public List<String> getAdditionalInfo()
	{
		return null;
	}

	@Override
	public String getAttributeName(int attribute)
	{
		if (attribute < f.getNumFragments())
			return f.getFragmentSmiles(attribute);
		else
			return d.getEndpoint();
	}

	@Override
	public String getAttributeValueSpace(int attribute)
	{
		return "{0,1}";
	}

	@Override
	public String getAttributeValue(int instance, int attribute)
	{
		if (attribute < f.getNumFragments())
		{
			List<Integer> mols = f.getMoleculesForFragment(attribute);
			if (mols == null)
				return null;
			else if (mols.contains(instance))
				return "1";
			else
				return null;
		}
		else
			return d.getMoleculeActivity(instance) + "";
	}

	@Override
	public int getNumAttributes()
	{
		return f.getNumFragments() + 1;
	}

	@Override
	public int getNumInstances()
	{
		return d.getNumMolecules();
	}

	// @Override
	// public String getRelationName()
	// {
	// return d.getDatasetName() + " " + f.getFragmentName();
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
		return f.getFragmentsForMolecule(instance) == null;
	}

	// @Override
	// public File getArffFile()
	// {
	// return DataFileManager.getArffFile(d.getDatasetName(), f.getFragmentName());
	// }
}
