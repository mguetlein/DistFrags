package data;

import util.MinMaxAvg;
import data.util.Molecule;

public interface MoleculeData
{
	public String getDatasetBaseName();

	public String getDatasetName();

	public int getNumMolecules();

	public String getMoleculeSmiles(int index);

	public Molecule getMolecule(int index);

	public MinMaxAvg getMoleculeSizeInfo();

}
