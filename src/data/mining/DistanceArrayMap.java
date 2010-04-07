package data.mining;

import java.util.Arrays;

import org.openbabel.OBAtom;
import org.openbabel.OBConversion;
import org.openbabel.OBMol;
import org.openbabel.OBMolAtomBFSIter;
import org.openbabel.vectorMol;

public class DistanceArrayMap implements DistanceMap
{
	int map[][];

	public DistanceArrayMap(OBMol mol)
	{
		// StopWatchUtil.start("ob-distance-map");

		map = new int[(int) mol.NumAtoms() + 1][(int) mol.NumAtoms() + 1];
		for (int i = 0; i < map.length; i++)
			Arrays.fill(map[i], -1);

		vectorMol separatedMolecules = mol.Separate();

		int molIndexOffset = 0;

		for (int i = 0; i < separatedMolecules.size(); i++)
		{
			OBMol m = separatedMolecules.get(i);

			// System.out.println(i + " m " + m.GetFormula());

			for (int sepIndex = 1; sepIndex < m.NumAtoms() + 1; sepIndex++)
			{
				OBAtom atom = m.GetAtom(sepIndex);
				OBMolAtomBFSIter bfsIterator = new OBMolAtomBFSIter(m, (int) atom.GetIdx());

				for (OBAtom atom2 : bfsIterator)
				{
					if (atom.GetIdx() == atom2.GetIdx())
						continue;

					map[(int) (atom.GetIdx() + molIndexOffset)][(int) (atom2.GetIdx() + molIndexOffset)] = atom2
							.GetCurrentDepth() - 1;
				}
			}

			molIndexOffset += m.NumAtoms();
		}
		// if (true == true)
		// return;

		// SWIGTYPE_p_std__vectorT_OpenBabel__OBAtom_p_t__iterator atomIterator = mol.BeginAtoms();
		// OBAtom atom = mol.BeginAtom(atomIterator);
		// do
		// {
		// OBMolAtomBFSIter bfsIterator = new OBMolAtomBFSIter(mol, (int) atom.GetIdx());
		//
		// for (OBAtom atom2 : bfsIterator)
		// {
		// if (atom.GetIdx() == atom2.GetIdx())
		// continue;
		//
		// // int currentDepth = atom2.GetCurrentDepth();
		//
		// // if (atom2.GetCurrentDepth() == 1 && m.GetBond((int) atom.GetIdx(), (int) atom2.GetIdx()) == null)
		// // continue;
		// // throw new IllegalStateException("no bond " + atom.GetType() + " " + atom2.GetType());
		//
		// map[(int) atom.GetIdx()][(int) atom2.GetIdx()] = atom2.GetCurrentDepth() - 1;
		// }
		// }
		// while ((atom = mol.NextAtom(atomIterator)) != null);
		// StopWatchUtil.stop("ob-distance-map");
	}

	@Override
	public int getDistance(long idx1, long idx2)
	{
		return map[(int) idx1][(int) idx2];
	}

	@Override
	public String toString()
	{
		return toString(null);
	}

	public String toString(OBMol mol)
	{
		String s = "distance map:\n       ";

		for (int j = 1; j < map[0].length; j++)
		{
			String a = "";
			if (mol != null)
				a = "(" + mol.GetAtom(j).GetType() + ")";

			s += j + a + " ";
		}
		s += "\n";

		for (int i = 1; i < map.length; i++)
		{
			String a = "";
			if (mol != null)
				a = "(" + mol.GetAtom(i).GetType() + ")";

			s += i + a + " -> [";

			for (int j = 1; j < map[0].length; j++)
			{
				s += map[i][j] + " ";
			}
			s += "]\n";
		}

		return s;
	}

	public static void main(String args[])
	{
		String s = "N.c1:c:c:c:c:c1.OC";
		// String s = "c1:c:c:c:c:c1";
		// String s = "CCC";

		System.loadLibrary("openbabel");

		OBConversion conv = new OBConversion();
		conv.SetInFormat("smiles");

		OBMol mol = new OBMol();
		conv.ReadString(mol, s);

		DistanceArrayMap distanceMap = new DistanceArrayMap(mol);
		System.out.println(distanceMap.toString(mol));
	}

}
