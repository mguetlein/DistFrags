package data.mining;

import java.util.HashMap;

import org.openbabel.OBAtom;
import org.openbabel.OBMol;
import org.openbabel.OBMolAtomBFSIter;
import org.openbabel.SWIGTYPE_p_std__vectorT_OpenBabel__OBAtom_p_t__iterator;

public class DistanceHashMap implements DistanceMap
{
	HashMap<Long, HashMap<Long, Integer>> map;

	public DistanceHashMap(OBMol mol)
	{
		map = new HashMap<Long, HashMap<Long, Integer>>();

		SWIGTYPE_p_std__vectorT_OpenBabel__OBAtom_p_t__iterator atomIterator = mol.BeginAtoms();
		OBAtom atom = mol.BeginAtom(atomIterator);
		do
		{
			OBMolAtomBFSIter bfsIterator = new OBMolAtomBFSIter(mol, (int) atom.GetIdx());
			for (OBAtom atom2 : bfsIterator)
			{
				HashMap<Long, Integer> map2 = map.get(atom.GetIdx());
				if (map2 == null)
				{
					map2 = new HashMap<Long, Integer>();
					map.put(atom.GetIdx(), map2);
				}
				map2.put(atom2.GetIdx(), atom2.GetCurrentDepth());
			}
		}
		while ((atom = mol.NextAtom(atomIterator)) != null);
	}

	@Override
	public int getDistance(long idx1, long idx2)
	{
		HashMap<Long, Integer> m = map.get(idx1);
		if (m == null)
			return -1;
		Integer d = m.get(idx2);
		if (d == null)
			return -1;
		else
			return d;
	}

	@Override
	public String toString()
	{
		String s = "distance map:\n";
		for (Long atom1Idx : map.keySet())
		{
			s += atom1Idx + " -> [";
			HashMap<Long, Integer> m = map.get(atom1Idx);
			for (Long atom2Idx : m.keySet())
			{
				s += "(" + atom2Idx + "," + m.get(atom2Idx) + ") ";
			}
			s += "]\n";
		}
		return s;
	}

}
