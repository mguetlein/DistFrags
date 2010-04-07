package data;

import java.util.List;

public interface FragmentMoleculeData extends FragmentData
{
	public List<Integer> getMoleculesForFragment(int index);

	public List<Integer> getFragmentsForMolecule(int index);

	public String getAdditionalInfo(MoleculeData d, int indent);

	public FragmentMoleculeData getSubset(String fragmentName, List<Integer> fragmentSubset);
}
