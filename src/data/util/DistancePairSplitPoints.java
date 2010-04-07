package data.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import util.ArrayUtil;
import util.StringUtil;
import data.DistancePairData;
import data.MoleculeActivityData;

public class DistancePairSplitPoints
{
	HashMap<Integer, Double> splitPoints = new HashMap<Integer, Double>();
	HashMap<Integer, Double> entropy = new HashMap<Integer, Double>();

	DistancePairData distancePairs;
	MoleculeActivityData data;

	public DistancePairSplitPoints(DistancePairData distancePairs, MoleculeActivityData data)
	{
		this.distancePairs = distancePairs;
		this.data = data;
	}

	public double getSplitPoint(int pairIndex)
	{
		if (!splitPoints.containsKey(pairIndex))
			calculateSplitPoint(pairIndex);
		return splitPoints.get(pairIndex);
	}

	public double getEntropy(int pairIndex)
	{
		if (!splitPoints.containsKey(pairIndex))
			calculateSplitPoint(pairIndex);
		return entropy.get(pairIndex);
	}

	public String getDistancePairSplitPointName(int pairIndex)
	{
		return distancePairs.getDistancePairSplitPointName(pairIndex, getSplitPoint(pairIndex));
	}

	public String getVisualizationString(int pairIndex)
	{
		if (!splitPoints.containsKey(pairIndex))
			calculateSplitPoint(pairIndex);

		List<double[]> act = distancePairs.getActivityDistancesForDistancePair(data, pairIndex);

		Arrays.sort(act.get(0));
		Arrays.sort(act.get(1));
		double aa[] = act.get(0);
		double ia[] = act.get(1);

		String aTag = " <font color=\"#CC0000\">";// <i>";
		String aClose = "</font>";// </i>

		String iTag = " <font color=\"#0000FF\">";
		String iClose = "</font>";

		String order = "<html>[ ";

		int aIndex = 0;
		int iIndex = 0;
		boolean splitAdded = false;

		while (aIndex < aa.length || iIndex < ia.length)
		{
			boolean aIsNext = false;

			if (aIndex >= aa.length)
				aIsNext = false;
			else if (iIndex >= ia.length)
				aIsNext = true;
			else
				aIsNext = aa[aIndex] < ia[iIndex];

			double val;
			if (aIsNext)
			{
				val = aa[aIndex];
				aIndex++;
			}
			else
			{
				val = ia[iIndex];
				iIndex++;
			}

			if (!splitAdded && val > getSplitPoint(pairIndex))
			{
				order += "   &#60;&#151;&#62;  ";
				splitAdded = true;
			}

			if (aIsNext)
				order += aTag + StringUtil.formatDouble(val, 1) + aClose;
			else
				order += iTag + StringUtil.formatDouble(val, 1) + iClose;
		}
		order += "  ]</html>";

		return order;

	}

	private void calculateSplitPoint(int pairIndex)
	{
		assert (!splitPoints.containsKey(pairIndex));

		List<double[]> acts = distancePairs.getActivityDistancesForDistancePair(data, pairIndex);
		if (acts == null)
		{
			entropy.put(pairIndex, null);
			splitPoints.put(pairIndex, null);
			return;
		}

		double splitPoint = -1;
		double minEntropy = Double.MAX_VALUE;

		double distances[] = ArrayUtil.concat(acts.get(0), acts.get(1));
		Arrays.sort(distances);

		for (int i = 0; i < distances.length; i++)
		{
			// skip distance if equal to previous
			if (i > 0 && distances[i] == distances[i - 1])
				continue;

			// check distance
			double e = entropyForSplitPoint(distances[i], acts);
			if (splitPoint == -1 || e < minEntropy)
			{
				minEntropy = e;
				splitPoint = distances[i];
			}

			// set prev to previous distance, not equal value;
			double prev = -1;
			int index = i - 1;
			while (index > 0 && prev == -1)
			{
				if (distances[index] != distances[i])
					prev = distances[index];
				index--;
			}
			if (prev != -1)
			{
				// check point between this distance and previous distance
				double intermediate = (prev + distances[i]) / 2.0;
				e = entropyForSplitPoint(intermediate, acts);
				if (e <= minEntropy) // intermediate distances are preferable -> use <=
				{
					minEntropy = e;
					splitPoint = intermediate;
				}
			}
		}

		assert (splitPoint != -1);

		entropy.put(pairIndex, minEntropy);
		splitPoints.put(pairIndex, splitPoint);
	}

	private static double entropyForSplitPoint(double splitPoint, List<double[]> acts)
	{
		int numActivesLEQ = 0;
		int numActivesG = 0;
		for (int i = 0; i < acts.get(0).length; i++)
			if (acts.get(0)[i] <= splitPoint)
				numActivesLEQ++;
			else
				numActivesG++;

		int numInActivesLEQ = 0;
		int numInActivesG = 0;
		for (int i = 0; i < acts.get(1).length; i++)
			if (acts.get(1)[i] <= splitPoint)
				numInActivesLEQ++;
			else
				numInActivesG++;

		double sumLEQ = numActivesLEQ + numInActivesLEQ;
		double sumG = numActivesG + numInActivesG;

		if (sumLEQ == 0)
			return Double.MAX_VALUE;
		if (sumG == 0)
			return Double.MAX_VALUE;
		else
		{

			double eLEQ = entropy(numActivesLEQ, numInActivesLEQ, sumLEQ);
			double eG = entropy(numActivesG, numInActivesG, sumG);
			double res = (sumLEQ * eLEQ + sumG * eG) / (sumLEQ + sumG);

			// Status.INFO.println(StringUtil.formatDouble(splitPoint) + " leq: " + numActivesLEQ + "/" + numInActivesLEQ
			// + " -> " + eLEQ + " g: " + numActivesG + "/" + numInActivesG + " -> " + eG + " --> " + res);

			return res;

		}
	}

	private static double entropy(int numActives, int numInactives, double sum)
	{
		assert (sum != 0 && (sum == numActives + numInactives));
		if (numActives == 0 || numInactives == 0)
			return 0;

		return -((numActives / sum) * logBase2(numActives / sum)) - ((numInactives / sum) * logBase2(numInactives / sum));
	}

	private static double logBase2(double d)
	{
		return Math.log(d) / Math.log(2);
	}

}
