package eval;

import org.apache.commons.lang.NotImplementedException;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.J48;
import weka.core.AbstractStringSetDistanceFunction;
import weka.core.NoNeighboursIBk;
import weka.core.setdistance.CompleteLinkageDistance;
import weka.core.setdistance.HausdorffDistance;
import weka.core.setdistance.RIBLDistance;
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
		return new NamedClassifier(new J48());
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

	public static NamedClassifier getIBK_singleLinkage()
	{
		return getSetValuedIBK("IBk-singleLinkage", new SingleLinkageDistance());
	}

	public static NamedClassifier getIBK_completeLinkage()
	{
		return getSetValuedIBK("IBk-completeLinkage", new CompleteLinkageDistance());
	}

	public static NamedClassifier getIBK_hausdoffDistance()
	{
		return getSetValuedIBK("IBk-hausdorffDistance", new HausdorffDistance());
	}

	public static NamedClassifier getIBK_RIBL()
	{
		return getSetValuedIBK("IBk-RIBL", new RIBLDistance());
	}

	public static NamedClassifier getIBK_tanimoto()
	{
		return getSetValuedIBK("IBk-Tanimoto", new AbstractStringSetDistanceFunction()
		{
			@Override
			public double distance(double[][] matrix)
			{
				throw new NotImplementedException("set distance not implemented, only tanimoto for nominal distance");
			}
		});
	}

	private static NamedClassifier getSetValuedIBK(String name, AbstractStringSetDistanceFunction distance)
	{
		IBk knn = new NoNeighboursIBk();
		knn.setKNN(5);
		try
		{
			knn.getNearestNeighbourSearchAlgorithm().setDistanceFunction(distance);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return new NamedClassifier(knn, name);
	}

}
