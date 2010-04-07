package data.mining;

import gui.ProgressDialog;
import io.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;

import launch.Settings;
import data.FragmentData;
import data.FragmentMoleculeData;
import data.FragmentMoleculeDataImpl;
import data.MoleculeData;
import data.util.Molecule;

public class FragmentChecker
{

	public static FragmentMoleculeData checkFragments(String fragmentName, MoleculeData molecules, FragmentData fragments)
	{
		if (!Settings.isModeOpenBabel() && !Settings.isModeCDK())
			throw new IllegalStateException();

		Status.INFO.println("Checking fragments\n - dataset: '" + molecules + "'\n - fragments: '" + fragments + "' ... ");

		HashMap<Integer, List<Integer>> fragmentsToMolecules = new HashMap<Integer, List<Integer>>();
		// HashMap<Integer, List<Integer>> moleculesToFragments = new HashMap<Integer, List<Integer>>();

		int max = molecules.getNumMolecules() * fragments.getNumFragments();
		int count = 0;

		ProgressDialog progress = ProgressDialog.showProgress(Settings.SHOW_PROGRESS_DIALOGS ? (JFrame) null : Status.INFO,
				"Checking feautures - " + fragmentName, Status.INDENT + "> ", max);

		for (int i = 0; i < molecules.getNumMolecules(); i++)
		{
			Molecule mol = molecules.getMolecule(i);
			if (mol == null)
				continue;

			for (int j = 0; j < fragments.getNumFragments(); j++)
			{
				boolean match;
				if (Settings.isModeOpenBabel())
					match = mol.isOBSubgraph(fragments.getFragmentSmiles(j));
				else
				{
					Molecule frag = fragments.getFragmentMolecule(j);
					if (frag == null)
						match = false;
					else
						match = mol.isCDKSubgraph(frag);
				}

				if (match)
				{
					if (fragmentsToMolecules.containsKey(j))
						fragmentsToMolecules.get(j).add(i);
					else
					{
						List<Integer> mols = new ArrayList<Integer>();
						mols.add(i);
						fragmentsToMolecules.put(j, mols);
					}
				}

				count++;
				if (count % 200 == 0)
					progress.update(count);
			}
		}

		progress.close(count);
		Status.INFO.println(" - done");

		return new FragmentMoleculeDataImpl(fragmentName, fragments.getFragments(), fragmentsToMolecules);// ,
		// moleculesToFragments);
	}

}
