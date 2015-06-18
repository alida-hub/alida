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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;

import com.mxgraph.swing.util.mxGraphTransferable;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.gui.ALDOperatorChooserTree;
import de.unihalle.informatik.Alida.gui.ALDOperatorChooserTreeNode;
import de.unihalle.informatik.Alida.operator.ALDOperatorLocation;

/**
 * Main frame of Grappa graphical editor for Alida.
 * 
 * @author Birgit Moeller
 */
public class ALDGrappaFrame extends JFrame implements ActionListener {
	
	/**
	 * Tree of available operators.
	 */
	protected ALDOperatorChooserTree opTree;

	/**
	 * Scroll pane displaying the operator tree.
	 */
	protected JScrollPane opTreePane;

	/**
	 * Stores the recently selected path in the chooser tree.
	 */
	protected ALDOperatorLocation lastSelectedLocation = null;
	
	/**
	 * Component which holds the graph.
	 */
	protected ALDGrappaWorkbench workBench;

	/**
	 * Menubar.
	 */
	protected ALDGrappaMenuBar grappaMenu;
	
	/**
	 * Text field to enter filter to select operators
	 */
	protected JTextField filterField;
	
	/**
	 * Label of status bar, changes dynamically.
	 */
	protected JTextArea status;
	
	/**
	 * Default constructor
	 * 
	 * @throws ALDOperatorException
	 */
	public ALDGrappaFrame(
			Collection<ALDOperatorLocation> standardLocations,
			Collection<ALDOperatorLocation> applicationLocations) {
		super();

		// JGraphX has some problems accessing the right class loader, 
		// sometimes that seems to fail and nodes cannot be dragged in Grappa;
		// this is a solution to the problem as described at
		// http://undocumentedmatlab.com/blog/jgraph-and-bde/
		try {
	    mxGraphTransferable.dataFlavor = new DataFlavor(
	    	DataFlavor.javaJVMLocalObjectMimeType
	    		+ "; class=com.mxgraph.swing.util.mxGraphTransferable", null,
	    			new com.mxgraph.swing.util.mxGraphTransferable(null, null).
	    				getClass().getClassLoader());
    } catch (ClassNotFoundException e) {
    	System.err.println("[ALDGrappaFrame] cannot initialize JGraphX " + 
    			"transferables, class not found!");
	    e.printStackTrace();
    }
		
		// configure operator selection menu
		this.opTree = new ALDOperatorChooserTree(standardLocations, 
				applicationLocations);
		// add the listener for mouse actions appearing in the tree
		this.opTree.addMouseListener(new ChooserTreeMouseAdapter());
		this.opTreePane = new JScrollPane(this.opTree);
		this.opTreePane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		// panel to hold tree and filter panel
		JPanel selectionPanel = new JPanel();
		selectionPanel.setLayout(new BorderLayout());
		JPanel filterPanel = new JPanel();
		filterPanel.add(new JLabel("Filter:"));
		this.filterField = new JTextField(18);
		this.filterField.setActionCommand( "filter");
		this.filterField.addActionListener(this);
		filterPanel.add(this.filterField);
		selectionPanel.add(filterPanel,BorderLayout.NORTH);
		selectionPanel.add(this.opTreePane);
		
		// add workbench
		this.workBench = this.initWorkbench();
		
		// add menu bar
		this.grappaMenu = 
			new ALDGrappaMenuBar(this,this.opTree,this.workBench);
		this.setJMenuBar(this.grappaMenu);
		
		// create split pane
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				selectionPanel, this.workBench);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(300);
		
		Dimension minimumSize = new Dimension(100, 50);
		this.opTreePane.setMinimumSize(minimumSize);
		this.workBench.setMinimumSize(minimumSize);
		
		this.add( splitPane);
		
		// add status bar
		String activeFlow = this.workBench.getWorkflowName();
		this.status = new JTextArea(5, 200);
		this.status.setLineWrap(true);
		// make sure that scrollbar  is always at bottom
		DefaultCaret caret = (DefaultCaret)this.status.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		// add the scroll pane
		JScrollPane scrollPane = new JScrollPane(this.status); 
		scrollPane.setVerticalScrollBarPolicy(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		this.status.setEditable(false);
		this.postWorkflowStatusMessage(activeFlow, "ready");
		this.add(scrollPane, BorderLayout.SOUTH);
		
		// some global settings
		this.setTitle("Grappa");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setMinimumSize(new Dimension(800, 500));
		this.setPreferredSize(new Dimension(800, 500));
		this.pack();
	}

