package weka.classifiers.trees;

import weka.classifiers.trees.j48.BinC45ModelSelection;
import weka.classifiers.trees.j48.C45ModelSelection;
import weka.classifiers.trees.j48.ModelSelection;
import weka.classifiers.trees.j48.PruneableClassifierTree;
import weka.classifiers.trees.j48.SubsetC45PruneableClassifierTree;
import weka.core.Capabilities;
import weka.core.Instances;

public class SubsetJ48 extends J48
{
	public Capabilities getCapabilities()
	{
		Capabilities result;
		try
		{
			if (!getReducedErrorPruning())
				result = new SubsetC45PruneableClassifierTree(null, !getUnpruned(), getConfidenceFactor(), getSubtreeRaising(),
						!getSaveInstanceData(), getCollapseTree()).getCapabilities();
			else
				result = new PruneableClassifierTree(null, !getUnpruned(), getNumFolds(), !getSaveInstanceData(), getSeed())
						.getCapabilities();
		}
		catch (Exception e)
		{
			result = new Capabilities(this);
			result.disableAll();
		}
		result.setOwner(this);
		return result;
	}

	public void buildClassifier(Instances instances) throws Exception
	{

		ModelSelection modSelection;

		if (getBinarySplits())
			modSelection = new BinC45ModelSelection(getMinNumObj(), instances, getUseMDLcorrection());
		else
			modSelection = new C45ModelSelection(getMinNumObj(), instances, getUseMDLcorrection());
		if (!getReducedErrorPruning())
			m_root = new SubsetC45PruneableClassifierTree(modSelection, !getUnpruned(), getConfidenceFactor(), getSubtreeRaising(),
					!getSaveInstanceData(), getCollapseTree());
		else
			m_root = new PruneableClassifierTree(modSelection, !getUnpruned(), getNumFolds(), !getSaveInstanceData(), getSeed());
		m_root.buildClassifier(instances);
		if (getBinarySplits())
		{
			((BinC45ModelSelection) modSelection).cleanup();
		}
		else
		{
			((C45ModelSelection) modSelection).cleanup();
		}
	}
}
