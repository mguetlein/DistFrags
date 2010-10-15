package data;

import java.util.List;

import data.util.SmilesContainer;

public class FragmentDataImpl implements FragmentData, SmilesContainer
{
	String fragmentName;

	List<String> fragments;

	public FragmentDataImpl(String fragmentName, List<String> fragments)
	{
		this.fragmentName = fragmentName;
		this.fragments = fragments;
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
