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

package de.unihalle.informatik.Alida.dataio.provider.xmlbeans;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerCmdline;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans;
//import de.unihalle.informatik.Alida.demo.AnalyzeExperiment;
import de.unihalle.informatik.Alida.demo.ExperimentalData;
import de.unihalle.informatik.Alida.demo.NormalizeExperimentalDataOp;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDWorkflowException;
import de.unihalle.informatik.Alida.workflows.ALDWorkflow;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowDataIOXmlbeans;
import de.unihalle.informatik.Alida.workflows.ALDWorkflow.ALDWorkflowContextType;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowNodeID;

/**
 * JUnit test class for {@link ALDWorkflowDataIOXmlbeans}.
 * 
 * @author posch
 */
public class TestALDWorkflowDataIOXmlbeans {

	File tmpFile;
	boolean debug = false;

	/**
	 * Fixture.
	 * @throws IOException 
	 */
	@Before
	public void initTestClass() throws IOException { 
		tmpFile = File.createTempFile("tmp", "xml");

	}
	
	/**
	 * Test if we can write and read back an example workflow
	 * @throws ALDOperatorException 
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 * @throws XmlException 
	 * @throws ALDWorkflowException 
	 */
	@Test
	public void testInitWithDefaultObject() 
			throws ALDOperatorException, ALDDataIOProviderException, 
			ALDDataIOManagerException, XmlException, ALDWorkflowException {
		
		ALDWorkflow workflow = createTestWorkflow();
		
		ALDDataIOManagerXmlbeans.writeXml(tmpFile, workflow);

		if ( debug ) {
			ALDDataIOManagerXmlbeans.writeXml("workflow.xml", workflow);
		}
		
		ALDWorkflow workflowIn = (ALDWorkflow) ALDDataIOManagerXmlbeans.readXml(tmpFile, ALDWorkflow.class);

		assertTrue("Expected an object, but got null!", workflowIn != null);
//			assertTrue("Expected object handed over, got another...", 
//										dataRead != null && dataRead.equals(this.dummyClassObj));
		if ( debug ) {
			workflowIn.print();
		}
	}
	
	private ALDWorkflow createTestWorkflow() 
			throws ALDOperatorException, ALDDataIOManagerException, 
			ALDDataIOProviderException, ALDWorkflowException {
		ALDWorkflow workflow = new ALDWorkflow( "Test work flow",  ALDWorkflowContextType.OTHER);

		ExperimentalData experiment = 
				(ExperimentalData) ALDDataIOManagerCmdline.getInstance().readData( null, ExperimentalData.class,
						"{description=test experiment,data=[[1.0,2.0,3.0],[11.1,12.2,15.55]]}");
		NormalizeExperimentalDataOp normalizeOp = new NormalizeExperimentalDataOp( experiment);
		normalizeOp.setVerbose(true);
		ALDWorkflowNodeID normId = workflow.createNode(normalizeOp);

//		AnalyzeExperiment analyzeOp = new AnalyzeExperiment();
//		ALDWorkflowNodeID analyzeId = workflow.createNode(analyzeOp);
//
////		System.out.println("XX " + normId + " " + analyzeId);
//		workflow.createEdge(normId, "result", analyzeId, "experiment");

		if ( debug ) {
			workflow.print();
		}

		return workflow;
	}
}