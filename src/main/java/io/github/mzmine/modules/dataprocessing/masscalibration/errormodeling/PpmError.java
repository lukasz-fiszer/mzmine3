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

package io.github.mzmine.modules.dataprocessing.masscalibration.errormodeling;

/**
 * Ppm error type,
 * parts per million is just fractional multiplied by 10^6
 */
public class PpmError implements ErrorType {
  public double calculateError(double measured, double actual) {
    double fractionalError = (measured - actual) / actual;
    return fractionalError * 1_000_000;
  }

  public double calibrateAgainstError(double value, double error) {
//    System.out.println("calibrating " + value + " against " + error);
    double fractionalError = error / 1_000_000;
    double divide = 1 + fractionalError;
    double shifted =  value / divide;
//    System.out.println("divide " + divide + " fractional error " + fractionalError + " shifted " + shifted);
    double change = shifted - value;
    double errorValue = change / value;
    double ppmError = errorValue * 1000000;
//    System.out.println(value + " shifted to " + shifted);
//    System.out.println("change " + change + " error value " + errorValue + " ppm error " + ppmError);
    return shifted;
  }
}
