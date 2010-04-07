package data;

import java.util.ArrayList;
import java.util.List;

import launch.Settings;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import util.MinMaxAvg;
import data.util.Molecule;
import data.util.MoleculeCache;
import data.util.SmilesContainer;

public class MoleculeDataImpl implements MoleculeData, SmilesContainer
{
	protected List<String> smiles;

	private String datasetName;

	private String datasetBaseName;

	private MoleculeCache molecules;

	public MoleculeDataImpl(String datasetBaseName, String datasetName, List<String> smiles)
	{
		this.datasetBaseName = datasetBaseName;
		this.datasetName = datasetName;
		this.smiles = smiles;

		if (Settings.isModeCDK() || Settings.isModeOpenBabel())
			molecules = new MoleculeCache(this);
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
	public Molecule getMolecule(int index)
	{
		return molecules.getMolecule(index);
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

	@Override
	public MinMaxAvg getMoleculeSizeInfo()
	{
		if (!Settings.isModeCDK() && !Settings.isModeOpenBabel())
			throw new NotImplementedException();

		List<Integer> size = new ArrayList<Integer>();
		for (int j = 0; j < getNumMolecules(); j++)
		{
			Molecule m = getMolecule(j);
			if (m != null)
				size.add(m.getAtomCount());
		}

		return MinMaxAvg.minMaxAvg(size);
	}

}
