package io.arff;

import io.Status;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class ArffCombiner
{
	public static void combine(File destinationFile, File... arffFiles)
	{
		boolean lastAttributeIsClass[] = new boolean[arffFiles.length];
		Arrays.fill(lastAttributeIsClass, true);
		combine(destinationFile, arffFiles, lastAttributeIsClass);
	}

	public static void combine(File destinationFile, File[] arffFiles, boolean lastAttributeIsClass[])
	{

		try
		{
			Status.INFO.println(Status.INDENT + "Merging '" + arffFiles.length + "' arff files");
			Status.addIndent();

			Instances instances = null;
			for (int i = 0; i < arffFiles.length; i++)
			{
				Status.INFO.println(Status.INDENT + "Reading arff file: " + arffFiles[i].getName());
				if (i == 0)
				{
					instances = new Instances(new BufferedReader(new FileReader(arffFiles[i])));
					if (lastAttributeIsClass[i])
						instances.setClassIndex(instances.numAttributes() - 1);
				}
				else
				{
					Instances newInstances = new Instances(new BufferedReader(new FileReader(arffFiles[i])));
					if (lastAttributeIsClass[i])
						instances.setClassIndex(instances.numAttributes() - 1);

					instances = mergeInstances(instances, newInstances);
				}
			}

			Status.INFO.println(Status.INDENT + "Writing to file: " + destinationFile.getName());

			PrintStream out = new PrintStream(destinationFile);
			out.println(instances.toString());
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

	/**
	 * adpated version of Instances.mergeInstances()
	 * 
	 * @param first
	 * @param second
	 * @return
	 */
	private static Instances mergeInstances(Instances first, Instances second)
	{
		if (first.numInstances() != second.numInstances())
			throw new IllegalArgumentException("Instance sets must be of the same size");

		boolean twoClassAttributes = (first.classIndex() >= 0 && second.classIndex() >= 0);

		// Create the vector of merged attributes
		FastVector newAttributes = new FastVector();
		for (int i = 0; i < first.numAttributes(); i++)
		{
			if (!twoClassAttributes || i != first.classIndex())
				newAttributes.addElement(first.attribute(i));
		}
		for (int i = 0; i < second.numAttributes(); i++)
		{
			newAttributes.addElement(second.attribute(i));
		}

		// Create the set of Instances
		Instances merged = new Instances(first.relationName() + '_' + second.relationName(), newAttributes, first
				.numInstances());
		// Merge each instance
		for (int i = 0; i < first.numInstances(); i++)
		{
			merged.add(mergeInstance(first.instance(i), second.instance(i), twoClassAttributes));
		}
		return merged;
	}

	/**
	 * adapted version of Instance.mergeInstance()
	 * 
	 * @param first
	 * @param second
	 * @return
	 */
	private static Instance mergeInstance(Instance first, Instance second, boolean twoClassAttributes)
	{
		int m = 0;
		double[] newVals = new double[first.numAttributes() + second.numAttributes() - (twoClassAttributes ? 1 : 0)];
		for (int j = 0; j < first.numAttributes(); j++, m++)
		{
			if (!twoClassAttributes || j != first.classIndex())
				newVals[m] = first.value(j);
			else
				m--;
		}
		for (int j = 0; j < second.numAttributes(); j++, m++)
		{
			newVals[m] = second.value(j);
		}
		return new Instance(1.0, newVals);
	}
}
