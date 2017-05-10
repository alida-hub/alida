/*
 * This file is part of MiToBo, the Microscope Image Analysis Toolbox.
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
 * Fore more information on MiToBo, visit
 *
 *    http://www.informatik.uni-halle.de/mitobo/
 *
 */

package de.unihalle.informatik.Alida.version;

import java.io.*;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import de.unihalle.informatik.Alida.version.ALDVersionProvider;

/**
 * Version provider to extract version information from local Git repository.
 * <p>
 * Note that the environment variable GIT_DIR has to be set to the Git
 * repository location, i.e. the .git repository which should be accessed.
 * Please set the variable exactly to the .git directory and not just to the
 * parent directory of the .git directory.
 *
 * @author moeller
 */
public class ALDVersionProviderGit extends ALDVersionProvider {
	
	/**
	 * Version info string.
	 */
	private static String localVersion = null;
	
	/**
	 * Name of revision file.
	 */
	private static final String revFile = "revision.txt";
	
	@Override
  public String getVersion() {

		// extract version only once
		if (ALDVersionProviderGit.localVersion != null) {
			return ALDVersionProviderGit.localVersion;
		}
		return ALDVersionProviderGit.getRepositoryInfo();
	}

	/**
	 * Returns information about current commit.
	 * <p>
	 * If no git repository is found, the method checks for a file 
	 * "revision.txt" as it is present in Alida jar files. 
	 * If the file does not exist or is empty, a dummy string is returned.
	 * 
	 * @return 	Info string.
	 */
	private static String getRepositoryInfo() {

		ALDVersionProviderGit.localVersion = "Unknown";
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
    try {
    	Repository repo = null;
  		try {
  			repo = builder.readEnvironment().build();
  		} catch (IllegalArgumentException e) {
  			// problem accessing environment... fall back to default
  			if (repo != null)
  				repo.close();
  			return ALDVersionProviderGit.localVersion;
  		}
  		
  		// check if GIT_DIR is set
  		if (!builder.getGitDir().isDirectory()) {
  			if (repo != null)
  				repo.close();
  			return ALDVersionProviderGit.localVersion;
  		}
  		
  		// extract the active branch
  		String activeBranch = repo.getBranch();

  		// extract last commit 
  		Ref HEAD = repo.findRef(activeBranch);

  		// safety check if everything is alright with repository
  		if (HEAD == null) {
  			repo.close();
  			throw new IOException();
  		}
  		
  		// extract state of repository
  		String state = repo.getRepositoryState().toString(); 

  		ALDVersionProviderGit.localVersion = 
  				activeBranch + " : " + HEAD.toString() + " ( " + state + " ) ";
  		
  		// clean-up
  		repo.close();
    } catch (IOException e) {
    	// accessing the Git repository failed, search for file
    	InputStream is= null;
    	BufferedReader br= null;
    	String vLine= null;

    	// initialize file reader and extract version information
    	try { 
    		System.out.print("Searching for local revision file...");
    		is= ALDVersionProviderGit.class.getResourceAsStream("/" + 
    				ALDVersionProviderGit.revFile);
    		br= new BufferedReader(new InputStreamReader(is));
    		vLine= br.readLine();
    		if (vLine == null) {
    			System.err.println("ALDVersionProviderGit: " + 
    					"revision file is empty...!?");
    			br.close();
    			is.close();
    			return ALDVersionProviderGit.localVersion;
    		}	
    		ALDVersionProviderGit.localVersion = vLine;
    		br.close();
    		is.close();
    		return vLine;
    	}
    	catch (Exception ex) {
    		try {
    			if (br != null)
    				br.close();
    			if (is != null)
    				is.close();
				} catch (IOException eo) {
					// nothing to do here
				}
    		return ALDVersionProviderGit.localVersion;
    	}
    }
		return ALDVersionProviderGit.localVersion;
  }
}
