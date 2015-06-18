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

/* 
 * Most recent change(s):
 * 
 * $Rev: 6237 $
 * $Date: 2012-11-14 19:10:54 +0100 (Mi, 14 Nov 2012) $
 * $Author: moeller $
 * 
 */

package de.unihalle.informatik.Alida.grappa;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.w3c.dom.Document;

import com.mxgraph.io.mxCodec;
import com.mxgraph.io.mxCodecRegistry;
import com.mxgraph.io.mxObjectCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.view.mxGraph;

import de.unihalle.informatik.Alida.exceptions.ALDWorkflowException;
import de.unihalle.informatik.Alida.operator.ALDOperatorLocation;
import de.unihalle.informatik.Alida.workflows.ALDWorkflow;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowClassEvent;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowClassEvent.ALDWorkflowClassEventType;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowStorageInfo;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowClassEventListener;

/**
 * Main frame of Grappa.
 * @author Birgit Moeller
 */

public class ALDGrappaWorkbench extends JTabbedPane 
implements ActionListener, ALDWorkflowClassEventListener, ChangeListener {

	protected ALDGrappaFrame mainFrame;

	protected JFrame renameWin;

	protected JTextField renameTextField;

	protected File lastLoadFilePath = new File(System.getProperty("user.home"));

	protected File lastSaveFilePath = new File(System.getProperty("user.home"));
	
	/**
	 * Flag to indicate if progress events are to be shown in window or not.
	 */
	private boolean showProgressEvents = true;

	/**
	 * List of workflows currently managed.
	 */
	protected LinkedList<ALDGrappaWorkbenchTab> workflows = 
			new LinkedList<ALDGrappaWorkbenchTab>();

	/**
	 * Default constructor
	 */
	public ALDGrappaWorkbench(ALDGrappaFrame frame) {
		super();
		// add ourselves as listener for change events
		this.addChangeListener(this);
		this.mainFrame = frame;
		this.initRenameWin();
		// register for load events
		ALDWorkflow.addALDWorkflowClassEventListener(this);
		// init an empty workflow on start-up
		this.initNewWorkflow();
		this.setSize(400, 400);
	}

	/**
	 * Method to turn on/off display of progress events in status field.
	 * @param flag	If true, progress event messages are displayed, otherwise not.
	 */
	public void setShowProgressEvents(boolean flag) {
		this.showProgressEvents = flag;
	}
	
	/**
	 * Method to request whether to display progress events or not.
	 * @return True, if progress event messages are to be shown.
	 */
	public boolean showProgressEvents() {
		return this.showProgressEvents;
	}

	/**
	 * Adds a new workflow (tab) to the workbench.
	 * @return	True, if addition was successful.
	 */
	public boolean addNewWorkflow() {
		return this.initNewWorkflow();
	}

	/**
	 * Returns name of currently active workflow.
	 * @return	Name of workflow.
	 */
	public String getWorkflowName() {
		String workflowName = "<no workflow present>";
		if (this.getSelectedComponent() != null)
			workflowName = 
				((ALDGrappaWorkbenchTab)this.getSelectedComponent()).
						getWorkflowTitle();
		return workflowName;
	}

	/**
	 * Rename the currently active workflow.
	 */
	public void renameWorkflow() {
		this.renameTextField.setText(this.getWorkflowName());
		this.renameWin.setVisible(true);
	}

	/**
	 * Removes the currently active workflow.
	 */
	public void removeWorkflow() {
		// we can only remove workflows if there are any...
		if (this.getSelectedComponent() != null) {
			// post system message
			this.mainFrame.postSystemStatusMessage("closing workflow <" 
					+ ((ALDGrappaWorkbenchTab)this.getSelectedComponent()).
						workflowTitle	+ ">...");
			this.remove(this.getSelectedComponent());
		}
	}

	/**
	 * Loads a new workflow from file(s).
	 */
	public void loadWorkflow() {
		final File file = showLoadFileSelectDialog();
		if (file == null || !file.exists()) {
			String name = "";
			if (file != null) {
				name = "\"" + file.getAbsolutePath() + "\"";
			}
			JOptionPane.showMessageDialog(this.mainFrame, 
				"The selected file " + name + " does not exist!", 
				"Error loading workflow",	JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			ALDWorkflow.load(file, true);
		} catch (ALDWorkflowException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//			try {
		//				Thread tSave = new Thread() {
		//					@Override
		//          public void run() {
		//						saveWorkflow(file);
		//					}
		//				};
		//				tSave.start();
		//				tSave.join();
		//				JOptionPane.showMessageDialog(null, "Save successful",
		//						"Message", JOptionPane.OK_CANCEL_OPTION);
		//			} catch (InterruptedException e1) {
		//				// TODO Auto-generated catch block
		//				e1.printStackTrace();
		//			}
	}

	/**
	 * Saves the currently active workflow to file(s).
	 */
	public void saveWorkflow() {
		final File file = showSaveFileSelectDialog();
		if (file != null) {
			// post system message
			this.mainFrame.postSystemStatusMessage("saving workflow <" 
				+ ((ALDGrappaWorkbenchTab)this.getSelectedComponent()).workflowTitle 
				+ ">...");
			((ALDGrappaWorkbenchTab)this.getSelectedComponent()).saveWorkflow(file);
		}
	}

	/**
	 * Run the currently active workflow.
	 */
	public void runWorkflow() {
		((ALDGrappaWorkbenchTab)this.getSelectedComponent()).runWorkflow();
	}

	public void interruptWorkflowExecution() {
		((ALDGrappaWorkbenchTab)this.getSelectedComponent()).interruptExecution();
	}

	public void addNodeToWorkflow(ALDOperatorLocation opNamePath) {
		((ALDGrappaWorkbenchTab)this.getSelectedComponent()).
		createNewWorkflowNode(opNamePath, -1, -1);
	}

	public ALDOperatorLocation popRecentlySelectedOperatorPath() {
		return this.mainFrame.popRecentlySelectedOperatorLocation();
	}

	public void clearTreeSelection() {
		this.mainFrame.opTree.clearSelection();
	}

	/**
	 * Closes the main window after cleaning up.
	 */
	public void quit() {
		for (ALDGrappaWorkbenchTab workflow : this.workflows) {
			workflow.quit();
		}
		this.mainFrame.setVisible(false);
		this.renameWin.setVisible(false);
	}

	protected boolean initNewWorkflow() {
		// init the new workflow graph
//		mxGraph newFlow = new mxGraph();
		ALDGrappaWorkbenchGraph newFlow = new ALDGrappaWorkbenchGraph();
		return this.initNewWorkflow(newFlow);
	}

	protected boolean initNewWorkflow(ALDGrappaWorkbenchGraph graph) {
		graph.setMinimumGraphSize(new mxRectangle(0, 0, 500, 500));
		graph.setAllowDanglingEdges(false);
		graph.setCellsEditable(false);
		// instantiate the associated tab in window
		ALDGrappaWorkbenchTab newContent = this.initNewTab(graph);
		newContent.setFocusable(true);
		newContent.setToolTips(true);

		this.workflows.add(newContent);
		this.addTab(newContent.workflowTitle,newContent);
		// post system message
		this.mainFrame.postSystemStatusMessage("adding new workflow <" 
			+ newContent.workflowTitle + ">...");
		return true;

		//		newComp.getGraphControl().addMouseListener(
		//				new GraphMouseAdapter(this));
		//		newComp.getConnectionHandler().addListener(mxEvent.CONNECT,elist);
		//		newFlow.addListener(mxEvent.CONNECT_CELL, elist);
		//		{
		//     ...
		//			@Override
		//      public boolean isCellFoldable(Object cell, boolean collapse) {
		//				return false;
		//			}
		//		};
		//		EdgeConnectListener elist = new EdgeConnectListener();

		// add graph to the component and finally to the panel

		//		HIER!!!
		//		graphComponent.getConnectionHandler().mouseReleased(arg0)


	}

	protected ALDGrappaWorkbenchTab initNewTab(ALDGrappaWorkbenchGraph graph) {
		return new ALDGrappaWorkbenchTab(this,graph);		
	}

	/**
	 * Setup of the window for renaming workflows.
	 */
	protected void initRenameWin() {
		this.renameWin = new JFrame("Rename workflow");
		JPanel mainPanel = new JPanel();
		mainPanel.add(new JLabel("New workflow name:"));
		this.renameTextField = new JTextField(25);
		this.renameTextField.setColumns(15);
		this.renameTextField.setEditable(true);
		mainPanel.add(this.renameTextField);
		JButton okButton = new JButton("Ok");
		okButton.setActionCommand("rename_button_ok");
		okButton.addActionListener(this);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("rename_button_cancel");
		cancelButton.addActionListener(this);
		mainPanel.add(okButton);
		mainPanel.add(cancelButton);
		this.renameWin.add(mainPanel);
		this.renameWin.setSize(200,115);
		this.renameWin.setResizable(false);
	}

	protected void initReloadedWorkflow(mxGraph _graph, ALDWorkflow _flow) {
		
		// check how many workflow tabs are currently present, if there
		// is only a single empty (unused) one, delete that tab first
		if (    this.workflows.size() == 1
				&& !this.workflows.get(0).workflowHasNodes()
				&&    this.workflows.get(0).getWorkflowTitle() 
				   == ALDWorkflow.untitledWorkflowName) {
			this.removeWorkflow();
		}
		// instantiate the associated tab in window
		ALDGrappaWorkbenchTab newContent = 
				this.initReloadedTab(_graph, _flow);
		newContent.setFocusable(true);
		newContent.setToolTips(true);
		this.workflows.add(newContent);
		this.addTab(newContent.getWorkflowTitle(),newContent);
		// set reloaded workflow active
		this.setSelectedIndex(this.getComponentCount()-1);
	}

	protected void renameWorkflow(String newName) {
		((ALDGrappaWorkbenchTab)this.getSelectedComponent()).setWorkflowTitle(
				newName);
//		this.setTitleAt(this.getSelectedIndex(), newName);
	}

	protected ALDGrappaWorkbenchTab initReloadedTab(mxGraph _graph, 
			ALDWorkflow _flow) {
		return new ALDGrappaWorkbenchTab(this,_graph,_flow);				
	}

	/**
	 * Show file dialog to load a workflow.
	 * @return  File path, might be null.
	 */
	protected File showLoadFileSelectDialog() {
		File file = null;
		JFileChooser fileDialog = new JFileChooser();
		fileDialog.setDialogType(JFileChooser.OPEN_DIALOG);
		fileDialog.setDialogTitle("Load workflow...");
		fileDialog.setApproveButtonText("Load");
		// configure file ending
		fileDialog.setFileFilter(new FileFilter() {
			@Override
      public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(
						ALDWorkflow.workflowXMLFileExtension)
						|| f.isDirectory();
			}
			@Override
      public String getDescription() {
				return "Alida Workflow (*." + 
						ALDWorkflow.workflowXMLFileExtension + ")";
			}
		});
		fileDialog.setAcceptAllFileFilterUsed(false);
		fileDialog.setFileHidingEnabled(false);

		fileDialog.updateUI();

		fileDialog.setCurrentDirectory(this.lastLoadFilePath);
		fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);

		if (fileDialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			file = fileDialog.getSelectedFile();
			this.lastLoadFilePath = file;
		}
		return file;
	}

	/**
	 * Show file dialog to save a workflow.
	 * @return  File path, might be null.
	 */
	protected File showSaveFileSelectDialog() {
		// get name of workflow for filename default
		String title = 
			((ALDGrappaWorkbenchTab)this.getSelectedComponent()).getWorkflowTitle();
		File file = null;
		JFileChooser fileDialog = new JFileChooser();
		fileDialog.setDialogType(JFileChooser.OPEN_DIALOG);
		fileDialog.setDialogTitle("Save workflow...");
		fileDialog.setApproveButtonText("Save");
		fileDialog.setSelectedFile(new File(title + ".awf"));
		// configure file ending
		fileDialog.setFileFilter(new FileFilter() {
			@Override
      public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(
						ALDWorkflow.workflowXMLFileExtension)
						|| f.isDirectory();
			}
			@Override
      public String getDescription() {
				return "Alida Workflow (*." + 
						ALDWorkflow.workflowXMLFileExtension + ")";
			}
		});
		
		fileDialog.setFileHidingEnabled(false);
		
		fileDialog.updateUI();

		fileDialog.setCurrentDirectory(this.lastSaveFilePath);
		fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);

		if (fileDialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			file = fileDialog.getSelectedFile();
			// check if file has correct ending
			if (!(file.getAbsolutePath().endsWith(
					"." + ALDWorkflow.workflowXMLFileExtension))) {
				
				String newFilename = file.getAbsolutePath() + "." + ALDWorkflow.workflowXMLFileExtension;
				
				if ( JOptionPane.showOptionDialog(null, 
						"Filename " + " does not end with ." + ALDWorkflow.workflowXMLFileExtension +
						", use " + newFilename + " instead ?",
						"non standard extension",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
						null, null, null) == 0 ) {

					file = new File(newFilename);
				}
			}
			this.lastSaveFilePath = file;

			if (file.exists()) {
				if ( JOptionPane.showOptionDialog(null, 
						"File " + file.getAbsolutePath() + " exists, override?",
						"file exists",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
						null, null, null) != 0 ) {
					return null;
				}
			}

		}
	
		return file;
	}

	protected void handleWorkflowRenameEvent(String newName) {
		this.setTitleAt(this.getSelectedIndex(), newName);
	}

	protected void handleLoadWorkflowEvent(ALDWorkflowClassEvent event) {
		ALDWorkflowStorageInfo info = (ALDWorkflowStorageInfo)event.getId();
		ALDWorkflow alidaWorkflow = info.getWorkflow();
		String file = info.getFilename();
		file = file + ".gui";

		// post system message
		this.mainFrame.postSystemStatusMessage("loading workflow <" 
			+ alidaWorkflow.getName() + ">...");

		// read the file
		Document document;
		try {
			mxCodecRegistry.addPackage(
					"de.unihalle.informatik.Alida.grappa.ALDGrappaNodeInfo");
			mxCodecRegistry.register(new mxObjectCodec(new ALDGrappaNodeInfo()));
			mxCodecRegistry.addPackage(
					"de.unihalle.informatik.Alida.grappa.ALDGrappaNodePortInfo");
			mxCodecRegistry.register(new mxObjectCodec(new ALDGrappaNodePortInfo()));

			document = mxXmlUtils.parseXml(mxUtils.readFile(file));
			mxCodec codec = new mxCodec(document);

			// read the graph
			mxGraph newGraph = new mxGraph();
			codec.decode(document.getDocumentElement(), newGraph.getModel());

//			for (Object child : newGraph.getChildCells(newGraph.getDefaultParent())) {
//				mxCell cell = (mxCell) child;
//				String name = cell.getValue().toString();
//				Integer id = ((ALDGrappaNodeInfo)cell.getValue()).getRefID();
//				if (cell.isVertex()) {
//					System.out.println("- Loading node " + name + " with id = " + id);
//				} 
//				else {
//					System.out.println("- Loading edge " + name + " with id = " + id);
//				}
//			}
			
			// recover tab
			this.initReloadedWorkflow(newGraph, alidaWorkflow);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Loading workflow graphics failed!",
					"Workflow Load Error", JOptionPane.ERROR_MESSAGE);
			return;
		} 

		//		// set all operator configurations loaded and also register edges
		//			LinkedList<String> missingOperatorConfigurations = new LinkedList<String>();
		//			for (Object child : opGraph.getChildCells(opGraph.getDefaultParent())) {
		//				mxCell cell = (mxCell) child;
		//				if (cell.isVertex()) {
		//					String name = cell.getValue().toString();
		//					if ( operatorMap.get(name) != null ) {
		//						registerNode(cell, operatorMap.get(name));
		//					} else {
		//						// add newly instantiated (not configured) operator
		//
		//						if ( loadindConfigOk ) {
		//							missingOperatorConfigurations.add( name);
		//						}
		//						String opName = cell.getId().split("@")[0];
		//
		//						try {
		//							registerNode(cell,(ALDOperator) (Class.forName(opName).newInstance()));
		//						} catch (Exception e) {
		//							// may be show options pane
		//							JOptionPane.showMessageDialog(null, "Could not instantiate operator " + 
		//									opName,
		//									"Warning", JOptionPane.OK_CANCEL_OPTION);
		//						}
		//					}
		//
		//				} else if (cell.isEdge()) {
		//					registerEdge(cell);
		//				}
		//			}
		//						
		//			if ( ! missingOperatorConfigurations.isEmpty() ) {
		//				String missingNames = new String();
		//				for (String opName : missingOperatorConfigurations) {
		//					missingNames += "<br>" +
		//					 opName;
		//				}
		//				JOptionPane.showMessageDialog(null, "<html>Could not load configurations for operators " +
		//						missingNames + "</html>",
		//						"Warning", JOptionPane.OK_CANCEL_OPTION);
		//			}
		//			
		//			this.workFlowLoaded = true;
		//			// validate work flow to color nodes correctly
		//			LinkedList<mxCell> allNodes = 
		//					this.opGraph.getAllNodesTopologicallyOrdered();
		//			LinkedList<mxCell> processed = new LinkedList<mxCell>();
		//			for (mxCell node : allNodes) {
		//				if (!processed.contains(node)) {
		//					processed.add(node);
		//					boolean readyToRun = this.opDescrMap.get(node).
		//									getOperatorControlFrame().validateParameters(false, false);
		//					if (readyToRun)
		//						this.setToConfigured(node.getValue().toString(), true);
		//					else {
		//						LinkedList<mxCell> successors = 
		//								this.opGraph.getSubsequentNodes(node);
		//						for (mxCell sNode : successors) {
		//							this.setToConfigured(sNode.getValue().toString(), false);
		//							processed.add(sNode);
		//						}
		//					}
		//				}
		//			}
		//		} catch (FileNotFoundException e) {
		//			this.actionPerformed(new ActionEvent(this,1,"new"));
		//			JOptionPane.showMessageDialog(null, 
		//					"Cannot load operator configurations from " + 
		//							file.getAbsolutePath() + "." + opsXMLFileExtension 
		//							+": file not found!", "Error", JOptionPane.ERROR_MESSAGE);
		//		} catch (ALDOperatorException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//
		//		}
		//
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (command.equals("rename_button_ok")) {
			String newName = this.renameTextField.getText();
			this.renameWin.setVisible(false);
			String oldName = this.getWorkflowName();
			this.mainFrame.postSystemStatusMessage("renaming workflow <" 
				+ oldName + "> to <" + newName + ">...");
			this.renameWorkflow(newName);
			this.mainFrame.postWorkflowStatusMessage(newName, "ready");
		}
		else if (command.equals("rename_button_cancel")) {
			this.renameWin.setVisible(false);
		}
	}

	@Override
	public void handleALDWorkflowClassEvent(ALDWorkflowClassEvent event) {
		// extract event data
		ALDWorkflowClassEventType type = event.getEventType();

		// ignore all events except load events
		if (type.equals(
				ALDWorkflowClassEvent.ALDWorkflowClassEventType.LOAD_WORKFLOW)) {
			handleLoadWorkflowEvent(event);
		}
	}

	@Override
  public synchronized void stateChanged(ChangeEvent e) {
		// this event is triggered if the active tab changes or a tab is 
		// closed; then a status update is required
		String statusMsg = "ready";
		if (e instanceof ALDGrappaWorkflowTabChangeEvent) {
			statusMsg = ((ALDGrappaWorkflowTabChangeEvent)e).getEventMessage();
		}
		this.mainFrame.postWorkflowStatusMessage(
				this.getWorkflowName(), statusMsg);
  }
}
