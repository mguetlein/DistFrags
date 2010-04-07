package data.util;

import javax.swing.JFrame;

import util.SwingUtil;
import data.MoleculeData;
import freechart.HistogramPanel;

public class MoleculeSizeViewer
{
	public MoleculeSizeViewer(MoleculeData d)
	{
		double sizes[] = new double[d.getNumMolecules()];
		for (int i = 0; i < d.getNumMolecules(); i++)
		{
			Molecule m = d.getMolecule(i);
			int size = -1;
			if (m != null)
				size = m.getAtomCount();

			sizes[i] = size;
		}

		HistogramPanel p = new HistogramPanel(d.getDatasetName() + " - Molecule Size", null, "Num Atoms", "Num molecules",
				"Sizes", sizes, 20);

		JFrame f = new JFrame(d.getDatasetName());
		f.getContentPane().add(p);
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		SwingUtil.waitWhileVisible(f);
	}
}
