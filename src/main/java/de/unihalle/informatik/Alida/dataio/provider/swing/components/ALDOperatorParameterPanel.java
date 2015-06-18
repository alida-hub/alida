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

package de.unihalle.informatik.Alida.dataio.provider.swing.components;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import de.unihalle.informatik.Alida.operator.*;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.Parameter.Direction;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.*;
import de.unihalle.informatik.Alida.datatypes.ALDConfigurationValidator;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.*;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.gui.ALDOperatorControlFrame;

/**
 * Panel to hold graphical elements for configuring an operator.
 * 
 * @author Birgit Moeller
 */
public class ALDOperatorParameterPanel extends ALDParameterPanelParent {

	/**
	 * Slf4j logger.
	 */
	private static Logger logger = 
		LoggerFactory.getLogger(ALDOperatorParameterPanel.class);
	
	/**
	 * Types of parameters appearing in Alida's operator concept.
	 */
	private static enum ParameterType {
		/**
		 * Required parameters.
		 */
		PARAM_REQUIRED,
		/**
		 * Optional parameters.
		 */
		PARAM_OPTIONAL,
		/**
		 * Supplemental parameters.
		 */
		PARAM_SUPPLEMENTAL
	}
	
	/**
	 * Title string for the panel with required parameters.
	 */
	private final static String headerRequiredParamsPanel = 
		"Parameters (required)"; 
	
	/**
	 * Title string for the panel with optional parameters.
	 */
	private final static String headerOptionalParamsPanel = 
		"Parameters (optional)"; 

	/**
	 * Title string for the panel with supplemental parameters.
	 */
	private final static String headerSupplementalParamsPanel = 
		"Parameters (supplemental)"; 

	/**
	 * Main panel containing all operator parameters.
	 */
	private JPanel mainPanel;

	/**
	 * The operator associated with this panel.
	 */
	private ALDOperator operator = null;
	
	/**
	 * Reference to the control frame linked to this panel.
	 */
	protected ALDOperatorControlFrame parentFrame = null;

	/**
	 * Flag indicating if panel is used on top level or not.
	 * <p>
	 * Using the panel on top-level refers to usage with operators directly
	 * executed, while non-top-level use refers to usage with operators passed
	 * as parameters to other operators. In the second case no deep validation
	 * is done, i.e. required parameters are allowed to have null values.
	 */
	private boolean topLevelCall = false;

	/**
	 * List of all parameter descriptors associated with the operator.
	 */
	private LinkedList<ALDOpParameterDescriptor> opParamDescrips;

	/**
	 * Sub-panel managing the configuration of required parameters.
	 */
	private ALDParameterPanel panelRequiredParams = null;

	/**
	 * Sub-panel managing the configuration of optional parameters.
	 */
	private ALDParameterPanel panelOptionalParams = null;

	/**
	 * Sub-panel managing the configuration of supplemental parameters.
	 */
	private ALDParameterPanel panelSupplementalParams = null;

	/**
	 * Mode for displaying parameters.
	 * <p>
	 * In standard mode the view is restricted to parameters annotated as
	 * standard, in advanced mode all annotated parameters of the operator are
	 * displayed in the GUI.
	 */
	private Parameter.ExpertMode displayMode = Parameter.ExpertMode.STANDARD;

	/**
	 * Default constructor.
	 * <p> 
	 * Note that value change event listeners added after construction of the 
	 * object might will events generated during initialization. To avoid that
	 * a listener can be handed over to the object upon creation.
	 * 
	 * @param op					Operator associated with this panel.
	 * @param mode				Display mode of the panel.
	 * @param topLevel		If true, a top-level context is assumed.
	 * @param listener		Optional value change event listener.
	 */
	public ALDOperatorParameterPanel(ALDOperator op, Parameter.ExpertMode mode,
			boolean topLevel, ALDSwingValueChangeListener listener) {
		this.mainPanel = new JPanel();
		this.operator = op;
		this.displayMode = mode;
		this.topLevelCall = topLevel;
		if (listener != null)
			this.addValueChangeEventListener(listener);
		this.initPanel();
	}

	/**
	 * Inits the panel according to formerly specified descriptors.
	 */
	private void initPanel() {
		// update operator parameter descriptors
		this.updateOperatorParameterDescriptors();
		
		BoxLayout ylayout = new BoxLayout(this.mainPanel, BoxLayout.Y_AXIS);
		this.mainPanel.setLayout(ylayout);
		// create panel with required parameters
		this.panelRequiredParams = this.createInParameterPanel(Boolean.TRUE,
			Boolean.FALSE, headerRequiredParamsPanel);
		if (this.panelRequiredParams != null) {
			this.mainPanel.add(this.panelRequiredParams.getJPanel());
			// register for value change events
			this.panelRequiredParams.addValueChangeEventListener(this);
		}
		// create panel with optional parameters
		this.panelOptionalParams = this.createInParameterPanel(Boolean.FALSE,
			Boolean.FALSE, headerOptionalParamsPanel);
		// BoxLayout ylayout = new BoxLayout(this, BoxLayout.Y_AXIS);
		// this.setLayout(ylayout);
		// // create panel with required parameters
		// this.panelRequiredParams = this.createInParameterPanel(Boolean.TRUE,
		// Boolean.FALSE);
		// if (this.panelRequiredParams != null)
		// this.add(this.panelRequiredParams);
		// // create panel with optional parameters
		// this.panelOptionalParams = this.createInParameterPanel(Boolean.FALSE,
		// Boolean.FALSE);
		if (this.panelOptionalParams != null) {
			this.mainPanel.add(this.panelOptionalParams.getJPanel());
			// register for value change events
			this.panelOptionalParams.addValueChangeEventListener(this);
		}
		// create panel with required parameters
		this.panelSupplementalParams = this.createInParameterPanel(null,
			Boolean.TRUE, headerSupplementalParamsPanel);
		if (this.panelSupplementalParams != null) {
			this.mainPanel.add(this.panelSupplementalParams.getJPanel());
			// register for value change events
			this.panelSupplementalParams.addValueChangeEventListener(this);
		}
	}
	
