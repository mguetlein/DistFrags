package launch;

import eval.xval.CrossValidationFactory;
import filter.DistancePairFilter;
import filter.FragmentFilter;
import filter.KolmogorovSmirnovFilter;
import io.DataFileManager;
import io.Status;
import util.MemoryUtil;
import util.StringUtil;

public class DistancePairTest
{
	public static void main(String args[])
	{
		/**
		 * todo
		 * 
		 * set aromatic perceived ()<br>
		 * for each bond and atom<br>
		 * unset aromtic()
		 */

		// try
		// {
		// System.out.println("sleeping");
		// Thread.sleep(30000);
		// }
		// catch (InterruptedException e)
		// {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// --- challenge -------------------------------------
		// String dataset = "bbb_substrate";
		// String dataset = "cyp_3A4_substrate";
		// String dataset = "nctrer";
		// MoleculeActivityData d =
		// MoleculeDataIO.readFromSmilesAndClassFile(dataset,
		// "/home/martin/documents/iems/ex10/data/"
		// + dataset + ".smi", "/home/martin/documents/iems/ex10/data/" +
		// dataset + ".class");
		// FragmentMoleculeData f =
		// FragmentFactory.mineFragments(DataFileManager.FRAGMENT_TYPE_LINFRAG,
		// d);
		// ArffWriter.writeToArffFile(new
		// File("/home/martin/documents/iems/ex10/data/" + dataset + ".arff"),
		// new FragmentDataToArff(d, f));
		// ArffCombiner.combine(new
		// File("/home/martin/documents/iems/ex10/data/" + dataset +
		// ".combined.arff"), new File[] {
		// new File("/home/martin/documents/iems/ex10/data/" + dataset +
		// ".DESC.arff"),
		// new File("/home/martin/documents/iems/ex10/data/" + dataset +
		// ".arff") }, new boolean[] { true, false });
		// ------------------------------------------------

		// for (String d : DataFileManager.BINDING_DATASETS)
		// MoleculeFactory.getMoleculeActivityData(d);

		// new ChiSquareFragmentFilter(100, 0.05));

		// CrossValidationFactory.printInfo(DataFileManager.BINDING_DATASETS,
		// DataFileManager.FRAGMENT_TYPE_LINFRAG, true);

		// new KolmogorovSmirnovTest(100, 5000, 0.1), true);
		//
		// CrossValidationFactory.performFragmentCrossValidation(DataFileManager.SMALL_DATASET,
		// DataFileManager.FRAGMENT_TYPE_LINFRAG);
		// CrossValidationFactory.performDistancePairCrossValidation(DataFileManager.SMALL_DATASET,
		// DataFileManager.FRAGMENT_TYPE_LINFRAG);
		// CrossValidationFactory.performChiSquareFragmentCrossValidation(DataFileManager.BINDING_DATASETS,
		// DataFileManager.FRAGMENT_TYPE_LINFRAG, false);
		// CrossValidationFactory.performDistancePairCrossValidation(DataFileManager.BINDING_DATASETS,
		// DataFileManager.FRAGMENT_TYPE_LINFRAG);

		// ResultViews.printNumFeatures(DataFileManager.BINDING_DATASETS);
		// ResultViews.printClassificationResults(DataFileManager.BINDING_DATASETS);
		// ResultViews.printChiSquareClassificationComparison(DataFileManager.BINDING_DATASETS);
		// ResultViews.printCombineClassificationComparison(DataFileManager.BINDING_DATASETS);
		//

		String fragmentType = DataFileManager.FRAGMENT_TYPE_LINFRAG;
		FragmentFilter fraqFilter = null;
		String[] datasets = DataFileManager.CYP_2C9_INHIBITOR;
		// DistancePairFilter distFilter = new TTestFilter(2000, 0.01);
		DistancePairFilter distFilter = new KolmogorovSmirnovFilter(2000, 0.01);

		// CrossValidationFactory.printInfo(datasets, fragmentType, true);
		// CrossValidationFactory.mineFragments(datasets, fragmentType, fraqFilter);
		// CrossValidationFactory.mineDistances(datasets, fragmentType, distFilter, false);
		CrossValidationFactory.performFragmentAndDistancePairCrossValidation(datasets, fragmentType, fraqFilter, distFilter, false);

		System.gc();
		Status.INFO.println("\n" + StringUtil.getTimeStamp(Settings.START_TIME) + ", mem-usage: " + MemoryUtil.getUsedMemoryString());
		// System.exit(0);
	}
	// public static void moleculeSize()
	// {

	// MoleculeActivityData d =
	// MoleculeFactory.getMoleculeActivityData(DataFileManager.CPDB_DATASETS[11]);
	// // FragmentIO.createFMinerFile(new File("/home/martin/tmp/fminer.out.5"),
	// d);
	// new MoleculeSizeViewer(d);
	// }

