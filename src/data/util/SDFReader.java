package data.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;

import org.apache.commons.lang.ArrayUtils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import util.FileUtil;

public class SDFReader
{
	public static void main(String args[])
	{
		// String sdfFiles[] = { "Inhibitors_Chen_2C9_training_MOE.sdf", "Inhibitors_Chen_2C9_validation_MOE.sdf" };
		// String sdfFiles[] = { "Inhibitors_Chen_2D6_training_MOE.sdf", "Inhibitors_Chen_2D6_validation_MOE.sdf", };
		// String sdfFiles[] = { "Inhibitors_Chen_3A4_training_MOE.sdf", "Inhibitors_Chen_3A4_validation_MOE.sdf", };
		// String sdfFiles[] = { "Substrates_Chen_2C9_training_MOE.sdf", "Substrates_Chen_2C9_validation_MOE.sdf", };
		// String sdfFiles[] = { "Substrates_Chen_2D6_training_MOE.sdf", "Substrates_Chen_2D6_validation_MOE.sdf", };
		String sdfFiles[] = { "Substrates_Chen_3A4_training_MOE.sdf", "Substrates_Chen_3A4_validation_MOE.sdf" };
		for (int i = 0; i < sdfFiles.length; i++)
			sdfFiles[i] = "/home/martin/data/cyp/" + sdfFiles[i];
		String outputPath = "/home/martin/data/cyp/";
		// String datasetName = "cyp_2C9_inhibitor";
		// String datasetName = "cyp_2D6_inhibitor";
		// String datasetName = "cyp_3A4_inhibitor";
		// String datasetName = "cyp_2C9_substrate";
		// String datasetName = "cyp_2D6_substrate";
		String datasetName = "cyp_3A4_substrate";

		String smilesTag = null;
		String activityTag = "Class";
		String[] activityClasses = { "1", "0" };

		// String sdfFile = "/home/martin/data/estrogen/KIERBL_v1a_278_17Feb2009.sdf";
		// String outputPath = "/home/martin/data/estrogen/";
		// String datasetName = "kierbl";
		// String smilesTag = "STRUCTURE_SMILES";
		// String activityTag = "ActivityOutcome_KIERBL";
		// String[] activityClasses = { "inactive", "active" };

		// String sdfFile = "/home/martin/data/estrogen/NCTRER_v4b_232_15Feb2008.sdf";
		// String outputPath = "/home/martin/data/estrogen/";
		// String datasetName = "nctrer";
		// String smilesTag = "STRUCTURE_SMILES";
		// String activityTag = "ActivityOutcome_NCTRER";
		// String[] activityClasses = { "inactive", "active" };

		new SDFReader(datasetName, outputPath, sdfFiles, smilesTag, activityTag, activityClasses, true);
		// new SDFReader(sdfFiles, activityTag, activityClasses);
	}

	public SDFReader(String datasetName, String outputPath, String sdfFiles[], String smilesTag, String activityTag,
			String[] activityClasses, boolean removeHydronges)
	{
		SmilesGenerator smilesGenerator = new SmilesGenerator();
		smilesGenerator.setUseAromaticityFlag(true);

		try
		{

			File smiles = new File(outputPath + "/" + datasetName + "/data/" + datasetName + ".smi");
			FileUtil.createParentFolders(smiles);
			PrintStream smilesPrint = new PrintStream(smiles);
			System.out.println("Printing to file: " + smiles);

			File clazz = new File(outputPath + "/" + datasetName + "/data/" + datasetName + ".class");
			FileUtil.createParentFolders(clazz);
			PrintStream classPrint = new PrintStream(clazz);
			System.out.println("Printing to file: " + clazz);

			int count = 1;
			int indexCount[] = new int[activityClasses.length];

			for (String sdfFile : sdfFiles)
			{

				if (indexCount[0] > 0)
				{
					System.out.println((count - 1) + " molecules converted - still running");
					for (int i = 0; i < indexCount.length; i++)
					{
						System.out.println(activityClasses[i] + " : " + indexCount[i]);
					}
				}

				IteratingMDLReader reader = new IteratingMDLReader(new FileReader(new File(sdfFile)),
						DefaultChemObjectBuilder.getInstance());

				while (reader.hasNext())
				{
					IMolecule m = (IMolecule) reader.next();

					int index = ArrayUtils.indexOf(activityClasses, m.getProperty(activityTag));
					if (index == -1)
					{
						System.err.println("dropping illegal activity value: \"" + m.getProperty(activityTag)
								+ "\" for molecule: " + m.getProperty(smilesTag));
						continue;
					}

					indexCount[index]++;

					String smilesString;
					if (smilesTag != null)
						smilesString = String.valueOf(m.getProperty(smilesTag));
					else
						smilesString = smilesGenerator.createSMILES(removeHydronges ? (IMolecule) AtomContainerManipulator
								.removeHydrogens(m) : m);
					smilesPrint.println(count + "\t" + smilesString);
					classPrint.println(count + "\t\"" + activityTag + "\"\t" + index);

					count++;
				}
			}

			System.out.println((count - 1) + " molecules converted - done");
			for (int i = 0; i < indexCount.length; i++)
			{
				System.out.println(activityClasses[i] + " : " + indexCount[i]);
			}

			smilesPrint.close();
			classPrint.close();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public SDFReader(String sdfFiles[], String activityTag, String[] activityClasses)
	{

		try
		{
			for (String sdfFile : sdfFiles)
			{

				int count = 0;
				int indexCount[] = new int[activityClasses.length];

				File f = new File(sdfFile);
				IteratingMDLReader reader = new IteratingMDLReader(new FileReader(f), DefaultChemObjectBuilder.getInstance());

				while (reader.hasNext())
				{
					IMolecule m = (IMolecule) reader.next();

					int index = ArrayUtils.indexOf(activityClasses, m.getProperty(activityTag));
					if (index == -1)
					{
						System.err.println("dropping illegal activity value: \"" + m.getProperty(activityTag));
						continue;
					}

					indexCount[index]++;
					count++;
				}

				System.out.println(f.getName());
				System.out.println("# molecules: " + count);
				System.out.print("# class   ");
				for (int i = 0; i < indexCount.length; i++)
				{
					if (i > 0)
						System.out.print("          ");
					System.out.println(activityClasses[i] + ": " + indexCount[i]);
				}
				System.out.println();
			}
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
