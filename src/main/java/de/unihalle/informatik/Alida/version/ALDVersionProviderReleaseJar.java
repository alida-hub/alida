/*
 * This file is part of Alida, a Java library for 
 * Advanced Library for Integrated Development of Data Analysis Applications.
 *
 * Copyright (C) 2010 - @YEAR@
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Fore more information on Alida, visit
 *
 *    http://www.informatik.uni-halle.de/alida/
 *
 */

package de.unihalle.informatik.Alida.version;

import java.io.*;

import de.unihalle.informatik.Alida.version.ALDVersionProvider;

/**
 * Info class which provides Alida operators with version information  
 * from a version file distributed with the release jar archive.
 *
 * @author moeller
 */
public class ALDVersionProviderReleaseJar extends ALDVersionProvider {
	
	/**
	 * Release version or tag information.
	 */
	private static String releaseVersion = null;

	@Override
  public String getVersion() {
	  return getReleaseVersion();
  }

	/**
	 * Returns the release version/tag of the current jar archive.
	 * <p>
	 * If a file is passed to the function the tag/release information is  
	 * extracted from that file. If the file does not exist or is empty,
	 * a dummy string is returned.<br>
	 * For Alida a file named './revision.txt' is included in the jar file.
	 * 
	 * @param infofile 	File where to find the version information.
	 * @return Version or dummy string (if version file not available).
	 */
	private static String getReleaseVersion(String infofile) {

		InputStream is= null;
		BufferedReader br= null;
		String vLine= null;

		String dummy= "Unknown_Release";

		// read version file only once, should not change during one session
		if (ALDVersionProviderReleaseJar.releaseVersion != null) {
			return ALDVersionProviderReleaseJar.releaseVersion;
		}

		// initialize file reader 
		try { 
			is= ALDVersionProviderReleaseJar.class.getResourceAsStream(
						"/" + infofile);
			br= new BufferedReader(new InputStreamReader(is));
			vLine= br.readLine();
			if (vLine == null) {
				System.err.println("[ALDVersionProviderReleaseJar] " + 
						"getReleaseVersion(): Warning - version file is empty...!?");
				br.close();
				// remember version for upcoming requests
				ALDVersionProviderReleaseJar.releaseVersion = dummy;
				return dummy;
			}	
			br.close();
			ALDVersionProviderReleaseJar.releaseVersion = vLine;
			return vLine;
		}
		catch (Exception e) {
			System.err.println("[ALDVersionProviderReleaseJar] " + 
					"getReleaseVersion(): Warning - " + 
							"something went wrong on reading the version file...");
			try {
				if (br != null)
					br.close();
				if (is != null)
					is.close();
			} catch (IOException ee) {
				System.err.println(
						"[ALDVersionProviderReleaseJar::getReleaseVersion] "
							+ "problems on closing the file handles...");
				ee.printStackTrace();
			}
			ALDVersionProviderReleaseJar.releaseVersion = dummy;
			return dummy;
		}
	}

	/**
	 * Returns the tag/release version.
	 * <p>
	 * Note that the tag or release information is assumed to be found in a 
	 * file named "revision.txt" in the given jar archive.
	 * 
	 * @return Tag or release version.
	 */
	private static String getReleaseVersion() {
		ALDVersionProviderReleaseJar.releaseVersion = 
				ALDVersionProviderReleaseJar.getReleaseVersion("revision.txt");
		return ALDVersionProviderReleaseJar.releaseVersion;
	}
}
