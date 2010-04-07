package eval.xval;

import io.DataFileManager;
import io.Status;
import io.arff.ArffWriter;
import io.arff.FragmentDataToArff;

import java.io.File;

import data.FragmentMoleculeData;
import data.factories.FragmentFactory;
import filter.FragmentFilter;

public class FragmentCrossValidatable extends AbstractCrossValidatable
{
	FragmentFilter filter;

	public FragmentCrossValidatable(String fragmentType)
	{
		this(fragmentType, null);
	}

	public FragmentCrossValidatable(String fragmentType, FragmentFilter filter)
	{
		super(fragmentType);
		if (filter != null)
		{
			this.filter = filter;
			fragmentName = DataFileManager.getFragmentNameFiltered(fragmentName, filter);
		}
	}

	@Override
	public File getTestArffFile(int fold)
	{
		File file = DataFileManager.getArffFile(getTestDatasetName(fold), getFeatureArffName());
		if (!file.exists())
		{
			Status.INFO.println(Status.INDENT + "Creating test arff file: " + file.getName());
			Status.addIndent();
			ArffWriter.writeToArffFile(file, new FragmentDataToArff(getTestData(fold), getTestFragments(fold)));
			Status.remIndent();
		}
		return file;
	}

	@Override
	public File getTrainingArffFile(int fold)
	{
		File file = DataFileManager.getArffFile(getTrainingDatasetName(fold), getFeatureArffName());
		if (!file.exists())
		{
			Status.INFO.println(Status.INDENT + "Creating training arff file: " + file.getName());
			Status.addIndent();
			ArffWriter.writeToArffFile(file, new FragmentDataToArff(getTrainingData(fold), getTrainingFragments(fold)));
			Status.remIndent();
		}
		return file;
	}

	@Override
	protected FragmentMoleculeData getTrainingFragments(int fold)
	{
		if (trainFragments[fold] == null)
		{
			trainFragments[fold] = super.getTrainingFragments(fold);
			if (filter != null)
				trainFragments[fold] = FragmentFactory.applyFilter(trainFragments[fold], getTrainingData(fold), filter);
		}
		return trainFragments[fold];
	}

}
