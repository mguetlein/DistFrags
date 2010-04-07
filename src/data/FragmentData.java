package data;

import java.util.List;

import data.util.Molecule;

public interface FragmentData
{
	public String getFragmentName();

	public int getNumFragments();

	public String getFragmentSmiles(int index);

	public List<String> getFragments();

	public Molecule getFragmentMolecule(int index);
}
