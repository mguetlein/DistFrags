package io.arff;

import io.Status;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import weka.core.Instances;

public class ArffCombiner
{

	public static void combine(File destinationFile, File arffFile1, File arffFile2)
	{
		try
		{
			Status.INFO.println(Status.INDENT + "Merging arff files");
			Status.addIndent();

			Status.INFO.println(Status.INDENT + "Reading arff file: " + arffFile1.getName());
			Instances instances = new Instances(new BufferedReader(new FileReader(arffFile1)));
			instances.setClassIndex(instances.numAttributes() - 1);

			Status.INFO.println(Status.INDENT + "Reading arff file: " + arffFile2.getName());
			Instances newInstances = new Instances(new BufferedReader(new FileReader(arffFile2)));
			if (!instances.attribute(instances.numAttributes() - 1).name().equals(
					newInstances.attribute(newInstances.numAttributes() - 1).name()))
				throw new Error();
			newInstances.deleteAttributeAt(newInstances.numAttributes() - 1);

			Instances merged = Instances.mergeInstances(newInstances, instances);
			Status.INFO.println(Status.INDENT + "Writing to file: " + destinationFile.getName());

			PrintStream out = new PrintStream(destinationFile);
			out.println(merged.toString());
			out.close();

			Status.remIndent();
			// Status.INFO.printf("(%d instances, %d attributes)\n", instances.numInstances(), instances.numAttributes());
			// Status.INFO.println("Merging done");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