	/**
	 * Create a panel with an entry for each input parameter as requested.
	 * <p>
	 * The latter is determined via <code>useRequired</code> and
	 * <code>useSupplemental</code>.
	 * 
	 * @param useRequired
	 *            If non-null only parameters with a required flag are used; if
	 *            null the required flag is ignored.
	 * @param useSupplemental
	 *            If non-null use only parameters with supplemental flag,
	 *            otherwise supplemental flag is ignored.
	 * @param panelTitle	Title string to be shown at the top of the panel.
	 * @return Generated parameter panel.
	 */
	private ALDParameterPanel createInParameterPanel(Boolean useRequired,
			Boolean useSupplemental, String panelTitle) {

		// find appropriate parameters
		LinkedList<ALDOpParameterDescriptor> paramDescriptors = 
				new LinkedList<ALDOpParameterDescriptor>();
		for (ALDOpParameterDescriptor descr : this.opParamDescrips) {
			try {
				if ((descr.getDirection() == Parameter.Direction.IN || descr
						.getDirection() == Parameter.Direction.INOUT)
						&& (useRequired == null || descr.isRequired() == useRequired
								.booleanValue())
						&& (useSupplemental == null || descr.getSupplemental()
								.booleanValue() == useSupplemental
								.booleanValue())) {
					paramDescriptors.add(descr);
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}

		// ... and setup GUI elements
		if (paramDescriptors.size() > 0) {
			ALDParameterPanel panel = new ALDParameterPanel(this, paramDescriptors,
				panelTitle, this.topLevelCall, false);
			int componentCount = panel.changeViewMode(this.displayMode);
			if (componentCount == 0)
				panel.setVisible(false);
			else
				panel.setVisible(true);
			return panel;
		}
		return null;
	}

	/**
	 * Updates the set of parameter descriptors.
	 * <p>
	 * Note that the set of descriptors of an operator is not static, but might
	 * change due to dynamic parameter modifications by callback functions.
	 */
	private void updateOperatorParameterDescriptors() {
		this.opParamDescrips = new LinkedList<ALDOpParameterDescriptor>();

		// ask the operator for its IN / INOUT parameters
		LinkedList<String> namesSorted = new LinkedList<String>(
				this.operator.getParameterNames());
		java.util.Collections.sort(namesSorted);
		for (String pName : namesSorted) {
			if (logger.isDebugEnabled())
				logger.debug("Updating descriptors, found parameter {}...", pName);
			try {
				ALDOpParameterDescriptor descr = 
					this.operator.getParameterDescriptor(pName);

				// skip OUT parameters
				if (descr.getDirection().equals(Direction.OUT))
					continue;

				// get parameter values (...and use as defaults)
				// descr.setDefaultValue(this.operator.getParameter(pName));
				this.opParamDescrips.add(descr);
//				descr.setDefaultValue(this.getParameterValue(
//					new Boolean(descr.isRequired()), descr.getSupplemental(), descr));
			} catch (Exception e) {
				System.out
						.println("ALDOperatorParameterPanel: "
								+ " problems reading parameter descriptor... skipping!");
			}
		}
	}

	/**
	 * Updates panel according to values in currently given operator.
	 * @return List of parameters that could not be updated properly.
	 */
	private HashMap<ALDOpParameterDescriptor, String> updateParameterValues() {

		// update values in GUI of all parameter values
		HashMap<ALDOpParameterDescriptor, String> failedParams = 
				new HashMap<ALDOpParameterDescriptor, String>();
    
		for (ALDOpParameterDescriptor descr : this.opParamDescrips) {

			// ignore OUT parameters
			if (descr.getDirection().equals(Parameter.Direction.OUT))
				continue;

			// get parameter value from operator
			Object value = null;
			try {
				value = this.operator.getParameter(descr.getName());
				if (logger.isDebugEnabled()) {
					logger.debug(" {} = {}", descr.getName(), value);
				}
			} catch (ALDOperatorException ex) {
				failedParams.put(descr,
						"Reading parameter failed - " + ex.getCommentString());
				continue;
			}

			// is parameter required?
			if (descr.isRequired()) {
				if (this.panelRequiredParams != null) {
					try {
						this.panelRequiredParams.setParameter(descr, value);
					} catch (ALDDataIOException ex) {
						failedParams.put(descr, "Setting parameter failed - "
								+ ex.getCommentString());
						// Object[] options = { "OK" };
						// JOptionPane.showOptionDialog(null,
						// "Updating GUI failed! \n" +
						// "Could not set parameter \""+descr.getLabel()+"\"\n"+
						// e2.getCommentString(), "Warning",
						// JOptionPane.DEFAULT_OPTION,
						// JOptionPane.WARNING_MESSAGE,
						// null, options, options[0]);
						// } catch (ALDDataIOException e2) {
						// Object[] options = { "OK" };
						// JOptionPane.showOptionDialog(
						// null,
						// "Updating GUI failed! \n"
						// + "Could not set parameter \""
						// + descr.getLabel() + "\"\n"
						// + e2.getCommentString(), "Warning",
						// JOptionPane.DEFAULT_OPTION,
						// JOptionPane.WARNING_MESSAGE, null, options,
						// options[0]);
					}
				} else {
					failedParams.put(descr,
							"Panel for requested parameter not found!?");
					// Object[] options = { "OK" };
					// JOptionPane.showOptionDialog(null,
					// "Problem in GUI update! \n" +
					// "Found required parameters, but no panel?!", "Warning",
					// JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
					// null, options, options[0]);
				}
			}
			// Object[] options = { "OK" };
			// JOptionPane
			// .showOptionDialog(
			// null,
			// "Problem in GUI update! \n"
			// + "Found required parameters, but no panel?!",
			// "Warning", JOptionPane.DEFAULT_OPTION,
			// JOptionPane.WARNING_MESSAGE, null, options,
			// options[0]);
			// }
			// }
			// optional, but not supplemental?
			else if (!descr.getSupplemental().booleanValue()) {
//				if (value != null && this.panelOptionalParams != null) {
				if (this.panelOptionalParams != null) {
					// if (this.panelOptionalParams != null) {
					try {
						this.panelOptionalParams.setParameter(descr, value);
					} catch (ALDDataIOException ex) {
						failedParams.put(descr, "Setting parameter failed - "
								+ ex.getCommentString());
						// Object[] options = { "OK" };
						// JOptionPane.showOptionDialog(null,
						// "Updating GUI failed! \n" +
						// "Could not set parameter \""+descr.getLabel()+"\"\n"+
						// e2.getCommentString(), "Warning",
						// JOptionPane.DEFAULT_OPTION,
						// JOptionPane.WARNING_MESSAGE,
						// null, options, options[0]);
						// } catch (ALDDataIOException e2) {
						// Object[] options = { "OK" };
						// JOptionPane.showOptionDialog(
						// null,
						// "Updating GUI failed! \n"
						// + "Could not set parameter \""
						// + descr.getLabel() + "\"\n"
						// + e2.getCommentString(), "Warning",
						// JOptionPane.DEFAULT_OPTION,
						// JOptionPane.WARNING_MESSAGE, null, options,
						// options[0]);
					}
				} else if (value != null) {
					failedParams.put(descr,
							"Panel for requested parameter not found!?");
					// Object[] options = { "OK" };
					// JOptionPane.showOptionDialog(null,
					// "Problem in GUI update! \n" +
					// "Found optional parameters, but no panel?!", "Warning",
					// JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
					// null, options, options[0]);
				}
				// } else {
				// Object[] options = { "OK" };
				// JOptionPane
				// .showOptionDialog(
				// null,
				// "Problem in GUI update! \n"
				// + "Found optional parameters, but no panel?!",
				// "Warning", JOptionPane.DEFAULT_OPTION,
				// JOptionPane.WARNING_MESSAGE, null, options,
				// options[0]);
				// }
			}
			// must be supplemental...
			else {
				if (this.panelSupplementalParams != null) {
					try {
						this.panelSupplementalParams.setParameter(descr, value);
					} catch (ALDDataIOException ex) {
						failedParams.put(descr, "Setting parameter failed - "
								+ ex.getCommentString());
						// Object[] options = { "OK" };
						// JOptionPane.showOptionDialog(null,
						// "Updating GUI failed! \n" +
						// "Could not set parameter \""+descr.getLabel()+"\"\n"+
						// e2.getCommentString(), "Warning",
						// JOptionPane.DEFAULT_OPTION,
						// JOptionPane.WARNING_MESSAGE,
						// null, options, options[0]);
						// } catch (ALDDataIOException e2) {
						// Object[] options = { "OK" };
						// JOptionPane.showOptionDialog(
						// null,
						// "Updating GUI failed! \n"
						// + "Could not set parameter \""
						// + descr.getLabel() + "\"\n"
						// + e2.getCommentString(), "Warning",
						// JOptionPane.DEFAULT_OPTION,
						// JOptionPane.WARNING_MESSAGE, null, options,
						// options[0]);
					}
				} else {
					failedParams.put(descr,
							"Panel for requested parameter not found!?");
				}
			}
		}
		// update the GUI, i.e. redraw all components
		if (this.panelRequiredParams != null)
			this.panelRequiredParams.getJPanel().updateUI();
		if (this.panelOptionalParams != null)
			this.panelOptionalParams.getJPanel().updateUI();
		if (this.panelSupplementalParams != null)
			this.panelSupplementalParams.getJPanel().updateUI();

		// return list with failed descriptors or null
		if (failedParams.size() > 0)
			return failedParams;
		return null;
	}
	
	/**
	 * Initializes all internal members dealing with operator descriptors.
	 */
	private void updateParameterPanels() {

		// remove the panels and add only the ones containing parameters
		this.mainPanel.removeAll();
		
		LinkedList<ALDOpParameterDescriptor> requiredParams =
			this.getParameterDescriptors(ParameterType.PARAM_REQUIRED);
		if (requiredParams.size() > 0) {
			if (this.panelRequiredParams == null) {
				String title = "Parameters (required)";
				this.panelRequiredParams = new ALDParameterPanel(this, requiredParams,
						title, this.topLevelCall, false);
				this.panelRequiredParams.changeViewMode(this.displayMode);
				this.panelRequiredParams.addValueChangeEventListener(this);
			}
			else {
				this.panelRequiredParams.updateOperatorDescriptors(requiredParams);
			}
			this.mainPanel.add(this.panelRequiredParams.getJPanel());
		}

		LinkedList<ALDOpParameterDescriptor> optionalParams =
			this.getParameterDescriptors(ParameterType.PARAM_OPTIONAL);
		if (optionalParams.size() > 0) {
			if (this.panelOptionalParams == null) {
				String title = "Parameters (optional)";
				this.panelOptionalParams = new ALDParameterPanel(this, optionalParams,
						title, this.topLevelCall, false);
				this.panelOptionalParams.changeViewMode(this.displayMode);
				this.panelOptionalParams.addValueChangeEventListener(this);
			}
			else {
				this.panelOptionalParams.updateOperatorDescriptors(optionalParams);
			}
			this.mainPanel.add(this.panelOptionalParams.getJPanel());
		}
		
		LinkedList<ALDOpParameterDescriptor> supplementParams =
			this.getParameterDescriptors(ParameterType.PARAM_SUPPLEMENTAL);
		if (supplementParams.size() > 0) {
			if (this.panelSupplementalParams == null) {
				String title = "Supplemental Parameters";
				this.panelSupplementalParams = new ALDParameterPanel(this, 
					optionalParams,	title, this.topLevelCall, false);
				this.panelSupplementalParams.changeViewMode(this.displayMode);
				this.panelSupplementalParams.addValueChangeEventListener(this);
			}
			else {
				this.panelSupplementalParams.updateOperatorDescriptors(supplementParams);
			}
			this.mainPanel.add(this.panelSupplementalParams.getJPanel());
		}
		// update operator configuration by triggering event
//		this.handleValueChangeEvent(new ALDSwingValueChangeEvent(this, null));
	}
	
	/**
	 * Gets all the parameter descriptors of the given type.
	 * @param type	Type of descriptors to be returned.
	 * @return	List of parameters of requested type.
	 */
	private LinkedList<ALDOpParameterDescriptor> getParameterDescriptors(
			ParameterType type) {

		// allocate descriptor list
		LinkedList<ALDOpParameterDescriptor> parameterList = 
			new LinkedList<ALDOpParameterDescriptor>();

		// ask the operator for its IN / INOUT parameters
		LinkedList<String> namesSorted = new LinkedList<String>(
				this.operator.getParameterNames());
		java.util.Collections.sort(namesSorted);
		for (String pName : namesSorted) {
			try {
				ALDOpParameterDescriptor descr = this.operator
						.getParameterDescriptor(pName);
				
				// skip OUT parameters
				if (descr.getDirection().equals(Direction.OUT))
					continue;
				
				// fill temporary lists
				switch(type)
				{
				case PARAM_REQUIRED:
					if (descr.isRequired()) 
						parameterList.add(descr);
					break;
				case PARAM_OPTIONAL:
					if (!descr.isRequired() && !descr.getSupplemental().booleanValue())
						parameterList.add(descr);
					break;
				case PARAM_SUPPLEMENTAL:
					if (descr.getSupplemental().booleanValue()) 
						parameterList.add(descr);
					break;
				}
			} catch (Exception e) {
				System.out
						.println("ALDOperatorParameterPanel: "
								+ " problems reading parameter descriptor... skipping!");
			}
		}		
		return parameterList;
	}

	/**
	 * Retrieve parameter value from GUI and configure operator.
	 * @param paramName	Name of the parameter value to retrieve.
	 * @return True if successful, otherwise false.
	 */
	private boolean setOperatorParameter(String paramName) {

		if(logger.isDebugEnabled()){
			logger.debug("===========================================");
			logger.debug(" Setting operator parameter \" {} \"...", paramName);
			logger.debug("===========================================");
		}

		// search for the corresponding descriptor
		ALDOpParameterDescriptor paramDescr = null;
		for (ALDOpParameterDescriptor descr : this.opParamDescrips) {
			if (descr.getName().equals(paramName)) {
				paramDescr = descr;
				break;
			}
		}
		if (   paramDescr == null 
				|| paramDescr.getDirection().equals(Parameter.Direction.OUT))
			return false;

		Object value = null;
		// is parameter required?
		if (paramDescr.isRequired()) {
			if (this.panelRequiredParams != null) {
				try {
					value = this.panelRequiredParams.readParameter(paramDescr);
				} catch (ALDDataIOException ex) {
					return false;
				}
			} else {
				return false;
			}
		}
		// optional, but not supplemental?
		else if (!paramDescr.getSupplemental().booleanValue()) {
			if (this.panelOptionalParams != null) {
				try {
					value = this.panelOptionalParams.readParameter(paramDescr);
				} catch (ALDDataIOException ex) {
					return false;
				}
			} else {
				return false;
			}
		}
		// must be supplemental...
		else {
			if (this.panelSupplementalParams != null) {
				try {
					value = this.panelSupplementalParams.readParameter(paramDescr);
				} catch (ALDDataIOException ex) {
					return false;
				}
			} else {
				return false;
			}
		}
		if (logger.isDebugEnabled())
			logger.debug("Setting {} to {}...", paramDescr.getName(), value);
		try {
			this.operator.setParameter(paramName, value);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Operator is configured:");
			this.operator.print();
		}
		return true;
	}

	/**
	 * Get the current value for a paramter.
	 * <p>
	 * Usually the value of a parameter is fetched directly from the
	 * operator itself. However, sometimes values depend on
	 * the system state or are taken from external devices (database, GUI). This
	 * method allows to customize the reading of parameter values and is
	 * intended to be overriden by sub-classes.
	 * 
	 * @param isRequired				Should be true, if parameter is required.
	 * @param isSupplemental		Should be true, if parameter is required.
	 * @param descr							Parameter descriptor in question.
	 * @return Current value of requested parameter, might be null.
	 */
	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDParameterPanelParent#getParameterValue(boolean, boolean, de.unihalle.informatik.Alida.operator.ALDParameterDescriptor)
	 */
	@Override
  protected Object getParameterValue(boolean isRequired, boolean isSupplemental,	
  		ALDParameterDescriptor descr) {
		try {
			return this.operator.getParameter(descr.getName());
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Retrieve parameter values from GUI and configure operator accordingly.
	 * 
	 * @return Null if successful, otherwise list with parameters that could not
	 *         been set.
	 */
	// TODO should be private in future...
	public HashMap<ALDOpParameterDescriptor, String> readGUIParameterValues() {
		// for ( Map.Entry<ALDOpParameterDescriptor,JComponent> entry :
		// this.componentMap.entrySet() ) {
		HashMap<ALDOpParameterDescriptor, String> failedParams = 
			new HashMap<ALDOpParameterDescriptor, String>();
		for (ALDOpParameterDescriptor descr : this.opParamDescrips) {

			if (logger.isDebugEnabled()) 
				logger.debug("... parameter {}", descr.getName());
			
			// skip output parameters
			if (descr.getDirection().equals(Parameter.Direction.OUT))
				continue;

			// skip batch mode parameter
			if (   this.parentFrame != null
			    && this.parentFrame.isBatchModeActive()
			    && this.parentFrame.getBatchInputParameters() != null
			    && this.parentFrame.getBatchInputParameters().size() > 0
			    && descr.getName().equals(
			      this.parentFrame.getBatchInputParameters().getFirst())) {
			    continue;
			}

			Object value = null;
			// is parameter required?
			if (descr.isRequired()) {
				if (this.panelRequiredParams != null) {
					try {
						value = this.panelRequiredParams.readParameter(descr);
					} catch (ALDDataIOException ex) {
						failedParams.put(
								descr,
								"Reading value failed - "
										+ ex.getCommentString());
						continue;
					}
				} else {
					failedParams.put(descr,
							"Panel for requested parameter not found!?");
					continue;
				}
			}
			// optional, but not supplemental?
			else if (!descr.getSupplemental().booleanValue()) {
				if (this.panelOptionalParams != null) {
					try {
						value = this.panelOptionalParams.readParameter(descr);
					} catch (ALDDataIOException ex) {
						failedParams.put(
								descr,
								"Reading value failed - "
										+ ex.getCommentString());
						continue;
					}
				} else {
					failedParams.put(descr,
							"Panel for requested parameter not found!?");
					continue;
				}
			}
			// must be supplemental...
			else {
				if (this.panelSupplementalParams != null) {
					try {
						value = this.panelSupplementalParams
								.readParameter(descr);
					} catch (ALDDataIOException ex) {
						failedParams.put(
								descr,
								"Reading value failed - "
										+ ex.getCommentString());
						continue;
					}
				} else {
					failedParams.put(descr,
							"Panel for requested parameter not found!?");
					continue;
				}
			}
			if (logger.isDebugEnabled())
				logger.debug("Setting {} to {}...", descr.getName(), value);
			try {
				this.operator.setParameter(descr.getName(), value);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
				ex.printStackTrace();
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("=============================");
			logger.debug("Operator is configured:      ");
			logger.debug("=============================");
			this.operator.print();
		}
		
		if (failedParams.size() > 0)
			return failedParams;
		return null;
	}

	/**
	 * Get access to the main panel.
	 * 
	 * @return Reference to the main panel.
	 */
	public JPanel getJPanel() {
		return this.mainPanel;
	}

	/**
	 * Configure parent frame in which given one is embedded.
	 * @param pf	Reference to the parent frame.
	 */
	public void setParentFrame(ALDOperatorControlFrame pf) {
		this.parentFrame = pf;
	}

	/**
	 * Changes panel for a parameter to indicate that it is linked.
	 * 
	 * @param paramName					Name of the parameter.
	 * @param sourceOp					Source node from where the edge originates.
	 * @param sourceParamName		Name of the source parameter.
	 * @return	True if operation was successful, otherwise false.
	 */
	public boolean setParameterLinked(
			String paramName, String sourceOp, String sourceParamName) {
		try {
	    ALDOpParameterDescriptor desc = 
	    		this.operator.getParameterDescriptor(paramName);
    	String linkValue = sourceOp + "@" + sourceParamName;
	    if (desc.isRequired()) {
	    	return this.panelRequiredParams.setParamExternallyConfigured(
	    			desc, linkValue);
	    }
	    else if (!desc.getSupplemental().booleanValue()) {
	    	return this.panelOptionalParams.setParamExternallyConfigured(
	    			desc, linkValue);
	    }
	    else {
	    	return this.panelSupplementalParams.setParamExternallyConfigured(
	    			desc, linkValue);	    	
	    }
    } catch (ALDOperatorException e) {
    	return false;
    }
	}
	
	/**
	 * Changes panel for a parameter to indicate that it is not linked anymore.
	 * 
	 * @param paramName					Name of the parameter.
	 * @return	True if operation was successful, otherwise false.
	 */
	public boolean setParameterNotLinked(String paramName) {
		try {
	    ALDOpParameterDescriptor desc = 
	    		this.operator.getParameterDescriptor(paramName);
	    if (desc.isRequired()) {
	    	boolean success = 
	    			this.panelRequiredParams.setParamNotExternallyConfigured(desc);
	    	this.updateConfigurationStatus(this.operator.unconfiguredItems());
	    	return success;
	    }
	    else if (!desc.getSupplemental().booleanValue()) {
	    	boolean success = 
	    			this.panelOptionalParams.setParamNotExternallyConfigured(desc);
	    	this.updateConfigurationStatus(this.operator.unconfiguredItems());
	    	return success;
	    }
	    else {
	    	boolean success = 
	    			this.panelSupplementalParams.setParamNotExternallyConfigured(desc);
	    	this.updateConfigurationStatus(this.operator.unconfiguredItems());
	    	return success;
	    }
    } catch (ALDOperatorException e) {
    	return false;
    }
	}

	public boolean setParameterBatchModeInput(String paramName) {
		try {
	    ALDOpParameterDescriptor desc = 
	    		this.operator.getParameterDescriptor(paramName);
	    String linkValue = "<batch mode input>";
	    if (desc.isRequired())
	    	return this.panelRequiredParams.setParamExternallyConfigured(desc, 
	    																															linkValue);
	    else if (desc.getSupplemental().booleanValue()) {
	    	return this.panelSupplementalParams.setParamExternallyConfigured(desc, 
						linkValue);
	    }
	    else
	    	return this.panelOptionalParams.setParamExternallyConfigured(desc, 
						linkValue);
    } catch (ALDOperatorException e) {
    	return false;
    }
	}

	/**
	 * Resets operator configuration to default values.
	 * @throws ALDDataIOProviderException
	 */
//	public HashMap<ALDOpParameterDescriptor, String> resetOperatorParameters() 
//			throws ALDDataIOProviderException {
//		
//		// disable event handling during resetting operator
//		this.valueChangeEventsDisabled = true;
//		
//		Class<?> opClass = this.operator.getClass();
//		try {
//	    this.operator = this.operator.getClass().newInstance();
//			this.updateOperatorDescriptors();
//			HashMap<ALDOpParameterDescriptor, String> failedParams= 
//					this.updatePanel();
//			// turn on event handling again
//			this.valueChangeEventsDisabled = false;
//			return failedParams;
//    } catch (Exception e) {
//    	throw new ALDDataIOProviderException(
//    			ALDDataIOProviderExceptionType.OBJECT_INSTANTIATION_ERROR,
//    			"[ALDOperatorParameterPanel] Could not init object of type " + 
//    				opClass.toString() + "...\n" + e.getMessage());
//    }
//	}

	/**
	 * Sets a new operator for the panel.
	 * <p>
	 * This function is, e.g., called in case of loading a new operator 
	 * from file.
	 * 
	 * @param op		New operator object to be associated with GUI.
	 * @return Null in case of success, otherwise list of parameters that could
	 *          not be set.
	 * @throws ALDDataIOProviderException
	 */
	public HashMap<ALDOpParameterDescriptor, String> setNewOperator(
			ALDOperator op) throws ALDDataIOProviderException {
		// check if new operator has same class like given one...
		if (!(op.getClass().equals(this.operator.getClass()))) {
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR,
					"[ALDOperatorParameterPanel::updateConfigPanel()] "
							+ "Update with different object type requested...!\n"
							+ "Old class: " + this.operator.getClass() + "\n"
							+ "New class: " + op.getClass());
		}
		this.operator = op;
		this.updateOperatorParameterDescriptors();
		this.updateParameterPanels();
		HashMap<ALDOpParameterDescriptor, String> failedParams= 
			this.updateParameterValues();
		// notify everybody of the change
//		this.handleValueChangeEvent(new ALDSwingValueChangeEvent(this));
		return failedParams;
	}

	/**
	 * Marks configurations status of required parameters by color.
	 * <p>
	 * A required parameter which is neither properly configured nor 
	 * has an incoming edge is gets a red label, all others get black labels. 
	 */
	public void updateConfigurationStatus(Collection<String> missingParams) {
		if (this.panelRequiredParams != null) {
			Collection<String> requiredParams =
				this.operator.getInInoutNames(new Boolean(true));
			for (String rp : requiredParams) {
				if (logger.isDebugEnabled())
					logger.debug("Checking {}...", rp);
				try {
					ALDOpParameterDescriptor desc= this.operator.getParameterDescriptor(rp);
					if (missingParams.contains(rp)) {
						// check if parameter is probably configured via batch provider
						if (   this.parentFrame != null
								&& this.parentFrame.isBatchModeActive()
								&& this.parentFrame.getBatchInputParameters() != null
								&& !this.parentFrame.getBatchInputParameters().isEmpty()
								&& this.parentFrame.getBatchInputParameters().getFirst().equals(rp)) {
							this.panelRequiredParams.markParameterProbablyConf(desc);
							if (logger.isDebugEnabled())
								logger.debug("--> Could be configured...");
						}
						else {
							this.panelRequiredParams.markParameter(desc);
							if (logger.isDebugEnabled())
								logger.debug("--> Missing because it's null...");
						}
					}
					else {
						// if parameter is not missing, try a deep-validation
						Object paramValue = this.operator.getParameter(desc.getName()); 
						if (paramValue instanceof ALDConfigurationValidator) {
							if (((ALDConfigurationValidator)paramValue).isConfigured()) {
								this.panelRequiredParams.unmarkParameter(desc);
								if (logger.isDebugEnabled())
									logger.debug("--> Configured!");
							}
							else {
								if (!this.topLevelCall) {
									this.panelRequiredParams.markParameter(desc);
									if (logger.isDebugEnabled())
										logger.debug("--> Missing because miss-configured...");
								}
								else {
									this.panelRequiredParams.markParameterProbablyConf(desc);
									if (logger.isDebugEnabled())
										logger.debug("--> Could be configured...");
								}
							}
						}
						else {
							this.panelRequiredParams.unmarkParameter(desc);
							if (logger.isDebugEnabled())
								logger.debug("--> Configured!");
						}
					}
				} catch (ALDOperatorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		// mark optional parameters in gray that are null
		if (this.panelOptionalParams != null) {
			Collection<String> optionalParams =
					this.operator.getInInoutNames(new Boolean(false));
			for (String op : optionalParams) {
				try {
					if (this.operator.getParameter(op) == null) {
						this.panelOptionalParams.markParameter(
								this.operator.getParameterDescriptor(op));
					}
					else {
						this.panelOptionalParams.unmarkParameter(
								this.operator.getParameterDescriptor(op));
					}
				} catch (ALDOperatorException e) {
					// just skip parameters that cannot be processed
				}
			}
		}
	}
	
	/**
	 * Switches view mode of the panel.
	 * 
	 * @param mode
	 *            Desired view mode.
	 */
	public void changeViewMode(Parameter.ExpertMode mode) {
		int visibleComponents = 0;
		if (this.panelRequiredParams != null) {
			visibleComponents = this.panelRequiredParams.changeViewMode(mode);
			if (visibleComponents == 0)
				this.panelRequiredParams.setVisible(false);
			else
				this.panelRequiredParams.setVisible(true);
		}
		if (this.panelOptionalParams != null) {
			visibleComponents = this.panelOptionalParams.changeViewMode(mode);
			if (visibleComponents == 0)
				this.panelOptionalParams.setVisible(false);
			else
				this.panelOptionalParams.setVisible(true);
		}
		if (this.panelSupplementalParams != null) {
			visibleComponents = this.panelSupplementalParams.changeViewMode(mode);
			if (visibleComponents == 0)
				this.panelSupplementalParams.setVisible(false);
			else
				this.panelSupplementalParams.setVisible(true);
		}
	}

	/**
	 * Disables all components in the different sub-panels.
	 */
	public void disableComponents() {
		if (this.panelRequiredParams != null)
			this.panelRequiredParams.disableComponents();
		if (this.panelOptionalParams != null)
			this.panelOptionalParams.disableComponents();
		if (this.panelSupplementalParams != null)
			this.panelSupplementalParams.disableComponents();
	}
	
	/**
	 * Enables all components in the different sub-panels.
	 */
	public void enableComponents() {
		if (this.panelRequiredParams != null)
			this.panelRequiredParams.enableComponents();
		if (this.panelOptionalParams != null)
			this.panelOptionalParams.enableComponents();
		if (this.panelSupplementalParams != null)
			this.panelSupplementalParams.enableComponents();
	}
	
	/**
	 * Disposes all components in the different sub-panels.
	 */
	public void dispose() {
		if (this.panelRequiredParams != null)
			this.panelRequiredParams.dispose();
		if (this.panelOptionalParams != null)
			this.panelOptionalParams.dispose();
		if (this.panelSupplementalParams != null)
			this.panelSupplementalParams.dispose();
	}
	
	/**
	 * Check if operator is ready to run.
	 * <p>
	 * The operator can be run if all GUI elements of required parameters return
	 * non-null values, and if furtheron the operator's validate() method
	 * returns true.
	 * 
	 * @param deepValidate
	 *            True if operator has to be validate itself.
	 * @return True, if operator is properly configured.
	 * @throws ALDOperatorException
	 */
	public HashMap<ALDParameterDescriptor, String> validateOperatorParameters(
			boolean deepValidate) throws ALDOperatorException {

		// init result list
		HashMap<ALDParameterDescriptor, String> failedList = new HashMap<ALDParameterDescriptor, String>();

		// first check if all GUI elements can deliver proper values.
		HashMap<ALDParameterDescriptor, String> tmpList;
		Set<ALDParameterDescriptor> keys;
		if (this.panelRequiredParams != null) {
			this.panelRequiredParams.unmarkAllParameters();
			tmpList = this.panelRequiredParams.validateParameters();
			if (tmpList != null) {
				keys = tmpList.keySet();
				for (ALDParameterDescriptor descr : keys) {
					failedList.put(descr, tmpList.get(descr));
				}
			}
		}
		if (this.panelOptionalParams != null) {
			this.panelOptionalParams.unmarkAllParameters();
			tmpList = this.panelOptionalParams.validateParameters();
			if (tmpList != null) {
				keys = tmpList.keySet();
				for (ALDParameterDescriptor descr : keys) {
					failedList.put(descr, tmpList.get(descr));
				}
			}
		}
		if (this.panelSupplementalParams != null) {
			this.panelSupplementalParams.unmarkAllParameters();
			tmpList = this.panelSupplementalParams.validateParameters();
			if (tmpList != null) {
				keys = tmpList.keySet();
				for (ALDParameterDescriptor descr : keys) {
					failedList.put(descr, tmpList.get(descr));
				}
			}
		}

		// check required operator parameters, but only on top level
		if (failedList.size() == 0 && this.topLevelCall) {
			for (ALDOpParameterDescriptor descr : this.opParamDescrips) {

				// try to read current parameter value
				@SuppressWarnings("unused")
				Object readValue = null;
				if (descr.isRequired()) {

					// skip output parameters
					if (descr.getDirection() == Parameter.Direction.OUT)
						continue;

					try {
						readValue = this.panelRequiredParams
								.readParameter(descr);
					} catch (ALDDataIOException e) {
						if (this.panelOptionalParams != null) {
							// try to read from optional parameters panel
							try {
								readValue = this.panelOptionalParams
										.readParameter(descr);
								// if (readValue != null)
								// paramType = 1; // optional parameter
							} catch (ALDDataIOException e1) {
								if (this.panelSupplementalParams != null) {
									// if still null, try to read from
									// supplemental parameters panel
									try {
										readValue = this.panelSupplementalParams
												.readParameter(descr);
										// if (readValue != null)
										// paramType = 2; // supplemental
										// parameter
									} catch (ALDDataIOException e2) {
										failedList.put(descr,
												e2.getCommentString());
										// Object[] options = { "OK" };
										// JOptionPane
										// .showOptionDialog(
										// null,
										// "Reading parameter \""
										// + descr.getLabel()
										// + "\" failed!"
										// + "Reason:\n"
										// + e2.getCommentString(),
										// "Warning",
										// JOptionPane.DEFAULT_OPTION,
										// JOptionPane.WARNING_MESSAGE,
										// null, options,
										// options[0]);
									}
								}
							}
						}
					}
				}
			}
		}
		if (failedList.size() != 0)
			return failedList;

		// validate the operator itself
		this.readGUIParameterValues();
		if (deepValidate) {
			this.operator.validate();
		}
		return null;
	}

	@Override
	public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
		
		ALDParameterDescriptor paramDescr = event.getParamDescriptor();
		if (paramDescr == null) {
			if(logger.isDebugEnabled()){
				logger.debug("===========================================");
				logger.debug("Setting parameters of {}...", this.operator.getName());
				logger.debug("===========================================");
			}
			this.readGUIParameterValues();
		}
		else {
			if(logger.isDebugEnabled()){
				logger.debug("===========================================");
				logger.debug("Parameter \" {} \" of {} changed, setting new value...",
					paramDescr.getName(), this.operator.getName());
				logger.debug("===========================================");
			}
			this.setOperatorParameter(paramDescr.getName());
			// check if parameter could have changed operator interface
//			if (   paramDescr.parameterModificationMode() 
//					!= Parameter.ParameterModificationMode.MODIFIES_NOTHING) {
//				this.updateOperatorParameterDescriptors();
//				this.updateParameterPanels();
//			}
		}
		// update the GUI since setting the parameters could have changed other 
		// parameter values as well due to callbacks
//		this.updateParameterValues();
		// validate the operator parameters
//		List<String> missList = this.operator.unconfiguredItems();
//		this.updateConfigurationStatus(missList);
		// inform listeners of the changes
		this.fireALDSwingValueChangeEvent(event);
	}

	/**
	 * Replace the panel of given parameter to insert a link.
	 * 
	 * @param opLink
	 *            Name of parameter.
	 * @param value
	 *            Default value of the new panel.
	 * @param editorFrame
	 */
//	public void changePanel(String opLink, Object value,
//			ALDEditorFrame editorFrame) {
//		String opName = opLink.split("@")[0];
//		String parName = opLink.split("@")[1];
//		for (ALDOpParameterDescriptor descr : this.opParamDescrips) {
//			if (descr.getName().equals(parName)) {
//				if (descr.getDirection() == Direction.IN) {
//					// check if parameter is required, optional or supplemental
//					if (descr.isRequired()) {
//						panelRequiredParams.setFrame(editorFrame, opName);
//						panelRequiredParams.changePanel(descr, value);
//					} else if (!descr.getSupplemental().booleanValue()) {
//						panelOptionalParams.setFrame(editorFrame, opName);
//						panelOptionalParams.changePanel(descr, value);
//					} else {
//						panelSupplementalParams.setFrame(editorFrame, opName);
//						panelSupplementalParams.changePanel(descr, value);
//					}
//					this.mainPanel.updateUI();
//				}
//			}
//		}
//	}
	
	/**
	 * TODO Function should usually not be required...
	 * 
	 * @param opLink
	 * @param b
	 */
//	public void setDescriptorLinked(String opLink, boolean b) {
//		String parName = opLink.split("@")[1];
//		for (ALDOpParameterDescriptor descr : this.opParamDescrips) {
//			if (descr.getName().equals(parName)) {
//				descr.setLink(b);
//			}
//		}
//	}

	// TODO comments
	public void copyConfiguration(ALDOperatorParameterPanel opParameterPanel)
			throws ALDDataIOException {
		if (this.panelRequiredParams != null)
			this.panelRequiredParams
					.copyConfiguration(opParameterPanel.panelRequiredParams);
		if (this.panelOptionalParams != null)
			this.panelOptionalParams
					.copyConfiguration(opParameterPanel.panelOptionalParams);
		if (this.panelSupplementalParams != null)
			this.panelSupplementalParams
					.copyConfiguration(opParameterPanel.panelSupplementalParams);
	}
}
