package data.factories;

import io.DataFileManager;
import io.MoleculeDataIO;
import io.Status;
import data.MoleculeActivityData;
import data.MoleculeData;

public class MoleculeFactory
{

	public static MoleculeData getMoleculeData(String datasetName)
	{
		throw new Error("not yet implemented");
	}

	public static MoleculeActivityData getMoleculeActivityData(String datasetName)
	{
		MoleculeActivityData data = readFromSmilesAndClassFile(datasetName);
		return data;
	}

	@SuppressWarnings("unused")
	private static MoleculeData readFromArffFile(String datasetName, String arffFile, int smilesAttributeIndex)
	{
		return null;
	}

	@SuppressWarnings("unused")
	private static MoleculeData readFromSmilesFile(String datasetName)
	{

		String smilesFile = DataFileManager.getSmilesFile(datasetName);
		MoleculeData res = MoleculeDataIO.readFromSmilesFile(datasetName, smilesFile);
		Status.INFO.println(Status.INDENT + res);
		return res;
	}

	@SuppressWarnings("unused")
	private static MoleculeActivityData readFromArffFile(String datasetName, String arffFile, int smilesAttributeIndex,
			int activitIndex)
	{
		return null;
	}

	private static MoleculeActivityData readFromSmilesAndClassFile(String datasetName)
	{
		String smilesFile = DataFileManager.getSmilesFile(datasetName);
		String classFile = DataFileManager.getClassFile(datasetName);
		MoleculeActivityData res = MoleculeDataIO.readFromSmilesAndClassFile(datasetName, smilesFile, classFile);
		Status.INFO.println(Status.INDENT + res);
		return res;
	}
}