	// public static void cvInfo()
	// {
	// MoleculeActivityData d =
	// MoleculeFactory.getMoleculeActivityData(DataFileManager.CPDB_DATASETS[0]);
	// CrossValidationFactory.printInfo(d, 10, 1, true,
	// DataFileManager.FRAGMENT_TYPE_LINFRAG);
	// }

	// public static void ak_datasets()
	// {
	// String ak[] = new String[] { "AK_Mouse", "AK_MultiCellCall", "AK_Rat",
	// "AK_SingleCellCall" };
	// String cpdb[] = new String[] { "mouse_carcinogenicity",
	// "multi_cell_call", "rat_carcinogenicity", "single_cell_call" };
	//
	// for (int i = 0; i < ak.length; i++)
	// {
	// MoleculeActivityData d = MoleculeFactory.getMoleculeActivityData(ak[i]);
	// MoleculeActivityData d2 =
	// MoleculeFactory.getMoleculeActivityData(cpdb[i]);
	//
	// int match = 0;
	// int differentAct = 0;
	// for (int j = 0; j < d.getNumMolecules(); j++)
	// {
	//
	// for (int j2 = 0; j2 < d2.getNumMolecules(); j2++)
	// {
	// if (d.getMolecule(j) != null && d2.getMolecule(j2) != null
	// && (d.getMolecule(j).getAtomCount() == d2.getMolecule(j2).getAtomCount())
	// && d.getMolecule(j).isIsomorph(d2.getMolecule(j2)))
	// {
	// if (d.getMoleculeActivity(j) != d2.getMoleculeActivity(j2))
	// {
	// Status.INFO.println("different act: '" + d.getMoleculeSmiles(j) + "' : "
	// + d.getMoleculeActivity(j) + "  '" + d2.getMoleculeSmiles(j2) + "' : "
	// + d2.getMoleculeActivity(j2));
	// differentAct++;
	// }
	// match++;
	// break;
	// }
	// }
	// }
	// Status.INFO.println("matches: " + match + " different-act: " +
	// differentAct + "\n");
	// }
	//
	// Status.INFO.println();
	// }
	//
	// public static void cvTest()
	// {
	// Random r = new Random();
	// MoleculeActivityData d =
	// MoleculeFactory.getMoleculeActivityData(DataFileManager.ESTROGEN_DATASETS[1]);
	//
	// String s = d.getMoleculeSmiles(r.nextInt(d.getNumMolecules()));
	//
	// List<CrossValidationData> xvals = new ArrayList<CrossValidationData>();
	// int numFolds = 10;// 2 + r.nextInt(9);
	// long seed = 1;// r.nextLong();
	// xvals.add(new CrossValidationData(d, numFolds, seed, false));
	// xvals.add(new CrossValidationData(d, numFolds, seed, true));
	//
	// for (CrossValidationData xval : xvals)
	// {
	// Status.INFO.println();
	// for (int i = 0; i < xval.getNumFolds(); i++)
	// {
	// Status.INFO.println((i + 1) + ":");
	//
	// for (int k = 0; k < 2; k++)
	// {
	// boolean test = k == 1;
	// MoleculeActivityData data = xval.getMoleculeActivityData(i, test);
	// Status.INFO.print(data);
	// for (int j = 0; j < data.getNumMolecules(); j++)
	// if (data.getMoleculeSmiles(j).equals(s))
	// Status.INFO.print(" <- " + s);
	// Status.INFO.println();
	// }
	// }
	// }
	// }

