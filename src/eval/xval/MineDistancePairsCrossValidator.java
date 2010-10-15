package eval.xval;

import util.SwingUtil;
import data.DistancePairData;
import data.FragmentMoleculeData;
import data.MoleculeActivityData;
import data.factories.DistancePairFactory;
import data.factories.FragmentFactory;
import data.util.CrossValidationData;
import eval.ResultHandler;
import filter.DistancePairFilter;
import gui.DistancePairDataBrowser;

public class MineDistancePairsCrossValidator extends AbstractCrossValidator
{
	CrossValidationData data;

	String fragmentType;

	boolean browsePairs;

	DistancePairFilter filter;

	public MineDistancePairsCrossValidator(String[] datasets, String fragmentType, DistancePairFilter filter,
			boolean browsePairs)
	{
		super(datasets);
		this.browsePairs = browsePairs;
		this.filter = filter;
		this.fragmentType = fragmentType;
	}

	@Override
	protected void crossValidateFold(int fold)
	{
		MoleculeActivityData train = data.getMoleculeActivityData(fold, false);
		FragmentMoleculeData frag = FragmentFactory.mineFragments(fragmentType, train);

		DistancePairData pairsOfInterest;

		if (filter == null)
			pairsOfInterest = DistancePairFactory.mineDistancePairs(train, frag);
		else
			pairsOfInterest = DistancePairFactory.applyFilter_minePairs(train, frag, filter);

		ResultHandler.getInstance().setDistances(train, pairsOfInterest, fold);
		if (browsePairs)
		{
			DistancePairDataBrowser browser = new DistancePairDataBrowser(train, pairsOfInterest);
			SwingUtil.waitWhileVisible(browser);
		}

	}

	@Override
	protected void storeResults()
	{}

	@Override
	protected void initializeDataset(CrossValidationData data)
	{
		this.data = data;
	}
}
