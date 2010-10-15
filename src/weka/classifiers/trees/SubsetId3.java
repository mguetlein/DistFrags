package weka.classifiers.trees;

import io.Status;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import util.ArrayUtil;
import util.StringUtil;
import util.VectorUtil;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.InformativeClassifier;
import weka.classifiers.Sourcable;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.NoSupportForMissingValuesException;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.setdistance.SetDistanceCache;

public class SubsetId3 extends AbstractClassifier implements TechnicalInformationHandler, Sourcable, InformativeClassifier
{

	/** for serialization */
	static final long serialVersionUID = -2693678647096322561L;

	/** The node's successors. */
	private SubsetId3[] m_Successors;

	/** Attribute used for splitting. */
	private Attribute m_Attribute;

	/** Class value if node is leaf. */
	private double m_ClassValue;

	/** Class distribution if node is leaf. */
	private double[] m_Distribution;

	/** Class attribute of dataset. */
	private Attribute m_ClassAttribute;

	private int minNumObj = -1;
	private int subsetMinNumObj = 3;

	public static enum SubsetFeatureType
	{
		INCLUDES, EXCLUDES
	}

	private static class SplitNumbers
	{
		int occurs[];
		int notOccurs[];
	}

	private static class SubsetFeature
	{
		Attribute attr;
		double infoGain;
		double distance;
		SubsetFeatureType type;
		double epsilon;

		@Override
		public String toString()
		{
			return ((attr != null) ? attr.name() : "") + (type == SubsetFeatureType.INCLUDES ? " includes " : " excludes ")
					+ StringUtil.formatDouble(distance, 2, 5) + " +- " + StringUtil.formatDouble(epsilon, 2, 5) + " (infoGain: "
					+ StringUtil.formatDouble(infoGain, 3, 6) + ")";
		}

		public String toString(int clazz)
		{
			String typeString;
			if (type == SubsetFeatureType.INCLUDES)
				if (clazz == 1)
					typeString = "  includes ";
				else
					typeString = " !includes ";
			else if (clazz == 0)
				typeString = "  excludes ";
			else
				typeString = " !excludes ";

			return attr.name() + typeString + StringUtil.formatDouble(distance, 2, 5) + " +- " + StringUtil.formatDouble(epsilon, 2, 5)
					+ " (infoGain: " + StringUtil.formatDouble(infoGain, 3, 6) + ")";
		}

	}

	private SubsetFeatureType availableSubsetFeatureTypes[];
	private SubsetFeature subsetFeature;

	private boolean ignoreSubsets = false;

	private static String WATCH_PAIR = null; // "@attribute O-C-C-C<-->O-C-C-C string";
	public static boolean INFO_OUT = false;
	public static boolean DBG_OUT = INFO_OUT && false;
	public static boolean DETAIL_OUT = DBG_OUT && false;
	public static int DBG_NUM_NOMINAL = 0;
	public static int DBG_NUM_STRING = 0;

	// public SubsetId3()
	// {
	//
	// }

	public SubsetId3(SubsetFeatureType availableSubsetFeatureTypes[], boolean ignoreSubsets)
	{
		super();
		this.availableSubsetFeatureTypes = availableSubsetFeatureTypes;
		this.ignoreSubsets = ignoreSubsets;
	}

	/**
	 * Returns a string describing the classifier.
	 * 
	 * @return a description suitable for the GUI.
	 */
	public String globalInfo()
	{

		return "Class for constructing an unpruned decision tree based on the ID3 "
				+ "algorithm. Can only deal with nominal attributes. No missing values "
				+ "allowed. Empty leaves may result in unclassified instances. For more " + "information see: \n\n"
				+ getTechnicalInformation().toString();
	}