	// public static void cvTestDist()
	// {
	// // String datasetName = DataFileManager.getDatasetName(0);
	// String datasetName = DataFileManager.getDatasetNameCpdb(3);
	//
	// String smilesFile = DataFileManager.getSmilesFile(datasetName);
	// String classFile = DataFileManager.getClassFile(datasetName);
	//
	// MoleculeActivityData d =
	// MoleculeFactory.getMoleculeActivityData(datasetName);
	//
	// int numFolds = 5;
	//
	// Status.INFO.println("\nCV");
	//
	// CrossValidation cv = new CrossValidation(d, numFolds, 1);
	//
	// for (int i = 0; i < numFolds; i++)
	// {
	// Status.INFO.println("\n\nTrain fold:" + (i + 1));
	//
	// MoleculeActivityData trainData = cv.getMoleculeActivityData(i, false);
	// Status.INFO.println(trainData);
	// Status.INFO.println();
	//
	// // for (int j = 0; j < train.getNumMolecules(); j++)
	// // {
	// // Status.INFO.println(" " + j + " " + train.getMoleculeSmiles(j));
	// // }
	//
	// FragmentMoleculeData trainFragments =
	// FragmentFactory.mineFragments(trainData);
	// Status.INFO.println();
	//
	// FragmentMoleculeData trainChiSquareFragments =
	// FragmentFactory.applyChiSquareFilter(trainFragments, trainData);
	// Status.INFO.println();
	//
	// DistancePairData trainPairs =
	// DistancePairFactory.mineDistancePairs(trainData, trainFragments);
	// Status.INFO.println();
	//
	// DistancePairData trainFStatisticsPairs =
	// DistancePairFactory.applyFStatisticsFilter(trainPairs, trainData);
	// Status.INFO.println();
	//
	// // if (true == true)
	// // break;
	//
	// Status.INFO.println("\nTest fold:" + (i + 1));
	//
	// MoleculeActivityData testData = cv.getMoleculeActivityData(i, true);
	// Status.INFO.println(testData);
	// Status.INFO.println();
	//
	// FragmentMoleculeData testFragments =
	// FragmentFactory.checkFragments(testData, trainFragments);
	// Status.INFO.println();
	//
	// FragmentMoleculeData testChiSquareFragments =
	// FragmentFactory.checkFragments(testData, trainChiSquareFragments);
	// Status.INFO.println();
	//
	// DistancePairData testPairs =
	// DistancePairFactory.checkDistancePairs(testData, testFragments,
	// trainPairs);
	// Status.INFO.println();
	//
	// DistancePairData testFStatisticsPairs =
	// DistancePairFactory.checkDistancePairs(testData, testFragments,
	// trainFStatisticsPairs);
	// Status.INFO.println();
	//
	// // if (true == true)
	// // break;
	//
	// // new DistancePairDataBrowser(testData, testFStatisticsPairs);
	//
	// // FragmentMoleculeData testFragments =
	// FeatureGeneratorFactory.mineFragments(testData, trainFragments);
	// // Status.INFO.println(testFragments.toString());
	// // Status.INFO.println(testFragments.getAdditionalInfo(trainData));
	// //
	// // DistancePairData testPairs =
	// DistancePairMiner.mineDistancePairs(testData, testFragments, trainPairs);
	// // Status.INFO.println(testPairs.toString());
	//
	// // Status.INFO.println("\nTest data");
	// // for (int j = 0; j < testData.getNumMolecules(); j++)
	// // {
	// // Status.INFO.println(" " + j + " " + testData.getMoleculeSmiles(j) +
	// " " + testData.getMoleculeActivity(j));
	// // }
	// // Status.INFO.println();
	//
	// // Evaluation.eval(train, chiSquare, test);
	//
	// Status.INFO.println("\nAll linear fragments\n");
	// FragmentEvaluation eval = new FragmentEvaluation(trainData,
	// trainFragments, testData);
	// eval.setTestFragments(testFragments);
	// eval.evaluate();
	// Status.INFO.println();
	//
	// Status.INFO.println("\nChi square fragments\n");
	// FragmentEvaluation eval2 = new FragmentEvaluation(trainData,
	// trainChiSquareFragments, testData);
	// eval.setTestFragments(testChiSquareFragments);
	// eval2.evaluate();
	// Status.INFO.println();
	//
	// Status.INFO.println("\nAll distance pairs\n");
	// DistancePairEvaluation eval3 = new DistancePairEvaluation(trainData,
	// trainFragments, trainPairs, testData);
	// eval3.setTestFragments(testFragments);
	// eval3.setTestDistancePairs(testPairs);
	// eval3.evaluate();
	// Status.INFO.println();
	//
	// Status.INFO.println("\nFstatistics distance pairs\n");
	// DistancePairEvaluation eval4 = new DistancePairEvaluation(trainData,
	// trainFragments, trainFStatisticsPairs,
	// testData);
	// eval4.setTestFragments(testFragments);
	// eval4.setTestDistancePairs(testFStatisticsPairs);
	// eval4.evaluate();
	// Status.INFO.println();
	//
	// Status.INFO.println("\nChi square fragments and Fstatistics distance pairs\n");
	// FragmentAndDistancePairEvaluation eval5 = new
	// FragmentAndDistancePairEvaluation(trainData,
	// trainChiSquareFragments, trainFStatisticsPairs, testData);
	// eval5.setTestFragments(testChiSquareFragments);
	// eval5.setTestDistancePairs(testFStatisticsPairs);
	// eval5.evaluate();
	// Status.INFO.println();
	//
	// // if (true == true)
	// // break;
	// }
	// }

