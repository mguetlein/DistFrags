package eval;

import io.DataFileManager;
import datamining.ReplaceValueResultSetFilter;

public class DatasetNiceNameResultSetFilter extends ReplaceValueResultSetFilter
{
	public DatasetNiceNameResultSetFilter(String datasetNames[])
	{
		for (int i = 0; i < datasetNames.length; i++)
		{
			String niceName = DataFileManager.getNiceDatasetName(datasetNames[i]);
			if (!niceName.equals(datasetNames[i]))
				this
						.replace(ResultHandler.getPropertyString(ResultHandler.PROPERTY_DATASET_NAME), datasetNames[i],
								niceName);
		}
	}
}
