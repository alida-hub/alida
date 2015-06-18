package de.unihalle.informatik.Alida.tools;

import java.util.Collection;
import java.util.Locale;

import javax.swing.JComponent;

import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.grappa.ALDGrappaFrame;
import de.unihalle.informatik.Alida.helpers.ALDClassInfo;
import de.unihalle.informatik.Alida.operator.ALDOperatorLocation;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowHelper;

/**
 * Main class for invoking Grappa from commandline in Alida context.
 * 
 * @author Birgit Moeller
 */
public class ALDGrappaRunner {

	/**
	 * Main function called from outside.
	 * @param args	List of commandline arguments.
	 */
	public static void main(String [] args) {
		// global language settings
		Locale.setDefault(Locale.ENGLISH);
		JComponent.setDefaultLocale(Locale.ENGLISH);
		
		// search for available operators
		Collection<ALDOperatorLocation> standardOps = 
				configureCollectionStandardOps();
		Collection<ALDOperatorLocation> applicationOps = 
				configureCollectionApplicationOps();

		// open editor window
		ALDGrappaFrame grappaWin = new ALDGrappaFrame(standardOps, applicationOps);
		grappaWin.setVisible(true);
	}
	
	/**
	 * Configure collection of operators belonging to the standard set.
	 * @return	List of standard operators.
	 */
	protected static Collection<ALDOperatorLocation> 
	configureCollectionStandardOps() {
		Collection<ALDOperatorLocation> standardOps = 
				ALDClassInfo.lookupOperators(ALDAOperator.Level.STANDARD, ALDAOperator.ExecutionMode.SWING);
		standardOps.addAll( ALDWorkflowHelper.lookupWorkflows());

		return standardOps;
	}
	
	/**
	 * Configure collection of operators belonging to the application set.
	 * @return	List of application operators.
	 */
	protected static Collection<ALDOperatorLocation> 
	
	configureCollectionApplicationOps() {
		Collection<ALDOperatorLocation> allicationOps = 
				ALDClassInfo.lookupOperators(ALDAOperator.Level.APPLICATION, ALDAOperator.ExecutionMode.SWING);
		allicationOps.addAll( ALDWorkflowHelper.lookupWorkflows());

		return allicationOps;
	}
	
}

/*BEGIN_MITOBO_ONLINE_HELP
<h1 class="title">Grappa - the Workflow Editor</h1>
<br>

<p><img src="pics/Grappa_logo.png" alt="Grappa"></p>

<p>Grappa is a graphical programming editor for Alida. It allows to 
graphically combine operators into more complex workflows. To this end 
operators are linked to nodes of a directed, acyclic graph. Each node has 
several ports corresponding to the underlying operator's input and output 
parameters. By drawing edges between ports a flow of data can be defined, 
which in the end results in a complete workflow acting on given input data 
and producing . </p>
END_MITOBO_ONLINE_HELP*/