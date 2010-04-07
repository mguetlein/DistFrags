package filter;

import data.FragmentMoleculeData;
import data.MoleculeActivityData;

public interface FragmentFilter extends Filter
{
	public FragmentMoleculeData apply(String fragmentName, FragmentMoleculeData f, MoleculeActivityData d);
}
