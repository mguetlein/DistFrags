package eval;

import java.io.File;

public interface WekaEvaluation
{
	public void evalTrainTest(String datasetName, File arffFileTrain, File arffFileTest);

	public void adjustClassifiersToDataset(String datasetName);
}
