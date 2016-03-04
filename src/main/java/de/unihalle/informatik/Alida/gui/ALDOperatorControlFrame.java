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

package de.unihalle.informatik.Alida.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;

import javax.swing.*;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;
import de.unihalle.informatik.Alida.operator.*;
import de.unihalle.informatik.Alida.operator.events.ALDConfigurationEvent;
import de.unihalle.informatik.Alida.operator.events.ALDOpParameterUpdateEventListener;
import de.unihalle.informatik.Alida.workflows.ALDWorkflow;
import de.unihalle.informatik.Alida.workflows.ALDWorkflowNode.ALDWorkflowNodeState;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.indexing.SezPozAdapter;
import de.unihalle.informatik.Alida.batch.ALDBatchInputManagerSwing;
import de.unihalle.informatik.Alida.batch.ALDBatchOutputManagerSwing;
import de.unihalle.informatik.Alida.batch.provider.input.swing.ALDBatchInputIteratorSwing;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.*;
import de.unihalle.informatik.Alida.exceptions.ALDBatchIOException;
import de.unihalle.informatik.Alida.exceptions.ALDBatchIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.gui.ALDOperatorGUIExecutionProxy.WorkflowThreadStatus;

/**
 * Frame to configure and run an operator.
 * <p>
 * If the operator is properly configured, i.e. its input parameters are set, it
 * is executed as an {@link ALDWorkflow} containing only a single node. 
 * Result display and interaction with the operator are handled by the event 
 * mechanisms of the workflow. 
 * <p>
 * Controllable operators trigger the display of additional buttons in GUI for
 * pausing, resuming and interrupting calculations.
 * 
 * @author Stefan Posch
 * @author Birgit Moeller
 */