	protected ALDGrappaWorkbench initWorkbench() {
		return new ALDGrappaWorkbench(this);
	}

	/**
	 * Post a new status message to the log window.
	 * @param wflow		Name of corresponding workflow.
	 * @param msg			Message to display.
	 */
	protected synchronized void postWorkflowStatusMessage(
			String wflow, String msg) {
		if (this.status != null)
			this.status.append("[" + wflow + "] " + msg + "\n");
	}
	
	/**
	 * Post a new system status message to the log window.
	 * @param msg			Message to display.
	 */
	protected synchronized void postSystemStatusMessage(String msg) {
		if (this.status != null)
			this.status.append(" === " + msg + "\n");
	}

	public ALDOperatorLocation popRecentlySelectedOperatorLocation() {
		ALDOperatorLocation selectedPath = this.lastSelectedLocation;
		this.lastSelectedLocation = null;
		return selectedPath;
	}

	/**
	 * Terminates Grappa after some clean-ups.
	 */
	public void quit() {
		this.workBench.quit();
		this.setVisible(false);
	}
	
	@Override
	public void actionPerformed(ActionEvent ev) {
		// extract command
		String command = ev.getActionCommand();
		if (command.equals("filter")) {
			this.opTree.setOpNameFilter( this.filterField.getText());
		}
	}

	/**
	 * MouseListener for the operator tree.
	 */
	protected class ChooserTreeMouseAdapter extends MouseAdapter {

//		ActionListener listener;
//
//		TreeMouseAdapter(ActionListener list) {
//			this.listener = list;
//		}

