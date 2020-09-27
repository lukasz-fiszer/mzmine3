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

package io.github.mzmine.modules.io.rawdataimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Detector of raw data file format
 */
public class RawDataFileTypeDetector {

  private static Logger logger = Logger.getLogger(RawDataFileTypeDetector.class.getName());

  /*
   * See "https://unidata.ucar.edu/software/netcdf/docs/netcdf_introduction.html#netcdf_format"
   */
  private static final String CDF_HEADER = "CDF";
  private static final String HDF_HEADER = "HDF";

  /*
   * mzML files with index start with <indexedmzML><mzML>tags, but files with no index contain only
   * the <mzML> tag. See
   * "http://psidev.cvs.sourceforge.net/viewvc/psidev/psi/psi-ms/mzML/schema/mzML1.1.0.xsd"
   */
  private static final String MZML_HEADER = "<mzML";

  /*
   * mzXML files with index start with <mzXML><msRun> tags, but files with no index contain only the
   * <msRun> tag. See "http://sashimi.sourceforge.net/schema_revision/mzXML_3.2/mzXML_3.2.xsd"
   */
  private static final String MZXML_HEADER = "<msRun";

  // See "http://www.psidev.info/sites/default/files/mzdata.xsd.txt"
  private static final String MZDATA_HEADER = "<mzData";

  // See "https://code.google.com/p/unfinnigan/wiki/FileHeader"
  private static final String THERMO_HEADER = String.valueOf(
      new char[] {0x01, 0xA1, 'F', 0, 'i', 0, 'n', 0, 'n', 0, 'i', 0, 'g', 0, 'a', 0, 'n', 0});

  private static final String GZIP_HEADER = String.valueOf(new char[] {0x1f, 0x8b});

  private static final String ZIP_HEADER = String.valueOf(new char[] {'P', 'K', 0x03, 0x04});

  /**
   * @return Detected file type or null if the file is not of any supported type
   */
  public static RawDataFileType detectDataFileType(File fileName) {

    if (fileName.isDirectory()) {
      // To check for Waters .raw directory, we look for _FUNC[0-9]{3}.DAT
      for (File f : fileName.listFiles()) {
        if (f.isFile() && f.getName().toUpperCase().matches("_FUNC[0-9]{3}.DAT"))
          return RawDataFileType.WATERS_RAW;
      }
      // We don't recognize any other directory type than Waters
      return null;
    }

    try {

      // Read the first 1kB of the file into a String
      InputStreamReader reader =
          new InputStreamReader(new FileInputStream(fileName), StandardCharsets.ISO_8859_1);
      char buffer[] = new char[1024];
      reader.read(buffer);
      reader.close();
      String fileHeader = new String(buffer);

      if (fileName.getName().toLowerCase().endsWith(".csv")) {
        if (fileHeader.contains(":") && fileHeader.contains("\\")
            && !fileHeader.contains("file name")) {
          logger.fine("ICP raw file detected");
          return RawDataFileType.ICPMSMS_CSV;
        }
        logger.fine("Agilent raw detected");
        return RawDataFileType.AGILENT_CSV;
      }

      if (fileHeader.startsWith(THERMO_HEADER)) {
        return RawDataFileType.THERMO_RAW;
      }

      if (fileHeader.startsWith(GZIP_HEADER)) {
        return RawDataFileType.GZIP;
      }

      if (fileHeader.startsWith(ZIP_HEADER)) {
        return RawDataFileType.ZIP;
      }

      /*
       * Remove specials (Unicode block) from header if any
       * https://en.wikipedia.org/wiki/Specials_(Unicode_block)
       */
      fileHeader = fileHeader.replaceAll("[^\\x00-\\x7F]", "");

      if (fileHeader.startsWith(CDF_HEADER) || fileHeader.startsWith(HDF_HEADER)) {

        return RawDataFileType.NETCDF;
      }

      if (fileHeader.contains(MZML_HEADER))
        return RawDataFileType.MZML;

      if (fileHeader.contains(MZDATA_HEADER))
        return RawDataFileType.MZDATA;

      if (fileHeader.contains(MZXML_HEADER))
        return RawDataFileType.MZXML;

    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;

  }

}
