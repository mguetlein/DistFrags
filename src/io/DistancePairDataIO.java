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
import data.DistancePairData;
import data.DistancePairDataImpl;
import data.FragmentMoleculeData;
import data.MoleculeActivityData;

public class DistancePairDataIO
{
	public static final File OB_DISTANCE_DIR = new File(Settings.USER_HOME
			+ "/workspace/OBDistance/Release");

	public static void mineDistancesExternalOB(File f, MoleculeActivityData data,
			FragmentMoleculeData fragments)
	{
		DataFileManager.createParentFolders(f);
		Status.INFO.println(Status.INDENT + "Mining distances via external OBDistance tool: '"
				+ f.getName() + "(.tmp)'");

		File tempSmilesFile = MoleculeDataIO.createTmpSmilesFile(data);
		File tempClassFile = MoleculeDataIO.createTmpClassFile(data);
		File tempFragmentFile = FragmentIO.createTmpFragmentFile(data.getDatasetName(), fragments,
				true);

		DatasetSizeSettings.setCurrentDatasetSize(data.getDatasetBaseName());
		String command = OB_DISTANCE_DIR + "/OBDistance -s " + tempSmilesFile.getAbsolutePath()
				+ " -f " + tempFragmentFile.getAbsolutePath() + " -c "
				+ tempClassFile.getAbsolutePath() + " -z -o -d -a -m "
				+ DatasetSizeSettings.MIN_FREQUENCY + " -i "
				+ DatasetSizeSettings.MIN_FREQUENCY_PER_CLASS;

		ExternalTool.run("OBDistance", f, null, command);

		tempSmilesFile.delete();
		tempClassFile.delete();
		tempFragmentFile.delete();
		// Status.INFO.println("done");
	}

	public static void checkDistancesExternalOB(File f, MoleculeActivityData testData,
			FragmentMoleculeData testFragments, DistancePairData trainingDists)
	{
		DataFileManager.createParentFolders(f);
		Status.INFO.println(Status.INDENT + "Checking distances via external OBDistance tool: '"
				+ f.getName() + "(.tmp)'");

		File tempSmilesFile = MoleculeDataIO.createTmpSmilesFile(testData);
		File tempClassFile = MoleculeDataIO.createTmpClassFile(testData);
		File tempFragmentFile = FragmentIO.createTmpFragmentFile(testData.getDatasetName(),
				testFragments, true);
		File tempDistFile = DistancePairDataIO.createTmpDistancePairFileWithoutDistances(testData
				.getDatasetName(), trainingDists);

		String command = OB_DISTANCE_DIR + "/OBDistance -z -o -s "
				+ tempSmilesFile.getAbsolutePath() + " -f " + tempFragmentFile.getAbsolutePath()
				+ " -c " + tempClassFile.getAbsolutePath() + " -t "
				+ tempDistFile.getAbsolutePath() + " -x -a";

		ExternalTool.run("OBDistance", f, null, command);

		tempSmilesFile.delete();
		tempClassFile.delete();
		tempFragmentFile.delete();
		tempDistFile.delete();
		// Status.INFO.println("done");
	}

