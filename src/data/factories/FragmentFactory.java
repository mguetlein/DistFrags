package data.factories;

import io.DataFileManager;
import io.FragmentIO;
import io.Status;

import java.io.File;

import data.FragmentData;
import data.FragmentMoleculeData;
import data.MoleculeActivityData;
import data.MoleculeData;
import filter.ChiSquareFragmentFilter;
import filter.FragmentFilter;

public class FragmentFactory
{

	public static FragmentMoleculeData mineFragments(String fragmentType,
			MoleculeActivityData molecules)
	{
		File f = DataFileManager.getLinfragFile(molecules.getDatasetName(), fragmentType);

		if (!f.exists())
		{
			if (fragmentType.equals(DataFileManager.FRAGMENT_TYPE_LINFRAG))
				FragmentIO.createLinfragFile(f, molecules);
			// FragmentIO.createLinfragFileUsingFminer(f, molecules);

			else if (fragmentType.equals(DataFileManager.FRAGMENT_TYPE_FMINER))
				FragmentIO.createFMinerFile(f, molecules);
			else
				throw new IllegalArgumentException("unkown fragment type: " + fragmentType);

			if (!f.exists())
				throw new IllegalStateException("fragment file not found: " + f);
		}

		FragmentMoleculeData res = FragmentIO.readFragments(fragmentType, f, molecules);

		Status.INFO.println(Status.INDENT + res.toString());
		Status.INFO.println(res.getAdditionalInfo(molecules, Status.INDENT.length()));
		return res;
	}

	public static FragmentMoleculeData checkFragments(MoleculeData molecules, FragmentData fragments)
	{
		File f = DataFileManager.getLinfragFile(molecules.getDatasetName(), fragments
				.getFragmentName());

		FragmentMoleculeData res;
		if (f.exists())
		{
			res = FragmentIO.readFragments(fragments.getFragmentName(), f, molecules);
		}
		else
		{
			FragmentIO.checkFragmentsExternOB(f, molecules, fragments);
			res = FragmentIO.readFragments(fragments.getFragmentName(), f, molecules);
		}

		Status.INFO.println(Status.INDENT + res.toString());
		Status.INFO.println(res.getAdditionalInfo(molecules, Status.INDENT.length()));
		return res;
	}

	public static FragmentMoleculeData applyFilter(FragmentMoleculeData fragments,
			MoleculeActivityData data, FragmentFilter filter)
	{
		String fragmentName = DataFileManager.getFragmentNameFiltered(fragments.getFragmentName(),
				filter);

		File f = DataFileManager.getLinfragFile(data.getDatasetName(), fragmentName);

		FragmentMoleculeData res = null;

		if (f.exists())
		{
			res = FragmentIO.readFragments(fragmentName, f, data);
		}
		else
		{
			res = filter.apply(fragmentName, fragments, data);
			FragmentIO.writeFragmentFile(f, res);
		}

		Status.INFO.println(Status.INDENT + res);
		Status.INFO.println(res.getAdditionalInfo(data, Status.INDENT.length()));

		return res;
	}

	public static FragmentMoleculeData applyChiSquareFilter(FragmentMoleculeData fragments,
			MoleculeActivityData data)
	{
		return applyFilter(fragments, data, new ChiSquareFragmentFilter(100, 0.05));
	}
}
