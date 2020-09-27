/*
 * Copyright 2006-2020 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.modules.visualization.intensityplot;

import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.datamodel.data.ModularFeatureList;
import io.github.mzmine.gui.mainwindow.MZmineTab;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StatisticalLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import io.github.mzmine.datamodel.PeakList;
import io.github.mzmine.gui.chartbasics.gui.javafx.EChartViewer;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.parametertypes.DoubleParameter;
import io.github.mzmine.parameters.parametertypes.WindowSettingsParameter;
import io.github.mzmine.util.dialogs.AxesSetupDialog;
import io.github.mzmine.util.javafx.FxIconUtil;
import io.github.mzmine.util.javafx.WindowsMenu;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 */
public class IntensityPlotTab extends MZmineTab {

  private static final Image pointsIcon = FxIconUtil.loadImageFromResources("icons/pointsicon.png");
  private static final Image linesIcon = FxIconUtil.loadImageFromResources("icons/linesicon.png");
  private static final Image axesIcon = FxIconUtil.loadImageFromResources("icons/axesicon.png");

  //private final Scene mainScene;
  private final BorderPane mainPane;

  static final Font legendFont = new Font("SansSerif", Font.PLAIN, 10);
  static final Font titleFont = new Font("SansSerif", Font.PLAIN, 11);

  private Logger logger = Logger.getLogger(this.getClass().getName());

  private IntensityPlotDataset dataset;
  private JFreeChart chart;

  private PeakList peakList;

  public IntensityPlotTab(ParameterSet parameters) {
    super("Intensity plot", true, false);

    mainPane = new BorderPane();
    //mainScene = new Scene(mainPane);

    // Use main CSS
    //mainScene.getStylesheets()
    //    .addAll(MZmineCore.getDesktop().getMainWindow().getScene().getStylesheets());
    //setScene(mainScene);

    peakList = parameters.getParameter(IntensityPlotParameters.peakList).getValue()
        .getMatchingPeakLists()[0];

    String title = "Intensity plot [" + peakList + "]";
    String xAxisLabel =
        parameters.getParameter(IntensityPlotParameters.xAxisValueSource).getValue().toString();
    String yAxisLabel =
        parameters.getParameter(IntensityPlotParameters.yAxisValueSource).getValue().toString();

    // create dataset
    dataset = new IntensityPlotDataset(parameters);

    // create new JFreeChart
    logger.finest("Creating new chart instance");
    Object xAxisValueSource =
        parameters.getParameter(IntensityPlotParameters.xAxisValueSource).getValue();
    boolean isCombo = (xAxisValueSource instanceof ParameterWrapper)
        && (!(((ParameterWrapper) xAxisValueSource).getParameter() instanceof DoubleParameter));
    if ((xAxisValueSource == IntensityPlotParameters.rawDataFilesOption) || isCombo) {

      chart = ChartFactory.createLineChart(title, xAxisLabel, yAxisLabel, dataset,
          PlotOrientation.VERTICAL, true, true, false);

      CategoryPlot plot = (CategoryPlot) chart.getPlot();

      // set renderer
      StatisticalLineAndShapeRenderer renderer = new StatisticalLineAndShapeRenderer(false, true);
      renderer.setDefaultStroke(new BasicStroke(2));
      plot.setRenderer(renderer);
      plot.setBackgroundPaint(Color.white);

      // set tooltip generator
      CategoryToolTipGenerator toolTipGenerator = new IntensityPlotTooltipGenerator();
      renderer.setDefaultToolTipGenerator(toolTipGenerator);

      CategoryAxis xAxis = plot.getDomainAxis();
      xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

    } else {

      chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset,
          PlotOrientation.VERTICAL, true, true, false);

      XYPlot plot = (XYPlot) chart.getPlot();

      XYErrorRenderer renderer = new XYErrorRenderer();
      renderer.setDefaultStroke(new BasicStroke(2));
      plot.setRenderer(renderer);
      plot.setBackgroundPaint(Color.white);

      // set tooltip generator
      XYToolTipGenerator toolTipGenerator = new IntensityPlotTooltipGenerator();
      renderer.setDefaultToolTipGenerator(toolTipGenerator);

    }

    chart.setBackgroundPaint(Color.white);

    // create chart JPanel
    EChartViewer chartPanel = new EChartViewer(chart);
    mainPane.setCenter(chartPanel);

    ToolBar toolBar = new ToolBar();
    toolBar.setOrientation(Orientation.VERTICAL);
    mainPane.setRight(toolBar);

