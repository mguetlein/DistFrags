package eval;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.SubsetId3;
import weka.classifiers.trees.SubsetJ48;
import weka.core.IBkCombineDistances;
import weka.core.LinearNNSearchCombineDistances;
import weka.core.SetDistanceFunction;
import weka.core.TanimotoDistanceFunction;
import weka.core.old.NoNeighboursIBk;
import weka.core.old.OldStringSetDistanceFunction;
import weka.core.setdistance.CompleteLinkageDistance;
import weka.core.setdistance.HausdorffDistance;
import weka.core.setdistance.RIBLDistance;
import weka.core.setdistance.SetDistance;
import weka.core.setdistance.SingleLinkageDistance;

public class WekaClassifierFactory
{
	public static class NamedClassifier
	{
		public Classifier classifier;
		public String name;

		public NamedClassifier(Classifier classifier, String name)
		{
			this.classifier = classifier;
			if (name == null)
				this.name = classifier.getClass().getSimpleName();
			else
				this.name = name;
		}

		public NamedClassifier(Classifier classifier)
		{
			this(classifier, null);
		}
	}

	public static NamedClassifier getZeroR()
	{
		return new NamedClassifier(new ZeroR());
	}

	public static NamedClassifier getJ48()
	{
		return new NamedClassifier(new SubsetJ48());
	}

	public static NamedClassifier getId3()
	{
		return getId3("Id3-plain");
	}

	public static NamedClassifier getId3Both()
	{
		return getId3("Id3-both", SubsetId3.SubsetFeatureType.values());
	}

	public static NamedClassifier getId3Include()
	{
		return getId3("Id3-include", SubsetId3.SubsetFeatureType.INCLUDES);
	}

	public static NamedClassifier getId3Exclude()
	{
		return getId3("Id3-exclude", SubsetId3.SubsetFeatureType.EXCLUDES);
	}

	private static NamedClassifier getId3(String name, SubsetId3.SubsetFeatureType... types)
	{
		SubsetId3 id3 = new SubsetId3(types, types.length == 0);
		return new NamedClassifier(id3, name);
	}

	public static NamedClassifier getSMO()
	{
		return new NamedClassifier(new SMO());
	}

	public static NamedClassifier getIBK()
	{
		return new NamedClassifier(new IBk(5));
	}

	public static NamedClassifier getNaiveBayes()
	{
		return new NamedClassifier(new NaiveBayes());
	}

	private static NamedClassifier getIBK_combineDistances(String name, SetDistance setDistance)
	{
		IBk ibk = new IBkCombineDistances(5);
		ibk.setNearestNeighbourSearchAlgorithm(new LinearNNSearchCombineDistances(new TanimotoDistanceFunction(), new SetDistanceFunction(
				setDistance)));
		return new NamedClassifier(ibk, name);
	}

	public static NamedClassifier getIBK_singleLinkage()
	{
		return getIBK_combineDistances("IBk-singleLinkage", new SingleLinkageDistance());
	}

	public static NamedClassifier getIBK_completeLinkage()
	{
		return getIBK_combineDistances("IBk-completeLinkage", new CompleteLinkageDistance());
	}

	public static NamedClassifier getIBK_hausdoffDistance()
	{
		return getIBK_combineDistances("IBk-hausdorffDistance", new HausdorffDistance());
	}

	public static NamedClassifier getIBK_RIBL()
	{
		return getIBK_combineDistances("IBk-RIBL", new RIBLDistance());
	}

	public static NamedClassifier getIBK_tanimoto()
	{
		IBk ibk = new IBkCombineDistances(5);
		ibk.setNearestNeighbourSearchAlgorithm(new LinearNNSearchCombineDistances(new TanimotoDistanceFunction()));
		return new NamedClassifier(ibk, "IBk-tanimoto");
	}

	/**
	 * @deprecated
	 */
	private static NamedClassifier getSetValuedIBK(String name, SetDistance distance)
	{
		IBk knn = new NoNeighboursIBk();
		knn.setKNN(5);
		try
		{
			knn.getNearestNeighbourSearchAlgorithm().setDistanceFunction(new OldStringSetDistanceFunction(distance));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return new NamedClassifier(knn, name);
	}

}
