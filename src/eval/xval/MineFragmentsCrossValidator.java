package eval.xval;

import io.DataFileManager;
import io.FragmentIO;

import java.io.File;

import data.FragmentMoleculeData;
import data.MoleculeActivityData;
import data.factories.FragmentFactory;
import data.util.CrossValidationData;
import eval.ResultHandler;
import filter.FragmentFilter;

public class MineFragmentsCrossValidator extends AbstractCrossValidator
{
	CrossValidationData data;

	String fragmentType;

	FragmentFilter filter;

	public MineFragmentsCrossValidator(String[] datasets, String fragmentType, FragmentFilter filter)
	{
		super(datasets);
		this.fragmentType = fragmentType;
		this.filter = filter;
	}

	@Override
	protected void crossValidateFold(int fold)
	{
		MoleculeActivityData train = data.getMoleculeActivityData(fold, false);

		FragmentMoleculeData fragsOfInterest;

		if (filter == null)
			fragsOfInterest = FragmentFactory.mineFragments(fragmentType, train);
		else
		{
			String fragmentName = DataFileManager.getFragmentNameFiltered(fragmentType, filter);
			File f = DataFileManager.getLinfragFile(data.getDatasetNames(fold, false), fragmentName);
			if (f.exists())
				fragsOfInterest = FragmentIO.readFragments(fragmentName, f, train);
			else
			{
				FragmentMoleculeData frags = FragmentFactory.mineFragments(fragmentType, train);
				fragsOfInterest = FragmentFactory.applyFilter(frags, train, filter);
			}

			// FragmentFactory.applyFilter(fragsOfInterest, train, filter);
		}

		ResultHandler.getInstance().setFragments(train, fragsOfInterest, fold);
	}

	@Override
	protected void initializeDataset(CrossValidationData data)
	{
		this.data = data;
	}

	@Override
	protected void storeResults()
	{}

}
