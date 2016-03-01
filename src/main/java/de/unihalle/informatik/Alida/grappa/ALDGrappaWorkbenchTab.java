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

package de.unihalle.informatik.Alida.grappa;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.BlockingDeque;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.io.mxCodec;
import com.mxgraph.io.mxCodecRegistry;
import com.mxgraph.io.mxObjectCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxXmlUtils;
import com.mxgraph.view.mxGraph;

import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing.ProviderInteractionLevel;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDOperatorParameterPanel;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOException;
import de.unihalle.informatik.Alida.exceptions.ALDException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDWorkflowException;
import de.unihalle.informatik.Alida.gui.ALDOperatorResultFrame;
import de.unihalle.informatik.Alida.gui.ALDOperatorConfigurationFrame;
import de.unihalle.informatik.Alida.operator.ALDOpParameterDescriptor;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.operator.ALDOperatorLocation;
import de.unihalle.informatik.Alida.operator.events.ALDOpParameterUpdateEvent;
import de.unihalle.informatik.Alida.operator.events.ALDOpParameterUpdateEventListener;
import de.unihalle.informatik.Alida.operator.events.ALDOpParameterUpdateEvent.EventType;
import de.unihalle.informatik.Alida.workflows.ALDWorkflow;
import de.unihalle.informatik.Alida.workflows.ALDWorkflow.ALDWorkflowContextType;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowEdge;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowID;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowEdgeID;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowNode;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowNode.ALDWorkflowNodeState;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowNodeID;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEvent;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowStorageInfo;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEvent.*;
import de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEventListener;

/**
 * Grappa workflow graph component.
 * 
 * @author Birgit Moeller
 */