    Button linesVisibleButton = new Button(null, new ImageView(linesIcon));
    linesVisibleButton.setTooltip(new Tooltip("Switch lines on/off"));
    linesVisibleButton.setOnAction(e -> {
      Plot plot = chart.getPlot();

      Boolean linesVisible;

      if (plot instanceof CategoryPlot) {
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) ((CategoryPlot) plot).getRenderer();
        linesVisible = renderer.getDefaultLinesVisible();
      } else {
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) ((XYPlot) plot).getRenderer();
        linesVisible = renderer.getDefaultLinesVisible();
        renderer.setDrawSeriesLineAsPath(true);
      }

      // check for null value
      if (linesVisible == null)
        linesVisible = false;

      // update the icon
      if (linesVisible) {
        linesVisibleButton.setGraphic(new ImageView(linesIcon));
      } else {
        linesVisibleButton.setGraphic(new ImageView(pointsIcon));
      }

      // switch the button
      linesVisible = !linesVisible;

      if (plot instanceof CategoryPlot) {
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) ((CategoryPlot) plot).getRenderer();
        renderer.setDefaultLinesVisible(linesVisible);
      } else {
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) ((XYPlot) plot).getRenderer();
        renderer.setDefaultLinesVisible(linesVisible);
        renderer.setDrawSeriesLineAsPath(true);
      }
    });

    if (chart.getPlot() instanceof XYPlot) {
      Button setupAxesButton = new Button(null, new ImageView(axesIcon));
      setupAxesButton.setTooltip(new Tooltip("Setup ranges for axes"));
      setupAxesButton.setOnAction(e -> {
        AxesSetupDialog dialog =
            new AxesSetupDialog(MZmineCore.getDesktop().getMainWindow(), chart.getXYPlot());
        dialog.show();
      });
      toolBar.getItems().add(setupAxesButton);
    }

    // disable maximum size (we don't want scaling)
    // chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
    // chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);

    // set title properties
    TextTitle chartTitle = chart.getTitle();
    chartTitle.setMargin(5, 0, 0, 0);
    chartTitle.setFont(titleFont);

    LegendTitle legend = chart.getLegend();
    legend.setItemFont(legendFont);
    legend.setBorder(0, 0, 0, 0);

    Plot plot = chart.getPlot();

    // set shape provider
    IntensityPlotDrawingSupplier shapeSupplier = new IntensityPlotDrawingSupplier();
    plot.setDrawingSupplier(shapeSupplier);

    // set y axis properties
    NumberAxis yAxis;
    if (plot instanceof CategoryPlot)
      yAxis = (NumberAxis) ((CategoryPlot) plot).getRangeAxis();
    else
      yAxis = (NumberAxis) ((XYPlot) plot).getRangeAxis();
    NumberFormat yAxisFormat = MZmineCore.getConfiguration().getIntensityFormat();
    if (parameters.getParameter(IntensityPlotParameters.yAxisValueSource)
        .getValue() == YAxisValueSource.RT)
      yAxisFormat = MZmineCore.getConfiguration().getRTFormat();
    yAxis.setNumberFormatOverride(yAxisFormat);

    setContent(mainPane);
    //setTitle(title);
    // setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    // setBackground(Color.white);


    //WindowsMenu.addWindowsMenu(mainScene);

    // pack();

    // get the window settings parameter
    //ParameterSet paramSet =
    //    MZmineCore.getConfiguration().getModuleParameters(IntensityPlotModule.class);
    //WindowSettingsParameter settings =
    //    paramSet.getParameter(IntensityPlotParameters.windowSettings);

    // update the window and listen for changes
    // settings.applySettingsToWindow(this);
    // this.addComponentListener(settings);

  }

  JFreeChart getChart() {
    return chart;
  }

  @Nonnull
  @Override
  public Collection<? extends RawDataFile> getRawDataFiles() {
    return peakList.getRawDataFiles();
  }

  @Nonnull
  @Override
  public Collection<? extends ModularFeatureList> getFeatureLists() {
    return new ArrayList<>(Collections.singletonList((ModularFeatureList)peakList));
  }

  @Nonnull
  @Override
  public Collection<? extends ModularFeatureList> getAlignedFeatureLists() {
    return Collections.emptyList();
  }

  @Override
  public void onRawDataFileSelectionChanged(Collection<? extends RawDataFile> rawDataFiles) {

  }

  @Override
  public void onFeatureListSelectionChanged(Collection<? extends ModularFeatureList> featureLists) {

  }

  @Override
  public void onAlignedFeatureListSelectionChanged(
      Collection<? extends ModularFeatureList> featurelists) {

  }
}
