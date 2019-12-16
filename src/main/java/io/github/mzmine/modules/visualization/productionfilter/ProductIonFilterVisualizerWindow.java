/*
 * Copyright 2006-2020 The MZmine Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MZmine 2; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.modules.visualization.productionfilter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import com.google.common.collect.Range;

import io.github.mzmine.datamodel.RawDataFile;
import io.github.mzmine.main.MZmineCore;
import io.github.mzmine.modules.visualization.spectra.simplespectra.SpectraVisualizerModule;
import io.github.mzmine.parameters.ParameterSet;
import io.github.mzmine.parameters.parametertypes.WindowSettingsParameter;
import io.github.mzmine.parameters.parametertypes.tolerances.MZTolerance;
import io.github.mzmine.taskcontrol.TaskPriority;

/**
 * Product Ion Filter generated by Shawn Hoogstra : shoogstr@uwo.ca
 */
public class ProductIonFilterVisualizerWindow extends JFrame implements ActionListener {

  private static final long serialVersionUID = 1L;
  private ProductIonFilterToolBar toolBar;
  private ProductIonFilterPlot ProductIonFilterPlot;

  private ProductIonFilterDataSet dataset;

  private RawDataFile dataFile;

  // Shawn

  // For m/z tolerance for previous filters
  private MZTolerance mzDifference;
  private List<Double> targetedMZ_List;
  private List<Double> targetedNF_List;
  private File fileName;
  private double basePeakPercent;
  // Shawn

  public ProductIonFilterVisualizerWindow(RawDataFile dataFile, ParameterSet parameters) {

    super(dataFile.getName());

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setBackground(Color.white);

    this.dataFile = dataFile;

    // Retrieve parameter's values
    Range<Double> rtRange =
        parameters.getParameter(ProductIonFilterParameters.retentionTimeRange).getValue();
    Range<Double> mzRange = parameters.getParameter(ProductIonFilterParameters.mzRange).getValue();
    Object xAxisType = parameters.getParameter(ProductIonFilterParameters.xAxisType).getValue();

    // Shawn
    // Double mzSelect =
    // parameters.getParameter(ProductIonFilterParameters.mzSelect).getValue();
    // Set product ion m/z selection cut off

    mzDifference = parameters.getParameter(ProductIonFilterParameters.mzDifference).getValue();

    targetedMZ_List =
        parameters.getParameter(ProductIonFilterParameters.targetedMZ_List).getValue();
    targetedNF_List =
        parameters.getParameter(ProductIonFilterParameters.targetedNF_List).getValue();

    fileName = parameters.getParameter(ProductIonFilterParameters.fileName).getValue();

    basePeakPercent =
        parameters.getParameter(ProductIonFilterParameters.basePeakPercent).getValue();

    // Shawn

    // Set window components
    dataset = new ProductIonFilterDataSet(dataFile, xAxisType, rtRange, mzRange, this, mzDifference,
        targetedMZ_List, targetedNF_List, basePeakPercent, fileName);

    ProductIonFilterPlot = new ProductIonFilterPlot(this, dataset, xAxisType);
    add(ProductIonFilterPlot, BorderLayout.CENTER);

    toolBar = new ProductIonFilterToolBar(this);
    add(toolBar, BorderLayout.EAST);

    MZmineCore.getTaskController().addTask(dataset, TaskPriority.HIGH);

    updateTitle();

    // Add the Windows menu
    JMenuBar menuBar = new JMenuBar();
    // menuBar.add(new WindowsMenu());
    setJMenuBar(menuBar);

    pack();

    // get the window settings parameter
    ParameterSet paramSet =
        MZmineCore.getConfiguration().getModuleParameters(ProductIonFilterVisualizerModule.class);
    WindowSettingsParameter settings =
        paramSet.getParameter(ProductIonFilterParameters.windowSettings);

    // update the window and listen for changes
    settings.applySettingsToWindow(this);
    this.addComponentListener(settings);

  }

  void updateTitle() {

    StringBuffer title = new StringBuffer();
    title.append("[");
    title.append(dataFile.getName());
    title.append("]: fragment filter");

    setTitle(title.toString());

    ProductIonFilterDataPoint pos = getCursorPosition();

    if (pos != null) {
      title.append(", ");
      title.append(pos.getName());
    }

    ProductIonFilterPlot.setTitle(title.toString());

  }

  /**
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent event) {

    String command = event.getActionCommand();

    if (command.equals("HIGHLIGHT")) {
      JDialog dialog = new ProductIonFilterSetHighlightDialog(ProductIonFilterPlot, command);
      dialog.setVisible(true);
    }

    if (command.equals("SHOW_SPECTRUM")) {
      ProductIonFilterDataPoint pos = getCursorPosition();
      if (pos != null) {
        SpectraVisualizerModule.showNewSpectrumWindow(dataFile, pos.getScanNumber());
      }
    }

  }

  public ProductIonFilterDataPoint getCursorPosition() {
    double xValue = (double) ProductIonFilterPlot.getXYPlot().getDomainCrosshairValue();
    double yValue = (double) ProductIonFilterPlot.getXYPlot().getRangeCrosshairValue();

    ProductIonFilterDataPoint point = dataset.getDataPoint(xValue, yValue);
    return point;

  }

  ProductIonFilterPlot getPlot() {
    return ProductIonFilterPlot;
  }

}