public class ALDOperatorControlFrame extends ALDOperatorConfigurationFrame 
	implements ItemListener {

	/* ***********************************
	 * Some local data type declarations.
	 * ***********************************/
  
	/**
	 * Default run button color of unconfigured operators. 
	 */
	protected final static Color buttonColor_unconfigured = 
		new Color(255, 94, 94);
	
	/**
	 * Default run button color of runnable operators. 
	 */
	protected final static Color buttonColor_runnable =
		new Color(255, 255, 122);
	
	/**
	 * Default run button color of running operators. 
	 */
	protected final static Color buttonColor_running =
		new Color(106, 106, 214);

	/**
	 * Default run button color of ready operators. 
	 */
	protected final static Color buttonColor_ready =
		new Color(118, 200, 118);

	/**
	 * Reference to the surrounding workflow manager.
	 */
	protected ALDOperatorGUIExecutionProxy execProxy = null;
	
	/* **********************************************************************
	 * Buttons and other components for interactions with running operators.
	 * **********************************************************************/

	/**
	 * Result display button.
	 */
	protected JButton resultDisplayButton;

	/**
	 * Run button.
	 */
	protected JButton runButton;
	
	/**
	 * Pause button, changes its label.
	 */
	protected JButton pauseButton;

	/**
	 * Stop button to stop snake optimization (after end of iteration).
	 */
	protected JButton stopButton;

	/**
	 * Button for stepping through optimization process.
	 */
	protected JButton stepButton;

	/**
	 * Box for activating step-through mode.
	 */
	protected JCheckBox stepThroughBox;

	/**
	 * Field for specifying number of iterations per step-through step.
	 */
	protected JTextField stepThroughStepSize;

	/* **********************************************************************
	 * Buttons and other components for batch mode.
	 * **********************************************************************/

	/**
	 * Configuration panel for batch mode.
	 */
	protected JPanel batchConfigPanel;
	
	/**
	 * Checkbox for enabling/disabling batch mode.
	 */
	protected JCheckBox activateBatchMode;
	
	/**
	 * Set of checkboxes to select input parameters in batch mode.
	 */
	protected Vector<JCheckBox> inputCheckBoxes;
	
	/**
	 * Components in GUI linked to the different input batch parameters.
	 */
	protected HashMap<String, ALDSwingComponent> inputProviderComps;
	
	/**
	 * Set of checkboxed to select parameters to summarize in batch mode.
	 */
	protected Vector<JCheckBox> outputCheckBoxes;
	
	/**
	 * Flag to indicate if progress events are to be shown in status bar or not.
	 */
	private boolean showProgressEvents = true;

	/**
	 * Constructs a new control frame for an operator object.
	 * @param _op		Operator to be associated with this frame object.	
	 * @param em		Reference to the surrounding execution proxy.
	 * @param pL		Parameter update listener propagating value change events.		
	 * @throws ALDOperatorException Thrown if construction fails.
	 */
	public ALDOperatorControlFrame(ALDOperator _op, 
		ALDOperatorGUIExecutionProxy em, ALDOpParameterUpdateEventListener pL) 
			throws ALDOperatorException {
		super(_op, pL);
		this.execProxy = em;
		// provide the operator parameter panel with reference to this frame
		this.operatorParameterPanel.setParentFrame(this);
		// adjust title string of window
		this.titleString = "ALDOperatorControlFrame: " + this.op.getName();
		this.setTitle(this.titleString);
	}
	
	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.gui.ALDOperatorConfigurationFrame#setupAdditionalTabContents()
	 */
	@Override
	protected HashMap<JComponent, String> setupAdditionalTabContents() {
		HashMap<JComponent, String> map = new HashMap<JComponent, String>();
		
		// setup batch configuration panel and add to map
		boolean batchModeSupport = false;
		Index<ALDAOperator, ALDOperator> indexItems = SezPozAdapter.load(
				ALDAOperator.class, ALDOperator.class);
		for (final IndexItem<ALDAOperator, ALDOperator> item : indexItems) {
			if (item.className().equals(this.op.getClass().getName())) {
				batchModeSupport = item.annotation().allowBatchMode();
				break;
			}
		}
		if (batchModeSupport) {
			this.setupBatchConfigPanel();
			// put batch mode panel into a scrollpane
			JScrollPane batchScroller = new JScrollPane(this.batchConfigPanel);
			map.put(batchScroller, "Batch Mode Configuration");
			// return filled map
			return map;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.gui.ALDOperatorConfigurationFrame#setupAdditionalMenuOptionItems()
	 */
	@Override
  protected Collection<JComponent> setupAdditionalMenuOptionItems() {
		LinkedList<JComponent> list = new LinkedList<JComponent>();
		list.add(new JLabel("<html><u>Status Bar</u></html>"));
		JCheckBox optionCheckboxProgressEvents = 
				new JCheckBox("Show Progress Messages");
		optionCheckboxProgressEvents.setSelected(true);
		this.showProgressEvents = true;
		optionCheckboxProgressEvents.setActionCommand("optionShowProgress");
		optionCheckboxProgressEvents.addItemListener(this);
		list.add(optionCheckboxProgressEvents);
		return list;
	}

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.gui.ALDOperatorConfigurationFrame#setupAdditionalMenus()
	 */
	@Override
	protected Collection<JMenu> setupAdditionalMenus() {
		LinkedList<JMenu> menuList = new LinkedList<JMenu>();
		JMenu actionsM = new JMenu("Actions");
		JMenuItem itemRun = new JMenuItem("Run");
		itemRun.setActionCommand("actionsM_run");
		itemRun.addActionListener(this);
		actionsM.add(itemRun);
		return menuList;
	}

	/**
	 * Instantiates batch mode configuration panel.
	 */
	protected void setupBatchConfigPanel() {
		this.batchConfigPanel = new JPanel();
		this.batchConfigPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		BoxLayout ylayout = new BoxLayout(this.batchConfigPanel, BoxLayout.Y_AXIS);
		this.batchConfigPanel.setLayout(ylayout);
		this.activateBatchMode = new JCheckBox("Activate Batch Mode");
		this.activateBatchMode.setActionCommand("batchModeToggled");
		this.activateBatchMode.addItemListener(this);

		// add list of available input parameters
		this.inputCheckBoxes = new Vector<JCheckBox>();
		this.inputProviderComps = new HashMap<String, ALDSwingComponent>();
		this.outputCheckBoxes = new Vector<JCheckBox>();
		Collection<String> inputParamNames = this.op.getInInoutNames();
		Collection<String> outputParamNames = this.op.getOutInoutNames();

		// check for which parameters you have providers
		int inputParamCount = 0;
		ALDOpParameterDescriptor descr = null;
		HashMap<Integer, Vector<ALDOpParameterDescriptor>> guiOrderHashIn = 
				new HashMap<Integer, Vector<ALDOpParameterDescriptor>>();
		for (String param : inputParamNames) {
			try {
				descr = this.op.getParameterDescriptor(param);
				if (ALDBatchInputManagerSwing.getInstance().
						providerAvailable(descr.getMyclass())){
					++inputParamCount;
					Integer order = new Integer(descr.getDataIOOrder());
					if (guiOrderHashIn.containsKey(order)) {
						guiOrderHashIn.get(order).add(descr);
					} else {
						Vector<ALDOpParameterDescriptor> paramVec = 
								new Vector<ALDOpParameterDescriptor>();
						paramVec.add(descr);
						guiOrderHashIn.put(order, paramVec);
					}
				}
			} catch (ALDOperatorException ex) {
				ex.printStackTrace();
			}
		}
		// sort input parameters
		Set<Integer> keys = guiOrderHashIn.keySet();
		LinkedList<Integer> keyListIn = new LinkedList<Integer>();
		for (Integer key : keys) {
			keyListIn.add(key);
		}
		Collections.sort(keyListIn);

		// get output parameters for which providers are available
		int outputParamCount = 0;
		HashMap<Integer, Vector<ALDOpParameterDescriptor>> guiOrderHashOut = 
				new HashMap<Integer, Vector<ALDOpParameterDescriptor>>();
		for (String param : outputParamNames) {
			try {
				descr = this.op.getParameterDescriptor(param);
				if (ALDBatchOutputManagerSwing.getInstance().
						providerAvailable(descr.getMyclass())){
					++outputParamCount;
					Integer order = new Integer(descr.getDataIOOrder());
					if (guiOrderHashOut.containsKey(order)) {
						guiOrderHashOut.get(order).add(descr);
					} else {
						Vector<ALDOpParameterDescriptor> paramVec = 
								new Vector<ALDOpParameterDescriptor>();
						paramVec.add(descr);
						guiOrderHashOut.put(order, paramVec);
					}
				}
			} catch (ALDOperatorException ex) {
				ex.printStackTrace();
			}
		}
		// sort output parameters
		keys = guiOrderHashOut.keySet();
		LinkedList<Integer> keyListOut = new LinkedList<Integer>();
		for (Integer key : keys) {
			keyListOut.add(key);
		}
		Collections.sort(keyListOut);

		// determine size
		int paramNum = (inputParamCount > outputParamCount) ? 
																					inputParamCount : outputParamCount;

		// init header
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new GridLayout(2+paramNum,2));
		labelPanel.add(this.activateBatchMode);
		labelPanel.add(new JLabel());
		labelPanel.add(new JLabel("Batch mode input parameters:"));
		labelPanel.add(new JLabel("Batch mode output parameters:"));
		this.batchConfigPanel.add(labelPanel,BorderLayout.NORTH);

		// temporary array for components
		JComponent [][] boxArray = new JComponent[paramNum][2]; 
		
		// add input check boxes, first required, than optional, than supplemental
		ButtonGroup inButtonSet = new ButtonGroup();
		int index = 0;
		for (Integer key : keyListIn) {
			Vector<ALDOpParameterDescriptor> descrips = guiOrderHashIn.get(key);
			for (ALDOpParameterDescriptor d : descrips) {
				// required parameters first
				if (d.isRequired()) {
					this.addBatchInputParameter(d, inButtonSet, boxArray, index);
					++index;
				}
			}
		}
		for (Integer key : keyListIn) {
			Vector<ALDOpParameterDescriptor> descrips = guiOrderHashIn.get(key);
			for (ALDOpParameterDescriptor d : descrips) {
				// required parameters first
				if (!d.isRequired() && !(d.getSupplemental().booleanValue())) {
					this.addBatchInputParameter(d, inButtonSet, boxArray, index);
					++index;
				}
			}
		}
		for (Integer key : keyListIn) {
			Vector<ALDOpParameterDescriptor> descrips = guiOrderHashIn.get(key);
			for (ALDOpParameterDescriptor d : descrips) {
				// required parameters first
				if (d.getSupplemental().booleanValue()) {
					this.addBatchInputParameter(d, inButtonSet, boxArray, index);
					++index;
				}
			}
		}

		// checkboxes for output parameters
		index = 0;
		for (Integer key : keyListOut) {
			Vector<ALDOpParameterDescriptor> descrips = guiOrderHashOut.get(key);
			for (ALDOpParameterDescriptor d : descrips) {
				JCheckBox paramChecker = new JCheckBox(d.getLabel());
				paramChecker.setActionCommand("output_" + d.getName());
				paramChecker.setSelected(false);
				paramChecker.setEnabled(false);
				paramChecker.addItemListener(this);
				this.outputCheckBoxes.add(paramChecker);
				boxArray[index][1] = paramChecker;
				++index;
			}
		}

		for (index = 0; index < paramNum; ++index) {
			if (inputParamCount > index)
				labelPanel.add(boxArray[index][0]);
			else
				labelPanel.add(new JLabel(""));
			if (outputParamCount > index)
				labelPanel.add(boxArray[index][1]);
			else
				labelPanel.add(new JLabel(""));
		}
		// add to batch mode panel
		this.batchConfigPanel.add(labelPanel,BorderLayout.CENTER);
	}

	/**
	 * Add batch input mode parameter.
	 * @param d							Descriptor of the parameter.
	 * @param inButtonSet		Set to which the radio button is to be added.
	 * @param boxArray			Array of components.
	 * @param index					Index of the parameter.
	 */
	protected void addBatchInputParameter(ALDParameterDescriptor d, 
			ButtonGroup inButtonSet, JComponent [][] boxArray, int index) {
		ALDSwingComponent providerComp = null;
		try {
			// get batch iterator
			ALDBatchInputIteratorSwing bit =
					((ALDBatchInputIteratorSwing)
							(ALDBatchInputManagerSwing.getInstance().getProvider(
									d.getMyclass(), ALDBatchInputIteratorSwing.class)));
			if (bit != null) {
				providerComp = 
						bit.createGUIElement(d.getField(), d.getMyclass(), null, d);
				if (providerComp != null) {
					JPanel paramPanel = new JPanel(new FlowLayout(
							FlowLayout.LEADING));
					JCheckBox paramChecker = new JCheckBox(d.getLabel());
					paramChecker.setSelected(false);
					paramChecker.setEnabled(false);
					inButtonSet.add(paramChecker);
					paramPanel.add(paramChecker);
					paramPanel.add(providerComp.getJComponent());
					this.inputProviderComps.put(d.getName(), providerComp);
					this.inputCheckBoxes.add(paramChecker);
					// install listener
					paramChecker.setActionCommand("input_" + d.getName());
					paramChecker.addItemListener(this);
					boxArray[index][0] = paramPanel;
					// register value change listener
					providerComp.addValueChangeEventListener(this);
				}
			}
		} catch (ALDBatchIOManagerException exm) {
			exm.printStackTrace();
		} catch (ALDBatchIOException exio) {
			exio.printStackTrace();
		}
	}
	
	/**
	 * Adds set of control buttons to the input panel.
	 */
	@Override
  protected JPanel addContextSpecificButtonPanel() {
		
		// init the panel
		JPanel runPanel = new JPanel();
		runPanel.setLayout(new GridLayout(2, 1));

		// check if operator is controllable and if so, if it supports 
		// step-wise execution; if not omit corresponding components
		if (  this.op instanceof ALDOperatorControllable &&
				((ALDOperatorControllable)this.op).supportsStepWiseExecution()) {
			this.stepThroughBox = new JCheckBox("Step-wise execution");
			this.stepThroughBox.setActionCommand("stepFlagToggled");
			this.stepThroughBox.addItemListener(this);
			JLabel label = new JLabel("                 Step size = ");
			this.stepThroughStepSize = new JTextField("1", 5);
			this.stepThroughStepSize.setEnabled(false);
			this.stepButton = new JButton("Next Step");
			this.stepButton.setActionCommand("step");
			this.stepButton.addActionListener(this);
			this.stepButton.setBounds(50, 60, 80, 30);
			this.stepButton.setEnabled(false);

			JPanel stepPanel = new JPanel();
			stepPanel.setLayout(new BoxLayout(stepPanel, BoxLayout.LINE_AXIS));
			stepPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
			stepPanel.add(this.stepThroughBox);
			stepPanel.add(label);
			stepPanel.add(this.stepThroughStepSize);
			stepPanel.add(Box.createRigidArea(new Dimension(70, 0)));
			stepPanel.add(this.stepButton);
			runPanel.add(stepPanel);
		}

		// button to re-display result frame
		this.resultDisplayButton = new JButton("Display Results");
		this.resultDisplayButton.setActionCommand("display");
		this.resultDisplayButton.addActionListener(this);
		this.resultDisplayButton.setBounds(50, 60, 80, 30);
		this.resultDisplayButton.setEnabled(false);

		// run button
		this.runButton = new JButton("Run");
		this.runButton.setActionCommand("run");
		this.runButton.addActionListener(this);
		this.runButton.setBounds(50, 60, 80, 30);

		// only controllable operators can be paused and stopped
		if (this.op instanceof ALDOperatorControllable) {
			// pause button for controllable plugins
			this.pauseButton = new JButton("Pause");
			this.pauseButton.setActionCommand("pause");
			this.pauseButton.addActionListener(this);
			this.pauseButton.setBounds(50, 60, 80, 30);
			this.pauseButton.setEnabled(false);

			// stop button for controllable plugins
			this.stopButton = new JButton("Stop");
			this.stopButton.setActionCommand("stop");
			this.stopButton.addActionListener(this);
			this.stopButton.setBounds(50, 60, 80, 30);
			this.stopButton.setEnabled(false);
		}

		// now set up a panel to hold all buttons
		JPanel controlPanel = new JPanel();
		controlPanel
				.setLayout(new BoxLayout(controlPanel, BoxLayout.LINE_AXIS));
		controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		controlPanel.add(this.resultDisplayButton);
		controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		controlPanel.add(Box.createHorizontalGlue());
		// controlPanel.add(validateButton);
		controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		controlPanel.add(this.runButton);
		// if operator is not controllable, disable control buttons
		if (this.op instanceof ALDOperatorControllable) {
			controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
			controlPanel.add(this.pauseButton);
			controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
			controlPanel.add(this.stopButton);
		}
		controlPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		runPanel.add(controlPanel);
		return runPanel;
	}

	/**
	 * Method to request whether to display progress events or not.
	 * @return True, if progress event messages are to be shown.
	 */
	public boolean showProgressEvents() {
		return this.showProgressEvents;
	}

	/**
	 * Check if batch mode is currently active.
	 * @return	True, if batch mode is active.
	 */
	public boolean isBatchModeActive() {
		return this.activateBatchMode != null 
				&& this.activateBatchMode.isSelected();		
	}
	
	/**
	 * Returns selected input parameters for batch mode.
	 * @return	List of selected inputs.
	 */
	public LinkedList<String> getBatchInputParameters() {
		LinkedList<String> inputParams = new LinkedList<String>();
		for (JCheckBox box : this.inputCheckBoxes) {
			if (box.isSelected())
				// action command encodes parameter name, i.e. command is defined as
				// string "input_<var-name>" -> extract name by splitting string
				inputParams.add(box.getActionCommand().split("_")[1]);
		}
		return inputParams;
	}
	
	/**
	 * Returns an iterator for a specific input parameter in batch mode.
	 * @param param		Operator input parameter of interest.
	 * @return	Corresponding iterator.
	 * @throws ALDBatchIOException	Thrown if iterator init fails.
	 * @throws ALDOperatorException	Thrown if operator query fails.
	 */
	public Iterator<Object> getInputParamIterator(String param) 
			throws ALDBatchIOException, ALDOperatorException {
		return ALDBatchInputManagerSwing.getInstance().readData(null, 
				this.op.getParameterDescriptor(param).getMyclass(), 
				this.inputProviderComps.get(param));		
	}
	
	/**
	 * Returns selected output parameters for batch mode.
	 * @return	List of selected outputs to summarize.
	 */
	public LinkedList<String> getBatchOutputParameters() {
		LinkedList<String> outputParams = new LinkedList<String>();
		for (JCheckBox box : this.outputCheckBoxes) {
			if (box.isSelected())
				outputParams.add(box.getActionCommand().split("_")[1]);
		}
		return outputParams;
	}

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.gui.ALDOperatorConfigurationFrame#quit()
	 */
	@Override
  public boolean quit() {
		if (this.op instanceof ALDOperatorControllable) {
			this.stopButton.setEnabled(false);
			this.pauseButton.setEnabled(false);
		}
		return super.quit();
	}

	/**
	 * Updates the window according to the configuration status of the operator.
	 * <p>
	 * In the window the run button color is changed according to the state of 
	 * the underlying operator node.
	 * 
	 * @param state				State of the operator and workflow node, respectively.
	 */
	public void updateNodeStatus(ALDWorkflowNodeState state) {
		// update the run button color and the tooltip
		switch(state)
		{
			case UNCONFIGURED:
				if (!this.isBatchModeActive()) {
					if (this.stepThroughBox== null || !this.stepThroughBox.isSelected()) {
						this.runButton.setEnabled(true);
						this.runButton.setBackground(buttonColor_unconfigured);
						this.runButton.setToolTipText("Operator not configured");
					}
					this.resultDisplayButton.setEnabled(false);
					// enable the parameter components
					this.operatorParameterPanel.enableComponents();
				}
				else {
					// in batch mode something different happens
					if (    this.op.unconfiguredItems().size() > 1
							|| (     this.getBatchInputParameters() != null 
							    &&   this.getBatchInputParameters().size() > 0
								  && !(this.op.unconfiguredItems().get(0).equals( 
															this.getBatchInputParameters().getFirst())))) {
						for (String s: this.op.unconfiguredItems())
							System.out.println(s);
						System.out.println(this.getBatchInputParameters().getFirst());
						// operator is really unconfigured
						if (    this.stepThroughBox== null 
								|| !this.stepThroughBox.isSelected()) {
							this.runButton.setEnabled(true);
							this.runButton.setBackground(buttonColor_unconfigured);
							this.runButton.setToolTipText("Operator not configured");
						}
						this.resultDisplayButton.setEnabled(false);
						// enable the parameter components
						this.operatorParameterPanel.enableComponents();						
					}
					else {
						// batch parameter is the only one which is missing
						if (    this.stepThroughBox== null 
								|| !this.stepThroughBox.isSelected()) {
							this.runButton.setEnabled(true);
							this.runButton.setBackground(buttonColor_runnable);
							this.runButton.setToolTipText("Operator ready to run");
						}
						this.resultDisplayButton.setEnabled(false);
						// enable the parameter components
						this.operatorParameterPanel.enableComponents();
					}
				}
				break;
			case RUNNABLE:
				if (this.stepThroughBox== null || !this.stepThroughBox.isSelected()) {
					this.runButton.setEnabled(true);
					this.runButton.setBackground(buttonColor_runnable);
					this.runButton.setToolTipText("Operator ready to run");
				}
				this.resultDisplayButton.setEnabled(false);
				// enable the parameter components
				this.operatorParameterPanel.enableComponents();
				break;
			case RUNNING:
				if (this.stepThroughBox== null || !this.stepThroughBox.isSelected()) {
					this.runButton.setBackground(buttonColor_running);
					this.runButton.setToolTipText("Operator is running...");
				}
				this.runButton.setEnabled(false);
				this.resultDisplayButton.setEnabled(false);
				// disable the parameter components
				this.operatorParameterPanel.disableComponents();
				break;
			case READY:
				if (this.stepThroughBox== null || !this.stepThroughBox.isSelected()) {
					this.runButton.setEnabled(true);
					this.runButton.setBackground(buttonColor_ready);
					this.runButton.setToolTipText(
							"Operator execution finished, results available.");
					this.resultDisplayButton.setEnabled(true);
				}
				// enable the parameter components
				this.operatorParameterPanel.enableComponents();
				break;
			default:
				break;
		} 
		// update the parameters
		super.updateParamConfigurationStatus(this.op.unconfiguredItems());
	}

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.gui.ALDOperatorConfigurationFrame#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		// get the event command
		String command = e.getActionCommand();

		// run the configured operator
		if (command.equals("run") || command.equals("actionsM_run")) {
			// check if batch mode is requested and parameters have been selected
			if (   this.activateBatchMode != null
					&& this.activateBatchMode.isSelected()) {
				try {
					this.execProxy.runWorkflowInBatchMode();
				} catch (ALDBatchIOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ALDOperatorException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else {
				if (this.op instanceof ALDOperatorControllable) {
					this.pauseButton.setEnabled(true);
					this.stopButton.setEnabled(true);
					this.repaint();
					// make sure that the step-wise mode is disabled
					ALDConfigurationEvent confev = new ALDConfigurationEvent(this);
					confev.disableStepwiseExecution();
					this.execProxy.configureWorkflow(confev);
				}
				this.execProxy.runWorkflow();
			}
		}
		// quit, i.e. close window and kill the thread
		if (   command.equals("frame_close")
				|| command.equals("close") 
				|| command.equals("fileM_quit")) {
			this.execProxy.quit();
		}
		// stop operator execution
		else if (command.equals("stop")) {
			this.execProxy.stopWorkflow();
			this.stopButton.setEnabled(false);
			this.pauseButton.setEnabled(false);
			this.postSystemMessage(" Operator requested to stop...");
		}
		// pause operator execution
		else if (command.equals("pause")) {
			this.execProxy.pauseWorkflow();
			this.pauseButton.setActionCommand("continue");
			this.pauseButton.setText("Continue");
			this.stopButton.setEnabled(false);
			this.postSystemMessage(" Operator requested to pause...");
		}
		// resume operator execution
		else if (command.equals("continue")) {
			this.execProxy.resumeWorkflow();
			this.pauseButton.setActionCommand("pause");
			this.pauseButton.setText("Pause");
			this.stopButton.setEnabled(true);
			this.postSystemMessage(" Operator is running again...");
		}
		// next step in step-wise execution
		else if (command.equals("step")) {
			if (this.execProxy.getWorkflowThreadStatus().equals(
																			WorkflowThreadStatus.THREAD_RUNNING)) {
				this.execProxy.doNextStepInWorkflow();
				this.stopButton.setEnabled(true);
			}
			// start the thread
			else {
				this.postSystemMessage(" Operator is running...");
				try {
					String stepSizeText = this.stepThroughStepSize.getText();
					int size = Integer.valueOf(stepSizeText).intValue();
					// tell the operator the requested step size via event...
					ALDConfigurationEvent confev = new ALDConfigurationEvent(this);
					confev.setStepsize(size);
					confev.enableStepwiseExecution();
					// ... send it to operator
					this.execProxy.configureWorkflow(confev);
					this.execProxy.runWorkflow();
					this.stopButton.setEnabled(true);
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
					ex.printStackTrace();
				}
			}
		} 
		else if (command.equals("display")) {
			this.execProxy.showResultFrame();
		}
		else {
			// all other actions are delegated to super class
			super.actionPerformed(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {

		String idString = null;

		// get the object affected by the event
		Object source = e.getItemSelectable();

		if (source instanceof JCheckBox) {
			JCheckBox box = (JCheckBox) source;
			idString = box.getActionCommand();
		}
		else if (source instanceof JComboBox) {
			JComboBox box = (JComboBox) source;
			idString = box.getActionCommand();
		}

		// error check
		if (idString == null)
			return;
		
		// step through optimization
		if (idString.equals("optionShowProgress")) {
			if (((JCheckBox)source).isSelected())
				this.showProgressEvents = true;
			else
				this.showProgressEvents = false;
		}
		else if (idString.equals("stepFlagToggled")) {
			if (this.stepThroughBox.isSelected()) {
				// step-wise execution
				this.stepThroughStepSize.setEnabled(true);
				this.stepButton.setEnabled(true);
				this.runButton.setEnabled(false);
				this.runButton.setBackground(Color.GRAY);
				this.pauseButton.setEnabled(false);
			} else {
				// no step-wise execution
				this.stepThroughStepSize.setEnabled(false);
				this.stepButton.setEnabled(false);
				this.runButton.setEnabled(true);
				this.runButton.setBackground(buttonColor_runnable);
				this.pauseButton.setEnabled(false);
			}
		}
		/* ***
		 * Batch mode commands
		 * ***/
		else if (idString.equals("batchModeToggled")) {
			if (this.activateBatchMode.isSelected()) {
				for (JCheckBox box : this.inputCheckBoxes) {
					box.setEnabled(true);
					// if this parameter was selected before, switch provider
					if (box.isSelected()) {
						String commandString = box.getActionCommand();
						String paramName = (commandString.split("_"))[1];
						this.operatorParameterPanel.setParameterBatchModeInput(paramName);
					}
				}
				for (JCheckBox box : this.outputCheckBoxes) {
					box.setEnabled(true);
				}
			}
			else {
				for (JCheckBox box : this.inputCheckBoxes) {
					// disable the box...
					box.setEnabled(false);
					// ... enable parameter component in config tab again
					String commandString = box.getActionCommand();
					String paramName = (commandString.split("_"))[1];
					this.operatorParameterPanel.setParameterNotLinked(paramName);
				}
				for (JCheckBox box : this.outputCheckBoxes) {
					box.setEnabled(false);
				}				
			}
			// propagate event to super class
			super.handleValueChangeEvent(new ALDSwingValueChangeEvent(this, null));
		}
		else if (idString.startsWith("input_")) {
			// input parameter selection was changed
			String paramName = (idString.split("_"))[1];
			if (((JCheckBox)source).isSelected())
				this.operatorParameterPanel.setParameterBatchModeInput(paramName);
			else
				this.operatorParameterPanel.setParameterNotLinked(paramName);
			// propagate event to super class
			super.handleValueChangeEvent(new ALDSwingValueChangeEvent(this, null));
		}
		else if (idString.startsWith("output_")) {
			// propagate event to super class
			super.handleValueChangeEvent(new ALDSwingValueChangeEvent(this, null));
		}
		// update GUI
		this.repaint();
	}
}
