package io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import launch.Settings;
import util.ArrayUtil;
import util.FileUtil;
import filter.Filter;

/**
 * plain dataset: <dataset>(.class/.smi)<br>
 * example: mouse_carcenogenicity.smi<br>
 * 
 * cv-dataset: <dataset>.<cv><br>
 * example: mouse_carcinogenitcity.cv_f5_s1_i1_train<br>
 * 
 * linfrag-file: <datasetfilename>.linfrag<br>
 * filtered-linfrag-file: <datasetfilename>.linfrag.<filter><br>
 * 
 * dist-pairs-file: <linfragfilename>.dist<br>
 * fstatistics-dist-pairs-file: <linfragfilename>.dist.<filter><br>
 * 
 * arff-file: <linfrag/distpair-file>.arff<br>
 * 
 * @author martin
 * 
 */
public class DataFileManager
{

	private final static String[] dataDirectories = new String[] { /* "/results/distance/data/", */"/data/cpdb/",
			"/data/cpdbdata/", "/data/various/", "/data/estrogen/", "/data/bbb/", "/data/cyp/" };

	public static final String[] DATASETS = new String[] { "mouse_300", "mouse_200", "AK_Mouse", "AK_MultiCellCall",
			"AK_Rat", "AK_SingleCellCall" };

	public static final String[] CPDB_DATASETS = new String[] { "hamster_carcinogenicity", "hamster_female_carcinogenicity",
			"hamster_male_carcinogenicity", "mouse_carcinogenicity", "mouse_female_carcinogenicity",
			"mouse_male_carcinogenicity", "multi_cell_call", "rat_carcinogenicity", "rat_female_carcinogenicity",
			"rat_male_carcinogenicity", "salmonella_mutagenicity", "single_cell_call" };

	public static final String[] CPDB_ALT_DATASETS = new String[] { "mouse_carcinogenicity_alt", "multi_cell_call_alt",
			"rat_carcinogenicity_alt", "salmonella_mutagenicity_alt" };

	public static final String[] ESTROGEN_DATASETS = new String[] { "kierbl", "nctrer" };

	public static final String[] BENCHMARK_DATASETS = ArrayUtil.concat(CPDB_ALT_DATASETS, ESTROGEN_DATASETS);

	public static final String[] BBB_DATASETS = new String[] { "bbb_inhibitor", /* "bbb_inducer", */"bbb_substrate" };

	public static final String[] CYP_DATASETS = new String[] { "cyp_2C9_inhibitor", "cyp_2D6_inhibitor",
			"cyp_3A4_inhibitor", "cyp_2C9_substrate", "cyp_2D6_substrate", "cyp_3A4_substrate" };

	public static final String[] BINDING_DATASETS = ArrayUtil.concat(BBB_DATASETS, ESTROGEN_DATASETS, CYP_DATASETS);

	public static final String[] SMALL_DATASET = new String[] { "nctrer" }; // , "bbb_inhibitor", "cyp_3A4_inhibitor" };

	// ------------------------------------------------------------------

	private static String[][] NICE_NAMES = new String[][] { { "mouse_carcinogenicity_alt", "Mouse Carcinogenicity" },
			{ "multi_cell_call_alt", "Multi Cell Call" }, { "rat_carcinogenicity_alt", "Rat Carcinogenicity" },
			{ "salmonella_mutagenicity_alt", "Salmonella Mutaginicity" }, { "kierbl", "KIERBL" }, { "nctrer", "NCTRER" } };

	public static String getSmilesFile(String dataset)
	{
		for (String dir : dataDirectories)
		{
			String s = Settings.USER_HOME + dir + dataset + "/data/" + dataset + ".smi";
			if (new File(s).exists())
				return s;
			// Status.WARN.println(Settings.USER_HOME + dir + dataset + "/data/" + dataset + ".smi");
		}
		throw new IllegalArgumentException("dataset not found: " + dataset);
	}

	public static String getClassFile(String dataset)
	{
		for (String dir : dataDirectories)
		{
			String s = Settings.USER_HOME + dir + dataset + "/data/" + dataset + ".class";
			if (new File(s).exists())
				return s;
			// Status.WARN.println(Settings.USER_HOME + dir + dataset + "/data/" + dataset + ".class");
		}
		throw new IllegalArgumentException("dataset not found: " + dataset);
	}

	// public static int getNumCpdbDatasetFiles()
	// {
	// return CPDB_DATASETS.size();
	// }
	//
	// public static String getDatasetNameCpdb(int i)
	// {
	// return CPDB_DATASETS.get(i);
	// }
	//
	// public static int getNumDatasetFiles()
	// {
	// return datasetNames.size();
	// }
	//
	// public static String getDatasetName(int i)
	// {
	// return datasetNames.get(i);
	// }

	// ---------------------------------------------------------------------------

	public static final String FRAGMENT_TYPE_LINFRAG = "linfrag";

	public static final String FRAGMENT_TYPE_FMINER = "fminer";