	// public static void cvTestFrags()
	// {
	// // int numDataset = 0;
	// // String datasetName = DataFileManager.getDatasetName(numDataset);
	// // String smilesFile = DataFileManager.getSmilesFile(numDataset);
	// // String classFile = DataFileManager.getClassFile(numDataset);
	//
	// // String datasetName = DataFileManager.getDatasetNameCpdb(0);
	// String datasetName = DataFileManager.getDatasetName(0);
	//
	// String smilesFile = DataFileManager.getSmilesFile(datasetName);
	// String classFile = DataFileManager.getClassFile(datasetName);
	//
	// MoleculeActivityData d =
	// MoleculeFactory.readFromSmilesAndClassFile(datasetName, smilesFile,
	// classFile);
	// // MoleculeData d = MoleculeDataReader.readFromSmilesFile(datasetName,
	// smilesFile);
	//
	// int numFolds = 5;
	// CrossValidation cv = new CrossValidation(d, numFolds, 3);
	// for (int i = 0; i < numFolds; i++)
	// {
	// Status.INFO.print("train " + (i + 1) + " ");
	// MoleculeActivityData train = cv.getMoleculeActivityData(i, false);
	// // MoleculeData train = cv.getMoleculeData(i, false);
	// Status.INFO.println(train);
	//
	// // for (int j = 0; j < train.getNumMolecules(); j++)
	// // {
	// // Status.INFO.println(" " + j + " " + train.getMoleculeSmiles(j));
	// // }
	//
	// FragmentMoleculeData f = FragmentFactory.mineFragments(train);
	//
	// FragmentMoleculeData chiSquare = FragmentFactory.applyChiSquareFilter(f,
	// train);
	//
	// Status.INFO.print("test  " + (i + 1) + " ");
	// MoleculeActivityData test = cv.getMoleculeActivityData(i, true);
	// // MoleculeData test = cv.getMoleculeData(i, true);
	// Status.INFO.println(test);
	//
	// // for (int j = 0; j < test.getNumMolecules(); j++)
	// // {
	// // Status.INFO.println(" " + j + " " + test.getMoleculeSmiles(j));
	// // }
	//
	// Status.INFO.println();
	//
	// TrainTestEvaluation eval = new FragmentDataEvaluation(train, f, test);
	// eval.evaluate();
	//
	// TrainTestEvaluation eval2 = new FragmentDataEvaluation(train, chiSquare,
	// test);
	// eval2.evaluate();
	//
	// // if (true == true)
	// // break;
	// }
	// }

	// public static void distanceArffTest()
	// {
	// MoleculeActivityData d =
	// MoleculeFactory.getMoleculeActivityData(DataFileManager.CPDB_DATASETS[0]);
	//
	// FragmentMoleculeData f =
	// FragmentFactory.mineFragments(DataFileManager.FRAGMENT_TYPE_LINFRAG, d);
	//
	// // String arffFile = DataFileManager.getArffFile(datasetName,
	// f.getFragmentInfo());
	//
	// // if (!new File(arffFile).exists())
	// // ArffWriterFactory.writeToArff(arffFile, d, f);
	//
	// DistancePairData p = DistancePairFactory.mineDistancePairs(d, f);
	// Status.INFO.println();
	//
	// DistancePairData p2 = DistancePairFactory.applyFStatisticsFilter(p, d);
	// Status.INFO.println();
	//
	// new DistancePairDataBrowser(d, p2);

	// String arffFile2 = DataFileManager.getArffFile(datasetName,
	// p.getFragmentInfo());

	// if (!new File(arffFile2).exists())
	// ArffWriterFactory.writeToArff(d, p);

	// OldEvaluation.evalFragments(d, f);
	// OldEvaluation.evalPairs(d, p);
	// }

	// public static void arffTest()
	// {
	// int numDataset = 2;
	//
	// String datasetName = DataFileManager.getDatasetNameCpdb(numDataset);
	//
	// MoleculeActivityData d =
	// MoleculeFactory.getMoleculeActivityData(datasetName);
	//
	// FragmentMoleculeData f = FragmentFactory.mineFragments(d);
	//
	// // String arffFile = DataFileManager.getArffFile(datasetName,
	// f.getFragmentInfo());
	//
	// // if (!new File(arffFile).exists())
	// // ArffWriterFactory.writeToArff(d, f);
	//
	// FragmentMoleculeData f2 = FragmentFactory.applyChiSquareFilter(f, d);
	//
	// // String arffFile2 = DataFileManager.getArffFile(datasetName,
	// f2.getFragmentInfo());
	//
	// // if (!new File(arffFile2).exists())
	// // ArffWriterFactory.writeToArff(d, f2);
	//
	// OldEvaluation.evalFragments(d, f);
	// OldEvaluation.evalFragments(d, f2);
	// }

}
