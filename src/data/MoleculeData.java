package data;


public interface MoleculeData
{
	public String getDatasetBaseName();

	public String getDatasetName();

	public int getNumMolecules();

	public String getMoleculeSmiles(int index);

}
