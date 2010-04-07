package eval;


public class OldEvaluation
{

	// public static void evalFragments(MoleculeActivityData d, FragmentMoleculeData f)
	// {
	// WekaEval.evalDataset(getArffFile(d, f));
	// }

	// public static void evalFragmentsTrainTest(MoleculeActivityData trainData, FragmentMoleculeData trainFragments,
	// MoleculeActivityData testData)
	// {
	// String arffTrain = getArffFile(trainData, trainFragments);
	// String arffTest = getTestArffFile(testData, trainFragments);
	// WekaEval.evalTrainTest(arffTrain, arffTest);
	// }

	// public static void evalPairs(MoleculeActivityData d, DistancePairData p)
	// {
	// WekaEval.evalDataset(getArffFile(d, p));
	// }

	// public static void evalPairsTrainTest(MoleculeActivityData trainData, FragmentMoleculeData trainFragments,
	// DistancePairData trainPairs, MoleculeActivityData testData)
	// {
	// String arffTrain = getArffFile(trainData, trainPairs);
	// String arffTest = getTestArffFile(testData, trainFragments, trainPairs);
	// WekaEval.evalTrainTest(arffTrain, arffTest);
	// }
	//
	// public static void evalFragmentsAndPairsTrainTest(MoleculeActivityData trainData, FragmentMoleculeData trainFragments,
	// DistancePairData trainPairs, MoleculeActivityData testData)
	// {
	// String arffTrain = getArffFile(trainData, trainPairs);
	// String arffTest = getTestArffFile(testData, trainFragments, trainPairs);
	// WekaEval.evalTrainTest(arffTrain, arffTest);
	// }

	// private static File getArffFile(MoleculeActivityData d, FragmentMoleculeData f)
	// {
	// File file = DataFileManager.getArffFile(d.getDatasetName(), f.getFragmentName(), FragmentDataToArff.ARFF_NAME);
	// if (!file.exists())
	// {
	// ArffWriterFactory.writeFragmentDataToArff(file, d, f);
	// }
	// return file;
	// }

	// private static File getArffFile(MoleculeActivityData d, DistancePairData p)
	// {
	// File file = DataFileManager.getArffFile(d.getDatasetName(), p.getFragmentName(), DistancePairDataToArff.ARFF_NAME);
	// if (!file.exists())
	// {
	// ArffWriter.writeToArffFile(file, new DistancePairDataToArff(d, p));
	// // ArffWriterFactory.writeDistancePairDataToArff(file, d, p);
	// }
	// return file;
	// }

	// private static String getTestArffFile(MoleculeActivityData testData, FragmentMoleculeData trainFragments)
	// {
	// String arff = DataFileManager.getArffFile(testData.getDatasetName(), trainFragments.getFragmentInfo());
	// File file = new File(arff);
	// if (!file.exists())
	// {
	// FragmentMoleculeData testFragments = FeatureGeneratorFactory.mineFragments(testData, trainFragments);
	//
	// Status.INFO.println(testFragments);
	// Status.INFO.println(testFragments.getAdditionalInfo(testData));
	//
	// DataFileManager.createParentFolders(file);
	// ArffWriterFactory.writeFragmentDataToArff(testData, testFragments);
	// }
	// return arff;
	// }
	//
	// private static String getTestArffFile(MoleculeActivityData testData, FragmentMoleculeData trainFragments,
	// DistancePairData trainingDistancePairs)
	// {
	// String arff = DataFileManager.getArffFile(testData.getDatasetName(), trainingDistancePairs.getFragmentInfo());
	// File file = new File(arff);
	// if (!file.exists())
	// {
	// FragmentMoleculeData testFragments = FeatureGeneratorFactory.mineFragments(testData, trainFragments);
	// Status.INFO.println(testFragments);
	// Status.INFO.println(testFragments.getAdditionalInfo(testData));
	//
	// DistancePairData testPairs = DistancePairMiner.mineDistancePairs(testData, testFragments, trainingDistancePairs);
	// Status.INFO.println(testPairs);
	// Status.INFO.println(testPairs.getAdditionalInfo(testData));
	//
	// DebugUtil.printMoleculesWithDistPairs(testData, testPairs);
	//
	// DataFileManager.createParentFolders(file);
	// ArffWriterFactory.writeDistancePairDataToArff(testData, testPairs);
	// }
	// return arff;
	// }

}
