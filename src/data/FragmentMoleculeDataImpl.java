package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import util.MinMaxAvg;
import datamining.ResultSet;

public class FragmentMoleculeDataImpl extends FragmentDataImpl implements FragmentMoleculeData
{
	HashMap<Integer, List<Integer>> fragmentsToMolecules;

	HashMap<Integer, List<Integer>> moleculesToFragments;

	public FragmentMoleculeDataImpl(String fragmentName, List<String> fragments,
			HashMap<Integer, List<Integer>> fragmentsToMolecules)
	// ,HashMap<Integer, List<Integer>> moleculesToFragments)
	{
		super(fragmentName, fragments);

		this.fragmentsToMolecules = fragmentsToMolecules;
		// this.moleculesToFragments = moleculesToFragments;
		createMoleculesToFragments();
	}

	private void createMoleculesToFragments()
	{
		moleculesToFragments = new HashMap<Integer, List<Integer>>();

		for (int featureIndex = 0; featureIndex < fragments.size(); featureIndex++)
		{
			List<Integer> indices = fragmentsToMolecules.get(featureIndex);

			if (indices != null)
			{
				for (Integer id : indices)
				{
					List<Integer> l = moleculesToFragments.get(id);
					if (l == null)
					{
						l = new ArrayList<Integer>();
						l.add(featureIndex);
						moleculesToFragments.put(id, l);
					}
					else
						l.add(featureIndex);
				}
			}
		}
	}

	@Override
	public List<Integer> getFragmentsForMolecule(int index)
	{
		return moleculesToFragments.get(index);
	}

	@Override
	public List<Integer> getMoleculesForFragment(int index)
	{
		return fragmentsToMolecules.get(index);
	}

	public String toString()
	{
		return "FragmentMoleculeData '" + fragmentName + "' (#fragments: " + fragments.size()
				+ ", #molecules-with-fragments: " + moleculesToFragments.keySet().size() + ")";
	}

	@Override
	public String getAdditionalInfo(MoleculeData d, int indent)
	{
		ResultSet set = new ResultSet();

		MinMaxAvg moleculesPerFragment = MinMaxAvg.listIteratorMinMaxAvg(fragmentsToMolecules.values(), fragments.size()
				- fragmentsToMolecules.size());
		moleculesPerFragment.addToResult(set, "mol-per-fraq");

		MinMaxAvg fragmentPerMolecule = MinMaxAvg.listIteratorMinMaxAvg(moleculesToFragments.values(), d.getNumMolecules()
				- moleculesToFragments.size());
		fragmentPerMolecule.addToResult(set, "fraq-per-mol");

		return set.toNiceString(indent, false);
	}

	@Override
	public FragmentMoleculeData getSubset(String fragmentName, List<Integer> fragmentSubset)
	{
		List<String> frags = new ArrayList<String>();
		HashMap<Integer, List<Integer>> fragsToMols = new HashMap<Integer, List<Integer>>();

		int count = 0;
		for (Integer index : fragmentSubset)
		{
			frags.add(fragments.get(index));
			fragsToMols.put(count, fragmentsToMolecules.get(index));

			count++;
		}

		return new FragmentMoleculeDataImpl(fragmentName, frags, fragsToMols);
	}
}
