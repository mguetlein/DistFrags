package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import launch.DatasetSizeSettings;
import launch.Settings;
import data.FragmentData;
import data.FragmentMoleculeData;
import data.FragmentMoleculeDataImpl;
import data.MoleculeActivityData;
import data.MoleculeData;

public class FragmentIO
{
	public static void checkFragmentsExternOB(File testFragmentFile, MoleculeData data, FragmentData fragments)
	{
		if (!Settings.isModeExternOpenBabel())
			throw new IllegalStateException();

		DataFileManager.createParentFolders(testFragmentFile);
		Status.INFO.println(Status.INDENT + "Checking fragments via external OBDistance tool: '"
				+ testFragmentFile.getName() + "(.tmp)'");

		File tempSmilesFile = MoleculeDataIO.createTmpSmilesFile(data);
		File tempFragmentFile = createTmpFragmentFile(data.getDatasetName(), fragments, false);

		String command = Settings.USER_HOME + "/software/OBDistance/OBDistance -s " + tempSmilesFile.getAbsolutePath()
				+ " -f " + tempFragmentFile + " -p -a";

		ExternalTool.run("OBDistance", testFragmentFile, null, command);

		tempSmilesFile.delete();
		tempFragmentFile.delete();
		// Status.INFO.println("done");
	}

	public static void createFMinerFile(File fragmentFile, MoleculeActivityData d)
	{
		DataFileManager.createParentFolders(fragmentFile);
		Status.INFO.println(Status.INDENT + "Mining fragments via external fminer tool: '" + fragmentFile.getName()
				+ "(.tmp)'");

		File tempSmilesFile = MoleculeDataIO.createTmpSmilesFile(d);
		File tempClassFile = MoleculeDataIO.createTmpClassFile(d);

		DatasetSizeSettings.setCurrentDatasetSize(d.getDatasetBaseName());
		File dir = new File(Settings.USER_HOME + "/software/fminer/");
		String command = dir.getAbsolutePath() + "/fminer -f " + DatasetSizeSettings.MIN_FREQUENCY + " -n "
				+ tempSmilesFile.getAbsolutePath() + " " + tempClassFile.getAbsolutePath();
		String[] env = new String[] { "FMINER_SMARTS=1", "FMINER_LAZAR=1" };

		ExternalTool.run("fminer", fragmentFile, null, command, env, dir);

		tempSmilesFile.delete();
		tempClassFile.delete();
		// Status.INFO.println("done");
	}

	public static void createLinfragFileUsingFminer(File fragmentFile, MoleculeActivityData d)
	{
		DataFileManager.createParentFolders(fragmentFile);
		Status.INFO.println(Status.INDENT + "Mining linear fragments via external fminer tool: '" + fragmentFile.getName()
				+ "(.tmp)'");

		File tempSmilesFile = MoleculeDataIO.createTmpSmilesFile(d);
		File tempClassFile = MoleculeDataIO.createTmpClassFile(d);

		DatasetSizeSettings.setCurrentDatasetSize(d.getDatasetBaseName());
		File dir = new File(Settings.USER_HOME + "/software/fminer/");
		String command = dir.getAbsolutePath() + "/fminer -f " + DatasetSizeSettings.MIN_FREQUENCY
				+ " -l 1 -n -d -b -p 0 -u " + tempSmilesFile.getAbsolutePath() + " " + tempClassFile.getAbsolutePath();
		String[] env = new String[] { "FMINER_SMARTS=1", "FMINER_LAZAR=1" };

		ExternalTool.run("fminer", fragmentFile, null, command, env, dir);

		tempSmilesFile.delete();
		tempClassFile.delete();
		// Status.INFO.println("done");
	}

	public static void createLinfragFile(File fragmentFile, MoleculeData d)
	{
		DataFileManager.createParentFolders(fragmentFile);
		Status.INFO.println(Status.INDENT + "Mining fragments via external linfrag tool: '" + fragmentFile.getName()
				+ "(.tmp)'");

		File tempSmilesFile = MoleculeDataIO.createTmpSmilesFile(d);

		DatasetSizeSettings.setCurrentDatasetSize(d.getDatasetBaseName());
		String command = Settings.USER_HOME + "/software/linfrag/linfrag -f " + DatasetSizeSettings.MIN_FREQUENCY + " -s "
				+ tempSmilesFile.getAbsolutePath() + " -a " + Settings.USER_HOME + "/software/linfrag/elements.txt";

		ExternalTool.run("linfrag", fragmentFile, ".*LEVEL.*", command);

		tempSmilesFile.delete();
		// Status.INFO.println("done");
	}

