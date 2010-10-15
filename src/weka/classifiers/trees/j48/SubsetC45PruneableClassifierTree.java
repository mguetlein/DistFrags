package weka.classifiers.trees.j48;

import weka.core.Capabilities;
import weka.core.Capabilities.Capability;

public class SubsetC45PruneableClassifierTree extends C45PruneableClassifierTree
{

	public SubsetC45PruneableClassifierTree(ModelSelection toSelectLocModel, boolean pruneTree, float cf, boolean raiseTree,
			boolean cleanup, boolean collapseTree) throws Exception
	{
		super(toSelectLocModel, pruneTree, cf, raiseTree, cleanup, collapseTree);
	}

	public Capabilities getCapabilities()
	{
		Capabilities result = super.getCapabilities();
		result.enable(Capability.STRING_ATTRIBUTES);
		return result;
	}

}
