package data;

import java.util.List;

public class MoleculeActivityDataImpl extends MoleculeDataImpl implements MoleculeActivityData
{
	String endpoint;

	List<Integer> activities;

	int numActives;
	int numInactives;

	// public MoleculeActivityDataImpl(String datasetBaseName, String datasetNameAndEndpoint, List<String> smiles, List<Integer>
	// activities)
	// {
	// this(datasetNameAndEndpoint, datasetNameAndEndpoint, smiles, activities);
	// }

	public MoleculeActivityDataImpl(String datasetBaseName, String datasetName, String endpoint, List<String> smiles,
			List<Integer> activities)
	{
		super(datasetBaseName, datasetName, smiles);
		this.endpoint = endpoint;
		this.activities = activities;

		for (Integer act : activities)
			if (act == 1)
				numActives++;
			else if (act == 0)
				numInactives++;
		assert (numActives + numInactives == activities.size());
	}

	@Override
	public int getMoleculeActivity(int index)
	{
		return activities.get(index);
	}

	public String toString()
	{
		return "MoleculeData (#molecules: " + smiles.size() + ", #actives: " + numActives + ")";
	}

	@Override
	public int getNumActives()
	{
		return numActives;
	}

	@Override
	public int getNumInactives()
	{
		return numInactives;
	}

	@Override
	public String getEndpoint()
	{
		return endpoint;
	}

}
