package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import data.MoleculeActivityData;
import data.MoleculeActivityDataImpl;
import data.MoleculeData;
import data.MoleculeDataImpl;

public class MoleculeDataIO
{
	public static File createTmpClassFile(MoleculeActivityData d)
	{
		try
		{
			File tempClassFile = File.createTempFile(d.getDatasetName(), ".class");
			Status.INFO.println(Status.INDENT + "Writing tmp class file file: '" + tempClassFile.getName() + "'");

			tempClassFile.deleteOnExit();
			PrintStream tmp = new PrintStream(tempClassFile);
			for (int i = 0; i < d.getNumMolecules(); i++)
				tmp.println((i + 1) + "\t" + d.getDatasetName() + "\t" + d.getMoleculeActivity(i));
			tmp.close();

			// Status.INFO.println("done");

			return tempClassFile;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static File createTmpSmilesFile(MoleculeData d)
	{
		try
		{
			File tempSmilesFile = File.createTempFile(d.getDatasetName(), ".smi");
			Status.INFO.println(Status.INDENT + "Writing tmp smiles file: '" + tempSmilesFile.getName() + "'");

			tempSmilesFile.deleteOnExit();
			PrintStream tmp = new PrintStream(tempSmilesFile);
			for (int i = 0; i < d.getNumMolecules(); i++)
			{
				String smiles = d.getMoleculeSmiles(i);
				if (smiles.length() == 0)
					smiles = "#";
				tmp.println((i + 1) + "\t" + smiles);
			}
			tmp.close();

			// Status.INFO.println("done");

			return tempSmilesFile;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static MoleculeData readFromSmilesFile(String datasetName, String smilesFile)
	{
		SmilesFileContent file = readSmilesFile(smilesFile);
		return new MoleculeDataImpl(datasetName, datasetName, file.smiles);
	}

	public static MoleculeActivityData readFromSmilesAndClassFile(String datasetName, String smilesFile, String classFile)
	{
		SmilesFileContent file = readSmilesFile(smilesFile);
		HashMap<Integer, Integer> idToActivity = readClassFile(classFile);

		List<Integer> activities = new ArrayList<Integer>();
		for (Integer ids : file.ids)
			activities.add(idToActivity.get(ids));

		return new MoleculeActivityDataImpl(datasetName, datasetName, datasetName, file.smiles, activities);
	}

	private static class SmilesFileContent
	{
		List<String> smiles = new ArrayList<String>();
		List<Integer> ids = new ArrayList<Integer>();
		HashMap<Integer, String> idToSmiles = new HashMap<Integer, String>();
	}

	private static SmilesFileContent readSmilesFile(String smilesFile)
	{
		try
		{
			SmilesFileContent res = new SmilesFileContent();

			File file = new File(smilesFile);

			Status.INFO.println(Status.INDENT + "Reading smiles: '" + file.getName() + "'");

			BufferedReader r = new BufferedReader(new FileReader(file));
			String s;

			while ((s = r.readLine()) != null)
			{
				int id = -1;
				String smile = null;

				Pattern p = Pattern.compile("^([0-9]*)\\s");
				Matcher m = p.matcher(s);
				if (m.find())
				{
					id = Integer.parseInt(m.group(1));
					// System.out.print(id + " ");
				}

				p = Pattern.compile("\\s(.*)$");
				m = p.matcher(s);
				if (m.find())
				{
					smile = m.group(1);
					// System.out.println(smile);
				}

				if (id == -1 || smile == null)
					throw new IllegalStateException("wrong smiles file format '" + smilesFile + "': '" + s + "', id: '" + id
							+ "', smiles: '" + smile + "'");

				if (smile.length() == 0)
					Status.WARN.println("empty smiles " + smile);

				if (res.ids.contains(id))
					throw new IllegalStateException("double id");
				res.ids.add(id);

				if (res.smiles.contains(smile))
					Status.WARN.println("duplicate smiles " + smile);
				res.smiles.add(smile);

				res.idToSmiles.put(id, smile);
			}

			// Status.INFO.println("done");

			r.close();

			return res;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private static HashMap<Integer, Integer> readClassFile(String classFile)
	{
		try
		{
			File file = new File(classFile);

			Status.INFO.println(Status.INDENT + "Reading class file: '" + file.getName() + "'");

			HashMap<Integer, Integer> idToActivity = new HashMap<Integer, Integer>();

			BufferedReader r = new BufferedReader(new FileReader(file));
			String s;

			while ((s = r.readLine()) != null)
			{
				// StringTokenizer tok = new StringTokenizer(s);

				int id = -1;
				String endpoint = null;
				Integer classValue = null;

				Pattern p = Pattern.compile("^([0-9]*)\\s");
				Matcher m = p.matcher(s);
				if (m.find())
				{
					id = Integer.parseInt(m.group(1));
					// System.out.print(id + " ");
				}

				p = Pattern.compile("\\s(\\\"(.*)\\\")\\s");
				m = p.matcher(s);
				if (m.find())
				{
					endpoint = m.group(1);
					// System.out.print(endpoint + " ");
				}
				else
				{
					p = Pattern.compile("\\t(.*)\\t");
					m = p.matcher(s);
					if (m.find())
					{
						endpoint = m.group(1);
						// System.out.println(endpoint + " ");
					}
				}

				p = Pattern.compile("\\s([0-1])$");
				m = p.matcher(s);
				if (m.find())
				{
					classValue = new Integer(m.group(1));
				}

				if (id == -1 || endpoint == null || classValue == null)
					throw new IllegalStateException("wrong class file format, " + classFile + ": '" + s + "', id:" + id
							+ ", endpoint:" + endpoint + ", act:" + classValue);

				if (idToActivity.containsKey(id))
					throw new IllegalStateException("double activity for id: " + id);

				// if (!endpoints.contains(endpoint))
				// endpoints.add(endpoint);

				idToActivity.put(id, classValue);
			}

			// Status.INFO.println("done");

			r.close();

			return idToActivity;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

}