		@Override
		public void mouseClicked(MouseEvent e) {
			
			// received a click, get currently selected tree entries
			if (e.getClickCount() == 1) {
        ALDOperatorChooserTreeNode node = 
          	ALDGrappaFrame.this.opTree.getLastSelectedPathComponent();
        if (node != null && node.isOperator()) {
					ALDGrappaFrame.this.lastSelectedLocation = node.getLocation();
//					ALDGrappaFrame.this.opTreePane.setCursor(new Cursor(Cursor.HAND_CURSOR));
//					ALDGrappaFrame.this.graphComponent.getGraphHandler().DEFAULT_CURSOR = 
//							new Cursor(Cursor.HAND_CURSOR);
				}
			}

			// on double-click add selected operator to graph
			if (e.getClickCount() == 2) {
				// get selected node
        ALDOperatorChooserTreeNode node = 
        	ALDGrappaFrame.this.opTree.getLastSelectedPathComponent();
				if (node != null && node.isOperator()) {
					ALDGrappaFrame.this.workBench.addNodeToWorkflow(
							ALDGrappaFrame.this.lastSelectedLocation);
//					try {
//						mxCell tmpNode = ALDGrappaFrame.this.buildNode(
//								ALDGrappaFrame.this.opTree.getLastSelectedPathComponent().
//																																getFullName(), 
//								ALDGrappaFrame.this.opTree.getLastSelectedPathComponent().
//																													toString(),	50, 50);
//						if (tmpNode == null) {
//							JOptionPane.showMessageDialog(null, "Building node \""
//									+ ALDGrappaFrame.this.opTree.getLastSelectedPathComponent() 
//									+ "\" failed!", "Warning", JOptionPane.OK_CANCEL_OPTION);
//							return;
//						}
						ALDGrappaFrame.this.lastSelectedLocation = null;
						ALDGrappaFrame.this.opTree.clearSelection();
//						ALDGrappaFrame.this.opTreePane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//						ALDGrappaFrame.this.graphComponent.getGraphHandler().DEFAULT_CURSOR = new Cursor(
//								Cursor.DEFAULT_CURSOR);
//					} catch (ALDOperatorException e1) {
//						JOptionPane.showMessageDialog(null, "Building node \""
//								+ ALDGrappaFrame.this.opTree.getLastSelectedPathComponent() + "\" failed!"
//								+ " Reason: \n" + e1.getCommentString(),
//								"Warning", JOptionPane.OK_CANCEL_OPTION);
//					}
				}
			}
		}
	}
//
//	/**
//	 * MouseListener for the graph with operators.
//	 */
//	class GraphMouseAdapter extends MouseAdapter {
//
//		ActionListener listener;
//
//		GraphMouseAdapter(ActionListener list) {
//			this.listener = list;
//		}
//
//		@Override
//		public void mousePressed(MouseEvent e) {
//			
////			System.out.println("Mouse event caught...");
////			System.out.println(e.toString());
//			
//			// double-click on node opens control frame
//			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()==2) {
//				mxCell cell = (mxCell) ALDGrappaFrame.this.graphComponent.getCellAt(
//																														e.getX(),e.getY());
//				// double-clicks on background are ignored
//				if (cell != null) {
//					if (cell.isVertex() && !cell.isConnectable()) {
//						ALDOperatorControlFrame frame = 
//							ALDGrappaFrame.this.opDescrMap.get(cell).
//																										getOperatorControlFrame();
//						frame.setVisible(true);
//					}
//				}
//			}
//			// left-click on background initializes node for formerly selected op
//			else if (e.getButton() == MouseEvent.BUTTON1 && tempOpName != null
//					&& tempOpNamePath != null) {
//				try {
//					buildNode(tempOpNamePath, tempOpName, e.getX(), e.getY());
//					tempOpNamePath = null;
//					tempOpName = null;
////					opTreePane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//					graphComponent.getGraphHandler().DEFAULT_CURSOR = new Cursor(
//							Cursor.DEFAULT_CURSOR);
//				} catch (ALDOperatorException e1) {
//					JOptionPane.showMessageDialog(null, "Building node \""
//							+ opTree.getLastSelectedPathComponent() + "\" failed!" + " Reason: \n"
//							+ e1.getCommentString(), "Warning",
//							JOptionPane.OK_CANCEL_OPTION);
//				}
//			}
////			else if (e.getButton() == MouseEvent.BUTTON1 
////					&& (tempOpName == null || tempOpNamePath == null)) {
////				System.out.println("Got left click, edge draw...");
////				mxCell cell = (mxCell) ALDEditorFrame.this.graphComponent.getCellAt(
////						e.getX(),e.getY());
////				System.out.println(graphComponent.getGraphHandler().isMoveEnabled());
////				if (cell == null)
////					return;
////				if (cell.isEdge()) {
////					System.out.println("... related to an edge");
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
////				}
////			}
//			// on right-click open context menu
//			else if (e.getButton() == MouseEvent.BUTTON3) {
//				mxCell cell = (mxCell) graphComponent.getCellAt(e.getX(),
//						e.getY());
//				if (cell != null) {
//					if (cell.isVertex() && !cell.isConnectable()
//							|| cell.isEdge()) {
//						PopUpMenu menu = new PopUpMenu(e.getX(), e.getY());
//						menu.show(e.getComponent(), e.getX(), e.getY());
//					} else {
//						// formerly the following code was for switching ports to text mode
////						ALDOperatorDescriptor opDescr = opDescrMap.get(cell
////								.getParent());
////						ALDOpParameterDescriptor descr = opDescr
////								.getParameterDescriptor(((ALDPort) cell
////										.getValue()).getParName());
////						String link = cell.getParent().getValue().toString()
////								+ "@"
////								+ ((ALDPort) cell.getValue()).getParName();
////						if (descr.getDirection() == Direction.IN
////								|| descr.getDirection() == Direction.INOUT) {
////							if (descr.isLink())
////								descr.setLink(false);
////							else
////								descr.setLink(true);
////							opDescr.getOperatorControlFrame().changePanel(link,
////									null);
////							opDescr.getOperatorControlFrame().setVisible(true);
////						} else {
////							JOptionPane.showMessageDialog(null,
////									"This parameter is an output!", "Warning",
////									JOptionPane.OK_CANCEL_OPTION);
////						}
//					}
//				}
//				else {
//					PopUpMenuBench menu = new PopUpMenuBench(e.getX(), e.getY());
//					menu.show(ALDGrappaFrame.this.graphComponent, e.getX(), e.getY());
//				}
//			}
//		}
//	}
//
//	/**
//	 * ConnectionListener for drawing an edge.
//	 */
//	class EdgeConnectListener implements mxIEventListener {
//		
//		private HashMap<mxCell, mxICell> edgeSources= new HashMap<mxCell, mxICell>();
//		
//		private HashMap<mxCell, mxICell> edgeTargets= new HashMap<mxCell, mxICell>();
//
//		@Override
//    public void invoke(Object sender, mxEventObject event) {
//			
////			System.out.println("Received an event...");
////			System.out.println(event.getName());
//			
//			String eventName = event.getName();
//			mxCell cell = null;
//			if (eventName.equals("connect"))
//				cell = (mxCell) event.getProperty("cell");
//			else if (eventName.equals("connectCell"))
//				cell = (mxCell) event.getProperty("edge");
//			if (cell == null || !cell.isEdge()) {
//				// nothing to do, if event is not related to an edge...
////				System.out.println("Event on vertex?!");
//				return;
//			}
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
//			if (ALDGrappaFrame.this.opGraph.getAllNodesTopologicallyOrdered()==null){
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
//			if (ALDGrappaFrame.this.opDescrMap.get(targetNode).getOperatorControlFrame().validateParameters(false, false)) {
//				ALDGrappaFrame.this.setToConfigured(targetNode.getValue().toString(),true);
//			}
//			else {
//				ALDGrappaFrame.this.setToConfigured(targetNode.getValue().toString(),false);
//			}
//		}
//	}
//
//	/**
//	 * Context Menu of nodes.
//	 */
//	protected class PopUpMenu extends JPopupMenu implements ActionListener {
//		private mxCell cell;
//
//		protected PopUpMenu(int x, int y) {
//			cell = (mxCell) graphComponent.getCellAt(x, y);
//
//			if (cell.isVertex()) {
//				JMenuItem confItem = new JMenuItem("Configure operator");
//				confItem.setActionCommand("conf");
//				confItem.addActionListener(this);
//
//				JMenu runMenu = new JMenu("Run");
//
//				JMenuItem runFlowItem = new JMenuItem("Workflow");
//				runFlowItem.setActionCommand("runFlow");
//				runFlowItem.addActionListener(this);
//
////				JMenu runFromMenu = new JMenu("from here");
////				JMenuItem runItem1 = new JMenuItem("with backtracking");
////				runItem1.setActionCommand("runFromHere");
////				runItem1.addActionListener(this);
//				JMenuItem runFromItem = new JMenuItem("...from here");
//				runFromItem.setActionCommand("runFromHere");
//				runFromItem.addActionListener(this);
////				JMenuItem runItem2 = new JMenuItem("without backtracking");
////				runItem2.setActionCommand("run2");
////				runItem2.addActionListener(this);
////				runFromMenu.add(runItem1);
////				runFromMenu.add(runItem2);
//
//				JMenuItem runUptoItem = new JMenuItem("...up to here");
//				runUptoItem.setActionCommand("runUptoHere");
//				runUptoItem.addActionListener(this);
////				JMenu runNodeMenu = new JMenu("node");
////				JMenuItem runNodeItem1 = new JMenuItem("with backtracking");
////				runNodeItem1.setActionCommand("runNode1");
////				runNodeItem1.addActionListener(this);
////				JMenuItem runNodeItem2 = new JMenuItem("without backtracking");
////				runNodeItem2.setActionCommand("runNode2");
////				runNodeItem2.addActionListener(this);
////				runNodeMenu.add(runNodeItem1);
////				runNodeMenu.add(runNodeItem2);
//
//				runMenu.add(runFlowItem);
//				runMenu.add(runFromItem);
//				runMenu.add(runUptoItem);
//
//				JMenuItem showItem = new JMenuItem("Show Results");
//				showItem.setActionCommand("show");
//				showItem.addActionListener(this);
//				if (!cell.getStyle().contains("strokeColor=green")) {
//					showItem.setEnabled(false);
//				}
//
////				JMenu copyNodeMenu = new JMenu("Copy node");
////				JMenuItem copyItem1 = new JMenuItem("without configuration");
////				copyItem1.setActionCommand("copyNode1");
////				copyItem1.addActionListener(this);
////				JMenuItem copyItem2 = new JMenuItem("with links");
////				copyItem2.setActionCommand("copyNode2");
////				copyItem2.addActionListener(this);
////				JMenuItem copyItem3 = new JMenuItem("with configuration");
////				copyItem3.setActionCommand("copyNode3");
////				copyItem3.addActionListener(this);
////				copyNodeMenu.add(copyItem1);
////				copyNodeMenu.add(copyItem2);
////				copyNodeMenu.add(copyItem3);
//
//				JMenuItem removeItem = new JMenuItem("Remove node");
//				removeItem.setActionCommand("removeNode");
//				removeItem.addActionListener(this);
//
//				JMenu viewMenu = new JMenu("Viewmode");
//				JMenuItem standardItem = new JMenuItem("Standard");
//				standardItem.setActionCommand("standard");
//				standardItem.addActionListener(this);
//				JMenuItem advancedItem = new JMenuItem("Advanced");
//				advancedItem.setActionCommand("advanced");
//				advancedItem.addActionListener(this);
//				viewMenu.add(standardItem);
//				viewMenu.add(advancedItem);
//
//				this.add(confItem);
//				this.add(runMenu);
//				this.add(showItem);
//				this.addSeparator();
//				this.add(viewMenu);
////				this.add(copyNodeMenu);
//				this.add(removeItem);
//			} else {
//				JMenuItem removeItem = new JMenuItem("Remove edge");
//				removeItem.setActionCommand("removeEdge");
//				removeItem.addActionListener(this);
//				this.add(removeItem);
//			}
//		}
//
//		/**
//		 * Change the mode for displaying parameters.
//		 * 
//		 * @param cell
//		 *            Node with the operator.
//		 * @param handlingMode
//		 *            New handling mode.
//		 */
//		private void changeView(mxCell cell, Parameter.ExpertMode handlingMode) {
//			for (int i = 0; i < cell.getChildCount(); i++) {
//				ALDOpParameterDescriptor descr = opDescrMap.get(cell)
//						.getParameterDescriptor(
//								((ALDPort) cell.getChildAt(i).getValue())
//										.getParName());
//				if (descr.getDirection() == Direction.IN
//						|| descr.getDirection() == Direction.INOUT) {
//					if (handlingMode == Parameter.ExpertMode.STANDARD) {
//						if (descr.getHandlingMode() == handlingMode) {
//							cell.getChildAt(i).setVisible(true);
//						} else {
//							cell.getChildAt(i).setVisible(false);
//						}
//					} else {
//						cell.getChildAt(i).setVisible(true);
//					}
//				}
//			}
//			opGraph.refresh();
//			opDescrMap.get(cell).getOperatorControlFrame()
//					.changeMode(handlingMode);
//		}
//
//		@Override
//    public void actionPerformed(ActionEvent e) {
//			String command = e.getActionCommand();
//
//			if (command.equals("conf")) {
//				ALDOperatorControlFrame frame = opDescrMap.get(cell)
//						.getOperatorControlFrame();
//				frame.setVisible(true);
//			} else if (command.equals("runFlow")) {
////				runOnlyNode = false;
////				opDescrMap.get(cell).setReverse(true);
////				for (mxCell cell : nodeMap.values()) {
////					int count = 0;
////					for (int i = 0; i < cell.getChildCount(); i++) {
////						Object[] inEdges = opGraph.getIncomingEdges(cell
////								.getChildAt(i));
////						count += inEdges.length;
////					}
////					if (count == 0)
////						runOpNode(cell.getValue().toString(), false);
////				}
//				ALDGrappaFrame.this.nodesToBeExecuted = 
//						opGraph.getAllNodesTopologicallyOrdered();
//				ALDGrappaFrame.this.runNextNode();
////				for (mxCell c : nodesToRun) {
////					System.out.println("Node: " + 
////							opDescrMap.get(c).getOperatorControlFrame().getALDOperator().getName());
////				}
////				for (mxCell c: nodesToRun) {
////					System.out.println("Running " + opDescrMap.get(c).getOperatorControlFrame().getALDOperator().getName());
////					runOpNode(cell.getValue().toString(), true);
////					try {
////	          Thread.sleep(1000);
////          } catch (InterruptedException e1) {
////	          // TODO Auto-generated catch block
////	          e1.printStackTrace();
////          }
////				}
//			} else if (command.equals("runFromHere")) {
//				// with backtracking
//				ALDGrappaFrame.this.nodesToBeExecuted = 
//						opGraph.getSubsequentSubgraphNodesTopologicallyOrdered(cell);
//				ALDGrappaFrame.this.runNextNode();
////				runOnlyNode = false;
////				opDescrMap.get(cell).setReverse(true);
////				runOpNode(cell.getValue().toString(), false);
////			} else if (command.equals("run2")) {
////				// without backtracking
////				runOnlyNode = false;
////				opDescrMap.get(cell).setReverse(false);
////				runOpNode(cell.getValue().toString(), false);
////			} else if (command.equals("runNode1")) {
////				// with backtracking
////				runOnlyNode = true;
////				opDescrMap.get(cell).setReverse(true);
////				runOpNode(cell.getValue().toString(), false);
////			} else if (command.equals("runNode2")) {
////				// without backtracking
////				runOnlyNode = true;
////				opDescrMap.get(cell).setReverse(false);
////				runOpNode(cell.getValue().toString(), false);
//			} else if (command.equals("runUptoHere")) {
//				// with backtracking
//				ALDGrappaFrame.this.nodesToBeExecuted = 
//						opGraph.getPrecedingNodesTopologicallyOrdered(cell);
////				for (mxCell c: ALDEditorFrame.this.nodesToBeExecuted) {
////					try {
////	          System.out.println("Running " + 
////opDescrMap.get(c).getOperatorControlFrame().getOperator().getName());
////          } catch (ALDOperatorException e1) {
////	          // TODO Auto-generated catch block
////	          e1.printStackTrace();
////          }
////				}
//				ALDGrappaFrame.this.runNextNode();
//			} else if (command.equals("show")) {
//				ALDOperatorControlFrame frame = opDescrMap.get(cell)
//						.getOperatorControlFrame();
//				frame.showResult();
//			} else if (command.equals("standard")) {
//				changeView(cell, Parameter.ExpertMode.STANDARD);
//			} else if (command.equals("advanced")) {
//				changeView(cell, Parameter.ExpertMode.ADVANCED);
//			} else if (command.equals("copyNode1")) {
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
//			} else if (command.equals("copyNode2")) {
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
//			} else if (command.equals("copyNode3")) {
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
//			} else if (command.equals("removeNode")) {
//				for (int i = 0; i < cell.getChildCount(); i++) {
//					Object[] inEdges = opGraph.getIncomingEdges(cell
//							.getChildAt(i));
//
//					if (inEdges.length != 0) {
//						removeEdge((mxCell) inEdges[0], false);
//					}
//
//					Object[] outEdges = opGraph.getOutgoingEdges(cell
//							.getChildAt(i));
//					for (int j = 0; j < outEdges.length; j++) {
//						removeEdge((mxCell) outEdges[j], false);
//					}
//				}
//				opDescrMap.remove(cell);
//				nodeMap.remove(cell.getValue());
//				opGraph.getModel().remove(cell);
//			} else if (command.equals("removeEdge")) {
//				removeEdge(cell, false);
//			}
//		}
//	}
//	
//	/**
//	 * Context Menu of nodes.
//	 */
//	protected class PopUpMenuBench extends JPopupMenu implements ActionListener {
//		private mxCell cell;
//
//		protected PopUpMenuBench(int x, int y) {
//			JMenuItem newItem = new JMenuItem("New Workflow");
//			newItem.setActionCommand("newFlow");
//			newItem.addActionListener(this);
//			JMenuItem confItem = new JMenuItem("Run Workflow");
//			confItem.setActionCommand("runFlow");
//			confItem.addActionListener(this);
//			this.add(newItem);
//			this.addSeparator();
//			this.add(confItem);
//		}
//
//		@Override
//    public void actionPerformed(ActionEvent e) {
//			String command = e.getActionCommand();
//			if (command.equals("newFlow")) { 
//				ALDGrappaFrame.this.actionPerformed(new ActionEvent(this,1,"new"));
//			} else if (command.equals("runFlow")) {
//				ALDGrappaFrame.this.actionPerformed(new ActionEvent(this,1,"runFlow"));
//			}
//		}
//	}
//	
////	protected class myConnectionHandler extends mxConnectionHandler {
////
////		public myConnectionHandler(mxGraphComponent arg0) {
////	    super(arg0);
////	    // TODO Auto-generated constructor stub
////    }
//
////		@Override
////		public void mousePressed(MouseEvent e) {
////			System.out.println("Mouse was pressed..");
////		}
////		
////		@Override
////		public void mouseReleased(MouseEvent e) {
////			System.out.println("Mouse was released...");
////		}
////
////		public void 	mouseDragged(MouseEvent e) {
////			System.out.println("Mouse was dragged...");			
////		}
////    
////		public void mouseMoved(MouseEvent e) {
////			System.out.println("Mouse was moved...");
////		}
////	}
//	
////	protected class myGraphComponent extends mxGraphComponent {
////		public myGraphComponent(mxGraph arg0) {
////	    super(arg0);
////	    // TODO Auto-generated constructor stub
////    }
////
////		protected mxConnectionHandler createConnectionHandler() {
////			return new myConnectionHandler(this);
////		}
////	}
}
