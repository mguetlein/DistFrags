package gui;

import freechart.HistogramPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
	MoleculeActivityData molData;

	DistancePairData distPairs;

	DistancePairSplitPoints split;

	JPanel chartContainer = new JPanel();

	int currentPair = 0;

	String lastSearch = null;

	public DistancePairDataBrowser(MoleculeActivityData d, DistancePairData f)
	{
		super(d.getDatasetName());

		this.molData = d;
		this.distPairs = f;

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
				if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_SPACE)
				{
					if (currentPair < DistancePairDataBrowser.this.distPairs.getNumDistancePairs() - 1)
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
					if (currentPair != DistancePairDataBrowser.this.distPairs.getNumDistancePairs() - 1)
					{
						currentPair = DistancePairDataBrowser.this.distPairs.getNumDistancePairs() - 1;
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
				else if (e.getKeyCode() == KeyEvent.VK_F)
				{
					String search = JOptionPane.showInputDialog("Search for distfrags:", lastSearch);
					if (search != null)
					{
						search(search);
						lastSearch = search;
					}
				}
				else if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					if (lastSearch != null)
						search(lastSearch);
				}

			}
		});

		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void search(String search)
	{
		int i;
		if (currentPair < distPairs.getNumDistancePairs() - 1)
			i = currentPair + 1;
		else
			i = 0;

		boolean match = false;
		do
		{
			if (distPairs.getDistancePairName(i).matches(".*" + search + ".*"))
			{
				match = true;
				break;
			}
			if (i < distPairs.getNumDistancePairs() - 1)
				i++;
			else
				i = 0;
		}
		while (i != currentPair);

		if (match)
		{
			currentPair = i;
			update();
		}
		else
		{
			JOptionPane.showMessageDialog(this, "not found: '" + search + "'");
		}
	}

	private void update()
	{
		System.out.println(distPairs.getDistancePairName(currentPair));
		System.out.println(currentPair);
		Set<Integer> set = distPairs.getMoleculesForDistancePair(currentPair);
		for (Integer m : set)
		{
			List<Double> dis = distPairs.getDistances(currentPair, m);

			String s = "";
			for (Double double1 : dis)
				s += StringUtil.formatDouble(double1.doubleValue()) + " ";

			System.out.print(StringUtil.concatWhitespace(s, 40) + "[" + molData.getMoleculeActivity(m) + "] "
					+ molData.getMoleculeSmiles(m));

			System.out.println();
		}
		System.out.println();

		chartContainer.removeAll();

		List<String> subtitle = new ArrayList<String>();
		subtitle.add("distance pair: " + (currentPair + 1) + "/" + distPairs.getNumDistancePairs());

		double fstats = distPairs.getFStatistic(molData, currentPair);
		subtitle.add("f-statistic: " + StringUtil.formatDouble(fstats, 5));

		double ttest = distPairs.getTTest(molData, currentPair);
		subtitle.add("t-test: " + StringUtil.formatDouble(ttest, 5));

		double kolmo = distPairs.getKolmogorovSmirnovTest(molData, currentPair);
		subtitle.add("kolmogorov: " + StringUtil.formatDouble(kolmo, 5));
		// Status.INFO.println(fstats);

		List<String> caption = new ArrayList<String>();
		caption.add("actives");
		caption.add("inactives");

		HistogramPanel p = new HistogramPanel(distPairs.getDistancePairName(currentPair), subtitle, "Distance in bonds", "Num occurences",
				caption, distPairs.getActivityDistancesForDistancePair(molData, currentPair), 25);

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
