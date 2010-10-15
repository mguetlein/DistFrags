package data.factories;

import io.DataFileManager;
import io.DistancePairDataIO;
import io.Status;

import java.io.File;

import data.DistancePairData;
import data.FragmentMoleculeData;
import data.MoleculeActivityData;
import filter.DistancePairFilter;

public class DistancePairFactory
{
	public static DistancePairData mineDistancePairs(MoleculeActivityData data,
			FragmentMoleculeData fragments)
	{
		String fragmentName = DataFileManager.getFragmentNameDistancePair(fragments
				.getFragmentName());

		File f = DataFileManager.getDistancePairFile(data.getDatasetName(), fragmentName);

		DistancePairData res = null;

		if (f.exists())
		{
			res = DistancePairDataIO.readFromDistancePairFile(fragmentName, f, fragments
					.getFragments());
		}
		else
		{
			DistancePairDataIO.mineDistancesExternalOB(f, data, fragments);
			res = DistancePairDataIO.readFromDistancePairFile(fragmentName, f, fragments
					.getFragments());
		}

		Status.INFO.println(Status.INDENT + res);
		Status.INFO.println(res.getAdditionalInfo(data, Status.INDENT.length()));

		return res;
	}

	public static DistancePairData checkDistancePairs(MoleculeActivityData testData,
			FragmentMoleculeData testFragments, DistancePairData trainingDistancePairs)
	{
		File f = DataFileManager.getDistancePairFile(testData.getDatasetName(),
				trainingDistancePairs.getFragmentName());

		DistancePairData res = null;

		if (f.exists())
		{
			res = DistancePairDataIO.readFromDistancePairFile(trainingDistancePairs
					.getFragmentName(), f, testFragments.getFragments());
		}
		else
		{
			DistancePairDataIO.checkDistancesExternalOB(f, testData, testFragments,
					trainingDistancePairs);
			res = DistancePairDataIO.readFromDistancePairFile(trainingDistancePairs
					.getFragmentName(), f, testFragments.getFragments());
		}

		Status.INFO.println(Status.INDENT + res);
		Status.INFO.println(res.getAdditionalInfo(testData, Status.INDENT.length()));

		return res;
	}

	public static DistancePairData applyFilter_minePairs(MoleculeActivityData data,
			FragmentMoleculeData frag, DistancePairFilter filter)
	{
		String distancePairName = DataFileManager.getFragmentNameFiltered(DataFileManager
				.getFragmentNameDistancePair(frag.getFragmentName()), filter);
		File f = DataFileManager.getDistancePairFile(data.getDatasetName(), distancePairName);
		DistancePairData res = null;

		if (f.exists())
			res = DistancePairDataIO.readFromDistancePairFile(distancePairName, f, frag
					.getFragments());
		else
		{
			DistancePairData pairs = DistancePairFactory.mineDistancePairs(data, frag);
			res = filter.apply(distancePairName, pairs, data);
			DistancePairDataIO.writeToDistancePairFile(f, res);
		}
		Status.INFO.println(Status.INDENT + res);
		Status.INFO.println(res.getAdditionalInfo(data, Status.INDENT.length()));

		return res;
	}

	public static DistancePairData applyFilter(DistancePairData pairs, MoleculeActivityData data,
			DistancePairFilter filter)
	{
		String fragmentName = DataFileManager.getFragmentNameFiltered(pairs.getFragmentName(),
				filter);
		File f = DataFileManager.getDistancePairFile(data.getDatasetName(), fragmentName);
		DistancePairData res = null;

		if (f.exists())
			res = DistancePairDataIO
					.readFromDistancePairFile(fragmentName, f, pairs.getFragments());
		else
		{
			res = filter.apply(fragmentName, pairs, data);
			DistancePairDataIO.writeToDistancePairFile(f, res);
		}
		Status.INFO.println(Status.INDENT + res);
		Status.INFO.println(res.getAdditionalInfo(data, Status.INDENT.length()));

		return res;
	}

	// public static DistancePairData applyFStatisticsFilter(DistancePairData pairs, MoleculeActivityData data)
	// {
	// return applyFilter(pairs, data, new FStatisticsFilter(100, 0.1));
	// }
}
