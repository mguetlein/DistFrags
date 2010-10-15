package launch;

public class DatasetSizeSettings
{
	public static boolean J48_UNPRUNED;
	public static int J48_MIN_NUM_OBJECTS;
	public static int MIN_FREQUENCY_PER_CLASS;
	public static int MIN_FREQUENCY;

	private static final String DATASET_SIZE_GIGANTIC = "dataset-gigantic";
	private static final String DATASET_SIZE_BIG = "dataset-big";
	private static final String DATASET_SIZE_MEDIUM = "dataset-medium";
	private static final String DATASET_SIZE_SMALL = "dataset-small";
	private static final String DATASET_SIZE_TINY = "dataset-tiny";

	private static String[][] DATASET_SIZES = new String[][]
	{
	{ "mouse_carcinogenicity_alt", DATASET_SIZE_MEDIUM },
	{ "multi_cell_call_alt", DATASET_SIZE_MEDIUM },
	{ "rat_carcinogenicity_alt", DATASET_SIZE_BIG },
	{ "salmonella_mutagenicity_alt", DATASET_SIZE_MEDIUM },
	{ "kierbl", DATASET_SIZE_TINY },
	{ "nctrer", DATASET_SIZE_TINY },
	{ "cyp_2C9_inhibitor", DATASET_SIZE_MEDIUM },
	{ "cyp_2D6_inhibitor", DATASET_SIZE_MEDIUM },
	{ "cyp_3A4_inhibitor", DATASET_SIZE_MEDIUM },
	{ "cyp_2C9_substrate", DATASET_SIZE_MEDIUM },
	{ "cyp_2D6_substrate", DATASET_SIZE_MEDIUM },
	{ "cyp_3A4_substrate", DATASET_SIZE_MEDIUM },
	{ "bbb_inhibitor", DATASET_SIZE_SMALL },
	{ "bbb_substrate", DATASET_SIZE_SMALL },
	{ "bbb_inducer", DATASET_SIZE_TINY } };

	public static void setCurrentDatasetSize(String datasetBaseName)
	{
		String size = DATASET_SIZE_MEDIUM;

		if (datasetBaseName != null)
		{
			size = null;
			for (int i = 0; i < DATASET_SIZES.length; i++)
				if (DATASET_SIZES[i][0].equals(datasetBaseName))
				{
					size = DATASET_SIZES[i][1];
					break;
				}
			if (size == null)
				throw new IllegalStateException("dataset size missing for dataset: " + datasetBaseName);
		}

		if (size.equals(DATASET_SIZE_GIGANTIC))
		{
			J48_UNPRUNED = true;
			J48_MIN_NUM_OBJECTS = 12;
			MIN_FREQUENCY_PER_CLASS = 5;
		}
		else if (size.equals(DATASET_SIZE_BIG))
		{
			J48_UNPRUNED = true;
			J48_MIN_NUM_OBJECTS = 8;
			MIN_FREQUENCY_PER_CLASS = 4;
		}
		else if (size.equals(DATASET_SIZE_MEDIUM))
		{
			J48_UNPRUNED = true;
			J48_MIN_NUM_OBJECTS = 4;
			MIN_FREQUENCY_PER_CLASS = 9;// 3;
		}
		else if (size.equals(DATASET_SIZE_SMALL))
		{
			J48_UNPRUNED = false;
			J48_MIN_NUM_OBJECTS = 2;
			MIN_FREQUENCY_PER_CLASS = 5;// 2;
		}
		else if (size.equals(DATASET_SIZE_TINY))
		{
			J48_UNPRUNED = false;
			J48_MIN_NUM_OBJECTS = 2;
			MIN_FREQUENCY_PER_CLASS = 3;// 1;
		}
		else
			throw new IllegalStateException("illegal dataset size");

		MIN_FREQUENCY = MIN_FREQUENCY_PER_CLASS * 2;
	}

}
