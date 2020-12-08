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

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Class for testing {@link ALDVersionProviderFactory}.
 * 
 * @author moeller
 */
public class TestALDVersionProviderFactory {

	/**
	 * Fixture.
	 */
	@Before
	public void init() {
		// nothing to do here for the moment
	}
	
	/**
	 * Test if factory works correctly.
	 * <p>
	 * The test is run by doing system calls to this class' main method. This
	 * is necessary because only then the enviroment variables for the process
	 * can be set to specific values.
	 */
	@Test
	public void testVersionProviderFactory() {
		
		// extract current class path and runtime environment
		String strClassPath = System.getProperty("java.class.path");
		// make sure that corresponding java executable is used
		String javaHome = System.getProperty("java.home");
		javaHome = javaHome.substring(0,javaHome.lastIndexOf(File.separator)+1);
		String javaExec = javaHome + "bin" + File.separator + "java";
		
		// define java command to test and build process
		String[] myCmd = new String[]{
			javaExec, "-cp", strClassPath,
			"de.unihalle.informatik.Alida.version.TestALDVersionProviderFactory",
			"null"};
		ProcessBuilder pb = new ProcessBuilder(myCmd);
		pb.directory(new File("."));
		Map<String, String> env = pb.environment();

		// start testing...
		boolean thrown = false;
		try {
			int i=0;
			String line;
			BufferedReader input;
			Process p;
			String [] results = new String[3];
			String[] expected = new String[]{	"false", "null", 
			 	"de.unihalle.informatik.Alida.version." 
			 			+ "ALDVersionProviderReleaseJar"};

			// if nothing is set, class name is null, but dummy is returned
			p = pb.start();
			input = new BufferedReader(new InputStreamReader(	p.getInputStream()));
			i = 0;
			while ((line = input.readLine()) != null) {
				results[i] = line;
				++i;
			}
		  input.close();
		  this.checkResult(results, expected);
		  
			// explicitly requesting dummy
		  env.put("ALIDA_VERSIONPROVIDER_CLASS", 	
		  	"de.unihalle.informatik.Alida.version.ALDVersionProviderDummy");
			expected = new String[]{	"true", 
				"de.unihalle.informatik.Alida.version.ALDVersionProviderDummy", 
				"de.unihalle.informatik.Alida.version.ALDVersionProviderDummy"};
			p = pb.start();
			input = new BufferedReader(new InputStreamReader(	p.getInputStream()));
			i = 0;
			while ((line = input.readLine()) != null) {
				results[i] = line;
				++i;
			}
		  input.close();
		  this.checkResult(results, expected);
		  
			// set non-existing class, dummy should be returned
		  env.put("ALIDA_VERSIONPROVIDER_CLASS", 	
		  	"de.unihalle.informatik.Alida.version.ALDVersionProviderNonExisting");
			expected = new String[]{	"true", 
				"de.unihalle.informatik.Alida.version.ALDVersionProviderNonExisting", 
				"de.unihalle.informatik.Alida.version.ALDVersionProviderReleaseJar"};
			p = pb.start();
			input = new BufferedReader(new InputStreamReader(	p.getInputStream()));
			i = 0;
			while ((line = input.readLine()) != null) {
				results[i] = line;
				++i;
			}
		  input.close();
		  this.checkResult(results, expected);

			// unset environment and explicitly request release jar provider
			expected = new String[]{	"true",
		  	"de.unihalle.informatik.Alida.version.ALDVersionProviderReleaseJar",
				"de.unihalle.informatik.Alida.version.ALDVersionProviderReleaseJar"};
			myCmd = new String[]{
					javaExec, "-cp", strClassPath,
					"de.unihalle.informatik.Alida.version.TestALDVersionProviderFactory",
					"de.unihalle.informatik.Alida.version.ALDVersionProviderReleaseJar"};
			pb = new ProcessBuilder(myCmd);
			pb.directory(new File("."));
			env = pb.environment();
		  env.remove("ALIDA_VERSIONPROVIDER_CLASS");
			p = pb.start();
			input = new BufferedReader(new InputStreamReader(	p.getInputStream()));
			i = 0;
			while ((line = input.readLine()) != null) {
				results[i] = line;
				++i;
			}
			input.close();
			this.checkResult(results, expected);
		  
			// request release jar provider explicitly, but overwrite in environment
			expected = new String[]{	"true", 
					"de.unihalle.informatik.Alida.version.ALDVersionProviderGit", 
					"de.unihalle.informatik.Alida.version.ALDVersionProviderGit"};
			myCmd = new String[]{
					javaExec, "-cp", strClassPath,
					"de.unihalle.informatik.Alida.version.TestALDVersionProviderFactory",
					"de.unihalle.informatik.Alida.version.ALDVersionProviderReleaseJar"};
			pb = new ProcessBuilder(myCmd);
			pb.directory(new File("."));
			env = pb.environment();
		  env.put("ALIDA_VERSIONPROVIDER_CLASS", 	
		  	"de.unihalle.informatik.Alida.version.ALDVersionProviderGit");
			p = pb.start();
			input = new BufferedReader(new InputStreamReader(	p.getInputStream()));
			i = 0;
			while ((line = input.readLine()) != null) {
				results[i] = line;
				++i;
			}
		  input.close();
		  this.checkResult(results, expected);
		  
		  // now unset again, i.e. environment should be used
			myCmd = new String[]{
					javaExec, "-cp", strClassPath,
					"de.unihalle.informatik.Alida.version.TestALDVersionProviderFactory",
					"null"};
			pb = new ProcessBuilder(myCmd);
			pb.directory(new File("."));
			pb.directory(new File("."));
			env = pb.environment();
		  env.put("ALIDA_VERSIONPROVIDER_CLASS", 	
		  	"de.unihalle.informatik.Alida.version.ALDVersionProviderGit");
			expected = new String[]{	"true", 
				"de.unihalle.informatik.Alida.version.ALDVersionProviderGit", 
				"de.unihalle.informatik.Alida.version.ALDVersionProviderGit"};
			p = pb.start();
			input = new BufferedReader(new InputStreamReader(	p.getInputStream()));
			i = 0;
			while ((line = input.readLine()) != null) {
				results[i] = line;
				++i;
			}
		  input.close();
		  this.checkResult(results, expected);
		} catch (IOException e) {
			thrown = true;
		}
		assertFalse("[TestALDVersionProviderFactory] I/O exception thrown!?", 
			thrown);
	}
	