	public static DistancePairData readFromDistancePairFile(String distancePairName, File f,
			List<String> fragments)
	{
		Status.INFO.println(Status.INDENT + "Read distance pairs from file '" + f.getName() + "'");

		List<int[]> distancePairs = new ArrayList<int[]>();
		HashMap<Integer, HashMap<Integer, List<Double>>> distancePairToMoleculeDistances = new HashMap<Integer, HashMap<Integer, List<Double>>>();

		try
		{
			BufferedReader r = new BufferedReader(new FileReader(f));
			String s;

			while ((s = r.readLine()) != null)
			{
				int[] pair = null;
				HashMap<Integer, List<Double>> distances = null;

				Pattern p = Pattern.compile("^\\[(.*)\\]");
				Matcher m = p.matcher(s);
				if (m.find())
				{
					pair = new int[2];
					String ps = m.group(1).trim();

					int index = ps.indexOf(';');
					assert (index != -1);

					pair[0] = Integer.parseInt(ps.substring(0, index));
					pair[1] = Integer.parseInt(ps.substring(index + 1));

					// assert (pair[0] != pair[1]);
				}

				p = Pattern.compile("\\{(.*)\\}$");
				m = p.matcher(s);
				if (m.find())
				{
					distances = new HashMap<Integer, List<Double>>();
					String ds = m.group(1).trim();

					StringTokenizer tok = new StringTokenizer(ds, ",");
					while (tok.hasMoreElements())
					{
						String token = tok.nextToken();
						assert (token.startsWith("(") && token.endsWith(")"));
						token = token.substring(1, token.length() - 1);

						int index = token.indexOf(';');
						assert (index != -1);

						int mol = Integer.parseInt(token.substring(0, index));
						List<Double> dist = new ArrayList<Double>();

						StringTokenizer tok2 = new StringTokenizer(token.substring(index + 1), "#");
						while (tok2.hasMoreTokens())
							dist.add(Double.parseDouble(tok2.nextToken()));

						distances.put(mol, dist);
					}
				}

				assert (pair != null);

				distancePairs.add(pair);
				distancePairToMoleculeDistances.put(distancePairs.size() - 1, distances);
				// else
				// Status.WARN.println("skipping zero distance pair");
			}
			r.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		DistancePairDataImpl res = new DistancePairDataImpl(distancePairName, fragments,
				distancePairs, distancePairToMoleculeDistances);

		// Status.INFO.println("done");

		return res;
	}

	public static File createTmpDistancePairFileWithoutDistances(String datasetName,
			DistancePairData d)
	{
		try
		{
			File tempDistanceFile = File.createTempFile(datasetName + "." + d.getFragmentName(),
					".class");
			Status.INFO.println(Status.INDENT + "Writing tmp class file file: '"
					+ tempDistanceFile.getName() + "'");
			tempDistanceFile.deleteOnExit();

			PrintStream tmp = new PrintStream(tempDistanceFile);
			for (int i = 0; i < d.getNumDistancePairs(); i++)
			{
				int[] pair = d.getDistancePair(i);
				StringBuffer buffer = new StringBuffer("[");
				buffer.append(pair[0]);
				buffer.append(";");
				buffer.append(pair[1]);
				buffer.append("]");
				tmp.println(buffer);
			}
			tmp.close();

			// Status.INFO.println("done");
			return tempDistanceFile;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static void writeToDistancePairFile(File f, DistancePairData d)
	{
		if (!Settings.WRITE_DISTANCE_PAIRS)
		{
			Status.WARN.println("storing of distance pair results disabled");
			return;
		}

		DataFileManager.createParentFolders(f);
		File tmp = new File(f + ".tmp");

		Status.INFO.println(Status.INDENT + "Write distance pairs to file '" + f.getName()
				+ "(.tmp)'");

		try
		{
			PrintStream out = new PrintStream(tmp);

			for (int i = 0; i < d.getNumDistancePairs(); i++)
			{
				int[] pair = d.getDistancePair(i);

				StringBuffer buffer = new StringBuffer("[");
				buffer.append(pair[0]);
				buffer.append(";");
				buffer.append(pair[1]);
				buffer.append("]");

				HashMap<Integer, List<Double>> distances = d.getMoleculesAndDistances(i);
				if (distances != null && distances.size() > 0)
				{
					buffer.append(",{");
					boolean first = true;
					for (Integer mol : distances.keySet())
					{
						if (!first)
							buffer.append(",");
						else
							first = false;
						buffer.append("(" + mol + ";");
						boolean firstDist = true;
						for (Double dist : distances.get(mol))
						{
							if (firstDist)
								firstDist = false;
							else
								buffer.append("#");
							buffer.append(dist);
						}
						buffer.append(")");
					}
					buffer.append("}");
				}

				out.println(buffer);
			}

			out.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		boolean res = tmp.renameTo(f);
		res |= tmp.delete();

		if (!res)
			throw new Error("renaming or delete file error");

		// Status.INFO.println("done");
	}
}