	/**
	 * Returns an instance of a TechnicalInformation object, containing detailed information about the technical background of this class,
	 * e.g., paper reference or book this class is based on.
	 * 
	 * @return the technical information about this class
	 */
	public TechnicalInformation getTechnicalInformation()
	{
		TechnicalInformation result;

		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "R. Quinlan");
		result.setValue(Field.YEAR, "1986");
		result.setValue(Field.TITLE, "Induction of decision trees");
		result.setValue(Field.JOURNAL, "Machine Learning");
		result.setValue(Field.VOLUME, "1");
		result.setValue(Field.NUMBER, "1");
		result.setValue(Field.PAGES, "81-106");

		return result;
	}

	/**
	 * Returns default capabilities of the classifier.
	 * 
	 * @return the capabilities of this classifier
	 */
	public Capabilities getCapabilities()
	{
		Capabilities result = super.getCapabilities();
		result.disableAll();

		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.STRING_ATTRIBUTES);

		// class
		result.enable(Capability.NOMINAL_CLASS);
		result.enable(Capability.MISSING_CLASS_VALUES);

		// instances
		result.setMinimumNumberInstances(0);

		return result;
	}

	/**
	 * Builds Id3 decision tree classifier.
	 * 
	 * @param data
	 *            the training data
	 * @exception Exception
	 *                if classifier can't be built successfully
	 */
	public void buildClassifier(Instances data) throws Exception
	{

		// can classifier handle the data?
		getCapabilities().testWithFail(data);

		// remove instances with missing class
		data = new Instances(data);
		data.deleteWithMissingClass();

		makeTree(data);
	}

	public static int makeTreeCounter = 0;

	/**
	 * Method for building an Id3 tree.
	 * 
	 * @param data
	 *            the training data
	 * @exception Exception
	 *                if decision tree can't be built successfully
	 */
	@SuppressWarnings("unchecked")
	private void makeTree(Instances data) throws Exception
	{
		makeTreeCounter++;
		if (INFO_OUT)
			Status.INFO.println("\n" + makeTreeCounter + " make tree, #num-instances: " + data.numInstances());

		// Check if no instances have reached this node.
		if (data.numInstances() == 0)
		{
			m_Attribute = null;
			m_ClassValue = Utils.missingValue();
			m_Distribution = new double[data.numClasses()];
			return;
		}

		subsetFeature = null;
		double[] infoGains = null;

		if (data.numInstances() > minNumObj)
		{
			// Compute attribute with maximum information gain.
			infoGains = new double[data.numAttributes()];
			Enumeration attEnum = data.enumerateAttributes();
			while (attEnum.hasMoreElements())
			{
				Attribute att = (Attribute) attEnum.nextElement();
				infoGains[att.index()] = computeInfoGain(data, att);
			}
			m_Attribute = data.attribute(Utils.maxIndex(infoGains));
			if (subsetFeature != null)
				subsetFeature.attr = m_Attribute;
		}

		// Make leaf if information gain is zero.
		// Otherwise create successors.
		if (data.numInstances() <= minNumObj || Utils.eq(infoGains[m_Attribute.index()], 0))
		{
			m_Attribute = null;
			m_Distribution = new double[data.numClasses()];
			Enumeration instEnum = data.enumerateInstances();
			while (instEnum.hasMoreElements())
			{
				Instance inst = (Instance) instEnum.nextElement();
				m_Distribution[(int) inst.classValue()]++;
			}
			Utils.normalize(m_Distribution);
			m_ClassValue = Utils.maxIndex(m_Distribution);
			m_ClassAttribute = data.classAttribute();
		}
		else
		{
			if (!DBG_OUT && INFO_OUT && m_Attribute.isString())
			{
				DBG_OUT = true;
				computeInfoGain(data, m_Attribute);
				DBG_OUT = false;
			}
			if (INFO_OUT)
				Status.INFO.println("best attribute: " + m_Attribute + " " + infoGains[m_Attribute.index()]);
			Instances[] splitData = splitData(data, m_Attribute);
			m_Successors = new SubsetId3[splitData.length];
			if (INFO_OUT)
			{
				if (m_Attribute.isNominal())
					Status.INFO.println("splitting data into : " + splitData[0].size() + " and " + splitData[1].size());
				else if (m_Attribute.isString())
					Status.INFO.println("splitting data into occurs: " + splitData[1].size() + ", !occurs: " + splitData[0].size() + " "
							+ subsetFeature);
			}

			for (int j = 0; j < splitData.length; j++)
			{
				m_Successors[j] = new SubsetId3(availableSubsetFeatureTypes, ignoreSubsets);
				m_Successors[j].makeTree(splitData[j]);
			}
		}
	}

	/**
	 * Classifies a given test instance using the decision tree.
	 * 
	 * @param instance
	 *            the instance to be classified
	 * @return the classification
	 * @throws NoSupportForMissingValuesException
	 *             if instance has missing values
	 */
	public double classifyInstance(Instance instance) throws NoSupportForMissingValuesException
	{

		if (instance.hasMissingValue())
		{
			throw new NoSupportForMissingValuesException("Id3: no missing values, " + "please.");
		}
		if (m_Attribute == null)
		{

			return m_ClassValue;
		}
		else
		{
			if (m_Attribute.isNominal())
				return m_Successors[(int) instance.value(m_Attribute)].classifyInstance(instance);
			else if (m_Attribute.isString())
			{
				if (subsetFeature == null)
					throw new IllegalStateException();

				return m_Successors[subsetClassify(instance, m_Attribute, subsetFeature.type, subsetFeature.distance, subsetFeature.epsilon)]
						.classifyInstance(instance);
			}
			else
				throw new IllegalStateException();
		}
	}

	/**
	 * Computes class distribution for instance using decision tree.
	 * 
	 * @param instance
	 *            the instance for which distribution is to be computed
	 * @return the class distribution for the given instance
	 * @throws NoSupportForMissingValuesException
	 *             if instance has missing values
	 */
	public double[] distributionForInstance(Instance instance) throws NoSupportForMissingValuesException
	{

		if (instance.hasMissingValue())
		{
			throw new NoSupportForMissingValuesException("Id3: no missing values, " + "please.");
		}
		if (m_Attribute == null)
		{
			// if (INFO_OUT)
			// Status.INFO.println(" --> " + m_ClassValue);
			return m_Distribution;
		}
		else
		{
			if (m_Attribute.isNominal())
				return m_Successors[(int) instance.value(m_Attribute)].distributionForInstance(instance);
			else if (m_Attribute.isString())
			{
				if (subsetFeature == null)
					throw new IllegalStateException();

				// if (INFO_OUT)
				// Status.INFO.print(" " + m_Attribute + " = " + instance.value(m_Attribute));
				return m_Successors[subsetClassify(instance, m_Attribute, subsetFeature.type, subsetFeature.distance, subsetFeature.epsilon)]
						.distributionForInstance(instance);
			}
			else
				throw new IllegalStateException();
		}
	}

	/**
	 * Prints the decision tree using the private toString method from below.
	 * 
	 * @return a textual description of the classifier
	 */
	public String toString()
	{

		if ((m_Distribution == null) && (m_Successors == null))
		{
			return "Id3: No model built yet.";
		}
		return "Id3\n\n" + toString(0);
	}

	private double infoGain(double totalEntropy, int totalSize, double[] splitEntropy, int[] splitSize)
	{
		// if (DBG_OUT)
		// {
		// Status.INFO.println("compute entropy");
		// }
		double d = totalEntropy;
		for (int j = 0; j < splitSize.length; j++)
		{
			if (splitSize[j] > 0)
			{
				double delta = ((double) splitSize[j] / (double) totalSize) * splitEntropy[j];
				if (DETAIL_OUT)
					System.out.println(delta);
				d -= delta;
			}
		}
		return d;
	}

	// private double infoGainForSplitPoint(double totalEntropy, int totalSize, Iterable<Double> actives, Iterable<Double> inactives,
	// double splitPoint)
	// {
	// int[] splitLEQ = new int[2];
	// int[] splitG = new int[2];
	//
	// for (Double d : actives)
	// {
	// if (d <= splitPoint)
	// splitLEQ[1]++;
	// else
	// splitG[1]++;
	// }
	// for (Double d : inactives)
	// {
	// if (d <= splitPoint)
	// splitLEQ[0]++;
	// else
	// splitG[0]++;
	// }
	//
	// int sumLEQ = splitLEQ[0] + splitLEQ[1];
	// int sumG = splitG[0] + splitG[1];
	//
	// double entropyLEG = computeEntropy(splitLEQ);
	// double entropyG = computeEntropy(splitG);
	//
	// int[] splitSize = { sumLEQ, sumG };
	// double[] splitEntropy = { entropyLEG, entropyG };
	//
	// double d = infoGain(totalEntropy, totalSize, splitEntropy, splitSize);
	// if (DBG_OUT)
	// Status.INFO.println("split-point: " + StringUtil.formatDouble(splitPoint, 2, 5) + ", gain: " + StringUtil.formatDouble(d, 2, 4));
	// return d;
	// }

	public static final double EPSILON[] = { 0.25, 0.75, 1.5, 3 };
	public static final double MIN_EPSILON = ArrayUtil.getMinMax(EPSILON)[0];

	private int subsetClassify(Instance ins, Attribute attr, SubsetFeatureType type, double distance, double epsilon)
	{
		if (!attr.isString())
			throw new IllegalStateException();
		String s = ins.stringValue(attr);
		if (s.length() <= 2)
		{
			if (type == SubsetFeatureType.INCLUDES)
				return 0;
			else if (type == SubsetFeatureType.EXCLUDES)
				return 1;
			else
				throw new IllegalStateException();
		}
		else if (distanceOccurs(SetDistanceCache.getSet(s), distance, epsilon))
			return 1;
		else
			return 0;
	}

	private boolean distanceOccurs(List<Double> sortedDistanceSet, double distance, double epsilon)
	{
		for (Double d : sortedDistanceSet)
			if (Math.abs(d - distance) <= epsilon)
				return true;
		return false;
	}

	// private double infoGainForDistance(double totalEntropy, int totalSize, HashMap<List<Double>, Integer> setsAndClasses, double distance)
	// {
	// int[] splitTrue = new int[2];
	// int[] splitFalse = new int[2];
	//
	// for (List<Double> d : setsAndClasses.keySet())
	// {
	// if (classify(d, distance))
	// splitTrue[setsAndClasses.get(d)]++;
	// else
	// splitFalse[setsAndClasses.get(d)]++;
	// }
	//
	// int sumTrue = splitTrue[0] + splitTrue[1];
	// int sumFalse = splitFalse[0] + splitFalse[1];
	//
	// double entropyTrue = computeEntropy(splitTrue);
	// double entropyFalse = computeEntropy(splitFalse);
	//
	// int[] splitSize = { sumTrue, sumFalse };
	// double[] splitEntropy = { entropyTrue, entropyFalse };
	//
	// double d = infoGain(totalEntropy, totalSize, splitEntropy, splitSize);
	// if (DBG_OUT)
	// Status.INFO.println("distance: " + StringUtil.formatDouble(distance, 2, 5) + ", gain: " + StringUtil.formatDouble(d, 2, 4));
	// return d;
	// }

	private SplitNumbers getSplits(Vector<List<Double>> sets, Vector<Integer> classes, double distance, double epsilon)
	{
		SplitNumbers s = new SplitNumbers();
		s.occurs = new int[2];
		s.notOccurs = new int[2];
		for (int i = 0; i < sets.size(); i++)
		{
			if (distanceOccurs(sets.get(i), distance, epsilon))
				s.occurs[classes.get(i)]++;
			else
				s.notOccurs[classes.get(i)]++;
		}
		if (DBG_OUT)
			Status.INFO.print("distance: " + StringUtil.formatDouble(distance, 2, 5) + " +- " + StringUtil.formatDouble(epsilon, 2, 5)
					+ ", occurs: " + ArrayUtil.toString(s.occurs, 2) + " !occurs: " + ArrayUtil.toString(s.notOccurs, 2) + " -> ");
		return s;
	}

	// private double getWeightedEntropySum(int[]... splits)
	// {
	// double entropySum = 0;
	// for (int[] split : splits)
	// {
	// int sum = split[0] + split[1];
	// if (sum > 0)
	// entropySum += sum * computeEntropy(split);
	// }
	// if (DBG_OUT)
	// Status.INFO.println("--> " + StringUtil.formatDouble(entropySum, 2, 5));
	// return entropySum;
	// }

	/**
	 * Computes information gain for an attribute.
	 * 
	 * @param data
	 *            the data for which info gain is to be computed
	 * @param att
	 *            the attribute
	 * @return the information gain for the given attribute and data
	 * @throws Exception
	 *             if computation fails
	 */
	private double computeInfoGain(Instances data, Attribute att) throws Exception
	{
		boolean dbgReset = false;
		if (!DBG_OUT && WATCH_PAIR != null && (WATCH_PAIR.equals(att.toString())))
		{
			DBG_OUT = true;
			dbgReset = true;
		}

		try
		{
			if (DBG_OUT)
				Status.INFO.println("\nCompute INFO-GAIN for " + att);

			if (att.isNominal())
			{
				int[] splitSize = new int[att.numValues()];
				double[] splitEntropy = new double[splitSize.length];
				Instances[] splitData = splitData(data, att);
				for (int j = 0; j < splitSize.length; j++)
				{
					splitSize[j] = splitData[j].numInstances();
					splitEntropy[j] = computeEntropy(splitData[j]);
				}
				double infoGain = infoGain(computeEntropy(data), data.numInstances(), splitEntropy, splitSize);
				if (DBG_OUT)
					Status.INFO.println("--> gain: " + infoGain + "\n");
				return infoGain;
			}
			else
			{
				if (ignoreSubsets)
					return 0;

				Vector<List<Double>> sets = new Vector<List<Double>>();
				Vector<Integer> classes = new Vector<Integer>();
				Vector<Double> distancesVector = new Vector<Double>();
				int[] missingSplit = new int[2];
				int classCounts[] = new int[2];

				for (int i = 0; i < data.numInstances(); i++)
				{
					String s = data.instance(i).stringValue(att);
					int clazz = (int) data.instance(i).classValue();
					if (s.length() > 2)
					{
						if (DBG_OUT)
							Status.INFO.println("class: " + clazz + ", set: " + s);

						List<Double> set = SetDistanceCache.getSet(s);
						sets.add(set);
						classes.add(clazz);
						classCounts[clazz]++;
						for (Double d : set)
							distancesVector.add(d);
					}
					else
						missingSplit[clazz]++;
				}
				if (classCounts[0] < subsetMinNumObj || classCounts[1] < subsetMinNumObj)
					return 0;
				else
				{
					double distances[] = ArrayUtil.toPrimitiveDoubleArray(distancesVector);
					Arrays.sort(distances);
					if (DBG_OUT)
						Status.INFO.println("all distances: " + ArrayUtil.toString(distances, true));

					Vector<Double> splitDistances = new Vector<Double>();
					for (int i = 0; i < distances.length; i++)
					{
						// skip distance if equal to previous
						if (i > 0 && Math.abs(distances[i] - distances[i - 1]) < (0.33 * MIN_EPSILON))
							continue;
						splitDistances.add(distances[i]);

						// set prev to previous distance, not equal value;
						double prev = -1;
						int index = i - 1;
						while (index >= 0 && prev == -1)
						{
							if (distances[index] != distances[i])
								prev = distances[index];
							index--;
						}
						if (prev != -1 && Math.abs(distances[i] - prev) > MIN_EPSILON)
							splitDistances.add((prev + distances[i]) / 2.0);
					}
					if (DBG_OUT)
						Status.INFO.println("plus intermadiate, minus equal distances: " + VectorUtil.toString(splitDistances, true));

					double totalEntropy = computeEntropy(data);
					int totalSize = data.numInstances();
					SubsetFeature feature = null;

					for (Double d : splitDistances)
					{
						for (Double e : EPSILON)
						{
							SplitNumbers tmpSplit = getSplits(sets, classes, d, e);

							// DETAIL_OUT = (Math.abs(d - 3.63) < 0.01 && e == 0.75);

							for (SubsetFeatureType type : availableSubsetFeatureTypes)
							{
								int[] splitCheckSize, splitAddMissing;

								if (type == SubsetFeatureType.INCLUDES)
								{
									splitCheckSize = tmpSplit.occurs;
									splitAddMissing = tmpSplit.notOccurs.clone();
								}
								else
								{ // type = SubsetFeatureType.EXCLUDES
									splitCheckSize = tmpSplit.notOccurs;
									splitAddMissing = tmpSplit.occurs.clone();
								}

								if (ArrayUtil.sum(splitCheckSize) < subsetMinNumObj)
								{
									if (DBG_OUT)
										Status.INFO.print((type == SubsetFeatureType.INCLUDES ? "incl" : "excl") + ":    n/a ");
									continue;
								}
								for (int i = 0; i < 2; i++)
									splitAddMissing[i] += missingSplit[i];
								if (DETAIL_OUT)
									System.out.println("\n" + ArrayUtil.toString(splitCheckSize) + " "
											+ ArrayUtil.toString(splitAddMissing));
								double[] entropy = { computeEntropy(splitCheckSize), computeEntropy(splitAddMissing) };
								int[] size = { ArrayUtil.sum(splitCheckSize), ArrayUtil.sum(splitAddMissing) };
								if (DETAIL_OUT)
									System.out.println(ArrayUtil.toString(entropy) + " " + ArrayUtil.toString(size));

								double tmpGain = infoGain(totalEntropy, totalSize, entropy, size);
								if (DBG_OUT)
									Status.INFO.print((type == SubsetFeatureType.INCLUDES ? "incl" : "excl") + ": "
											+ StringUtil.formatDouble(tmpGain, 3, 6) + " ");

								if (feature == null || feature.infoGain < tmpGain)
								{
									feature = new SubsetFeature();
									feature.infoGain = tmpGain;
									feature.distance = d;
									feature.epsilon = e;
									feature.type = type;
								}
							}
							if (DBG_OUT)
								System.out.println();
							if (DETAIL_OUT)
								System.exit(0);
						}
					}
					if (feature == null)
						return 0;
					else
					{
						if (subsetFeature == null || subsetFeature.infoGain < feature.infoGain)
							subsetFeature = feature;
						if (DBG_OUT)
							Status.INFO.println("--> " + feature);
						return feature.infoGain;
					}
				}
			}
		}
		finally
		{
			if (dbgReset)
				DBG_OUT = false;
		}
	}

	/**
	 * Computes the entropy of a dataset.
	 * 
	 * @param data
	 *            the data for which entropy is to be computed
	 * @return the entropy of the data's class distribution
	 * @throws Exception
	 *             if computation fails
	 */
	@SuppressWarnings("unchecked")
	private double computeEntropy(Instances data) throws Exception
	{
		int[] classCounts = new int[data.numClasses()];
		Enumeration instEnum = data.enumerateInstances();
		while (instEnum.hasMoreElements())
		{
			Instance inst = (Instance) instEnum.nextElement();
			classCounts[(int) inst.classValue()]++;
		}
		return computeEntropy(classCounts);
	}

	private double computeEntropy(int[] classCounts)
	{
		int size = 0;
		double entropy = 0;
		for (int j = 0; j < classCounts.length; j++)
		{
			size += (int) classCounts[j];
			if (classCounts[j] > 0)
			{
				entropy -= classCounts[j] * Utils.log2(classCounts[j]);
			}
		}
		entropy /= (double) size;
		return entropy + Utils.log2(size);
	}

	/**
	 * Splits a dataset according to the values of a nominal attribute.
	 * 
	 * @param data
	 *            the data which is to be split
	 * @param att
	 *            the attribute to be used for splitting
	 * @return the sets of instances produced by the split
	 */
	@SuppressWarnings("unchecked")
	private Instances[] splitData(Instances data, Attribute att)
	{
		if (att.isNominal())
		{
			Instances[] splitData = new Instances[att.numValues()];
			for (int j = 0; j < att.numValues(); j++)
			{
				splitData[j] = new Instances(data, data.numInstances());
			}
			Enumeration instEnum = data.enumerateInstances();
			while (instEnum.hasMoreElements())
			{
				Instance inst = (Instance) instEnum.nextElement();
				splitData[(int) inst.value(att)].add(inst);
			}
			for (int i = 0; i < splitData.length; i++)
			{
				splitData[i].compactify();
			}
			return splitData;
		}
		else if (att.isString())
		{
			if (subsetFeature == null)
				throw new IllegalStateException();

			Instances[] splitData = new Instances[2];
			for (int j = 0; j < splitData.length; j++)
				splitData[j] = new Instances(data, data.numInstances());
			Enumeration instEnum = data.enumerateInstances();
			while (instEnum.hasMoreElements())
			{
				Instance inst = (Instance) instEnum.nextElement();
				splitData[subsetClassify(inst, att, subsetFeature.type, subsetFeature.distance, subsetFeature.epsilon)].add(inst);
			}
			for (int i = 0; i < splitData.length; i++)
				splitData[i].compactify();
			return splitData;
		}
		else
			throw new IllegalStateException();
	}

	/**
	 * Outputs a tree at a certain level.
	 * 
	 * @param level
	 *            the level at which the tree is to be printed
	 * @return the tree as string at the given level
	 */
	private String toString(int level)
	{

		StringBuffer text = new StringBuffer();

		if (m_Attribute == null)
		{
			if (Utils.isMissingValue(m_ClassValue))
			{
				text.append(": null");
			}
			else
			{
				text.append(": " + m_ClassAttribute.value((int) m_ClassValue));
			}
		}
		else
		{
			if (m_Attribute.isNominal())
			{
				for (int j = 0; j < m_Attribute.numValues(); j++)
				{
					text.append("\n");
					for (int i = 0; i < level; i++)
					{
						text.append("|  ");
					}
					text.append(m_Attribute.name() + " = " + m_Attribute.value(j));
					text.append(m_Successors[j].toString(level + 1));
				}
			}
			else if (m_Attribute.isString())
			{
				for (int j = 0; j < 2; j++)
				{
					text.append("\n");
					for (int i = 0; i < level; i++)
					{
						text.append("|  ");
					}
					text.append(subsetFeature.toString(j));
					text.append(m_Successors[j].toString(level + 1));
				}
			}
			else
				throw new IllegalStateException();
		}
		return text.toString();
	}

	/**
	 * Adds this tree recursively to the buffer.
	 * 
	 * @param id
	 *            the unqiue id for the method
	 * @param buffer
	 *            the buffer to add the source code to
	 * @return the last ID being used
	 * @throws Exception
	 *             if something goes wrong
	 */
	protected int toSource(int id, StringBuffer buffer) throws Exception
	{
		int result;
		int i;
		int newID;
		StringBuffer[] subBuffers;

		buffer.append("\n");
		buffer.append("  protected static double node" + id + "(Object[] i) {\n");

		// leaf?
		if (m_Attribute == null)
		{
			result = id;
			if (Double.isNaN(m_ClassValue))
				buffer.append("    return Double.NaN;");
			else
				buffer.append("    return " + m_ClassValue + ";");
			if (m_ClassAttribute != null)
				buffer.append(" // " + m_ClassAttribute.value((int) m_ClassValue));
			buffer.append("\n");
			buffer.append("  }\n");
		}
		else
		{
			buffer.append("    // " + m_Attribute.name() + "\n");

			// subtree calls
			subBuffers = new StringBuffer[m_Attribute.numValues()];
			newID = id;
			for (i = 0; i < m_Attribute.numValues(); i++)
			{
				newID++;

				buffer.append("    ");
				if (i > 0)
					buffer.append("else ");
				buffer.append("if (((String) i[" + m_Attribute.index() + "]).equals(\"" + m_Attribute.value(i) + "\"))\n");
				buffer.append("      return node" + newID + "(i);\n");

				subBuffers[i] = new StringBuffer();
				newID = m_Successors[i].toSource(newID, subBuffers[i]);
			}
			buffer.append("    else\n");
			buffer.append("      throw new IllegalArgumentException(\"Value '\" + i[" + m_Attribute.index()
					+ "] + \"' is not allowed!\");\n");
			buffer.append("  }\n");

			// output subtree code
			for (i = 0; i < m_Attribute.numValues(); i++)
			{
				buffer.append(subBuffers[i].toString());
			}
			subBuffers = null;

			result = newID;
		}

		return result;
	}

	/**
	 * Returns a string that describes the classifier as source. The classifier will be contained in a class with the given name (there may be
	 * auxiliary classes), and will contain a method with the signature:
	 * 
	 * <pre>
	 * <code>
	 * public static double classify(Object[] i);
	 * </code>
	 * </pre>
	 * 
	 * where the array <code>i</code> contains elements that are either Double, String, with missing values represented as null. The generated
	 * code is public domain and comes with no warranty. <br/>
	 * Note: works only if class attribute is the last attribute in the dataset.
	 * 
	 * @param className
	 *            the name that should be given to the source class.
	 * @return the object source described by a string
	 * @throws Exception
	 *             if the souce can't be computed
	 */
	public String toSource(String className) throws Exception
	{
		StringBuffer result;
		int id;

		result = new StringBuffer();

		result.append("class " + className + " {\n");
		result.append("  public static double classify(Object[] i) {\n");
		id = 0;
		result.append("    return node" + id + "(i);\n");
		result.append("  }\n");
		toSource(id, result);
		result.append("}\n");

		return result.toString();
	}

	/**
	 * Returns the revision string.
	 * 
	 * @return the revision
	 */
	public String getRevision()
	{
		return RevisionUtils.extract("$Revision: 5987 $");
	}

	/**
	 * Main method.
	 * 
	 * @param args
	 *            the options for the classifier
	 */
	public static void main(String[] args)
	{
		runClassifier(new Id3(), args);
	}

	public int getSize()
	{
		if (m_Attribute == null)
			return 1;

		int sum = 0;
		for (SubsetId3 son : m_Successors)
			sum += son.getSize();
		return sum + 1;
	}

	public int getDepth()
	{
		if (m_Attribute == null)
			return 0;

		int max = 0;
		for (SubsetId3 son : m_Successors)
		{
			int d = son.getDepth();
			if (max < d)
				max = d;
		}
		return max + 1;
	}

	public int getLeaves()
	{
		if (m_Attribute == null)
			return 1;

		int sum = 0;
		for (SubsetId3 son : m_Successors)
			sum += son.getLeaves();
		return sum;
	}

	public int getBinary()
	{
		if (m_Attribute == null)
			return 0;

		int sum = 0;
		for (SubsetId3 son : m_Successors)
			sum += son.getBinary();
		return sum + (subsetFeature == null ? 1 : 0);
	}

	public int getIncludes()
	{
		if (m_Attribute == null)
			return 0;

		int sum = 0;
		for (SubsetId3 son : m_Successors)
			sum += son.getIncludes();
		return sum + (subsetFeature == null ? 0 : (subsetFeature.type == SubsetFeatureType.INCLUDES ? 1 : 0));
	}

	public int getExcludes()
	{
		if (m_Attribute == null)
			return 0;

		int sum = 0;
		for (SubsetId3 son : m_Successors)
			sum += son.getExcludes();
		return sum + (subsetFeature == null ? 0 : (subsetFeature.type == SubsetFeatureType.EXCLUDES ? 1 : 0));
	}

	@Override
	public String getInfo()
	{
		String s = "d:" + getDepth() + " ";
		s += "s:" + getSize() + " ";
		s += "l:" + getLeaves() + " + b:" + getBinary() + " + i:" + getIncludes() + " + e:" + getExcludes();
		return s;
	}

}
