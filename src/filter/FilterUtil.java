package filter;

import java.util.Collection;

import org.apache.commons.math.stat.inference.ChiSquareTestImpl;

import data.MoleculeActivityData;

public class FilterUtil
{

	public static double getChiSquarePValue(int numActives, int numInactives, int totalNumActives, int totalNumInactives)
	{
		long counts[][] = new long[][] { { totalNumActives, totalNumInactives }, { numActives, numInactives } };

		ChiSquareTestImpl chi = new ChiSquareTestImpl();

		double d = 1;
		try
		{
			d = chi.chiSquareTest(counts);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return d;
	}

	public static double getChiSquarePValue(Collection<Integer> moleculesForFragment, MoleculeActivityData activityData)
	{
		int numActives = 0;
		int numInactives = 0;
		for (Integer mol : moleculesForFragment)
		{
			int act = activityData.getMoleculeActivity(mol);
			if (act == 1)
				numActives++;
			else if (act == 0)
				numInactives++;
		}
		assert (numActives + numInactives == moleculesForFragment.size());

		return getChiSquarePValue(numActives, numInactives, activityData.getNumActives(), activityData.getNumInactives());
	}
}
