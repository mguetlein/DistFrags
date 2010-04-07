package gui;

import freechart.HistogramPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import util.StringUtil;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import data.DistancePairData;
import data.MoleculeActivityData;
import data.util.DistancePairSplitPoints;

public class DistancePairDataBrowser extends JFrame
{
	MoleculeActivityData d;

	DistancePairData f;

	DistancePairSplitPoints split;

	JPanel chartContainer = new JPanel();

	int currentPair = 0;

	public DistancePairDataBrowser(MoleculeActivityData d, DistancePairData f)
	{
		super(d.getDatasetName());

		this.d = d;
		this.f = f;

		split = new DistancePairSplitPoints(f, d);

		chartContainer = new JPanel(new BorderLayout());
		getContentPane().add(chartContainer);

		update();

		pack();
		setSize(new Dimension(Math.min(1000, getWidth()), getHeight()));
		setLocationRelativeTo(null);
		setVisible(true);

		addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_ENTER
						|| e.getKeyCode() == KeyEvent.VK_SPACE)
				{
					if (currentPair < DistancePairDataBrowser.this.f.getNumDistancePairs() - 1)
					{
						currentPair++;
						update();
					}
				}
				else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
				{
					if (currentPair > 0)
					{
						currentPair--;
						update();
					}
				}
				else if (e.getKeyCode() == KeyEvent.VK_END)
				{
					if (currentPair != DistancePairDataBrowser.this.f.getNumDistancePairs() - 1)
					{
						currentPair = DistancePairDataBrowser.this.f.getNumDistancePairs() - 1;
						update();
					}
				}
				else if (e.getKeyCode() == KeyEvent.VK_HOME)
				{
					if (currentPair != 0)
					{
						currentPair = 0;
						update();
					}
				}

			}
		});

		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void update()
	{
		chartContainer.removeAll();

		List<String> subtitle = new ArrayList<String>();
		subtitle.add("distance pair: " + (currentPair + 1) + "/" + f.getNumDistancePairs());

		double fstats = f.getFStatistic(d, currentPair);
		subtitle.add("f-statistic: " + StringUtil.formatDouble(fstats, 5));

		double ttest = f.getTTest(d, currentPair);
		subtitle.add("t-test: " + StringUtil.formatDouble(ttest, 5));

		double kolmo = f.getKolmogorovSmirnovTest(d, currentPair);
		subtitle.add("kolmogorov: " + StringUtil.formatDouble(kolmo, 5));
		// Status.INFO.println(fstats);

		List<String> caption = new ArrayList<String>();
		caption.add("actives");
		caption.add("inactives");

		HistogramPanel p = new HistogramPanel(f.getDistancePairName(currentPair), subtitle, "Distance in bonds",
				"Num occurences", caption, f.getActivityDistancesForDistancePair(d, currentPair), 15);

		DefaultFormBuilder b = new DefaultFormBuilder(new FormLayout("p"));
		String splitpoint = "splitpoint: <= " + StringUtil.formatDouble(split.getSplitPoint(currentPair)) + ", entropy: "
				+ StringUtil.formatDouble(split.getEntropy(currentPair));
		b.append(splitpoint);
		b.append(new JLabel(split.getVisualizationString(currentPair)));
		b.setBackground(Color.WHITE);
		b.setBorder(new EmptyBorder(5, 5, 5, 5));

		chartContainer.add(p);
		chartContainer.add(b.getPanel(), BorderLayout.SOUTH);
		chartContainer.revalidate();
	}
}