public class ALDGrappaWorkbenchTab extends mxGraphComponent 
	implements ALDWorkflowEventListener {
	
	/* ******************
	 * Internal members.
	 * ******************/

	/**
	 * Slf4j logger.
	 */
	private static Logger logger = 
			LoggerFactory.getLogger(ALDOperatorParameterPanel.class);

	/**
	 * Reference to the surrounding work bench object.
	 */
	protected ALDGrappaWorkbench workBench;

	/**
	 * Reference to the underlying Alida workflow object.
	 */
	protected ALDWorkflow alidaWorkflow;

	/**
	 * Map of node cells and their corresponding Alida workflow IDs.  
	 */
	protected HashMap<mxCell, ALDWorkflowNodeID> graphNodeIDs;
	
	/**
	 * Map of Alida workflow IDs and their corresponding node cells.  
	 */
	protected HashMap<ALDWorkflowNodeID, mxCell> graphNodes;

	/**
	 * Map of edge cells and their corresponding Alida workflow IDs.  
	 */
	protected HashMap<mxCell, ALDWorkflowEdgeID> graphEdgeIDs;
	
	/**
	 * Map of Alida workflow IDs and their corresponding edge cells.  
	 */
	protected HashMap<ALDWorkflowEdgeID, mxCell> graphEdges;

	/**
	 * Map of node cells and their corresponding configuration windows.
	 */
	protected HashMap<mxCell, ALDOperatorConfigurationFrame> configWindows;

	/**
	 * Map of window configurations for parameter display.
	 * <p>
	 * This map stores for each node of the workflow the setting of the
	 * boolean option for displaying all or only the non-expert parameters.
	 * If the value of an entry is true, all parameters are to be shown,
	 * if it is false, only standard parameters are visible.
	 * <p>
	 * The values stored in this hashmap are mainly used during the 
	 * creation of menus of type {@link ContextMenuNodeEdge}.
	 */
	protected HashMap<mxCell, Boolean> nodeConfigShowAllParameters;

	/**
	 * Title of this workflow as shown on the tab.
	 */
	protected String workflowTitle;
	
	/**
	 * List of actions performed on the Alida workflow object.
	 */
	protected HashMap<ALDWorkflowID, WorkflowModifyAction> actionsOnWorkflow;

	/**
	 * Message window shown while workflow execution is aborted.
	 */
	protected JFrame interruptWin;

	/**
	 * for debug message to stdout
	 */
	protected boolean debug = false;
	
	/**
	 * Default constructor.
	 * @param bench 	Workbench to which this tab is to be attached.
	 * @param _graph	Workbench graph associated with this workflow.
	 */
	public ALDGrappaWorkbenchTab(ALDGrappaWorkbench bench,
			ALDGrappaWorkbenchGraph _graph) {
		super(_graph);
		this.actionsOnWorkflow = new HashMap<ALDWorkflowID, 
																ALDGrappaWorkbenchTab.WorkflowModifyAction>();
		this.graphNodeIDs = new HashMap<mxCell, ALDWorkflowNodeID>();
		this.graphNodes = new HashMap<ALDWorkflowNodeID, mxCell>();
		this.graphEdgeIDs = new HashMap<mxCell, ALDWorkflowEdgeID>();
		this.graphEdges = new HashMap<ALDWorkflowEdgeID, mxCell>();
		this.configWindows = 
				new HashMap<mxCell, ALDOperatorConfigurationFrame>();
		this.nodeConfigShowAllParameters = new HashMap<mxCell, Boolean>();
		this.workBench = bench;
		try {
			this.alidaWorkflow = new ALDWorkflow(ALDWorkflowContextType.GRAPPA);
			this.alidaWorkflow.addALDWorkflowEventListener(this);
		} catch (ALDOperatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.workflowTitle = this.alidaWorkflow.getName();
		this.graph.setMinimumGraphSize(new mxRectangle(0, 0, 500, 500));
		this.graph.setAllowDanglingEdges(false);
		this.graph.setCellsEditable(false);
		// add reference to workbench tab for handling tooltips
		((ALDGrappaWorkbenchGraph)this.graph).setWorkbenchTab(this);
		
		// some additional initializations
		this.initInterruptWin();

		// add event listeners
		this.addKeyListener(new GraphKeyListener());
		this.getGraphControl().addMouseListener(new GraphMouseAdapter());
		GraphEventListener gEListener = new GraphEventListener();
		this.graph.addListener(mxEvent.CONNECT_CELL, gEListener);
		this.getConnectionHandler().addListener(mxEvent.CONNECT, gEListener);
		this.getConnectionHandler().addListener(mxEvent.CONNECT_CELL, gEListener);
	}
	
	/**
	 * Constructor to setup tab from given (reloaded) workflow.
	 * @param bench 	Workbench to which this tab is to be attached.
	 * @param _graph	Workbench graph associated with this workflow.
	 * @param _flow 	Alida workflow associated with this tab.
	 */
	public ALDGrappaWorkbenchTab(ALDGrappaWorkbench bench, 
			mxGraph _graph, ALDWorkflow _flow) {
		super(_graph);
		this.actionsOnWorkflow = new HashMap<ALDWorkflowID, 
																ALDGrappaWorkbenchTab.WorkflowModifyAction>();
		this.graphNodeIDs = new HashMap<mxCell, ALDWorkflowNodeID>();
		this.graphNodes = new HashMap<ALDWorkflowNodeID, mxCell>();
		this.graphEdgeIDs = new HashMap<mxCell, ALDWorkflowEdgeID>();
		this.graphEdges = new HashMap<ALDWorkflowEdgeID, mxCell>();
		this.configWindows = 
				new HashMap<mxCell, ALDOperatorConfigurationFrame>();
		this.nodeConfigShowAllParameters = new HashMap<mxCell, Boolean>();
		this.workBench = bench;
		this.workflowTitle = _flow.getName();
		this.alidaWorkflow = _flow;
		this.alidaWorkflow.addALDWorkflowEventListener(this);

		// make sure that node ids are properly restored
		this.restoreIDs();
		
		// generate configuration windows
		this.restoreConfigWins();

		// make sure that all nodes and all wins show correct configurations
		this.updateWorkflowNodeStates();
	
		this.graph.setMinimumGraphSize(new mxRectangle(0, 0, 500, 500));
		this.graph.setAllowDanglingEdges(false);
		this.graph.setCellsEditable(false);

		// some additional initializations
		this.initInterruptWin();

		// add event listeners
		this.addKeyListener(new GraphKeyListener());
		this.getGraphControl().addMouseListener(new GraphMouseAdapter());
		GraphEventListener gEListener = new GraphEventListener();
		this.graph.addListener(mxEvent.CONNECT_CELL, gEListener);
		this.getConnectionHandler().addListener(mxEvent.CONNECT, gEListener);
		this.getConnectionHandler().addListener(mxEvent.CONNECT_CELL, gEListener);
		this.graph.refresh();
	}

	/**
	 * Setup of the window for renaming workflows.
	 */
	protected void initInterruptWin() {
		this.interruptWin = new JFrame("Workflow Message");
		JPanel mainPanel = new JPanel();
		mainPanel.add(new JLabel("Please wait, aborting execution..."));
		this.interruptWin.add(mainPanel);
		this.interruptWin.setSize(300,115);
		this.interruptWin.setResizable(false);
	}

	/**
	 * Restore maps of Alida workflow IDs and corresponding graph cells.
	 */
	protected void restoreIDs() {

		// get all the nodes of the graph
		Object [] allCells = 
				this.graph.getChildVertices(this.graph.getDefaultParent());
		LinkedList<mxCell> failedCells = new LinkedList<mxCell>();
		for (Object o : allCells) {
			if (((mxCell)o).isVertex()) {
				mxCell node = (mxCell)o;
				ALDGrappaNodeInfo nodeInfo = (ALDGrappaNodeInfo)node.getValue();
				
				if (logger.isDebugEnabled()) {
					logger.debug(" Found node: {}", nodeInfo.getNodeName());
				}
				
				ALDWorkflowNodeID nodeID = 
						this.alidaWorkflow.getNodeIdDuringLoading(
								nodeInfo.getRefID());
				
				if (logger.isDebugEnabled()) {
					logger.debug(" -> ID is = {}", nodeID.id);
				}

				if ( nodeID != null ) {
					// fill data structure
					this.graphNodes.put(nodeID, node);
					this.graphNodeIDs.put(node, nodeID);

					// edges are accessible via children
					if (logger.isDebugEnabled()) {
						logger.debug(" \t number of childs = {}", 
								new Integer(node.getChildCount()));
					}

					for (int c = 0; c<node.getChildCount(); ++c) {
						mxCell child = (mxCell)node.getChildAt(c);

						if (logger.isDebugEnabled()) {
							logger.debug(" \t -> child {} has {} edges", 
									new Integer(c), new Integer(child.getEdgeCount()));
						}

						for (int e = 0; e<child.getEdgeCount(); ++e) {
							mxCell edge = (mxCell)child.getEdgeAt(e);
							ALDGrappaNodeInfo edgeInfo = 
									(ALDGrappaNodeInfo)edge.getValue();
							ALDWorkflowEdgeID edgeID = 
									this.alidaWorkflow.getEdgeIdDuringLoading(
											edgeInfo.getRefID());
							if (     edgeID != null 
									&& !(this.graphEdges.containsKey(edgeID))) {
								// fill data structure
								this.graphEdges.put(edgeID, edge);
								this.graphEdgeIDs.put(edge, edgeID);
							}
						}
					}
				}
				// node ID is null, i.e. node has not been found in ALDWorkflow
				// (happens, e.g., if class is not present anymore)
				else {
					// store node for later problem solving
					failedCells.push(node);
				}
			}
		} // end of for-loop over all cells present in JGraphX graph
		
		// delete cells which could not be associated with nodes in ALDWorkflow
		Object[] obsoleteCells = new Object[failedCells.size()];
		Object[] edgesToRemove;

		int i=0;
		for (mxCell node: failedCells) {
			obsoleteCells[i] = node;
			++i;
			
			// check for childs (ports) and connected edges to remove them
			for (int c = 0; c<node.getChildCount(); ++c) {
				mxCell child = (mxCell)node.getChildAt(c);
				
				// array to store edges to remove
				edgesToRemove = new Object[child.getEdgeCount()];
				for (int e = 0; e<child.getEdgeCount(); ++e) {
					mxCell edge = (mxCell)child.getEdgeAt(e);
					ALDGrappaNodeInfo edgeInfo = 
							(ALDGrappaNodeInfo)edge.getValue();
					ALDWorkflowEdgeID edgeID = 
							this.alidaWorkflow.getEdgeIdDuringLoading(
									edgeInfo.getRefID());
					// just to make sure that everything is fine...
					if (edgeID != null) {
						System.err.println("[ALDGrappaWorkbenchTab::restoreIDs()] " +
								"found edge connected to non-existing node...!?");
						try {
							System.in.read();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					else {
						edgesToRemove[e] = edge;
					}
				}
				// delete all edges of this node
				this.graph.removeCells(edgesToRemove);
			}
		}
		// delete all the cells
		this.graph.removeCells(obsoleteCells);
	}
	
	/**
	 * Instantiates for each node in the current workflow a configuration window.
	 */
	protected void restoreConfigWins() {
		Set<ALDWorkflowNodeID> keys = this.graphNodes.keySet();
		ALDOperator op = null;
		for (ALDWorkflowNodeID nodeID : keys) {
			try {
				op = this.alidaWorkflow.getOperator(nodeID);
				ParameterUpdateListener pL = new ParameterUpdateListener(nodeID);
				ALDOperatorConfigurationFrame confWin = this.getNewConfigWin(op, pL);
				// check which input params (required/optional/supplemental) are linked
				Collection<String> inParams = op.getInInoutNames(new Boolean(false));
				inParams.addAll(op.getInInoutNames(new Boolean(true)));
				ALDWorkflowNode wNode = this.alidaWorkflow.getNode(nodeID);
				for (String ip: inParams) {
					// get input edges, should never be more than one!
					Collection<ALDWorkflowEdge> iEdges = wNode.getInEdgesForParameter(ip);
					if (iEdges.isEmpty()) {
						// if there are not incoming edges, skip parameter
						continue;
					}
					// get the edge and its metadata
					ALDWorkflowEdge edge = iEdges.iterator().next();
					ALDWorkflowNode sourceNode = edge.getSourceNode();
					String sourceParam = edge.getSourceParameterName();
					// mark the parameter as linked in configuration window
					confWin.setParameterLinked(
						ip, sourceNode.getOperator().getName(), sourceParam);
				}
//				confWin.addALDOpParameterUpdateEventListener(
//					new ParameterUpdateListener(nodeID));
				mxCell node = this.graphNodes.get(nodeID);
				this.configWindows.put(node, confWin);
				// initially configuration windows always show only standard
				// parameters
				this.nodeConfigShowAllParameters.put(node,new Boolean(false));
				// fire a parameter change event, this is the first time when the
				// listener can react on that
				confWin.fireALDOpParameterUpdateEvent(new ALDOpParameterUpdateEvent(
					this,	ALDOpParameterUpdateEvent.EventType.CHANGED));
			} catch (ALDException e) {
				String name = (op == null) ? "Unknown Op" : op.getName();
				System.err.println("[ALDGrappaWorkbenchTab::restoreConfigWins] " 
						+ "cannot instantiating config win for \"" + name + "\"...");
			}
		}
	}

	/**
	 * Checks the states of all nodes and updates their color.
	 */
	private void updateWorkflowNodeStates() {
		Set<ALDWorkflowNodeID> keys = this.graphNodes.keySet();
		LinkedList<ALDWorkflowNodeID> tmpList = 
				new LinkedList<ALDWorkflowNodeID>();
		for (ALDWorkflowNodeID nodeID : keys) {
			tmpList.add(nodeID);
		}
		// make sure that all node state are correct
		this.handleNodeStateChangeEvent(tmpList);
		
		// ATTENTION: never call this method here, at this point node 
		//            parameters are always up-to-date!
		//		this.handleNodeParameterChangeEvent(tmpList);
	}
	
	/**
	 * Switches the mode for displaying parameters of a node.
	 * @param cell  Graph cell/node for which mode is to be set. 
	 * @param mode  Mode to set.
	 */
	protected synchronized void setWorkflowNodeViewMode(mxCell cell, 
			Parameter.ExpertMode mode) {
		boolean showAll = mode.equals(Parameter.ExpertMode.ADVANCED);
		this.nodeConfigShowAllParameters.put(cell, new Boolean(showAll));
		ALDWorkflowNodeID nodeID = 
				ALDGrappaWorkbenchTab.this.graphNodeIDs.get(cell);
		ALDOperator op;
    try {
	    op = ALDGrappaWorkbenchTab.this.alidaWorkflow.getOperator(nodeID);
			for (int i = 0; i < cell.getChildCount(); i++) {
				mxCell port = (mxCell)cell.getChildAt(i);
				ALDOpParameterDescriptor descr =
		      op.getParameterDescriptor(
		      		((ALDGrappaNodePortInfo)port.getValue()).getPortName());
				// ignore outputs
				if (	 descr.getDirection().equals(Parameter.Direction.IN)
						|| descr.getDirection().equals(Parameter.Direction.INOUT)) {
					if (showAll) {
						port.setVisible(true);
					} 
					else {
						if (descr.getHandlingMode().equals(Parameter.ExpertMode.ADVANCED)){
							// check if port is linked, if so, should remain visible
							if (port.getEdgeCount() == 0)
								port.setVisible(false);
						}
						else {
							port.setVisible(true);
						}
					}
				}
			}
			this.graph.refresh();
    } catch (ALDException e) {
			JOptionPane.showMessageDialog(null, "Changing view mode faild,\n" +
					"something is wrong with the operator!",
					"Workflow Error", JOptionPane.ERROR_MESSAGE);
    }
	}

	/**
	 * Changes the workflow title.
	 * @param newTitle		New title of the workflow.
	 */
	public void setWorkflowTitle(String newTitle) {
		// set title for graph
//		this.workflowTitle = newTitle;
		// set title for underlying Alida workflow
		this.alidaWorkflow.setName(newTitle);
		this.processWorkflowEventQueue();
	}

	/**
	 * Returns the workflow title.
	 * @return	Current title of the workflow.
	 */
	public String getWorkflowTitle() {
		return this.workflowTitle;
	}

	/**
	 * Executes the complete workflow, i.e. all nodes currently present.
	 */
	public void runWorkflow() {
		try {
			// send status message
			this.workBench.stateChanged(new ALDGrappaWorkflowTabChangeEvent(
				this, "running workflow..."));
			this.alidaWorkflow.runWorkflow();
		} catch (ALDWorkflowException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Executes the workflow up to the specified node.
	 * @param nodeID		ID of node where to stop execution.
	 */
	public void runWorkflowNode(ALDWorkflowNodeID nodeID) {
		try {
			// send status message
			this.workBench.stateChanged(new ALDGrappaWorkflowTabChangeEvent(
				this, "running workflow node <" 
					+ this.alidaWorkflow.getNode(nodeID).getOperator().getName() 
						+ ">..."));
			this.alidaWorkflow.runNode(nodeID);
		} catch (ALDWorkflowException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Executes the workflow from the specified node to the end.
	 * @param nodeID		ID of node where to start execution.
	 */
	public void runWorkflowFromNode(ALDWorkflowNodeID nodeID) {
		try {
			// send status message
			this.workBench.stateChanged(new ALDGrappaWorkflowTabChangeEvent(
				this, "running workflow nodes starting with <" 
					+ this.alidaWorkflow.getNode(nodeID).getOperator().getName() 
						+ ">..."));
			this.alidaWorkflow.runFromNode(nodeID);
		} catch (ALDWorkflowException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Aborts running execution of the workflow.
	 */
	public void interruptExecution() {
		Dimension parentDim = this.workBench.mainFrame.getSize();
		this.interruptWin.setLocation((int)(parentDim.getWidth()/2), 300);
		this.interruptWin.setVisible(true);
		this.alidaWorkflow.interruptExecution();
		// post-process workflow events
		this.processWorkflowEventQueue();
	}
	
	/**
	 * Saves the workflow to the given file.
	 * @param file	File where to save the workflow.
	 */
	public synchronized void saveWorkflow(File file) {
		try {
			this.alidaWorkflow.save(file, true);
			// post-process workflow events
			this.processWorkflowEventQueue();
		} catch (ALDWorkflowException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Function to return tooltip text for a cell.
	 * @param cell		Cell for which tooltip is requested.
	 * @return	Tooltip text.
	 */
	public String getTooltipText(Object cell) {
		mxCell port = (mxCell) cell;
		if (port.isVertex() && port.isConnectable()) {
			ALDGrappaNodePortInfo portInfo = (ALDGrappaNodePortInfo)port.getValue();
			String toolTip = "<html>" + "Name: " + portInfo.getPortLabel()
					+ "<br>" + "Class: " + portInfo.getPortClassName() + "<br>"
					+ portInfo.getPortExplanation() + "</html>";
			return toolTip;
		}
		return "";
	}

	/**
	 * Check if the workflow is empty.
	 * @return	If true, the workflow contains at least one node.
	 */
	public boolean workflowHasNodes() {
		return !this.alidaWorkflow.getNodes().isEmpty();
	}
	
	/**
	 * Does clean-up on termination, i.e. closes all open windows.
	 */
	public void quit() {
		// close all configuration windows
		Set<mxCell> keys = this.configWindows.keySet();
		for (mxCell key : keys) {
			this.configWindows.get(key).setVisible(false);
		}
	}
	
	/**
	 * Creates a new workflow node.
	 * @param loc					Location data of the underlying operator.
	 * @param posX				x position where to place the new node.
	 * @param posY				y position where to place the new node.
	 */
	protected synchronized void createNewWorkflowNode(ALDOperatorLocation loc, 
																												int posX, int posY) {
		try {
			ALDWorkflowID actionID = this.alidaWorkflow.createNode(loc);
			// remember action on workflow for later reference
			WorkflowModifyAction graphMA = new WorkflowModifyAction();
			graphMA.setActionPositionX(posX);
			graphMA.setActionPositionY(posY);
			this.actionsOnWorkflow.put(actionID, graphMA);
		} catch (ALDWorkflowException ex) {
			JOptionPane.showMessageDialog(null, "Adding node \""
					+ loc.getName() + "\" failed!\n", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		// process workflow events
		this.processWorkflowEventQueue();
	}
	
	/**
	 * Copies a workflow node.
	 * @param opNamePath	Full path of the underlying operator.
	 * @param posX				x position where to place the new node.
	 * @param posY				y position where to place the new node.
	 */
//	protected synchronized void copyWorkflowNode(ALDOperator op, 
//																												int posX, int posY) {
//		try {
//			ALDWorkflowID actionID = this.alidaWorkflow.createNode(op);
//			// remember action on workflow for later reference
//			WorkflowModifyAction graphMA = new WorkflowModifyAction();
//			graphMA.setActionPositionX(posX);
//			graphMA.setActionPositionY(posY);
//			this.actionsOnWorkflow.put(actionID, graphMA);
//		} catch (ALDWorkflowException ex) {
//			JOptionPane.showMessageDialog(null, "Adding node \""
//					+ op.getName() + "\" failed!\n", 
//					"Error", JOptionPane.ERROR_MESSAGE);
//		}
//		// process workflow events
//		this.processWorkflowEventQueue();
//	}

	/**
	 * Copies only the links of a workflow node to a new node of same type.
	 * @param srcOp				Source node operator which is to be copied.
	 * @param srcNode			Source node to be copied.
	 * @param newOp				New operator instantiated from source operator.
	 * @param posX				x position where to place the new node.
	 * @param posY				y position where to place the new node.
	 */
	protected synchronized void copyWorkflowNodeLinksOnly(
			@SuppressWarnings("unused") ALDOperator srcOp, mxCell srcNode, 
													        ALDOperator newOp, int posX, int posY) {
		try {
			// first create the new operator node in the workflow
			ALDWorkflowNodeID newNodeID = this.alidaWorkflow.createNode(newOp);
			// remember action on workflow for later reference
			WorkflowModifyAction graphMA = new WorkflowModifyAction();
			graphMA.setActionPositionX(posX);
			graphMA.setActionPositionY(posY);
			this.actionsOnWorkflow.put(newNodeID, graphMA);
			
			// add all input edges of source operator node also to new one
			for (int i= 0; i<srcNode.getChildCount(); ++i) {
				mxCell child = (mxCell)srcNode.getChildAt(i);
				ALDGrappaNodePortInfo childInfo =
						(ALDGrappaNodePortInfo)child.getValue();
				if (childInfo.getPortDirection().equals("IN")) {
					// iterate over all edges of the source node port
					int childEdgeNum = child.getEdgeCount();
					for (int j=0; j<childEdgeNum; ++j) {
						mxCell edge = (mxCell)child.getEdgeAt(j);
						// check if edge is incoming
						if (edge.getTarget().equals(child)) {
							// get node ID of source node and output port parameter name
							ALDWorkflowNodeID sourceNodeID = 
									this.graphNodeIDs.get(edge.getSource().getParent());
							String sourceParameter = 
									((ALDGrappaNodePortInfo)edge.getSource().getValue()).
																																getPortName();
							String targetParameter = 
									((ALDGrappaNodePortInfo)edge.getTarget().getValue()).
																																getPortName();
							// add edge to workflow
							ALDWorkflowEdgeID edgeID = 
								ALDGrappaWorkbenchTab.this.alidaWorkflow.createEdge(
								sourceNodeID, sourceParameter, newNodeID, targetParameter);
							ALDGrappaWorkbenchTab.this.actionsOnWorkflow.put(edgeID, 
									new WorkflowModifyAction());
						}
					}
				}
			}
		} catch (ALDWorkflowException ex) {
			JOptionPane.showMessageDialog(null, "Copying node \""
					+ newOp.getName() + "\" failed!\n", 
					"Error", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
		// process workflow events
		this.processWorkflowEventQueue();
	}

	/**
	 * Adds a new edge object to the graph.
	 * <p> 
	 * Note that according to the event handling of the GUI the edge object 
	 * has already been inserted into the graph data structure when this 
	 * function is called. Accordingly this function mainly checks if the 
	 * insertion was correct, and if not, removes the formerly inserted edge.
	 *  
	 * @param edge		Edge object recently inserted. 
	 * @param sourceNodeID			ID of source node.
	 * @param sourcePort				Name of source port.
	 * @param targetNodeID			ID of target node.
	 * @param targetPort				Name of target port.
	 */
	protected synchronized void createNewWorkflowEdge(mxCell edge,
			ALDWorkflowNodeID sourceNodeID, ALDGrappaNodePortInfo sourcePort,
			ALDWorkflowNodeID targetNodeID, ALDGrappaNodePortInfo targetPort) {
		String sourceParameter = sourcePort.getPortName();
		String targetParameter = targetPort.getPortName();
		try {
			ALDWorkflowEdgeID edgeID = 
					ALDGrappaWorkbenchTab.this.alidaWorkflow.createEdge(
							sourceNodeID, sourceParameter, targetNodeID, targetParameter);
			ALDGrappaWorkbenchTab.this.actionsOnWorkflow.put(edgeID, 
					new WorkflowModifyAction());
			ALDGrappaWorkbenchTab.this.graphEdgeIDs.put(edge, edgeID);
			ALDGrappaWorkbenchTab.this.graphEdges.put(edgeID, edge);
			// add user data
			edge.setValue(new ALDGrappaNodeInfo());
		} catch (ALDWorkflowException ex) {
			/* Check for type of execption, i.e., check if implicit data type 
			 * conversion is possible; if so, ask the user for permission. */
			String message;
			switch(ex.getType())
			{
			case INCOMPATIBLE_TYPES_BUT_CONVERTIBLE:
				message = ex.getIdentStringWithoutType()+ex.getCommentString() 
					+ "\nDo you want to allow the data type conversion?";
				int retVal = JOptionPane.showConfirmDialog(null, message,
						"Edge Connect Warning", JOptionPane.YES_NO_OPTION);
				// user says "no", delete edge
				if (retVal == JOptionPane.NO_OPTION) {
					ALDGrappaWorkbenchTab.this.getGraph().getModel().remove(edge);
					ALDGrappaWorkbenchTab.this.getGraph().refresh();
				}
				// user says "yes", tell workflow that inserting edge is ok
				else {
					ALDWorkflowEdgeID edgeID;
					try {
						edgeID = ALDGrappaWorkbenchTab.this.alidaWorkflow.createEdge(
								sourceNodeID, sourceParameter, targetNodeID, targetParameter,
								new Boolean(true));
						ALDGrappaWorkbenchTab.this.actionsOnWorkflow.put(edgeID, 
								new WorkflowModifyAction());
						ALDGrappaWorkbenchTab.this.graphEdgeIDs.put(edge, edgeID);
						ALDGrappaWorkbenchTab.this.graphEdges.put(edgeID, edge);
						// add user data
						edge.setValue(new ALDGrappaNodeInfo());
					} catch (ALDWorkflowException e) {
						if ( this.debug ) {
							System.out.println("[ALDGrappaWorkbenchTab] workflow error, " + 
									"offers conversion, but does not do it...");
						}
						e.printStackTrace();
					}
				}
				break;
			case INCOMPATIBLE_TYPES:
			default:
				// something went wrong, remove edge
				ALDGrappaWorkbenchTab.this.getGraph().getModel().remove(edge);
				ALDGrappaWorkbenchTab.this.getGraph().refresh();
				// format proper error message
				message = ex.getIdentStringWithoutType()+ex.getCommentString();
				JOptionPane.showMessageDialog(null, message,
						"Edge Connect Error", JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
		// process workflow events
		ALDGrappaWorkbenchTab.this.processWorkflowEventQueue();
	}

	/**
	 * Removes the specified cell from the graph.
	 * @param node		Node cell to remove.
	 */
	protected void removeWorkflowNode(mxCell node) {
		ALDWorkflowNodeID nodeID = 
				ALDGrappaWorkbenchTab.this.graphNodeIDs.get(node);
		try {
			ALDGrappaWorkbenchTab.this.actionsOnWorkflow.put(nodeID, 
					new WorkflowModifyAction());
			ALDGrappaWorkbenchTab.this.alidaWorkflow.removeNode(nodeID);
		} catch (ALDWorkflowException e1) {
			JOptionPane.showMessageDialog(null, "Removing node failed!", 
					"Node Error", JOptionPane.ERROR_MESSAGE);
		}
		// process workflow events
		ALDGrappaWorkbenchTab.this.processWorkflowEventQueue();
	}
	
	/**
	 * Removes the specified edge from the workflow.
	 * @param edge		Edge cell to remove.
	 */
	protected void removeWorkflowEdge(mxCell edge) {
		ALDWorkflowEdgeID edgeID = 
				ALDGrappaWorkbenchTab.this.graphEdgeIDs.get(edge);
		try {
			ALDGrappaWorkbenchTab.this.actionsOnWorkflow.put(edgeID, 
					new WorkflowModifyAction());
			ALDGrappaWorkbenchTab.this.alidaWorkflow.removeEdge(edgeID);
		} catch (ALDWorkflowException e1) {
			JOptionPane.showMessageDialog(null, "Removing edge failed!", 
					"Edge Error", JOptionPane.ERROR_MESSAGE);
		}
		// process workflow events
		ALDGrappaWorkbenchTab.this.processWorkflowEventQueue();
	}

	/**
	 * Redirects the workflow edge.
	 * <p>
	 * Note that due to the event handling of the GUI the edge redirect 
	 * was already done when this function is called. Thus, this function
	 * mainly verifies the redirect and restores the old edge in case of
	 * failure.
	 * 
	 * @param edge			Edge cell that was redirected.
	 * @param id				Workflow ID of edge.
	 * @param sourceNodeID		Current ID of edge source node.
	 * @param targetNodeID		Current ID of edge target node.
	 * @param sourcePort			Current source parameter name.
	 * @param targetPort			Current target parameter name.
	 */
	protected synchronized void redirectWorkflowEdge(
			mxCell edge, ALDWorkflowEdgeID id, 
			ALDWorkflowNodeID sourceNodeID, ALDWorkflowNodeID targetNodeID,
			ALDGrappaNodePortInfo sourcePort, ALDGrappaNodePortInfo targetPort) {
		String sourceParameter = sourcePort.getPortName();
		String targetParameter = targetPort.getPortName();
		edge.setValue(new ALDGrappaNodeInfo());
		ALDWorkflowNodeID flowSourceNodeID = null;
		ALDWorkflowNodeID flowTargetNodeID = null;
		String flowSourceNodeParamter = null;
		String flowTargetNodeParamter = null;
		try {
      // get old source and target nodes as represented in workflow
      flowSourceNodeID = 
    		ALDGrappaWorkbenchTab.this.alidaWorkflow.getSourceNodeId(id);
      flowSourceNodeParamter =
    		ALDGrappaWorkbenchTab.this.alidaWorkflow.getSourceParameterName(id);
      flowTargetNodeID = 
    		ALDGrappaWorkbenchTab.this.alidaWorkflow.getTargetNodeId(id);
      flowTargetNodeParamter =
    		ALDGrappaWorkbenchTab.this.alidaWorkflow.getTargetParameterName(id);
      		
      // check if source or target changed
      if (   (!(sourceNodeID.equals(flowSourceNodeID)))
      		|| (!(sourceParameter.equals(flowSourceNodeParamter)))) {
//			System.out.println("Source node = " + sourceNode.getValue());
//			System.out.println("--- port = " + sourcePort.getValue());
//			System.out.println("Target node = " + targetNode.getValue());
//			System.out.println("--- port = " + targetPort.getValue());
      	// source of edge was redirected
  			ALDGrappaWorkbenchTab.this.alidaWorkflow.redirectSource(
  					id, sourceNodeID, sourceParameter);
  	  	// remember action
  			ALDGrappaWorkbenchTab.this.actionsOnWorkflow.put(id, 
  					new WorkflowModifyAction());
      }
      else if (   (!(targetNodeID.equals(flowTargetNodeID)))
      				  || (!(targetParameter.equals(flowTargetNodeParamter)))) {
//				System.out.println("Source node = " + sourceNode.getValue());
//				System.out.println("--- port = " + sourcePort.getValue());
//				System.out.println("Target node = " + targetNode.getValue());
//				System.out.println("--- port = " + targetPort.getValue());
      	// target of edge was redirected
      	String oldTargetParam =	ALDGrappaWorkbenchTab.
      			this.alidaWorkflow.getTargetParameterName(id);
      	ALDGrappaWorkbenchTab.this.alidaWorkflow.redirectTarget(
      			id, targetNodeID, targetParameter);
      	WorkflowModifyAction wma = new WorkflowModifyAction();
      	wma.oldEdgeTarget = flowTargetNodeID; 
      	wma.oldEdgeTargetParamName = oldTargetParam;
      	ALDGrappaWorkbenchTab.this.actionsOnWorkflow.put(id, wma);
     }
      else {
      	// just for debugging, we should never end-up here...
      	System.err.println("--- Edge redirect: nothing changed?!");
      }
    } catch (ALDWorkflowException e) {
			// format proper error message
			String message = e.getIdentStringWithoutType()+e.getCommentString();
			JOptionPane.showMessageDialog(null, message,
					"Edge Redirect Error", JOptionPane.ERROR_MESSAGE);
			// reset edge
			mxCell oldSource = 
					ALDGrappaWorkbenchTab.this.graphNodes.get(flowSourceNodeID);
			mxCell oldTarget = 
					ALDGrappaWorkbenchTab.this.graphNodes.get(flowTargetNodeID);
			int childs = oldSource.getChildCount();
			for (int i= 0; i<childs; ++i) {
				mxCell child = (mxCell)oldSource.getChildAt(i);
				try {
					ALDGrappaNodePortInfo childPortInfo = 
							(ALDGrappaNodePortInfo)child.getValue();
          if (childPortInfo.getPortName().equals(ALDGrappaWorkbenchTab.this.
          											alidaWorkflow.getSourceParameterName(id))) {
          	edge.setSource(child);
          	break;
          }
        } catch (ALDWorkflowException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
			}
			childs = oldTarget.getChildCount();
			for (int i= 0; i<childs; ++i) {
				mxCell child = (mxCell)oldTarget.getChildAt(i);
				try {
					ALDGrappaNodePortInfo childPortInfo = 
							(ALDGrappaNodePortInfo)child.getValue();
          if (childPortInfo.getPortName().equals(ALDGrappaWorkbenchTab.this.
          		alidaWorkflow.getTargetParameterName(id))) {
          	edge.setTarget(child);
          	break;
          }
        } catch (ALDWorkflowException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
			}
    }
		// process workflow events
		ALDGrappaWorkbenchTab.this.processWorkflowEventQueue();
	}
	
	/**
	 * Processes all events that were recently added to the queue.
	 * <p>
	 * Note that this function needs to be called after all actions on the 
	 * Alida workflow except calls to 'run' methods.
	 */
	protected synchronized void processWorkflowEventQueue() {
		BlockingDeque<ALDWorkflowEvent> queue = 
				this.alidaWorkflow.getEventQueue(this);
		ALDWorkflowEvent event = null; 	
		while (!queue.isEmpty()) {
			event = queue.pop();
			this.handleALDWorkflowEvent(event);
		}
		ALDGrappaWorkbenchTab.this.getGraph().refresh();
	}
	
	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEventListener#handleALDWorkflowEvent(de.unihalle.informatik.Alida.workflows.events.ALDWorkflowEvent)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized void handleALDWorkflowEvent(ALDWorkflowEvent event) {

		// extract event data
		ALDWorkflowEventType type = event.getEventType();
		Object eventInfo = event.getId();

		// get action file related to this action if available
		WorkflowModifyAction actionReference=this.actionsOnWorkflow.get(eventInfo);

		// handle the event
		switch(type) 
		{
		case ADD_NODE:
			if (   eventInfo instanceof ALDWorkflowNodeID 
					&& actionReference != null) {
				ALDOperator op;
				try {
					op = this.alidaWorkflow.getOperator(
							(ALDWorkflowNodeID)eventInfo);
					// request initial GUI values for operator
					ALDOperator initialOp = (ALDOperator)ALDDataIOManagerSwing.
							getInstance().getInitialGUIValue(null, op.getClass(), op, 
									null);
					// copy parameter settings
					Collection<String> params = op.getParameterNames();
					// copy the collection, because iterating over a collection
					// which could be meanwhile modified is a very bad idea!
					String[] paramArray = new String[params.size()];
					int i=0;
					for (String s: params) {
						paramArray[i] = s;
						++i;
					}
					for (i=0; i<paramArray.length; ++i) {
						String pname = paramArray[i];
						// if the parameter does not exist anymore (e.g., due to 
						// callback changes), skip it
						if (!op.hasParameter(pname))
							continue;
						op.setParameter(pname, initialOp.getParameter(pname));
					}
					this.handleAddNodeEvent(op, (ALDWorkflowNodeID)eventInfo, 
							actionReference.getActionPositionX(), 
							actionReference.getActionPositionY());
					// remove the action file as event was processed
					this.actionsOnWorkflow.remove(eventInfo);
				} catch (ALDWorkflowException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ALDDataIOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (ALDOperatorException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
			}
			break;
		case ADD_EDGE:
			if (!(eventInfo instanceof ALDWorkflowEdgeID)) {
				JOptionPane.showMessageDialog(null, "Adding edge failed!", 
						"Workflow Error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			// do some checks and then update configuration window
			try {
				ALDWorkflowEdgeID id = (ALDWorkflowEdgeID)eventInfo;
				if ( this.debug ) {
					System.out.println("- ID = " + eventInfo);
				}
				ALDWorkflowNodeID source = this.alidaWorkflow.getSourceNodeId(id);
				ALDWorkflowNodeID target = this.alidaWorkflow.getTargetNodeId(id);
				String sourceParam = this.alidaWorkflow.getSourceParameterName(id);
				String targetParam = this.alidaWorkflow.getTargetParameterName(id);
				
				// check if edge exists, if not insert edge into graph
				mxCell sourceNode = this.graphNodes.get(source);
				mxCell targetNode = this.graphNodes.get(target);
				boolean edgeExists = false;
				for (int i=0;i<sourceNode.getChildCount();++i) {
					mxCell child = (mxCell)sourceNode.getChildAt(i);
					if (this.graph.getOutgoingEdges(child).length > 0) {
						// process outgoing edges
						Object[] outEdges = this.graph.getOutgoingEdges(child);
						for (Object edge: outEdges) {
							mxCell outEdge = (mxCell)edge;
							ALDGrappaNodePortInfo targetInfo =
									(ALDGrappaNodePortInfo)
														 			( (mxCell)outEdge.getTarget()).getValue();
							if (   ((mxCell)outEdge.getTarget().getParent()).equals(targetNode)
									&& targetInfo.getPortName().equals(targetParam)) {
								edgeExists= true;
								break;
							}
						}
					}
				}
				// if edge does not exist, insert it
				if (!edgeExists) {
					// figure out source port
					mxCell sourcePort= null, targetPort= null;
					for (int i=0;i<sourceNode.getChildCount();++i) {
						mxCell child = (mxCell)sourceNode.getChildAt(i);
						ALDGrappaNodePortInfo sourceInfo =
																		(ALDGrappaNodePortInfo)child.getValue();
						if (sourceInfo.getPortName().equals(sourceParam)) {
							sourcePort = child;
							break;
						}
					}
					for (int i=0;i<targetNode.getChildCount();++i) {
						mxCell child = (mxCell)targetNode.getChildAt(i);
						ALDGrappaNodePortInfo targetInfo =
																		(ALDGrappaNodePortInfo)child.getValue();
						if (targetInfo.getPortName().equals(targetParam)) {
							targetPort = child;
							break;
						}
					}
					if (sourcePort==null || targetPort==null) {
						System.err.println("Something went wrong on adding edge...");
					}
					else {
						Object edge = ALDGrappaWorkbenchTab.this.getGraph().insertEdge(
								this.graph.getDefaultParent(), null, null, 
								sourcePort, targetPort);
						// store references of new edge
						ALDGrappaWorkbenchTab.this.graphEdgeIDs.put((mxCell)edge, id);
						ALDGrappaWorkbenchTab.this.graphEdges.put(id, (mxCell)edge);
						// add user data
						((mxCell)edge).setValue(new ALDGrappaNodeInfo());
						// refresh graph
						ALDGrappaWorkbenchTab.this.getGraph().refresh();
					}
				}
				
				// notify config window of link
				this.configWindows.get(this.graphNodes.get(target)).setParameterLinked(
						targetParam, this.alidaWorkflow.getOperator(source).getName(), 
						sourceParam);
				// finally remove corresponding ID
				this.actionsOnWorkflow.remove(eventInfo);
			} catch (ALDWorkflowException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case DELETE_NODE:
			if (!(eventInfo instanceof ALDWorkflowNodeID)) {
				JOptionPane.showMessageDialog(null, "Removing node failed!", 
						"Node Error", JOptionPane.ERROR_MESSAGE);
			}
			else {
				mxCell cell = this.graphNodes.get(eventInfo);
				// remove entries in hashmaps
				this.graphNodeIDs.remove(cell);
				this.graphNodes.remove(eventInfo);
				this.configWindows.remove(cell);
				this.nodeConfigShowAllParameters.remove(cell);
				// remove node from JGraphX model
				this.graph.getModel().remove(cell);
				// remove event reference
				this.actionsOnWorkflow.remove(eventInfo);
			}
			break;
		case DELETE_EDGE:
			if (!(eventInfo instanceof ALDWorkflowEdgeID)) {
				JOptionPane.showMessageDialog(null, "Removing edge failed!", 
						"Node Error", JOptionPane.ERROR_MESSAGE);
			}
			else {
				mxCell edge = this.graphEdges.get(eventInfo);
				if ( this.debug ) {
					System.out.println("Edge " + edge);
					System.out.println("- ID = " + eventInfo);
				}
				// update the configuration window
				ALDGrappaNodePortInfo targetPortInfo =
						(ALDGrappaNodePortInfo)(((mxCell)edge.getTarget()).getValue());
				String targetParam = targetPortInfo.getPortName();
				mxCell target = (mxCell)(((mxCell)edge.getTarget()).getParent());
				// notify config window of link
				this.configWindows.get(target).setParameterNotLinked(targetParam);
				// finally remove the edge
				this.graphEdges.remove(eventInfo);
				this.graphEdgeIDs.remove(edge);
				this.graph.getModel().remove(edge);
				this.actionsOnWorkflow.remove(eventInfo);
			}
			break;
		case REDIRECT_EDGE_SOURCE:
			// remove the corresponding ID, if available
			this.actionsOnWorkflow.remove(eventInfo);
			break;
		case REDIRECT_EDGE_TARGET:
			if (!(eventInfo instanceof ALDWorkflowEdgeID)) {
				JOptionPane.showMessageDialog(null, "Redirecting edge failed!", 
						"Workflow Edge Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			// update configuration windows
			try {
				// first, add link to new target node's window
				if ( this.debug ) {
					System.out.println("Updating new target window...");
				}
				ALDWorkflowEdgeID id = (ALDWorkflowEdgeID)eventInfo;
				ALDWorkflowNodeID source = this.alidaWorkflow.getSourceNodeId(id);
				ALDWorkflowNodeID target = this.alidaWorkflow.getTargetNodeId(id);
				String sourceParam = this.alidaWorkflow.getSourceParameterName(id);
				String targetParam = this.alidaWorkflow.getTargetParameterName(id);
				// notify config window of link
				this.configWindows.get(this.graphNodes.get(target)).setParameterLinked(
						targetParam, this.alidaWorkflow.getOperator(source).getName(), 
						sourceParam);
				
				// second, remove link from old target node's window
				WorkflowModifyAction wma = this.actionsOnWorkflow.get(eventInfo);
				if (wma != null) {
					if ( this.debug ) {
						System.out.println("Updating old target window...");
					}
					// my own action, so all data is in workflow action
					ALDWorkflowNodeID formerTargetNode = wma.oldEdgeTarget;
					String formerTargetParam = wma.oldEdgeTargetParamName;
					this.configWindows.get(this.graphNodes.get(formerTargetNode)).
						setParameterNotLinked(formerTargetParam);					
					// finally remove corresponding ID
					this.actionsOnWorkflow.remove(eventInfo);
				}
				else {
					// action of someone else, i.e. edge has not yet been redirected
					if ( this.debug ) {
						System.out.println("No information on action...");
					}
//				mxCell edge = this.graphEdges.get(eventID);
//				// update the configuration window
//				String targetParam = (String)(((mxCell)edge.getTarget()).getValue());
//				mxCell target = (mxCell)(((mxCell)edge.getTarget()).getParent());
//				// notify config window of link
//				this.configWindows.get(target).setParameterNotLinked(targetParam);
				}
			} catch (ALDWorkflowException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// remove the corresponding ID, if available
			this.actionsOnWorkflow.remove(eventInfo);
			break;
		case NODE_PARAMETER_CHANGE:
			this.handleNodeParameterChangeEvent(
																		(Collection<ALDWorkflowNodeID>)eventInfo);
			this.handleNodeStateChangeEvent(
																		(Collection<ALDWorkflowNodeID>)eventInfo);
			break;
		case NODE_STATE_CHANGE:
			this.handleNodeStateChangeEvent(
																		(Collection<ALDWorkflowNodeID>)eventInfo);
			break;
			//		case LOAD_WORKFLOW:
			//			if (!(eventID instanceof ALDWorkflowStorageInfo)) {
			//				JOptionPane.showMessageDialog(null, "Loading workflow failed!", 
			//						"Workflow Load Error", JOptionPane.ERROR_MESSAGE);
			//			}
			//			else {
			//				ALDWorkflowStorageInfo info = (ALDWorkflowStorageInfo)eventID;
			//				this.workBench.handleLoadWorkflowEvent(info.getFilename(),info.getWorkflow());
			//			}
			//			break;
		case NODE_EXECUTION_PROGRESS:
			// only proceed if progress event messages are to be displayed
			if (!this.workBench.showProgressEvents())
				break;
			// new status messages about execution progress received
			try {
	      HashSet<ALDWorkflowNodeID> idHash = 
	      	(HashSet<ALDWorkflowNodeID>) eventInfo;
	      Iterator<ALDWorkflowNodeID> nodeID = idHash.iterator();
	      ALDWorkflowNode node;
	      String msg;
	      while (nodeID.hasNext()) {
	      	node = this.alidaWorkflow.getNode(nodeID.next());
	      	msg = "\"" + node.getOperator().getName() + "\"" 
	      		+ " - " + node.getOperatorExecutionProgressDescr();
	      // update status message in control window
	      this.workBench.stateChanged(
	      	new ALDGrappaWorkflowTabChangeEvent(this, msg));
	      }
      } catch (ALDWorkflowException e1) {
      	System.err.println("[ALDOperatorGUIExecutionProxy] " 
      		+ "could not handle/update operator status message... ignoring!");
      }
			break;
		case RENAME:
			String newTitle = (String)eventInfo;
			this.workflowTitle = newTitle;
			this.workBench.handleWorkflowRenameEvent(newTitle);
			break;
		case SAVE_WORKFLOW:
			if (!(eventInfo instanceof ALDWorkflowStorageInfo)) {
				JOptionPane.showMessageDialog(null, "Saving workflow failed!", 
						"Workflow Save Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			ALDWorkflowStorageInfo info = (ALDWorkflowStorageInfo)eventInfo;
			this.handleSaveWorkflowEvent(info.getFilename());
			break;
		case RUN_FAILURE:
			// eventInfo object is of type ALDWorkflowNodeID...
			JOptionPane.showMessageDialog(null, 
					"Something went wrong during workflow execution...\n " +
					event.getEventMessage(), 
					"Workflow Execution Error", JOptionPane.ERROR_MESSAGE);
			break;
		case USER_INTERRUPT:
			this.interruptWin.setVisible(false);
			JOptionPane.showMessageDialog(null, "Execution was aborted!", 
					"Workflow Execution Message", JOptionPane.ERROR_MESSAGE);
			break;
		case SHOW_RESULTS:
			System.out.println("Received show_results event...");
			if (!(eventInfo instanceof ALDWorkflowNodeID)) {
				JOptionPane.showMessageDialog(null, "Cannot display results!", 
						"Workflow Execution Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			this.handleShowResultsEvent((ALDWorkflowNodeID)eventInfo);
			break;
		default:
			if ( this.debug ) {
				System.out.println("Got event of type: " + type.toString());
				System.out.println(" => type not yet handled by Grappa!");
			}
		}
		this.graph.refresh();
	}

	/**
	 * Adds a node to the workflow graph.
	 * 
	 * @param op		Operator associated with the node.
	 * @param id		Node ID.
	 * @param posX	X-Position of the new node.
	 * @param posY 	Y-Position of the new node.
	 */
	protected synchronized void handleAddNodeEvent(
			ALDOperator op, ALDWorkflowNodeID id, int posX, int posY) {
		
		// init the control frame
		ALDOperatorConfigurationFrame controlFrame = null;
		try {
			ParameterUpdateListener pListen = new ParameterUpdateListener(id);
			controlFrame = this.getNewConfigWin(op, pListen);
		} catch (ALDOperatorException e) {
			if ( this.debug ) {
				System.out.println("[ALDGrappaWorkbenchTab::addNode] " 
					+ "cannot instantiate control frame for \"" +op.getName()+ "\"...");
			}
			return;
		}

		// set position where to insert the node, if no position given, top left
		int x = (posX == -1 ? 50 : posX);
		int y = (posY == -1 ? 50 : posY);
		
		// get parameters of the operator
		int portCountLeft = 
				op.getInNames(null).size() + op.getInOutNames(null).size();
		int heightLeft = portCountLeft * 20;
		int portCountRight =
				op.getOutNames().size() + op.getInOutNames(null).size();
		int heightRight = portCountRight * 20;
		int height = 
				(heightLeft > heightRight ? heightLeft : heightRight);
//		int portCount = 
//				(portCountLeft > portCountRight ? portCountLeft : portCountRight);

		// add the vertex
//		mxCell v = (mxCell)this.graph.insertVertex(
//				this.graph.getDefaultParent(), null, op.getName(), x, y, 160, height);
		ALDGrappaNodeInfo nodeInf = new ALDGrappaNodeInfo(op.getName());
		mxCell v = (mxCell)this.graph.insertVertex(
				this.graph.getDefaultParent(), null, nodeInf, x, y, 160, height);
		v.setConnectable(false);
		v.setStyle(getNodeStyleString("red"));

		// add ports to the node
		try {
			this.addPortsToNode(op, v);
		} catch (ALDOperatorException e) {
			System.err.println("[ALDGrappaWorkbenchTab::addNode] " 
					+ "adding ports failed... undefined node state!!!");
		}
		
		// update the display
		this.graph.refresh();
		
		// store the control frame and the node id
		this.configWindows.put(v, controlFrame);
		this.nodeConfigShowAllParameters.put(v, new Boolean(false));
		this.graphNodeIDs.put(v, id);
		this.graphNodes.put(id, v);
		
		// providers might have changed parameters, synchronize operator and GUI
//		controlFrame.synchronizeOperatorWithGUI();
		
		LinkedList<ALDWorkflowNodeID> nodeList = 
				new LinkedList<ALDWorkflowNodeID>();
		nodeList.add(id);
		this.handleNodeParameterChangeEvent(nodeList);
		
		// tell the workflow that the operator configuration changed
		controlFrame.fireALDOpParameterUpdateEvent(
				new ALDOpParameterUpdateEvent(this, EventType.CHANGED));

//		// process event queue
//		ALDGrappaWorkbenchTab.this.processWorkflowEventQueue();
	}

	/**
	 * Update node's configuration window according to parameter status.
	 * @param idList	List of nodes that are to be updated.
	 */
	protected synchronized void handleNodeParameterChangeEvent(
			Collection<ALDWorkflowNodeID> idList) {
		
		for (ALDWorkflowNodeID nodeID: idList) {
			try {
				// update the configuration window
				mxCell node = this.graphNodes.get(nodeID);
				ALDOperator op = this.alidaWorkflow.getOperator(nodeID);
				this.configWindows.get(node).updateOperator(op);
				this.configWindows.get(node).updateParamConfigurationStatus(
						this.alidaWorkflow.getMissingRequiredInputs(nodeID));
				
				// update the ports of the node
				mxCell v = ALDGrappaWorkbenchTab.this.graphNodes.get(nodeID);
				
				// check if there is a difference in the ports
				Vector<String> portNames = new Vector<String>(); 
				for (int j=0; j<v.getChildCount();++j) {
					mxICell child = v.getChildAt(j);
					ALDGrappaNodePortInfo pi = 
							(ALDGrappaNodePortInfo)child.getValue();
					portNames.add(pi.getPortName());
				}
				Collection<String> parameters = op.getParameterNames();
				
				boolean nodePortsChanged = false;
				if (portNames.size() != parameters.size())
					// ports and parameters diverge, i.e. someting changed... 
					nodePortsChanged = true;
				
				if (!nodePortsChanged) {
					// check if for all parameters a port exists
					for (String s : parameters) {
						if (!portNames.contains(s)) {
							nodePortsChanged = true;
							break;
						}
					}
				}
				if (!nodePortsChanged) {
					// check if for all ports a corresponding parameter exists
					for (String s: portNames) {
						if (!parameters.contains(s)) {
							nodePortsChanged = true;
							break;
						}
					}
				}
				
				// there were changes in the ports, update node
				if (nodePortsChanged) {
					// remove all former ports
					int portNum = v.getChildCount();
					for (int i=portNum-1;i>=0;--i) {
						mxICell child = v.getChildAt(i);
//						if (child.getEdgeCount() != 0) {
//							int edges = child.getEdgeCount();
//							for (int j=edges-1;j>=0;--j) {
//								mxCell edge = (mxCell)child.getEdgeAt(j); 
//								// update the configuration window
//								ALDGrappaNodePortInfo targetPortInfo =
//										(ALDGrappaNodePortInfo)(
//												((mxCell)edge.getTarget()).getValue());
//								String targetParam = targetPortInfo.getPortName();
//								mxCell target = 
//										(mxCell)(((mxCell)edge.getTarget()).getParent());
//								// notify config window of link
//								this.configWindows.get(target).setParameterNotLinked(
//										targetParam);
//							}
//						}
						ALDGrappaNodePortInfo portInfo =
								(ALDGrappaNodePortInfo)(child.getValue());
						if (!(parameters.contains(portInfo.getPortName()))) {
							// remove the port itself
							v.remove(child);
						}
					}

					// add ports of parameters to the node
					this.addPortsToNode(op, v);

					// update the display
					ALDGrappaWorkbenchTab.this.graph.refresh();
				}
			}
			catch (ALDException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// update nodes themselves, i.e. their color
		this.handleNodeStateChangeEvent(idList);
	}

	/**
	 * Update node color according to node's state.
	 * @param idList	List of nodes that are to be updated.
	 */
	protected synchronized void handleNodeStateChangeEvent(
			Collection<ALDWorkflowNodeID> idList) {
		for (ALDWorkflowNodeID nodeID: idList) {
			try {
				ALDWorkflowNodeState nodeState = 
						this.alidaWorkflow.getNode(nodeID).getState();
				mxCell node = this.graphNodes.get(nodeID);
				Object [] nodeToModify = new Object[]{node};
				switch(nodeState)
				{
				case UNCONFIGURED:
					this.graph.setCellStyle(getNodeStyleString("red"),nodeToModify);
					break;
				case CONFIGURED:
					this.graph.setCellStyle(getNodeStyleString("orange"),nodeToModify);
					break;
				case RUNNABLE:
					this.graph.setCellStyle(getNodeStyleString("yellow"),nodeToModify);
					break;
				case RUNNING:
					this.graph.setCellStyle(getNodeStyleString("blue"),nodeToModify);
					break;
				case READY:
					this.graph.setCellStyle(getNodeStyleString("green"),nodeToModify);
					break;
				}
			}
			catch (ALDWorkflowException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.graph.refresh();
	}
	
	/**
	 * Stores the workflow to the given file.
	 * @param file	File where to save the workflow.
	 */
	protected synchronized void handleSaveWorkflowEvent(String file) {
		ALDGrappaNodeInfo info;

		// register user data object class
		mxCodec enc = new mxCodec();
		mxCodecRegistry.addPackage(
				"de.unihalle.informatik.Alida.grappa.ALDGrappaNodeInfo");
		mxCodecRegistry.register(new mxObjectCodec(new ALDGrappaNodeInfo()));
		mxCodecRegistry.addPackage(
				"de.unihalle.informatik.Alida.grappa.ALDGrappaNodePortInfo");
		mxCodecRegistry.register(new mxObjectCodec(new ALDGrappaNodePortInfo()));
		
		// set reference IDs of nodes for later restore
		HashMap<ALDWorkflowNodeID, Integer> nodeIDMap =
				this.alidaWorkflow.getMappingNodeIdToInteger();
		Set<ALDWorkflowNodeID> nodeKeys = nodeIDMap.keySet();
		for (ALDWorkflowNodeID nodeID: nodeKeys) {
			info = (ALDGrappaNodeInfo)this.graphNodes.get(nodeID).getValue();
			info.setRefID(nodeIDMap.get(nodeID));
		}

		// set reference IDs of edges for later restore
		HashMap<ALDWorkflowEdgeID, Integer> edgeIDMap =
				this.alidaWorkflow.getMappingEdgeIdToInteger();
		Set<ALDWorkflowEdgeID> edgeKeys = edgeIDMap.keySet();
		for (ALDWorkflowEdgeID edgeID: edgeKeys) {
			info = (ALDGrappaNodeInfo)this.graphEdges.get(edgeID).getValue();
			info.setRefID(edgeIDMap.get(edgeID));
		}
		try {
//			for (Object child : this.graph.getChildCells(this.graph.getDefaultParent())) {
//				mxCell cell = (mxCell) child;
//				String name = cell.getValue().toString();
//				Integer id = ((ALDGrappaNodeInfo)(cell.getValue())).getRefID();
//				if (cell.isVertex()) {
//					System.out.println("- Saving node " + name + " with id = " + id);
//					cell.toString();
//				} 
//				else {
//					System.out.println("- Saving edge " + name + " with id = " + id);
//				}
//			}
			mxUtils.writeFile(mxXmlUtils.getXml(enc.encode(this.graph.getModel())),
					file + ".gui");
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "Saving workflow graphics failed!",
				"Workflow Save Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Displays result frame for given node.
	 * @param nodeID	Node for which results are to be displayed.
	 */
	protected void handleShowResultsEvent(ALDWorkflowNodeID nodeID) {
		// there are results to display
		ALDOperator op;
		try {
			op = this.alidaWorkflow.getOperator(nodeID);
			ALDOperatorResultFrame resultFrame = 
					new ALDOperatorResultFrame(op, 
							ProviderInteractionLevel.ALL_ALLOWED);
			resultFrame.setVisible(true);
		} catch (ALDWorkflowException e) {
			JOptionPane.showMessageDialog(null, "Cannot display results!", 
					"Workflow Execution Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Generate a new configuration window.
	 * <p>
	 * Is to be overwritten by subclasses.
	 * 
	 * @param op			Operator for which window is requested.
	 * @param pListen Parameter update listener to attach to window.
	 * @return	New configuration window.
	 * @throws ALDOperatorException Thrown in case of failure.
	 */
	protected ALDOperatorConfigurationFrame getNewConfigWin(ALDOperator op,
			ParameterUpdateListener pListen) 
		throws ALDOperatorException {
		return new ALDOperatorConfigurationFrame(op, pListen);
	}
	
	/* *************************************************
	 * Static class functions, mainly internal helpers. 
	 * *************************************************/
	
	/**
	 * Method to properly format node style string.
	 * @param color		Color of node boundary.
	 * @return	Format string.
	 */
	protected static String getNodeStyleString(String color) {
		return new String(
				  "verticalLabelPosition=top;" 
				+ "verticalAlign=bottom;"
				+ "fontStyle=1;" 
				+ "strokeWidth=3.0;"
				+ "strokeColor=" + color);
	}
	
	/**
	 * Adds the ports of the operator's parameters to the given node. 
	 * @param op	Operator linked to the node.
	 * @param v		Node to which the ports are to be added.
	 * @throws ALDOperatorException Thrown in case of failure.
	 */
	protected void addPortsToNode(ALDOperator op, mxCell v) 
			throws ALDOperatorException {
		
		// get parameters of the operator
		int portCountLeft = 
			op.getInNames(null).size() + op.getInOutNames(null).size();
		int portCountRight =
			op.getOutNames().size() + op.getInOutNames(null).size();

		// calculate spacing of ports on left and right side
		double countIn = 1 / 	(	(double) (portCountLeft) 	+ 1);
		int posIn = 1;
		double countOut = 1 / ((double) (portCountRight) + 1);
		int posOut = 1;

		// list existing ports
		HashMap<String, mxICell> exPorts = new HashMap<String, mxICell>();
		for (int j=0; j<v.getChildCount();++j) {
			mxICell port = v.getChildAt(j);
			ALDGrappaNodePortInfo pi = (ALDGrappaNodePortInfo)port.getValue();
			exPorts.put(pi.getPortName(),port);
		}
		
		// build a port for each input parameter, sort first
		Collection<String> sortedParams = 
				ALDGrappaWorkbenchTab.sortParamsByIOOrder(
						op, op.getInNames(null));
		for (String s : sortedParams) {
			// get parameter descriptor and configure port layout
			try {
				ALDOpParameterDescriptor descr = op.getParameterDescriptor(s);

				double portPos = countIn * posIn;
				mxGeometry geo = new mxGeometry(0, portPos, 10, 10);
				geo.setOffset(new mxPoint(-5, -5));
				geo.setRelative(true);

				// port still exists, simply shift to new position
				if (exPorts.containsKey(s)) {
					mxICell port = exPorts.get(s);
					port.setGeometry(geo);
				}
				// no port there, create a new one
				else {
					mxCell port = new mxCell(null, geo, null);
					ALDGrappaNodePortInfo portInfo = 
							new ALDGrappaNodePortInfo(v,descr);
					port.setValue(portInfo);
					//						opDescr.addParameter(s, op.getParameterDescriptor(s), port);

					// required parameters are blue and not required are yellow
					if (descr.isRequired()) {
						port.setStyle("labelPosition=right;align=left;shape=ellipse;" 
								+ "perimter=ellipsePerimeter");
					} else if (!descr.getSupplemental().booleanValue()) {
						port.setStyle("labelPosition=right;align=left;fillColor=yellow;" 
								+ "shape=ellipse;perimter=ellipsePerimeter");
					} else {
						port.setStyle("labelPosition=right;align=left;fillColor=red;" 
								+ "shape=ellipse;perimter=ellipsePerimeter");
					}
					// configure vertex and add to graph
					port.setVertex(true);
					// check if view mode requires port to be visible,
					// by default only standard parameters are visible
					if (descr.getHandlingMode() != Parameter.ExpertMode.STANDARD)
						port.setVisible(false);
					this.graph.addCell(port, v);
				}
			} catch (ALDOperatorException ex) {
				System.out.println("[ALDGrappaWorkbenchTab::addNode] " 
						+ "problems configuring input port \"" + s + "\"...");
			}
			posIn++;
		}

		// build a port for each output parameter, sort first
		sortedParams = ALDGrappaWorkbenchTab.sortParamsByIOOrder(
				op, op.getOutNames());
		for (String s : sortedParams) {
			try {
				ALDOpParameterDescriptor descr = op.getParameterDescriptor(s);

				double portPos = countOut * posOut;
				mxGeometry geo = new mxGeometry(1, portPos, 10, 10);
				geo.setOffset(new mxPoint(-5, -5));
				geo.setRelative(true);

				mxCell port = new mxCell(null, geo,
						"labelPosition=left;align=right;shape=ellipse;" 
								+ "perimter=ellipsePerimeter");
				ALDGrappaNodePortInfo portInfo = 
						new ALDGrappaNodePortInfo(v,descr);
				port.setValue(portInfo);
				port.setVertex(true);
				this.graph.addCell(port, v);
			} catch (ALDOperatorException ex) {
				System.out.println("[ALDGrappaWorkbenchTab::addNode] " 
						+ "problems configuring output port \"" + s + "\"...");
			}
			posOut++;
		}

		// build a port for each inout parameter, sorted first
		sortedParams = ALDGrappaWorkbenchTab.sortParamsByIOOrder(
				op, op.getInOutNames(null));
		for (String s : sortedParams) {
			try {
				ALDOpParameterDescriptor descr = op.getParameterDescriptor(s);

				double portPos = countIn * posIn;
				mxGeometry geoLeft = new mxGeometry(1, portPos, 10, 10);
				geoLeft.setOffset(new mxPoint(-5, -5));
				geoLeft.setRelative(true);
				mxCell portLeft = 
						new mxCell(null, geoLeft, "labelPosition=left;align=right");
				ALDGrappaNodePortInfo portInfoLeft = 
						new ALDGrappaNodePortInfo(v, descr);
				portLeft.setValue(portInfoLeft);

				mxGeometry geoRight = new mxGeometry(0, portPos, 10, 10);
				geoRight.setOffset(new mxPoint(-5, -5));
				geoRight.setRelative(true);
				mxCell portRight = 
						new mxCell(null, geoRight, "labelPosition=right;align=left");
				ALDGrappaNodePortInfo portInfoRight = 
						new ALDGrappaNodePortInfo(v, descr);
				portRight.setValue(portInfoRight);

				portLeft.setVertex(true);
				this.graph.addCell(portLeft, v);
				portRight.setVertex(true);
				this.graph.addCell(portRight, v);
			} catch (ALDOperatorException ex) {
				System.out.println("[ALDGrappaWorkbenchTab::addNode] " 
						+ "problems configuring inout port \"" + s + "\"...");
			}
			// increment position counter
			posIn++;
		}
	}

	/**
	 * Sorts a set of parameters according to category and I/O order.
	 * <p>
	 * The parameters are first split-up into required, optional and
	 * supplemental parameters. Subsequently each subset is sorted 
	 * according to the parameters' data I/O order. Finally all sorted
	 * subsets are concatenated and returned in a single collection.
	 * 
	 * @param op			Operator to which the parameters belong.
	 * @param params	Set of parameters to sort.
	 * @return	Sorted set of parameter names.
	 * @throws ALDOperatorException Thrown in case of failure.
	 */
	private static Collection<String> sortParamsByIOOrder(ALDOperator op,
			Collection<String> params) throws ALDOperatorException {
		
		// get list of descriptors
		Vector<ALDOpParameterDescriptor> descriptors = 
				new Vector<ALDOpParameterDescriptor>();
		for (String s: params) 
				descriptors.add(op.getParameterDescriptor(s));

		// sort descriptors according to GUI order into hash tables
		HashMap<Integer, Vector<ALDOpParameterDescriptor>> 
			guiOrderHashRequired = 
				new HashMap<Integer, Vector<ALDOpParameterDescriptor>>();
		HashMap<Integer, Vector<ALDOpParameterDescriptor>> 
			guiOrderHashOptional = 
				new HashMap<Integer, Vector<ALDOpParameterDescriptor>>();
		HashMap<Integer, Vector<ALDOpParameterDescriptor>> 
			guiOrderHashSupplemental = 
				new HashMap<Integer, Vector<ALDOpParameterDescriptor>>();
		HashMap<Integer, Vector<ALDOpParameterDescriptor>> activeHash;
		for (ALDOpParameterDescriptor descr : descriptors) {
			// select the right hash according to parameter category
			if (descr.isRequired())
				activeHash = guiOrderHashRequired;
			else if (descr.getSupplemental().booleanValue())
				activeHash = guiOrderHashSupplemental;
			else
				activeHash = guiOrderHashOptional;
			
			// sort into bins by data IO order
			Integer order = new Integer(descr.getDataIOOrder());
			if (activeHash.containsKey(order)) {
				activeHash.get(order).add(descr);
			} 	
			else {
				Vector<ALDOpParameterDescriptor> paramVec = 
						new Vector<ALDOpParameterDescriptor>();
				paramVec.add(descr);
				activeHash.put(order, paramVec);
			}
		}
		// sort the hashes
		Collection<String> resultList = sortHash(guiOrderHashRequired);
		resultList.addAll(sortHash(guiOrderHashOptional));
		resultList.addAll(sortHash(guiOrderHashSupplemental));
		return resultList;
	}

	/**
	 * Sorts the items for each key alpha-numerically.
	 * <p>
	 * As result the sorted lists for all hash keys are concatenated
	 * according to the ascending order of their key values.
	 * 
	 * @param hash	Input hashmap to sort.
	 * @return List of sorted hashmap items.
	 */
	private static Collection<String> sortHash(
			HashMap<Integer, Vector<ALDOpParameterDescriptor>> hash) {
		// sort the keys of the hashmap in ascending order
		Set<Integer> keys = hash.keySet();
		LinkedList<Integer> keyList = new LinkedList<Integer>();
		for (Integer key : keys) {
			keyList.add(key);
		}
		Collections.sort(keyList);
		
		// create final result list
		LinkedList<String> sortedList = new LinkedList<String>();
		LinkedList<String> itemsOfCurrentOrder = new LinkedList<String>();
		
		// sort lists of items for each key value and concatenate all lists
		for (Integer n: keyList) {
			itemsOfCurrentOrder.clear();
			for (ALDOpParameterDescriptor d: hash.get(n)) {
				itemsOfCurrentOrder.add(d.getName());
			}
			// sort items in alpha-numerical ordering
			Collections.sort(itemsOfCurrentOrder);
			// add sorted items to final result list
			sortedList.addAll(itemsOfCurrentOrder);
		}
		return sortedList;
	}
	
	/**
	 * MouseListener for the workbench.
	 */
	protected class GraphMouseAdapter extends MouseAdapter {

//		ActionListener listener;
//
//		GraphMouseAdapter(ActionListener list) {
//			this.listener = list;
//		}
		
		@Override
		public synchronized void mousePressed(MouseEvent e) {
			
			// left-click on background initializes node for formerly selected op
			ALDOperatorLocation recentSelection = 
				ALDGrappaWorkbenchTab.this.workBench.popRecentlySelectedOperatorPath();
			if (e.getButton() == MouseEvent.BUTTON1 &&  recentSelection != null) {
				ALDGrappaWorkbenchTab.this.createNewWorkflowNode(recentSelection, 
						e.getX(), e.getY());
				ALDGrappaWorkbenchTab.this.workBench.clearTreeSelection();
//				try {
//					buildNode(tempOpNamePath, tempOpName, e.getX(), e.getY());
//					tempOpNamePath = null;
//					tempOpName = null;
//					//				opTreePane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//					graphComponent.getGraphHandler().DEFAULT_CURSOR = new Cursor(
//							Cursor.DEFAULT_CURSOR);
//				} catch (ALDOperatorException e1) {
//					JOptionPane.showMessageDialog(null, "Building node \""
//							+ opTree.getLastSelectedPathComponent() + "\" failed!" + " Reason: \n"
//							+ e1.getCommentString(), "Warning",
//							JOptionPane.OK_CANCEL_OPTION);
//				}
//			}
			}
			// double-click on node opens control frame
			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()==2) {
				mxCell cell = 
						(mxCell) ALDGrappaWorkbenchTab.this.getCellAt(e.getX(),e.getY());
				// double-clicks on background are ignored
				if (cell != null) {
					if (cell.isVertex() && !cell.isConnectable()) {
						ALDOperatorConfigurationFrame frame = 
								ALDGrappaWorkbenchTab.this.configWindows.get(cell);
						frame.setVisible(true);
					}
				}
			}
			else if (e.getButton() == MouseEvent.BUTTON1) {
//				System.out.println("Got left click, edge draw...");
//				mxCell cell = (mxCell) ALDGrappaWorkbenchTab.this.getCellAt(
//						e.getX(),e.getY());
////				System.out.println(graphComponent.getGraphHandler().isMoveEnabled());
//				if (cell == null)
//					return;
//				if (cell.isEdge()) {
//					System.out.println("... related to an edge");
////					String linkSource = cell.getSource().getParent().getValue()
////							.toString()
////							+ "@"
////							+ ((ALDPort) cell.getSource().getValue()).getParName();
////					String linkTarget = cell.getTarget().getParent().getValue()
////							.toString()
////							+ "@"
////							+ ((ALDPort) cell.getTarget().getValue()).getParName();
////					mxCell targetNode = (mxCell)cell.getTarget().getParent();
////					System.out.println(linkSource);
////					System.out.println(linkTarget);
//				}
			}
			// on right-click open context menu
			else if (e.getButton() == MouseEvent.BUTTON3) {
				mxCell cell = 
						(mxCell) ALDGrappaWorkbenchTab.this.getCellAt(e.getX(),	e.getY());
				if (cell != null) {
					if (cell.isVertex() && !cell.isConnectable() || cell.isEdge()) {
						ContextMenuNodeEdge menu = new ContextMenuNodeEdge(e.getX(), e.getY());
						menu.show(e.getComponent(), e.getX(), e.getY());
					} else {
						// formerly the following code was for switching ports to text mode
//						ALDOperatorDescriptor opDescr = opDescrMap.get(cell
//								.getParent());
//						ALDOpParameterDescriptor descr = opDescr
//								.getParameterDescriptor(((ALDPort) cell
//										.getValue()).getParName());
//						String link = cell.getParent().getValue().toString()
//								+ "@"
//								+ ((ALDPort) cell.getValue()).getParName();
//						if (descr.getDirection() == Direction.IN
//								|| descr.getDirection() == Direction.INOUT) {
//							if (descr.isLink())
//								descr.setLink(false);
//							else
//								descr.setLink(true);
//							opDescr.getOperatorControlFrame().changePanel(link,
//									null);
//							opDescr.getOperatorControlFrame().setVisible(true);
//						} else {
//							JOptionPane.showMessageDialog(null,
//									"This parameter is an output!", "Warning",
//									JOptionPane.OK_CANCEL_OPTION);
//						}
					}
				}
				// right-click on empty space of workbench, open graph context menu
				else {
					ContextMenuGraph contextMenu= new ContextMenuGraph();
					contextMenu.show(ALDGrappaWorkbenchTab.this, e.getX(), e.getY());
				}
			}
		}
	}

	protected class GraphKeyListener implements KeyListener {

		@SuppressWarnings("synthetic-access")
    @Override
    public void keyPressed(KeyEvent e) {
			// ignore all types if control key is not down
			if (!e.isControlDown())
				return;
			
			// process the key press
			switch(e.getKeyCode())
			{
//			case KeyEvent.VK_A:
//				// execute the whole workflow
//				ALDGrappaWorkbenchTab.this.runWorkflow();
//				break;
			case KeyEvent.VK_C: 
				System.out.println("Copying node...");
//				if (selectedCells.length == 1) {
//					mxCell selectedCell = (mxCell)selectedCells[0];
//				}
				break;
			case KeyEvent.VK_K: 
				System.out.println("Stopping execution...");
//				if (selectedCells.length == 1) {
//					mxCell selectedCell = (mxCell)selectedCells[0];
//				}
				break;
//			case KeyEvent.VK_L: 
//				// load workflow from file
//				ALDGrappaWorkbenchTab.this.workBench.loadWorkflow();
//				break;
//			case KeyEvent.VK_N: 
//				// add new workflow
//				ALDGrappaWorkbenchTab.this.workBench.addNewWorkflow();
//				break;
			case KeyEvent.VK_P: 
				// configure selected nodes
				for (Object cell : 
					ALDGrappaWorkbenchTab.this.graph.getChildVertices(
							ALDGrappaWorkbenchTab.this.graph.getDefaultParent())) {
					if (ALDGrappaWorkbenchTab.this.graph.isCellSelected(cell)) {
						ALDOperatorConfigurationFrame frame = 
								ALDGrappaWorkbenchTab.this.configWindows.get(cell);
						frame.setVisible(true);
					}
				}
				break;
			case KeyEvent.VK_R: 
				// execute the selected node
				Object selectedCells [] = 
						ALDGrappaWorkbenchTab.this.graph.getSelectionCells();
				if (selectedCells.length == 1) {
					mxCell selectedCell = (mxCell)selectedCells[0];
					if (selectedCell.isVertex())
						ALDGrappaWorkbenchTab.this.runWorkflowNode(
								ALDGrappaWorkbenchTab.this.graphNodeIDs.get(selectedCell));
					else
						JOptionPane.showMessageDialog(ALDGrappaWorkbenchTab.this, 
								"You did not select a valid node,\n " + 
								"please check your selection...",
								"Node selection problem", JOptionPane.WARNING_MESSAGE); 
				}
				else {
					JOptionPane.showMessageDialog(ALDGrappaWorkbenchTab.this, 
					"You selected more or less than one node,\n " + 
					"don't know which one to run...?!",
					"Node selection problem", JOptionPane.WARNING_MESSAGE); 
				}
				break;
//			case KeyEvent.VK_S: 
//				ALDGrappaWorkbenchTab.this.workBench.saveWorkflow();
//				break;
//			case KeyEvent.VK_U: 
//				ALDGrappaWorkbenchTab.this.workBench.renameWorkflow();
//				break;
			case KeyEvent.VK_V: 
				System.out.println("Pasting node...");
				break;
//			case KeyEvent.VK_W: 
//				// close workflow
//				ALDGrappaWorkbenchTab.this.workBench.removeWorkflow();
//				break;
			case KeyEvent.VK_X: 
				// remove selected nodes
				for (Object cell : 
					ALDGrappaWorkbenchTab.this.graph.getChildVertices(
							ALDGrappaWorkbenchTab.this.graph.getDefaultParent())) {
					if (ALDGrappaWorkbenchTab.this.graph.isCellSelected(cell))
						ALDGrappaWorkbenchTab.this.removeWorkflowNode((mxCell)cell);
				}
				for (Object cell : 
					ALDGrappaWorkbenchTab.this.graph.getChildEdges(
							ALDGrappaWorkbenchTab.this.graph.getDefaultParent())) {
					if (ALDGrappaWorkbenchTab.this.graph.isCellSelected(cell))
						ALDGrappaWorkbenchTab.this.removeWorkflowEdge((mxCell)cell);
				}
				break;
			}
    }

		@Override
    public void keyReleased(KeyEvent e) {
			// nothing to do here for the moment...
    }

		@Override
    public void keyTyped(KeyEvent e) {
			// nothing to do here for the moment...
    }
		
	}
	
	/**
	 * Event handler for mxGraph events.
	 */
	protected class GraphEventListener implements mxIEventListener {
		
//		private HashMap<mxCell, mxICell> edgeSources= new HashMap<mxCell, mxICell>();
//		
//		private HashMap<mxCell, mxICell> edgeTargets= new HashMap<mxCell, mxICell>();
//
		
		@Override
    public synchronized void invoke(Object sender, mxEventObject event) {
			// init some local helper variables
			String eventName = event.getName();

			if ( ALDGrappaWorkbenchTab.this.debug ) {
				System.out.println("Got event = " + eventName);
			}
			
			// connect events indicate addition/modification of edges
			mxCell edge = null;
			if (eventName.equals("connect")) {
				edge = (mxCell) event.getProperty("cell");
			}
			else if (eventName.equals("connectCell")) {
				edge = (mxCell) event.getProperty("edge");
			}
			if (edge == null || !edge.isEdge()) {
				// nothing to do, if event is not related to an edge...
				return;
			}
			
			// get source and target nodes as defined in mxGraph model
			mxICell sourcePort = edge.getSource();
			mxICell sourceNode = edge.getSource().getParent();
			mxICell targetPort = edge.getTarget();
			mxICell targetNode = edge.getTarget().getParent();
			ALDWorkflowNodeID sourceNodeID = 
					ALDGrappaWorkbenchTab.this.graphNodeIDs.get(sourceNode);
			ALDWorkflowNodeID targetNodeID = 
					ALDGrappaWorkbenchTab.this.graphNodeIDs.get(targetNode);

			// check if edge is known already
			ALDWorkflowEdgeID id = 
					ALDGrappaWorkbenchTab.this.graphEdgeIDs.get(edge);
			
			// redirect event, edge is known already
			if (id != null) {
				ALDGrappaWorkbenchTab.this.redirectWorkflowEdge(edge, id,
						sourceNodeID, targetNodeID, 
						(ALDGrappaNodePortInfo)sourcePort.getValue(), 
						(ALDGrappaNodePortInfo)targetPort.getValue());
			}
			// add new edge
			else {
				ALDGrappaWorkbenchTab.this.createNewWorkflowEdge(edge, 
//						sourceNodeID, (String)sourcePort.getValue(), 
//						targetNodeID, (String)targetPort.getValue());
						sourceNodeID, (ALDGrappaNodePortInfo)sourcePort.getValue(), 
						targetNodeID, (ALDGrappaNodePortInfo)targetPort.getValue());
				//			ALDOperatorDescriptor opDescrTarget = opDescrMap.get(cell
				//					.getTarget().getParent());
				//			ALDOpParameterDescriptor descr = opDescrTarget
				//					.getParameterDescriptor(((ALDPort) cell.getTarget()
				//							.getValue()).getParName());
				//			
				//			// if port is already linked to another input, abort operation
				//			if (descr.isLink()) {
				////				System.out.println("Port is already linked...");
				//				mxICell target = this.edgeTargets.get(cell);
				//				if (target != null) {
				//					ALDOperatorDescriptor tdescr = opDescrMap.get(target.getParent());
				//					ALDOpParameterDescriptor tpdescr = tdescr
				//							.getParameterDescriptor(((ALDPort) target.getValue()).getParName());
				//					tpdescr.setLink(false);
				//					// TODO HACK!!!! Eigentlich sollte dieses explizite Linkumschalten
				//					//               unnötig sein... irgendwo werden Referenzen ausgetauscht?!
				//					tdescr.getOperatorControlFrame().setDescriptorLinked(target.getParent().getValue().toString() + "@" + ((ALDPort)target.getValue()).getParName(),false);
				//					tdescr.getOperatorControlFrame().changePanel(target.getParent().getValue().toString() + "@" + ((ALDPort)target.getValue()).getParName(),null);
				//				}
				//				opGraph.getModel().remove(cell);
				//				this.edgeSources.remove(cell);
				//				this.edgeTargets.remove(cell);
				//				return;
				//			}
				//			
				//			// if workflow graph is cyclic, abort operation
				//			if (ALDEditorFrame.this.opGraph.getAllNodesTopologicallyOrdered()==null){
				////				System.out.println("Cyclic graph created...");
				////				System.out.println("cell: " + cell.toString() + " , source = " + cell.getSource().toString() + " , target = " + cell.getTarget());
				//				opGraph.getModel().remove(cell);
				//				this.edgeSources.remove(cell);
				//				this.edgeTargets.remove(cell);
				//				return;
				//			}
				//
				//			String linkSource = cell.getSource().getParent().getValue()
				//					.toString()
				//					+ "@"
				//					+ ((ALDPort) cell.getSource().getValue()).getParName();
				//			String linkTarget = cell.getTarget().getParent().getValue()
				//					.toString()
				//					+ "@"
				//					+ ((ALDPort) cell.getTarget().getValue()).getParName();
				//			mxCell targetNode = (mxCell)cell.getTarget().getParent();
				//			if (validateLink(linkSource, linkTarget)) {
				////				System.out.println("Validate successful...");
				//				descr.setLink(true);
				//				opDescrTarget.getOperatorControlFrame().setDescriptorLinked(linkTarget,true);
				//				opDescrTarget.getOperatorControlFrame().changePanel(linkTarget,
				//						linkSource);
				//				opDescrMap.get(cell.getSource().getParent()).addChild(
				//						cell.getTarget().getParent().getValue().toString());
				//				// modify old target operator port
				//				mxICell target = null;
				//				if ((target = this.edgeTargets.get(cell)) != null) {
				//					ALDOperatorDescriptor tdescr = opDescrMap.get(target.getParent());
				//					ALDOpParameterDescriptor tpdescr = tdescr
				//							.getParameterDescriptor(((ALDPort) target.getValue()).getParName());
				//					tpdescr.setLink(false);
				//					// TODO HACK!!!! Eigentlich sollte dieses explizite Linkumschalten
				//					//               unnötig sein... irgendwo werden Referenzen ausgetauscht?!
				//					tdescr.getOperatorControlFrame().setDescriptorLinked(target.getParent().getValue().toString() + "@" + ((ALDPort)target.getValue()).getParName(),false);
				//					tdescr.getOperatorControlFrame().changePanel(target.getParent().getValue().toString() + "@" + ((ALDPort)target.getValue()).getParName(),null);
				//				}
				//				// memorize edge
				//				this.edgeSources.put(cell, cell.getSource());
				//				this.edgeTargets.put(cell, cell.getTarget());
				////				System.out.println("cell: " + cell.toString() + " , source = " + cell.getSource().toString() + " , target = " + cell.getTarget());
				//			}
				//			else {
				////				System.out.println("Validate failed...");
				//				if (this.edgeTargets.get(cell) == null) {
				////					System.out.println("cell: " + cell.toString() + " , source = " + cell.getSource().toString() + " , target = " + cell.getTarget());
				////					System.out.println("Edge was not linked before...");
				//					opGraph.getModel().remove(cell);
				//					this.edgeSources.remove(cell);
				//					this.edgeTargets.remove(cell);
				//				}
				//				else {
				//					mxICell target = this.edgeTargets.get(cell);
				//					ALDOperatorDescriptor tdescr = opDescrMap.get(target.getParent());
				//					ALDOpParameterDescriptor tpdescr = tdescr
				//							.getParameterDescriptor(((ALDPort) target.getValue()).getParName());
				//					tpdescr.setLink(false);
				//					// TODO HACK!!!! Eigentlich sollte dieses explizite Linkumschalten
				//					//               unnötig sein... irgendwo werden Referenzen ausgetauscht?!
				//					tdescr.getOperatorControlFrame().setDescriptorLinked(target.getParent().getValue().toString() + "@" + ((ALDPort)target.getValue()).getParName(),false);
				//					tdescr.getOperatorControlFrame().changePanel(target.getParent().getValue().toString() + "@" + ((ALDPort)target.getValue()).getParName(),null);
				////					System.out.println("Edge was linked before...");
				////					System.out.println("cell: " + cell.toString() + " , source = " + cell.getSource().toString() + " , target = " + cell.getTarget());
				//					opGraph.getModel().remove(cell);
				//					this.edgeSources.remove(cell);
				//					this.edgeTargets.remove(cell);
				//					//					cell.setTarget(this.edgeTargets.get(cell));
				//					//					opGraph.insertEdge(null, null, null, this.edgeSources.get(cell), this.edgeTargets.get(cell));
				//					//					System.out.println("cell: " + cell.toString() + " , source = " + cell.getSource().toString() + " , target = " + cell.getTarget());
				//				}
				//			}
				//			// change node color
				//			if (ALDEditorFrame.this.opDescrMap.get(targetNode).getOperatorControlFrame().validateParameters(false, false)) {
				//				ALDEditorFrame.this.setToConfigured(targetNode.getValue().toString(),true);
				//			}
				//			else {
				//				ALDEditorFrame.this.setToConfigured(targetNode.getValue().toString(),false);
				//			}
			}
		}
	}
	
	/**
	 * Class to handle parameter update events triggered configuration windows.
	 * @author moeller
	 */
	protected class ParameterUpdateListener 
		implements ALDOpParameterUpdateEventListener {

		/**
		 * ID of the corresponding Grappa node.
		 */
		private ALDWorkflowNodeID id;
		
		/**
		 * Default constructor.
		 * @param nodeID	ID of node attached to this listener object.
		 */
		public ParameterUpdateListener(ALDWorkflowNodeID nodeID) {
			this.id = nodeID;
		}
		
		@Override
    public void handleALDParameterUpdateEvent(ALDOpParameterUpdateEvent e) {
			// notify workflow of change in node parameters
			try {
				if ( ALDGrappaWorkbenchTab.this.debug ) {
					System.out.println("Parameters of node " + this.id 
						+ " were updated, i.e., changed or reloaded...");
				}
				switch(e.getType())
				{
				case CHANGED:
					ALDGrappaWorkbenchTab.this.alidaWorkflow.nodeParameterChanged(
						this.id);
					ALDGrappaWorkbenchTab.this.processWorkflowEventQueue();
					break;
				case	LOADED:
					ALDGrappaWorkbenchTab.this.alidaWorkflow.setOperator(	this.id, 
						ALDGrappaWorkbenchTab.this.configWindows.get(
							ALDGrappaWorkbenchTab.this.graphNodes.get(this.id)).
								getOperator());
					break;
				}
				// process event queue
				ALDGrappaWorkbenchTab.this.processWorkflowEventQueue();
			} catch (ALDWorkflowException ex) {
				System.err.println("[ParameterChangeListener] Warning! " 
						+ "could not propagate parameter change event, node not found!");
			}	  
		}
	}

	protected class WorkflowModifyAction {
		
		public ALDWorkflowNodeID oldEdgeTarget;
		
		public String oldEdgeTargetParamName;
		
		/**
		 * X-coordinate of the working area where action happened.
		 */
		protected int actionPosition_x;
		
		/**
		 * Y-coordinate of the working area where action happened.
		 */
		protected int actionPosition_y;
		
		/**
		 * Default constructor.
		 */
		public WorkflowModifyAction() {
			// nothing to do here
		}
		
		/**
		 * Set x-coordinate of action's position.
		 * @param x		X-coordinate where action happened.
		 */
		public void setActionPositionX(int x) {
			this.actionPosition_x = x;
		}
		
		/**
		 * Set y-coordinate of action's position.
		 * @param y		Y-coordinate where action happened.
		 */
		public void setActionPositionY(int y) {
			this.actionPosition_y = y;
		}
		
		/**
		 * Returns the x-coordinate of the action's position.
		 * @return	x-coordinate of position.
		 */
		public int getActionPositionX() {
			return this.actionPosition_x;
		}

		/**
		 * Returns the y-coordinate of the action's position.
		 * @return	y-coordinate of position.
		 */
		public int getActionPositionY() {
			return this.actionPosition_y;
		}
	}
	
	/**
	 * Context menu for nodes and edges within a workflow.
	 */
	protected class ContextMenuNodeEdge extends JPopupMenu 
		implements ActionListener {
		
		/**
		 * Graph object to which the menu is to be attached.
		 * <p>
		 * The object can either be a graph node or an edge. Depending on 
		 * its type the menu shows different contents.
		 */
		private mxCell cell;
		
		/**
		 * Checkbox for selecting which set of parameters and ports, to show.
		 * <p>
		 * Either all parameters of an operator can be shown, which is 
		 * defined to be the expert mode, or only a subset of parameters
		 * dedicated to non-expert usage can be shown. If the checkbox is
		 * selected, all parameters are shown.
		 */
		private JCheckBoxMenuItem parameterShowModeItem;

		/**
		 * Default constructor for context menu.
		 * @param x		X-coordinate of mouse-click position.
		 * @param y		Y-coordinate of mouse-click position.
		 */
		protected ContextMenuNodeEdge(int x, int y) {
			this.cell = (mxCell)ALDGrappaWorkbenchTab.this.getCellAt(x, y);

			// context menu for nodes
			if (this.cell.isVertex()) {
				JMenuItem confItem = new JMenuItem("Configure");
				confItem.setActionCommand("configure");
				confItem.addActionListener(this);
				confItem.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_P, ActionEvent.CTRL_MASK));

				JMenu runMenu = new JMenu("Run...");

				JMenuItem runFlowItem = new JMenuItem("Workflow");
				runFlowItem.setActionCommand("runFlow");
				runFlowItem.addActionListener(this);
				runFlowItem.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_A, ActionEvent.CTRL_MASK));

				JMenuItem runUptoItem = new JMenuItem("Node");
				runUptoItem.setActionCommand("runNode");
				runUptoItem.addActionListener(this);
				runUptoItem.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_R, ActionEvent.CTRL_MASK));

				JMenuItem runFromItem = new JMenuItem("Nodes from here");
				runFromItem.setActionCommand("runFromHere");
				runFromItem.addActionListener(this);
//				JMenuItem runItem2 = new JMenuItem("without backtracking");
//				runItem2.setActionCommand("run2");
//				runItem2.addActionListener(this);
//				runFromMenu.add(runItem1);
//				runFromMenu.add(runItem2);

//				JMenu runNodeMenu = new JMenu("node");
//				JMenuItem runNodeItem1 = new JMenuItem("with backtracking");
//				runNodeItem1.setActionCommand("runNode1");
//				runNodeItem1.addActionListener(this);
//				JMenuItem runNodeItem2 = new JMenuItem("without backtracking");
//				runNodeItem2.setActionCommand("runNode2");
//				runNodeItem2.addActionListener(this);
//				runNodeMenu.add(runNodeItem1);
//				runNodeMenu.add(runNodeItem2);

				runMenu.add(runFlowItem);
				runMenu.add(runFromItem);
				runMenu.add(runUptoItem);

				JMenuItem stopItem = new JMenuItem("Stop");
				stopItem.setActionCommand("stop");
				stopItem.addActionListener(this);
				stopItem.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_K, ActionEvent.CTRL_MASK));

				JMenuItem showItem = new JMenuItem("Show Results");
				showItem.setActionCommand("show");
				showItem.addActionListener(this);
				if (!this.cell.getStyle().contains("strokeColor=green")) {
					showItem.setEnabled(false);
				}

				JMenuItem removeItem = new JMenuItem("Delete Node");
				removeItem.setActionCommand("removeNode");
				removeItem.addActionListener(this);
				removeItem.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_X, ActionEvent.CTRL_MASK));
				JMenu copyNodeMenu = new JMenu("Copy node...");
				JMenuItem copyComplete = new JMenuItem("config + links");
				copyComplete.setActionCommand("copyNodeComplete");
				copyComplete.addActionListener(this);
				copyComplete.setEnabled(false);
				copyComplete.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_C, ActionEvent.CTRL_MASK));
				JMenuItem copyConfigOnly = new JMenuItem("only config");
				copyConfigOnly.setActionCommand("copyNodeOnlyConfig");
				copyConfigOnly.addActionListener(this);
				copyConfigOnly.setEnabled(false);
				JMenuItem copyLinksOnly = new JMenuItem("... links only");
				copyLinksOnly.setActionCommand("copyNodeOnlyLinks");
				copyLinksOnly.addActionListener(this);
				copyNodeMenu.add(copyComplete);
				copyNodeMenu.add(copyConfigOnly);
				copyNodeMenu.add(copyLinksOnly);

				JMenu viewMenu = new JMenu("Options");
				// request the view mode of the node
				Boolean selected = 
						ALDGrappaWorkbenchTab.this.
							nodeConfigShowAllParameters.get(this.cell);
				this.parameterShowModeItem = 
						new JCheckBoxMenuItem("Show All Parameters", 
								selected.booleanValue());
				this.parameterShowModeItem.setActionCommand("viewModeChanged");
				this.parameterShowModeItem.addActionListener(this);
				viewMenu.add(this.parameterShowModeItem);

				this.add(confItem);
				this.add(runMenu);
				this.add(showItem);
				this.addSeparator();
				this.add(viewMenu);
//				this.add(copyNodeMenu);
				this.addSeparator();
				this.add(removeItem);
				this.add(copyNodeMenu);
			} 
			// context menu for edges
			else {
				JMenuItem removeItem = new JMenuItem("Remove edge");
				removeItem.setActionCommand("removeEdge");
				removeItem.addActionListener(this);
				removeItem.setAccelerator(KeyStroke.getKeyStroke(
						KeyEvent.VK_X, ActionEvent.CTRL_MASK));
				this.add(removeItem);
			}
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
    public synchronized void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();

			if (command.equals("configure")) {
				ALDOperatorConfigurationFrame frame = 
						ALDGrappaWorkbenchTab.this.configWindows.get(this.cell);
				frame.setVisible(true);
			} 
			else if (command.equals("runFlow")) {
				ALDGrappaWorkbenchTab.this.runWorkflow();
			} 
			else if (command.equals("runNode")) {
				ALDGrappaWorkbenchTab.this.runWorkflowNode(
						ALDGrappaWorkbenchTab.this.graphNodeIDs.get(this.cell));
			} 
			else if (command.equals("runFromHere")) {
				ALDGrappaWorkbenchTab.this.runWorkflowFromNode(
						ALDGrappaWorkbenchTab.this.graphNodeIDs.get(this.cell));
			} 
			else if (command.equals("stop")) {
				ALDGrappaWorkbenchTab.this.interruptExecution();
			}
			else if (command.equals("show")) {
				ALDWorkflowNodeID nodeID = 
						ALDGrappaWorkbenchTab.this.graphNodeIDs.get(this.cell);
				ALDGrappaWorkbenchTab.this.handleShowResultsEvent(nodeID);
			} 
			else if (command.equals("viewModeChanged")) {
				if (this.parameterShowModeItem.isSelected()) {
					// display all parameters
					ALDGrappaWorkbenchTab.this.setWorkflowNodeViewMode(
							this.cell, Parameter.ExpertMode.ADVANCED);
				} else {
					// display only default (non-expert) parameters
					ALDGrappaWorkbenchTab.this.setWorkflowNodeViewMode(
							this.cell, Parameter.ExpertMode.STANDARD);
				}
			}			
			// copy node including links and configuration
			else if (command.equals("copyNodeComplete")) {
			} 
			// copy node, but ignore links
			else if (command.equals("copyNodeOnlyConfig")) {
//				try {
//					mxCell copy = buildNode(cell.getId().split("@")[0], cell
//							.getValue().toString().split("_")[0], cell
//							.getGeometry().getX() + 20, cell.getGeometry()
//							.getY() + 20);
//					for (int i = 0; i < cell.getChildCount(); i++) {
//						Object[] inEdges = opGraph.getIncomingEdges(cell
//								.getChildAt(i));
//						if (inEdges.length != 0) {
//							ALDOperatorDescriptor opDescrTarget = opDescrMap
//									.get(copy);
//							ALDOpParameterDescriptor descr = opDescrTarget
//									.getParameterDescriptor(((ALDPort) copy
//											.getChildAt(i).getValue())
//											.getParName());
//							String linkSource = ((mxCell) inEdges[0])
//									.getSource().getParent().getValue()
//									.toString()
//									+ "@"
//									+ ((ALDPort) ((mxCell) inEdges[0])
//											.getSource().getValue())
//											.getParName();
//							String linkTarget = copy.getValue().toString()
//									+ "@"
//									+ ((ALDPort) cell.getChildAt(i).getValue())
//											.getParName();
//							descr.setLink(true);
//							opDescrTarget.getOperatorControlFrame()
//									.changePanel(linkTarget, linkSource);
//							opDescrTarget.getOperatorControlFrame().setDescriptorLinked(linkTarget,true);
//							drawEdge(linkSource, linkTarget);
//						}
//					}
//					opGraph.refresh();
//				} catch (ALDOperatorException e1) {
//					JOptionPane.showMessageDialog(null, "Copying node \""
//							+ cell.getValue().toString() + "\" failed!"
//							+ " Reason: \n" + e1.getCommentString(), "Warning",
//							JOptionPane.OK_CANCEL_OPTION);
//				}
			} 
			// copy node, but omit configuration and consider only links
			else if (command.equals("copyNodeOnlyLinks")) {
        try {
  				ALDOperator srcOp;
	        srcOp = ALDGrappaWorkbenchTab.this.alidaWorkflow.getOperator(
	        		ALDGrappaWorkbenchTab.this.graphNodeIDs.get(this.cell));
	        ALDOperator newOp = srcOp.getClass().newInstance();
					ALDGrappaWorkbenchTab.this.copyWorkflowNodeLinksOnly(
																						srcOp, this.cell, newOp, -1, -1);
        } catch (ALDWorkflowException e1) {
	        e1.printStackTrace();
        }
//				try {
//					buildNode(cell.getId().split("@")[0], cell.getValue()
//							.toString().split("_")[0], cell.getGeometry()
//							.getX() + 20, cell.getGeometry().getY() + 20);
//				} catch (ALDOperatorException e1) {
//					JOptionPane.showMessageDialog(null, "Copying node \""
//							+ cell.getValue().toString() + "\" failed!"
//							+ " Reason: \n" + e1.getCommentString(), "Warning",
//							JOptionPane.OK_CANCEL_OPTION);
//				}
        catch (InstantiationException e1) {
        	// TODO Auto-generated catch block
        	e1.printStackTrace();
        } catch (IllegalAccessException e1) {
        	// TODO Auto-generated catch block
        	e1.printStackTrace();
        }
//				try {
//					mxCell copy = buildNode(cell.getId().split("@")[0], cell
//							.getValue().toString().split("_")[0], cell
//							.getGeometry().getX() + 20, cell.getGeometry()
//							.getY() + 20);
//
//					ALDOperatorDescriptor opDescr1 = opDescrMap.get(cell);
//					ALDOperatorDescriptor opDescr2 = opDescrMap.get(copy);
//
//					opDescr1.getOperatorControlFrame().copyConfiguration(
//							opDescr2.getOperatorControlFrame());
//
//					for (int i = 0; i < cell.getChildCount(); i++) {
//						Object[] inEdges = opGraph.getIncomingEdges(cell
//								.getChildAt(i));
//						if (inEdges.length != 0) {
//							ALDOperatorDescriptor opDescrTarget = opDescrMap
//									.get(copy);
//							ALDOpParameterDescriptor descr = opDescrTarget
//									.getParameterDescriptor(((ALDPort) copy
//											.getChildAt(i).getValue())
//											.getParName());
//							String linkSource = ((mxCell) inEdges[0])
//									.getSource().getParent().getValue()
//									.toString()
//									+ "@"
//									+ ((ALDPort) ((mxCell) inEdges[0])
//											.getSource().getValue())
//											.getParName();
//							String linkTarget = copy.getValue().toString()
//									+ "@"
//									+ ((ALDPort) cell.getChildAt(i).getValue())
//											.getParName();
//							descr.setLink(true);
//							opDescrTarget.getOperatorControlFrame().setDescriptorLinked(linkTarget,true);
//							opDescrTarget.getOperatorControlFrame()
//									.changePanel(linkTarget, linkSource);
//							drawEdge(linkSource, linkTarget);
//						}
//					}
//					opGraph.refresh();
//				} catch (ALDOperatorException e1) {
//					JOptionPane.showMessageDialog(null, "Copying node \""
//							+ cell.getValue().toString() + "\" failed!"
//							+ " Reason: \n" + e1.getCommentString(), "Warning",
//							JOptionPane.OK_CANCEL_OPTION);
//				} catch (ALDDataIOException e2) {
//					// TODO Auto-generated catch block
//					e2.printStackTrace();
//				}
			} 
			else if (command.equals("removeNode")) {
				ALDGrappaWorkbenchTab.this.removeWorkflowNode(this.cell);
			} 
			else if (command.equals("removeEdge")) {
				ALDGrappaWorkbenchTab.this.removeWorkflowEdge(this.cell);
			}
		}
	}
	
	/**
	 * Context menu for whole workbench.
	 * <p>
	 * This menu is activated on right-click on background.
	 */
	protected class ContextMenuGraph extends JPopupMenu 
		implements ActionListener {
		
		/**
		 * Default constructor.
		 */
		protected ContextMenuGraph() {
			JMenuItem newItem = new JMenuItem("New");
			newItem.setActionCommand("new");
			newItem.addActionListener(this);
//			newItem.setAccelerator(KeyStroke.getKeyStroke(
//					KeyEvent.VK_N, ActionEvent.CTRL_MASK));
			JMenuItem closeItem = new JMenuItem("Close");
			closeItem.setActionCommand("close");
			closeItem.addActionListener(this);
//			closeItem.setAccelerator(KeyStroke.getKeyStroke(
//					KeyEvent.VK_W, ActionEvent.CTRL_MASK));
			JMenuItem loadItem = new JMenuItem("Load");
			loadItem.setActionCommand("load");
			loadItem.addActionListener(this);
//			loadItem.setAccelerator(KeyStroke.getKeyStroke(
//					KeyEvent.VK_L, ActionEvent.CTRL_MASK));
			JMenuItem saveItem = new JMenuItem("Save");
			saveItem.setActionCommand("save");
			saveItem.addActionListener(this);
//			saveItem.setAccelerator(KeyStroke.getKeyStroke(
//					KeyEvent.VK_S, ActionEvent.CTRL_MASK));
			JMenuItem renameItem = new JMenuItem("Rename");
			renameItem.setActionCommand("rename");
			renameItem.addActionListener(this);
//			renameItem.setAccelerator(KeyStroke.getKeyStroke(
//					KeyEvent.VK_U, ActionEvent.CTRL_MASK));
			JMenuItem runItem = new JMenuItem("Run");
			runItem.setActionCommand("run");
			runItem.addActionListener(this);
//			runItem.setAccelerator(KeyStroke.getKeyStroke(
//					KeyEvent.VK_A, ActionEvent.CTRL_MASK));
			JMenuItem stopItem = new JMenuItem("Stop");
			stopItem.setActionCommand("stop");
			stopItem.addActionListener(this);
//			stopItem.setAccelerator(KeyStroke.getKeyStroke(
//					KeyEvent.VK_K, ActionEvent.CTRL_MASK));
			this.add(newItem);
			this.add(renameItem);
			this.add(closeItem);
			this.addSeparator();
			this.add(loadItem);
			this.add(saveItem);
			this.addSeparator();
			this.add(runItem);
			this.add(stopItem);
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
    public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("new")) { 
				ALDGrappaWorkbenchTab.this.workBench.addNewWorkflow();
			} 
			else if (command.equals("rename")) {
				ALDGrappaWorkbenchTab.this.workBench.renameWorkflow();
			}
			else if (command.equals("close")) {
				ALDGrappaWorkbenchTab.this.workBench.removeWorkflow();
			}
			else if (command.equals("load")) {
				ALDGrappaWorkbenchTab.this.workBench.loadWorkflow();
			}
			else if (command.equals("save")) {
				ALDGrappaWorkbenchTab.this.workBench.saveWorkflow();
			}
			else if (command.equals("run")) {
				ALDGrappaWorkbenchTab.this.runWorkflow();
			}
			else if (command.equals("stop")) {
				ALDGrappaWorkbenchTab.this.interruptExecution();
			}
		}
	}
}