	public static File createTmpFragmentFile(String datasetName, FragmentData fragments, boolean printMoleculeOcurrences)
	{
		FragmentMoleculeData fragmentMoleculeData = null;
		if (printMoleculeOcurrences)
			fragmentMoleculeData = (FragmentMoleculeData) fragments;

		try
		{
			File tempFragFile = File.createTempFile(datasetName + "." + fragments.getFragmentName(), ".frag");
			Status.INFO.println(Status.INDENT + "Writing tmp fragment file: '" + tempFragFile.getName() + "'");
			tempFragFile.deleteOnExit();

			PrintStream out = new PrintStream(tempFragFile);

			for (int i = 0; i < fragments.getNumFragments(); i++)
			{
				StringBuffer s = new StringBuffer(fragments.getFragmentSmiles(i));
				if (printMoleculeOcurrences)
				{
					s.append("\t[ ");
					List<Integer> mols = fragmentMoleculeData.getMoleculesForFragment(i);
					if (mols != null)
						for (Integer mol : mols)
							s.append(mol + " ");
					s.append("]");
				}
				else
					s.append("\t#");
				out.println(s);
			}
			out.close();

			// Status.INFO.println("done");
			return tempFragFile;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static void writeFragmentFile(File file, FragmentMoleculeData fragments)
	{
		DataFileManager.createParentFolders(file);
		File tmp = new File(file + ".tmp");

		Status.INFO.println(Status.INDENT + "Writing fragment file '" + file.getName() + "(.tmp)'");

		try
		{
			PrintStream out = new PrintStream(tmp);

			for (int i = 0; i < fragments.getNumFragments(); i++)
			{
				out.print(fragments.getFragmentSmiles(i) + "\t[ ");
				List<Integer> mols = fragments.getMoleculesForFragment(i);
				if (mols != null)
					for (Integer mol : mols)
						out.print(mol + " ");
				out.println("]");
			}

			out.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		boolean res = tmp.renameTo(file);
		res |= tmp.delete();

		if (!res)
			throw new Error("renaming or delete file error");

		// Status.INFO.println("done");
	}

	public static FragmentMoleculeData readFragments(String fragmentName, File linfragFile, MoleculeData molecules)
	{
		List<String> fragments = new ArrayList<String>();
		HashMap<Integer, List<Integer>> fragmentsToMolecules = new HashMap<Integer, List<Integer>>();

		Status.INFO.println(Status.INDENT + "Reading fragments: '" + linfragFile.getName() + "'");

		// int infrequent = 0;

		try
		{
			BufferedReader r = new BufferedReader(new FileReader(linfragFile));
			String s;

			// ProgressDialog pro = ProgressDialog.showProgress(Status.INFO, "read", 345166);

			while ((s = r.readLine()) != null)
			{
				String fragment = null;
				String occurences = null;

				Pattern p = Pattern.compile("^(.*)\\[");
				Matcher m = p.matcher(s);
				if (m.find())
				{
					fragment = m.group(1).trim();
					// System.out.print(fragment + " ");
				}

				p = Pattern.compile("\\s\\[(.*)\\]$");
				m = p.matcher(s);
				if (m.find())
				{
					occurences = m.group(1);
					// System.out.println(occurences);
				}

				if (fragment == null || occurences == null)
					throw new IllegalStateException("wrong smiles file format, " + linfragFile + ": '" + s + "', fragment:"
							+ fragment + ", occurences:" + occurences);

				if (fragments.contains(fragment))
					Status.WARN.println("fragment occures twice " + fragment);

				// fragment = convert(fragment);

				List<Integer> moleculesIndices = new ArrayList<Integer>();
				StringTokenizer tok = new StringTokenizer(occurences);

				while (tok.hasMoreTokens())
					moleculesIndices.add(Integer.parseInt(tok.nextToken()));

				// if (moleculesIndices.size() >= Settings.MIN_FREQUENCY)
				// {
				fragments.add(fragment);

				// if (moleculesIndices.size() == 0)
				// throw new IllegalStateException("no occurences, " + linfragFile + ": '" + s + "', fragment:" + fragment
				// + ", occurences:" + occurences);

				fragmentsToMolecules.put(fragments.size() - 1, moleculesIndices);
				// }
				// else
				// infrequent++;

				// if (res.fragments.size() >= 5000)
				// break;

				// if (fragments.size() % 100 == 0)
				// pro.update(fragments.size());
			}

			r.close();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// for (int featureIndex = 0; featureIndex < res.fragments.size(); featureIndex++)
		// {
		// List<Integer> indices = res.fragmentsToMolecules.get(featureIndex);
		//
		// for (Integer id : indices)
		// {
		// List<Integer> fragments = res.moleculesToFragments.get(id);
		// if (fragments == null)
		// {
		// fragments = new ArrayList<Integer>();
		// fragments.add(featureIndex);
		// res.moleculesToFragments.put(id, fragments);
		// }
		// else
		// fragments.add(featureIndex);
		// }
		// }

		// for (Integer id : moleculeIds)
		// {
		// System.out.print(id + ": [ ");
		//
		// Vector<Integer> occ = moleculesToFeatures.get(id);
		// for (Integer i : occ)
		// System.out.print(i + " ");
		// System.out.println("]");
		// }

		// Status.INFO.println("done");// (skipped " + infrequent + " infrequent fragments)");

		return new FragmentMoleculeDataImpl(fragmentName, fragments, fragmentsToMolecules);
	}

}
