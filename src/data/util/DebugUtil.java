package data.util;

import io.Status;

import java.util.Set;

import data.DistancePairData;
import data.MoleculeData;

public class DebugUtil
{
	public static void printMoleculesWithDistPairs(MoleculeData d, DistancePairData p)
	{
		Status.INFO.println("\nDEBUG: distance pairs for " + d.getDatasetName() + "\n");

		for (int i = 0; i < d.getNumMolecules(); i++)
		{
			Status.INFO.println(i + " " + d.getMoleculeSmiles(i));

			if (p.getNumDistancePairsForMolecule(i) > 0)
			{
				for (int j = 0; j < p.getNumDistancePairs(); j++)
				{
					Set<Integer> mols = p.getMoleculesForDistancePair(j);
					if (mols.contains(i))
					{
						Status.INFO.printf(Status.TAB + "%4d %25s   %.2f\n", j, p.getDistancePairName(j), p
								.getDistances(j, i));
					}
				}
			}
		}

		Status.INFO.println();
	}
}
