/*
 * Copyright (c) 2018 Davide Pedranz. All rights reserved.
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package peersim.utilities;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.junit.ClassRule;
import org.junit.Test;
import peersim.core.CommonState;
import peersim.junit.PeersimClassRule;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class DistributionsTest {

	// plot settings
	private static final String PLOT_FORMAT = "png";
	private static final int PLOT_WIDTH = 900;
	private static final int PLOT_HEIGHT = 600;

	// different parameters to try
	private static final int N = 100000;
	private static final long[] MEANS_POISSONT = new long[]{1, 5, 10, 100};
	private static final long[] MEANS_EXPONENTIAL = new long[]{1, 5, 10, 100, 10 * 60 * 1000};

	@ClassRule
	public static final PeersimClassRule setup = new PeersimClassRule();

	@Test
	public void samplePoissontDistribution() throws IOException {
		for (long mean : MEANS_POISSONT) {
			final double[] samples = new double[N];
			for (int i = 0; i < N; i++) {
				samples[i] = CommonState.r.nextPoisson(mean);
			}
			plot(samples, "Poissont (mean " + mean + ")", "poissont_" + mean);
		}
	}

	@Test
	public void sampleExponentialDistribution() throws IOException {
		for (long mean : MEANS_EXPONENTIAL) {
			final double[] samples = new double[N];
			for (int i = 0; i < N; i++) {
				samples[i] = Distributions.nextExponential(mean);
			}
			plot(samples, "Exponential (mean " + mean + ")", "exponential_" + mean);
		}
	}

	private static void plot(double[] samples, String title, String name) throws IOException {

		// make histogram
		final HistogramDataset histogram = new HistogramDataset();
		histogram.addSeries("key", samples, 200);

		// put it on a chart
		final JFreeChart chart = ChartFactory.createHistogram(title, "Value", "Frequency",
			histogram, PlotOrientation.VERTICAL, false, false, false);

		// prepare the image
		final BufferedImage image = new BufferedImage(PLOT_WIDTH, PLOT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION, true);
		final Rectangle r = new Rectangle(0, 0, PLOT_WIDTH, PLOT_HEIGHT);
		chart.draw(g2, r);
		final BufferedImage chartImage = chart.createBufferedImage(PLOT_WIDTH, PLOT_HEIGHT, null);

		// save as file
		final File f = new File("distributions/" + name + "." + PLOT_FORMAT);
		ImageIO.write(chartImage, PLOT_FORMAT, f);
	}
}
