package eval;

import io.Status;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import launch.DatasetSizeSettings;
import launch.Settings;
import util.StopWatchUtil;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.InformativeClassifier;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.SubsetId3;
import weka.core.FastVector;
import weka.core.IBkDebbuger;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.old.KnnDebug;
import eval.WekaClassifierFactory.NamedClassifier;

public class DefaultWekaEvaluation implements WekaEvaluation
{
	public static int DEBUG_NUM_TEST_INSTANCES;

	protected List<WekaClassifierFactory.NamedClassifier> classifiers;

	public DefaultWekaEvaluation()
	{
		classifiers = Arrays.asList(Settings.CLASSIFIERS);
	}

	@Override
	public void adjustClassifiersToDataset(String datasetBaseName)
	{
		DatasetSizeSettings.setCurrentDatasetSize(datasetBaseName);

		for (NamedClassifier c : classifiers)
		{
			if (c.classifier instanceof J48)
			{
				J48 j48 = (J48) c.classifier;
				j48.setUnpruned(DatasetSizeSettings.J48_UNPRUNED);
				j48.setMinNumObj(DatasetSizeSettings.J48_MIN_NUM_OBJECTS);
			}
		}
	}

	@Override
	public void evalTrainTest(String datasetBaseName, File arffFileTrain, File arffFileTest)
	{
		adjustClassifiersToDataset(datasetBaseName);

		try
		{
			Status.INFO.println(Status.INDENT + "Evaluation:");
			Status.addIndent();

			Status.INFO.println(Status.INDENT + "Reading training arff file: " + arffFileTrain.getName());
			Instances trainData = new Instances(new BufferedReader(new FileReader(arffFileTrain)));
			trainData.setClassIndex(trainData.numAttributes() - 1);

			Status.INFO.println(Status.INDENT + "Reading test arff file: " + arffFileTest.getName());
			Instances testData = new Instances(new BufferedReader(new FileReader(arffFileTest)));
			testData.setClassIndex(testData.numAttributes() - 1);

			Status.INFO.println(Status.INDENT + "Training: instances: " + trainData.numInstances() + ". attributes: "
					+ trainData.numAttributes());
			Status.INFO
					.println(Status.INDENT + "Test: instances: " + testData.numInstances() + ". attributes: " + testData.numAttributes());

			assert (trainData.numAttributes() == testData.numAttributes());

			// List<Evaluation> res = new ArrayList<Evaluation>();

			for (WekaClassifierFactory.NamedClassifier clazzy : classifiers)
			{
				ResultHandler.getInstance().set(ResultHandler.PROPERTY_ALGORITHM, clazzy.name);

				Evaluation eval = new Evaluation(trainData)
				{
					public double evaluateModelOnceAndRecordPrediction(Classifier classifier, Instance instance) throws Exception
					{
						KnnDebug.SinglePredictionActualClassValue = (int) instance.classValue();
						double p = super.evaluateModelOnceAndRecordPrediction(classifier, instance);
						// System.out.println("prediciton: "
						// + ((NominalPrediction)
						// m_Predictions.lastElement()).distribution()[1]);
						return p;
					}
				};

				Status.INFO.println(Status.INDENT + "Building classifier " + clazzy.name + " with training data");

				long time = StopWatchUtil.getCpuTime();

				if (Settings.DEBUG_KNN_ANALYZE)
					IBkDebbuger.INSTANCE = new IBkDebbuger(testData.size(), trainData);

				clazzy.classifier.buildClassifier(trainData);

				if (clazzy.classifier instanceof InformativeClassifier)
					ResultHandler.getInstance().set(ResultHandler.PROPERTY_INFO, ((InformativeClassifier) clazzy.classifier).getInfo());

				ResultHandler.getInstance().set(ResultHandler.PROPERTY_MODEL_BUILD_TIME, StopWatchUtil.getCpuTime() - time);
				// Status.INFO.println("done");

				if (clazzy.classifier instanceof SubsetId3)
					Status.INFO.println(clazzy.classifier.toString());

				if (Settings.DEBUG_KNN_PRINT_TESTSET_PREDICT)
					KnnDebug.TestPredictions.clear();

				Status.INFO.println(Status.INDENT + "Evaluating testdata");

				time = StopWatchUtil.getCpuTime();
				DEBUG_NUM_TEST_INSTANCES = testData.size();
				Status.addIndent();
				// StopWatchUtil.start(clazzy.name);
				eval.evaluateModel(clazzy.classifier, testData);
				// StopWatchUtil.stop(clazzy.name);
				// StopWatchUtil.print();

				if (Settings.DEBUG_KNN_ANALYZE)
					IBkDebbuger.INSTANCE.analyze();

				Status.remIndent();
				ResultHandler.getInstance().set(ResultHandler.PROPERTY_MODEL_PREDICT_TIME, StopWatchUtil.getCpuTime() - time);
				// Status.INFO.println("done");

				if (Settings.DEBUG_KNN_PRINT_TESTSET_PREDICT)
				{
					for (int i = 0; i < testData.numInstances(); i++)
					{
						Prediction p = (Prediction) eval.predictions().elementAt(i);
						KnnDebug.TestPredictions.set(i, "predicted", p.predicted());
						KnnDebug.TestPredictions.set(i, "actual", p.actual());
						KnnDebug.TestPredictions.set(i, "wrong", p.predicted() != p.actual() ? "X" : "");
						KnnDebug.TestPredictions.addTestSmiles(i);
					}
					KnnDebug.TestPredictions.print();
				}

				ResultHandler.getInstance().set(ResultHandler.PROPERTY_EVAL_ACCURACY, eval.pctCorrect() / 100.0);
				ResultHandler.getInstance().set(ResultHandler.PROPERTY_EVAL_UNCLASSIFIED, eval.unclassified());

				ResultHandler.getInstance().set(ResultHandler.PROPERTY_EVAL_SENSITIVITY,
						new Double[] { eval.truePositiveRate(0), eval.truePositiveRate(1) });
				ResultHandler.getInstance().set(ResultHandler.PROPERTY_EVAL_SPECIFITIY,
						new Double[] { eval.trueNegativeRate(0), eval.trueNegativeRate(1) });

				ResultHandler.getInstance().set(ResultHandler.PROPERTY_EVAL_AUC,
						new Double[] { eval.areaUnderROC(0), eval.areaUnderROC(1) });
				ResultHandler.getInstance().set(ResultHandler.PROPERTY_EVAL_F_MEASURE, new Double[] { eval.fMeasure(0), eval.fMeasure(1) });

				ResultHandler.getInstance().set(ResultHandler.PROPERTY_NUM_TP,
						new Double[] { eval.numTruePositives(0), eval.numTruePositives(1) });
				ResultHandler.getInstance().set(ResultHandler.PROPERTY_NUM_FP,
						new Double[] { eval.numFalsePositives(0), eval.numFalsePositives(1) });
				ResultHandler.getInstance().set(ResultHandler.PROPERTY_NUM_TN,
						new Double[] { eval.numTrueNegatives(0), eval.numTrueNegatives(1) });
				ResultHandler.getInstance().set(ResultHandler.PROPERTY_NUM_FN,
						new Double[] { eval.numFalseNegatives(0), eval.numFalseNegatives(1) });

				ResultHandler.getInstance().set(ResultHandler.PROPERTY_NUM_FEATURES, trainData.numAttributes());

				ResultHandler.getInstance().set(ResultHandler.PROPERTY_NUM_TRAIN_INSTANCES, trainData.numInstances());
				ResultHandler.getInstance().set(ResultHandler.PROPERTY_NUM_TEST_INSTANCES, testData.numInstances());

				String classValues = "";
				String predictedValues = "";
				FastVector f = eval.predictions();

				for (int i = 0; i < f.size(); i++)
				{
					NominalPrediction p = (NominalPrediction) f.elementAt(i);
					classValues += ((int) p.actual()) + ";";
					predictedValues += p.distribution()[1] + ";";
				}
				classValues = classValues.substring(0, classValues.length() - 1);
				predictedValues = predictedValues.substring(0, predictedValues.length() - 1);
				ResultHandler.getInstance().set(ResultHandler.PROPERTY_CLASS_VALUES, classValues);
				ResultHandler.getInstance().set(ResultHandler.PROPERTY_PREDICTION_VALUES, predictedValues);

				ResultHandler.getInstance().push();

				if (Settings.DEBUG_ABORT_EVALUATION_AFTER_FIRST_ALGORITHM)
					break;

				// eval.predictions();

				// AbstractStringSetDistanceFunction.showNormedDistance(clazzy.name);

				// res.add(eval);
				// eval.toSummaryString()
			}

			// StopWatchUtil.print();

			// return res;
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			Status.remIndent();
		}

		// return null;
	}
	// public static void evalDataset(File arffFile)
	// {
	// try
	// {
	// Status.INFO.println("reading arff file: " + arffFile);
	// Instances data = new Instances(new BufferedReader(new
	// FileReader(arffFile)));
	// data.setClassIndex(data.numAttributes() - 1);
	//
	// Status.INFO.println("instances: " + data.numInstances() +
	// ". attributes: " + data.numAttributes());
	//
	// // J48 clazzy = new J48();
	// // clazzy.setUnpruned(true);
	//
	// // NaiveBayes clazzy = new NaiveBayes();
	// SMO clazzy = new SMO();
	//
	// Evaluation eval = new Evaluation(data);
	//
	// // Status.INFO.print("building classifier " +
	// clazzy.getClass().getSimpleName() + " ... ");
	// // clazzy.buildClassifier(data);
	// // Status.INFO.println("done");
	// // eval.evaluateModel(clazzy, data);
	//
	// Status.INFO.println("CV with classifier " +
	// clazzy.getClass().getSimpleName());
	// eval.crossValidateModel(clazzy, data, 3, new Random(1));
	//
	// Status.INFO.println(eval.toSummaryString());
	//
	// }
	// catch (FileNotFoundException e)
	// {
	// e.printStackTrace();
	// }
	// catch (IOException e)
	// {
	// e.printStackTrace();
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// }

}
