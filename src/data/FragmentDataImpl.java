package data;

import java.util.List;

import launch.Settings;
import data.util.Molecule;
import data.util.MoleculeCache;
import data.util.SmilesContainer;

public class FragmentDataImpl implements FragmentData, SmilesContainer
{
	String fragmentName;

	List<String> fragments;

	MoleculeCache molecules;

	public FragmentDataImpl(String fragmentName, List<String> fragments)
	{
		this.fragmentName = fragmentName;
		this.fragments = fragments;

		if (Settings.isModeCDK() || Settings.isModeOpenBabel())
			molecules = new MoleculeCache(this);
	}

	@Override
	public String getFragmentSmiles(int index)
	{
		return fragments.get(index);
	}

	@Override
	public int getNumFragments()
	{
		return fragments.size();
	}

	public String toString()
	{
		return "FragmentData '" + fragmentName + "' (#fragments: " + fragments.size() + ")";
	}

	@Override
	public List<String> getFragments()
	{
		return fragments;
	}

	@Override
	public Molecule getFragmentMolecule(int index)
	{
		return molecules.getMolecule(index);
	}

	@Override
	public String getFragmentName()
	{
		return fragmentName;
	}

	@Override
	public int getNumSmiles()
	{
		return getNumFragments();
	}

	@Override
	public String getSmiles(int index)
	{
		return getFragmentSmiles(index);
	}

}