	public static boolean isLegalFragmentType(String fragmentType)
	{
		return fragmentType != null
				&& (fragmentType.equals(FRAGMENT_TYPE_LINFRAG) || fragmentType.equals(FRAGMENT_TYPE_FMINER));
	}

	// ---------------------------------------------------------------------------

	public static String getFragmentNameFiltered(String fragmentName, Filter filter)
	{
		return fragmentName + "." + filter.getName();
	}

	public static String getFragmentNameDistancePair(String fragmentName)
	{
		return fragmentName + ".dist";
	}

	// public static String getFragmentNameDistancePairFiltered(String distancePairName, DistancePairFilter filter)
	// {
	// return distancePairName + "." + filter.getName();
	// }

	// ---------------------------------------------------------------------------

	public static File getLinfragFile(String datasetName, String fragmentName)
	{
		// if (CPDB_DATASETS.contains(datasetName) && fragmentName.equals(FRAGMENT_TYPE_LINFRAG))
		// return new File(Settings.USER_HOME + "/data/cpdb/" + datasetName + "/data/" + datasetName + "." + fragmentName);
		// else
		return new File(Settings.USER_HOME + "/results/distance/data/" // + datasetName + "/data/"
				+ datasetName + "." + fragmentName);
	}

	// public static File getLinfragFile(String datasetName, String linfragName)
	// {
	// return new File(Settings.USER_HOME+"/results/distance/data/" + datasetName + "/data/" + datasetName + "." + linfragName);
	// }

	// public static File getLinfragFile(String datasetName, String linfragName, String filterName)
	// {
	// assert (linfragName.startsWith(datasetName));
	// return new File(Settings.USER_HOME+"/results/distance/data/" + datasetName + "/data/" + linfragName + "." + filterName);
	// }

	public static File getDistancePairFile(String datasetName, String distancePairName)
	{
		// assert (linfragName.startsWith(datasetName));
		return new File(Settings.USER_HOME + "/results/distance/data/" // + datasetName + "/data/"
				+ datasetName + "." + distancePairName);
	}

	// public static File getDistancePairFile(String datasetName, String distPairName, String filterName)
	// {
	// assert (distPairName.startsWith(datasetName));
	// return new File(Settings.USER_HOME+"/results/distance/data/" + datasetName + "/data/" + distPairName + "." + filterName);
	// }

	public static File getArffFile(String datasetName, String featureArffName)
	{
		// String arffString = "";
		// if (arffName != null && arffName.length() > 0)
		// arffString = "." + arffName;

		// assert (featureName.startsWith(datasetName));
		return new File(Settings.USER_HOME + "/results/distance/data/" // + datasetName + "/arff/"
				+ datasetName + "." + featureArffName + ".arff");
	}

	// public static File getCombinedArffFile(String datasetName, String featureName1, String featureName2)
	// {
	// // // example 1:
	// // // 1: dataset.cv.linfrag
	// // // 2: dataset.cv.linfrag.dist
	// //
	// // // name: dataset.cv.1.linfrag.2.linfrag.dist
	// //
	// // // example 2:
	// // // 1: dataset.cv.linfrag.chisquare
	// // // 2: dataset.cv.linfrag.dist.fstatistics
	// //
	// // // name: dataset.cv.1.linfrag.chisquare.2.linfrag.dist.fstatistics
	// //
	// // assert (featureName1.startsWith(datasetName) && featureName2.startsWith(datasetName));
	//
	// // String filename = datasetName + ".1" + featureName1.substring(datasetName.length()) + ".2"
	// // + featureName2.substring(datasetName.length()) + ".arff";
	//
	// return new File(Settings.USER_HOME + "/results/distance/data/" + datasetName + "/arff/" + datasetName + "."
	// + featureName1 + "." + featureName2 + ".arff");
	// }

	public static void createParentFolders(String file)
	{
		createParentFolders(new File(file));
	}

	public static void createParentFolders(File file)
	{
		if (file.exists())
			throw new Error("file already exists " + file);

		FileUtil.createParentFolders(file);
	}

	public static File getEvalFile(String datasetBaseName, String experimentName)
	{
		return new File(Settings.USER_HOME + "/results/distance/data/" // + datasetBaseName + "/eval/"
				+ datasetBaseName + "." + experimentName + ".eval");
	}

	public static List<File> getExistingEvalFiles(String datasetBaseNames[], String[] experimentName)
	{
		List<File> res = new ArrayList<File>();

		for (String d : datasetBaseNames)
		{
			for (String e : experimentName)
			{
				File f = getEvalFile(d, e);
				if (f.exists())
					res.add(f);
			}
		}
		return res;
	}

	public static String getNiceDatasetName(String datasetName)
	{
		for (int i = 0; i < NICE_NAMES.length; i++)
			if (NICE_NAMES[i][0].equals(datasetName))
				return NICE_NAMES[i][1];
		return datasetName;
	}

}