	/**
	 * Helper to compare to string arrays.
	 * @param results		Resulting array.
	 * @param expected	Expecting array.
	 */
	protected void checkResult(String [] results, String [] expected) {
		for (int i=0;i<results.length;++i)
			assertTrue("Expected: " + expected[i] + " , got: " + results[i],
				expected[i].equals(results[i]));
	}
	
	/**
	 * Main function calling methods from the factory class.
	 * <p>
	 * The main method prints three lines to standard out:
	 * - true or false, depending on if the class name is specified
	 * - name of the configured provider class
	 * - type of provider object
	 * The output is redirected from the system process to a buffered reader and
	 * then examined, see test routines above.
	 * <p>
	 * Note, calling this main method independent of any test runs directly is 
	 * completely senseless!
	 * 
	 * @param args	Class to request explicitly, "null" if none.
	 */
	public static void main(String [] args) {
		if (!args[0].equals("null"))
			// set the class directly
			ALDVersionProviderFactory.setProviderClass(args[0]);
		// check if class name is available
		System.out.println(ALDVersionProviderFactory.isClassNameSpecified());
		// request class name
		System.out.println(ALDVersionProviderFactory.getClassName());
		// get provider instance
		ALDVersionProvider prov = ALDVersionProviderFactory.getProviderInstance();
		// check type of provider
		System.out.println(prov.getClass().getName());
	}
}