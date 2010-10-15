package data;

import java.util.List;

import data.util.SmilesContainer;

public class MoleculeDataImpl implements MoleculeData, SmilesContainer
{
	protected List<String> smiles;

	private String datasetName;

	private String datasetBaseName;

	public MoleculeDataImpl(String datasetBaseName, String datasetName, List<String> smiles)
	{
		this.datasetBaseName = datasetBaseName;
		this.datasetName = datasetName;
		this.smiles = smiles;
	}

	@Override
	public String getMoleculeSmiles(int index)
	{
		return smiles.get(index);
	}

	@Override
	public int getNumMolecules()
	{
		return smiles.size();
	}

	public String toString()
	{
		return "MoleculeData (#molecules: " + smiles.size() + ")";
	}

	@Override
	public String getDatasetName()
	{
		return datasetName;
	}

	@Override
	public int getNumSmiles()
	{
		return getNumMolecules();
	}

	@Override
	public String getSmiles(int index)
	{
		return getMoleculeSmiles(index);
	}

	@Override
	public String getDatasetBaseName()
	{
		return datasetBaseName;
	}

}
