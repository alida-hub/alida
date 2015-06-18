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

import java.awt.*;
import java.io.IOException;
import java.util.*;

import de.unihalle.informatik.Alida.operator.*;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeEvent;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeListener;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeReporter;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida.grappa.ALDGrappaLinkDataIOSwing;

/**
 * Class for configuring sets of parameters via GUI in Alida.
 * <p>
 * This class provides a panel containing GUI elements to configure all
 * parameters of a set provided at initialization. Note that parameter labels
 * can be marked and unmarked externally. This feature is, e.g., be used during
 * parameter validation.
 * <p>
 * This panel allows to validate its parameters, i.e. if a certain parameter is
 * declared as required, but reading the parameter results in 'null', the
 * parameter is marked in the GUI.
 * 
 * @author moeller
 */
public class ALDParameterPanel extends ALDSwingValueChangeReporter 
  implements ALDSwingValueChangeListener {

	/**
	 * Slf4j logger.
	 */
	private static Logger logger = 
		LoggerFactory.getLogger(ALDOperatorParameterPanel.class);

	/**
	 * Reference of parent panel to which this one belongs.
	 */
	private ALDParameterPanelParent panelParent;
	
	/**
	 * The main panel containing everything.
	 */
	private JPanel mainPanel;

	/**
	 * Flag to disable editability of GUI elements.
	 * <p>
	 * If the flag is set <code>true</code>, parameter editing is not possible,
	 * i.e. the graphical elements in the window are not editable.
	 */
	private boolean onlyDisplay = false;

	/**
	 * Flag indicating if panel is used on top level or not.
	 * <p>
	 * Using the panel on top-level refers to usage with operators directly
	 * executed, while non-top-level use refers to usage with operators passed
	 * as parameters to other operators. In the second case no deep validation
	 * is done, i.e. required parameters are allowed to have null values.
	 * Consequently missing parameters are only marked orange in the GUI.
	 */
	private boolean topLevelCall = false;

	/**
	 * List of parameter descriptors managed by this panel.
	 */
	private LinkedList<? extends ALDParameterDescriptor> paramDescriptors;

	/**
	 * Optional title string for panel.
	 */
	private String titleString;
	
	/**
	 * The panel containing the title label.
	 */
	private JPanel titlePanel;

	/**
	 * Maps a descriptor to its <code>JLabel</code> component in the panel.
	 */
	private HashMap<ALDParameterDescriptor, JLabel> labelMap = null;

	/**
	 * Maps a parameter descriptor to its component in the GUI.
	 */
	private HashMap<ALDParameterDescriptor, ALDSwingComponent> compMap = null;

	/**
	 * Maps a parameter to the component shown if parameter is linked (in Grappa).
	 */
	private HashMap<ALDParameterDescriptor, ALDSwingComponent> compMapLinkBak;
	
	/**
	 * Maps a parameter descriptor to its panel in the GUI.
	 */
	private HashMap<ALDParameterDescriptor, JPanel> panelMap = null;
	
	/**
	 * Map keeping track if parameters are linked or not (in Grappa).
	 */
	private HashMap<ALDParameterDescriptor, Boolean> isParamLinked = null;

	/**
	 * Default constructor.
	 * 
	 * @param pPanel					Reference to the parent panel.
	 * @param paramDescrips		List of parameter descriptors to manage.
	 * @param title						Title of panel.
	 * @param topLevel				If true, null returns are forbidden.
	 * @param noEdit					If true, elements cannot be edited.
	 */
	public ALDParameterPanel(ALDParameterPanelParent pPanel,
			LinkedList<? extends ALDParameterDescriptor> paramDescrips,
			String title, boolean topLevel, boolean noEdit) {
		this.panelParent = pPanel;
		this.mainPanel = new JPanel();
		this.paramDescriptors = paramDescrips;
		this.titleString = title;
		this.topLevelCall = topLevel;
		this.onlyDisplay = noEdit;
		this.isParamLinked = new HashMap<ALDParameterDescriptor, Boolean>();
		for (ALDParameterDescriptor p : paramDescrips)
			this.isParamLinked.put(p, new Boolean(false));
		this.compMapLinkBak = 
				new HashMap<ALDParameterDescriptor, ALDSwingComponent>();
		this.buildInputPanel();
	}

	/**
	 * Builds up the input panel, only called on construction.
	 */
	private void buildInputPanel() {
		this.compMap = new HashMap<ALDParameterDescriptor, ALDSwingComponent>();
		this.labelMap = new HashMap<ALDParameterDescriptor, JLabel>();
		this.panelMap = new HashMap<ALDParameterDescriptor, JPanel>();
		BoxLayout ylayout = new BoxLayout(this.mainPanel, BoxLayout.Y_AXIS);
		this.mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.mainPanel.setLayout(ylayout);

		// and setup GUI elements
		if (this.paramDescriptors.size() > 0) {
			if (this.titleString != null && !this.titleString.isEmpty()) {
				// add new panel for label
				this.titlePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
				// new title label
				JLabel titleLabel = new JLabel(" " + this.titleString + " ");
				// element separator before label
				JSeparator labelSep1 = new JSeparator(SwingConstants.HORIZONTAL);
				labelSep1.setPreferredSize(new Dimension(200, 1));
				// add element separator before label
				this.titlePanel.add(labelSep1);
				this.titlePanel.add(titleLabel);
				// element separator after label
				JSeparator labelSep2 = new JSeparator(SwingConstants.HORIZONTAL);
				labelSep2.setPreferredSize(new Dimension(200, 1));
				// add element separator after label
				this.titlePanel.add(labelSep2);
			}
			// add the graphical components for the current set of parameters
			this.updateParameterGUIElements();
		}
	}
		
	/**
	 * Adds the graphical components for current parameters to panel.
	 */
	private void updateParameterGUIElements() {

		// delete old elements
		this.mainPanel.removeAll();

		// add title panel again, if available
		if (this.titlePanel != null)
			this.mainPanel.add(this.titlePanel);
		
		// sort descriptors according to GUI order into hash table
		HashMap<Integer, Vector<ALDParameterDescriptor>> guiOrderHash = 
			new HashMap<Integer, Vector<ALDParameterDescriptor>>();
		for (ALDParameterDescriptor descr : this.paramDescriptors) {
			Integer order = new Integer(descr.getDataIOOrder());
			if (guiOrderHash.containsKey(order)) {
				guiOrderHash.get(order).add(descr);
			} else {
				Vector<ALDParameterDescriptor> paramVec = 
					new Vector<ALDParameterDescriptor>();
				paramVec.add(descr);
				guiOrderHash.put(order, paramVec);
			}
		}
		Set<Integer> keys = guiOrderHash.keySet();
		LinkedList<Integer> keyList = new LinkedList<Integer>();
		for (Integer key : keys) {
			keyList.add(key);
		}
		Collections.sort(keyList);
		
		// add the parameters with their graphical components to the GUI
		for (Integer key : keyList) {
			Vector<ALDParameterDescriptor> descrips = guiOrderHash.get(key);
			for (ALDParameterDescriptor descr : descrips) {
				this.registerGUIElement(descr);
			}
		}
	}

	/**
	 * Adds a graphical component for the given parameter to the GUI.
	 * <p>
	 * The method checks if the parameter is already known, i.e., if graphical
	 * components, labels and panels have already been initialized before. If 
	 * so, these elements are used, otherwise new components are initialized.
	 * 
	 * @param descr		Descriptor of the parameter to be added to the GUI.
	 */
	private void registerGUIElement(ALDParameterDescriptor descr) {
		try {
			// check if descriptor is already registered, if not, create new element
			if (this.compMap.get(descr) == null) {
				if (logger.isDebugEnabled())
					logger.debug("Requesting GUI element for parameter {} "
						+ " of class {}...", descr.getName(), descr.getMyclass().getName());
				JPanel paramPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
				JLabel nameLabel = new JLabel(descr.getLabel());
				this.labelMap.put(descr, nameLabel);
				nameLabel.setToolTipText("<html>" + "Class: "
						+ descr.getMyclass().getSimpleName() + "<br>"
						+ descr.getExplanation() + "</html>");
				paramPanel.add(nameLabel);

				ALDSwingComponent aldElement = null;
				JComponent guiElement = null;
				if (!this.onlyDisplay) {
					try {
						aldElement = ALDDataIOManagerSwing
							.getInstance().createGUIElement(
								descr.getField(),	descr.getMyclass(),
								this.panelParent.getParameterValue(
									descr.isRequired(), false, descr), descr);
						guiElement = aldElement.getJComponent();
					} catch (ALDDataIOManagerException exp) {
						// check if a provider was found
						ALDSwingComponentLabel warning = new ALDSwingComponentLabel(
								"ERROR: provider returned no element!");
						warning.getJComponent().setForeground(
								java.awt.Color.red);
						this.compMap.put(descr, warning);
						paramPanel.add(warning.getJComponent());
						// add new element to panel
						this.mainPanel.add(paramPanel);
						// this.panelMap.put(descr, paramPanel);
					}
					if (aldElement != null && guiElement != null) {
						aldElement.addValueChangeEventListener(this);
						this.compMap.put(descr, aldElement);
						paramPanel.add(guiElement);
						this.panelMap.put(descr, paramPanel);
						// add new element to panel
						this.mainPanel.add(paramPanel);
					}
				}
				// display only, e.g. for result display of parametrized classes
				else {
					try {
						guiElement = ALDDataIOManagerSwing.getInstance().writeData(
							this.panelParent.getParameterValue(false, false, descr), descr);
					} catch (ALDDataIOManagerException exp) {
						// check if a provider was found
						ALDSwingComponentLabel warning = new ALDSwingComponentLabel(
								"ERROR: provider returned no element!");
						warning.getJComponent().setForeground(
								java.awt.Color.red);
						this.compMap.put(descr, warning);
						paramPanel.add(warning.getJComponent());
						// add new element to panel
						this.mainPanel.add(paramPanel);
						// this.panelMap.put(descr, paramPanel);
					}
					if (guiElement != null) {
						paramPanel.add(guiElement);
						this.panelMap.put(descr, paramPanel);
						// add new element to panel
						this.mainPanel.add(paramPanel);
					}
				}
			}
			// descriptor is already registered, add existing panel
			else {
				this.mainPanel.add(this.panelMap.get(descr));
			}				
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to access associated panel.
	 * 
	 * @return Reference to the main panel.
	 */
	public JPanel getJPanel() {
		return this.mainPanel;
	}

	/**
	 * Changes the view mode.
	 * <p>
	 * There are the view modes 'standard' and 'advanced' available in the GUI.
	 * In standard mode only the parameters annotated as standard are visible,
	 * while in advanced mode all parameters are visible.
	 * 
	 * @param mode	Desired display mode.
	 * @return Number of visible components in this panel.
	 */
	public int changeViewMode(Parameter.ExpertMode mode) {
		int visibleComponentCount = 0;
		Set<ALDParameterDescriptor> keys = this.labelMap.keySet();
		for (ALDParameterDescriptor descr : keys) {
			if (mode.equals(Parameter.ExpertMode.STANDARD)
					&& descr.getHandlingMode().equals(
							Parameter.ExpertMode.ADVANCED)) {
				if (this.labelMap.get(descr) != null) {
					this.labelMap.get(descr).setVisible(false);
				}
				if (this.compMap.get(descr) != null) {
					this.compMap.get(descr).getJComponent().setVisible(false);
				}
			} else if (descr.getHandlingMode().equals(
					Parameter.ExpertMode.STANDARD)) {
				if (this.labelMap.get(descr) != null)
					this.labelMap.get(descr).setVisible(true);
				if (this.compMap.get(descr) != null)
					this.compMap.get(descr).getJComponent().setVisible(true);
				++visibleComponentCount;
			} else {
				if (this.labelMap.get(descr) != null)
					this.labelMap.get(descr).setVisible(true);
				if (this.compMap.get(descr) != null)
					this.compMap.get(descr).getJComponent().setVisible(true);
				++visibleComponentCount;
			}
		}
		return visibleComponentCount;
	}
	
	/**
	 * Function to enable or disable the panel.
	 * @param b		If true, panel is enabled, otherwise components are disabled.
	 */
	public void setEnabled(boolean b) {
		Set<ALDParameterDescriptor> keys = this.labelMap.keySet();
		for (ALDParameterDescriptor descr : keys) {
			this.compMap.get(descr).getJComponent().setEnabled(b);
		}
	}

	/**
	 * Set panel visible or invisible.
	 * @param isVisible	If true, panel is set visible.
	 */
	public void setVisible(boolean isVisible) {
		this.mainPanel.setVisible(isVisible);
	}
	
	/**
	 * Disables all components in this panel.
	 */
	public void disableComponents() {
		Set<ALDParameterDescriptor> keys = this.compMap.keySet();
		for (ALDParameterDescriptor key: keys)
			this.compMap.get(key).disableComponent();
	}
	
	/**
	 * Enables all components in this panel.
	 */
	public void enableComponents() {
		Set<ALDParameterDescriptor> keys = this.compMap.keySet();
		for (ALDParameterDescriptor key: keys)
			this.compMap.get(key).enableComponent();
	}

	/**
	 * Disposes all components in this panel.
	 */
	public void dispose() {
		Set<ALDParameterDescriptor> keys = this.compMap.keySet();
		for (ALDParameterDescriptor key: keys)
			this.compMap.get(key).dispose();
	}

	/**
	 * Updates the set of parameter descriptors.
	 * @param newDescr	Set of new descriptors.
	 */
	public void updateOperatorDescriptors(
			LinkedList<ALDOpParameterDescriptor> newDescr) {
		
		// set new descriptors
		this.paramDescriptors = newDescr;
		this.isParamLinked = new HashMap<ALDParameterDescriptor, Boolean>();
		for (ALDParameterDescriptor p : this.	paramDescriptors)
			this.isParamLinked.put(p, new Boolean(false));
		
		// update internal maps keeping track of components, panels and labels
		boolean descriptorFound = false;
		Set<ALDParameterDescriptor> keys;
		HashMap<ALDParameterDescriptor, ALDSwingComponent> newCompMap = 
			new HashMap<ALDParameterDescriptor, ALDSwingComponent>();
		HashMap<ALDParameterDescriptor, ALDSwingComponent> newCompMapLinkBak = 
			new HashMap<ALDParameterDescriptor, ALDSwingComponent>();
		HashMap<ALDParameterDescriptor, JLabel> newLabelMap = 
			new HashMap<ALDParameterDescriptor, JLabel>();
		HashMap<ALDParameterDescriptor, JPanel> newPanelMap = 
			new HashMap<ALDParameterDescriptor, JPanel>();
		for (ALDParameterDescriptor nDescr: this.paramDescriptors) {
			descriptorFound = false;
			keys = this.compMap.keySet();
			for (ALDParameterDescriptor descr : keys) {
				String name = descr.getName();
				if (nDescr.getName().equals(name)) {
					descriptorFound = true;
					newCompMap.put(nDescr, this.compMap.get(descr));
					newCompMapLinkBak.put(nDescr, this.compMapLinkBak.get(descr));
					newLabelMap.put(nDescr, this.labelMap.get(descr));
					newPanelMap.put(nDescr, this.panelMap.get(descr));
					break;
				}
			}
			// if we could not find a descriptor in the old map, parameter is new 
			if (!descriptorFound) {
				newCompMap.put(nDescr, null);
				newLabelMap.put(nDescr, null);
				newCompMapLinkBak.put(nDescr, null);
				newPanelMap.put(nDescr, null);
			}
		}
		// set new maps
		this.compMap = newCompMap;
		this.compMapLinkBak = newCompMapLinkBak;
		this.labelMap = newLabelMap;
		this.panelMap = newPanelMap;
		
		// update the GUI, i.e., add graphical elements for new parameters and 
		// remove elements from parameters which no longer exist
		this.updateParameterGUIElements();
	}

	/**
	 * Get value of given parameter from the GUI element.
	 * @param descr		Descriptor of desired parameter.
	 * @return Value of parameter, might be null.
	 * @throws ALDDataIOException
	 */
	public Object readParameter(ALDParameterDescriptor descr)
			throws ALDDataIOProviderException {

		if (logger.isDebugEnabled())
			logger.debug("Reading parameter {}...", descr.getName());

		// get the value out of the parameter field
		if (!this.compMap.containsKey(descr)) {
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
					"[ALDParameterPanel::readParameter()] Descriptor not found!");
		}
		try {
			Object obj = null;
			// TODO return an ALDLinkException if value is null or runOpNode
			// failed
			if (this.isParamLinked.get(descr).booleanValue()) {
//				String link = (String) ALDDataIOManagerSwing.getInstance()
//						.readData(descr.getField(), String.class,
//								this.componentMap.get(descr));
//				if (link == null || link.isEmpty() || !link.contains("@"))
//					return null;
////				if (this.editorFrame.getValue(link) == null
////						&& descr.isReverse())
////					if (!this.editorFrame.runOpNode(link.split("@")[0], true))
////						return null;
//				obj = this.editorFrame.getValue(link);
			} else {
				obj = ALDDataIOManagerSwing.getInstance().readData(
						descr.getField(), descr.getMyclass(),
						this.compMap.get(descr));
			}
			if (logger.isDebugEnabled())
				logger.debug("---> value for {} is {}", descr.getName(), obj);
			return obj;
		} catch (ALDDataIOException ex) {
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
					"[ALDParameterPanel::readParameter()] "
							+ "Reading parameter value failed!\n" + "--> "
							+ ex.getCommentString());
			// if (descr.isRequired()) {
			// Object[] options = { "OK" };
			// JOptionPane.showOptionDialog(
			// null,
			// "Reading parameter \"" + descr.getLabel()
			// + "\" failed!" + "Reason:\n"
			// + ex.getCommentString(), "Warning",
			// JOptionPane.DEFAULT_OPTION,
			// JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			// return null;
			// }
		}
	}

	/**
	 * Set value of given parameter, i.e. pass the value to GUI component.
	 * @param descr		Descriptor of parameter to be updated.
	 * @param value		New value of parameter to be displayed.
	 * @throws ALDDataIOException
	 */
	public void setParameter(ALDParameterDescriptor descr, Object value)
			throws ALDDataIOProviderException {

		// check if parameter is registered
		if (!this.compMap.containsKey(descr)) {
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
					"[ALDParameterPanel::setParameter()] Descriptor " 
												+ descr.getName() + " not found!");
		}
		// ignore linked parameters (may happen in Grappa and batch mode)
		if (this.compMap.get(descr) instanceof ALDGrappaLinkDataIOSwing)
		    return;
		try {
			// ask the GUI component for updating its value
			if (value != null)
				ALDDataIOManagerSwing.getInstance().setValue(descr.getField(),
					value.getClass(), this.compMap.get(descr), value);
			else
				ALDDataIOManagerSwing.getInstance().setValue(descr.getField(),
					descr.getMyclass(), this.compMap.get(descr), value);
			this.mainPanel.updateUI();
		} catch (ALDDataIOException ex) {
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
					"[ALDParameterPanel::setParameter()] Setting parameter value failed!\n"
							+ "--> " + ex.getCommentString());
		}
	}

	public synchronized boolean setParamExternallyConfigured(
																							ALDParameterDescriptor desc, 
																							String val) {
		
		// do nothing, if parameter unknown
		if (!this.compMap.containsKey(desc))
			return false; 
		
		// get current GUI component
		ALDSwingComponent currentComponent = this.compMap.get(desc);
		boolean isVisible = currentComponent.getJComponent().isVisible();
		// store in backup list
		this.compMapLinkBak.put(desc, currentComponent);
		
		// remove old GUI elements
		this.panelMap.get(desc).remove(currentComponent.getJComponent());
		this.compMap.remove(desc);

		// generate GUI elements for link
		ALDSwingComponent elem = new ALDGrappaLinkDataIOSwing(val);
		JComponent guiElement = elem.getJComponent();
		this.labelMap.get(desc).setForeground(java.awt.Color.BLUE);
		this.labelMap.get(desc).updateUI();

		// update GUI
		if (guiElement != null) {
			this.compMap.put(desc, elem);
			this.panelMap.get(desc).add(guiElement);
			if (isVisible)
				guiElement.setVisible(true);
			else
				guiElement.setVisible(false);
			this.panelMap.get(desc).updateUI();
		}
		// remember that parameter is linked now
		this.isParamLinked.put(desc, new Boolean(true));
		return true;
	}
	
	public synchronized boolean setParamNotExternallyConfigured(
																								ALDParameterDescriptor desc) {
		
		// do nothing, if parameter unknown
		if (!this.compMap.containsKey(desc))
			return false; 
		
		// remove GUI elements for link
		this.panelMap.get(desc).remove(this.compMap.get(desc).getJComponent());
		this.compMap.remove(desc);

		// insert old ones again
		ALDSwingComponent elem = this.compMapLinkBak.get(desc);
		
		// if no old element available, generate new one (... should not happen!)
		if (elem == null) {
			try {
	      elem = ALDDataIOManagerSwing.getInstance().createGUIElement(
	      	desc.getField(), desc.getMyclass(), 
      			this.panelParent.getParameterValue(
							desc.isRequired(), false, desc), desc);
	      elem.addValueChangeEventListener(this);
      } catch (ALDDataIOException e) {
      	System.out.println("We should never end up here!!!");
	      e.printStackTrace();
	      try {
	        System.in.read();
        } catch (IOException e1) {
	        e1.printStackTrace();
        }
      }
		}
		@SuppressWarnings("null")
    JComponent guiElement = elem.getJComponent();
		this.labelMap.get(desc).setForeground(java.awt.Color.BLACK);
		this.labelMap.get(desc).updateUI();
		if (guiElement != null) {
			this.compMap.put(desc, elem);
			this.panelMap.get(desc).add(guiElement);
			if (this.panelMap.get(desc).isVisible())
				guiElement.setVisible(true);
			else
				guiElement.setVisible(false);	
			this.panelMap.get(desc).updateUI();
		}
		this.isParamLinked.put(desc, new Boolean(false));
		return true;
	}

	/**
	 * Sets label color of parameter to red (e.g. if validation failed).
	 * @param descr		Descriptor of parameter to mark.
	 */
	public void markParameter(ALDParameterDescriptor descr) {
		// get the value out of the parameter field
		if (!this.compMap.containsKey(descr))
			return; // do nothing, if parameter unknown
		if (!this.labelMap.containsKey(descr)) {
			return;
		}
		// do not mark linked parameters
		if (this.isParamLinked.get(descr).booleanValue())
			return;
		// mark parameter
		if (descr.isRequired()) {
			if (this.topLevelCall)
				this.labelMap.get(descr).setForeground(java.awt.Color.RED);
			else
				this.labelMap.get(descr).setForeground(java.awt.Color.ORANGE);			
		}
		else {
			this.labelMap.get(descr).setForeground(java.awt.Color.GRAY);
		}
	}

	public void markParameterProbablyConf(ALDParameterDescriptor descr) {
		// get the value out of the parameter field
		if (!this.compMap.containsKey(descr))
			return; // do nothing, if parameter unknown
		// do not mark linked parameters
		if (this.isParamLinked.get(descr).booleanValue())
			return;
		// mark parameter
		this.labelMap.get(descr).setForeground(java.awt.Color.ORANGE);			
	}

	/**
	 * Sets label color of parameter to black or of link to blue (e.g. if
	 * validation successful).
	 * 
	 * @param descr		Descriptor of parameter to unmark.
	 */
	public void unmarkParameter(ALDParameterDescriptor descr) {
		// get the value out of the parameter field
		if (!this.compMap.containsKey(descr))
			return; // do nothing, if parameter unknown
		if (!(this.isParamLinked.get(descr).booleanValue()))
			this.labelMap.get(descr).setForeground(java.awt.Color.BLACK);
		else
			this.labelMap.get(descr).setForeground(java.awt.Color.BLUE);
		this.labelMap.get(descr).updateUI();
	}

	/**
	 * Sets label colors of all parameter to black and of all links to blue.
	 */
	public void unmarkAllParameters() {
		Set<ALDParameterDescriptor> keys = this.labelMap.keySet();
		for (ALDParameterDescriptor descr : keys) {
			if (!(this.isParamLinked.get(descr).booleanValue()))
				this.labelMap.get(descr).setForeground(java.awt.Color.BLACK);
			else
				this.labelMap.get(descr).setForeground(java.awt.Color.BLUE);
		}
	}

	/**
	 * Check if all GUI elements can deliver a proper (non-null) value.
	 * <p>
	 * Parameters delivering null as value are marked red or orange. Red color
	 * is used for panels used in top-level context, orange otherwise.
	 * 
	 * @return Null, if successful, otherwise list of failing parameters.
	 */
	public HashMap<ALDParameterDescriptor, String> validateParameters() {

		// first reset all labels to black or blue (from probable previous
		// validations)
		for (Map.Entry<ALDParameterDescriptor, JLabel> entry : this.labelMap
				.entrySet()) {
			JLabel label = entry.getValue();
			if (!(this.isParamLinked.get(entry.getKey()).booleanValue()))
				label.setForeground(java.awt.Color.BLACK);
			else
				label.setForeground(java.awt.Color.BLUE);
		}

		HashMap<ALDParameterDescriptor, String> failedList = new HashMap<ALDParameterDescriptor, String>();
		Object readValue = null;
		for (Map.Entry<ALDParameterDescriptor, ALDSwingComponent> entry : this.compMap
				.entrySet()) {
			readValue = null;
			ALDParameterDescriptor descr = entry.getKey();
			ALDSwingComponent guiElement = entry.getValue();
			if (descr.isRequired()) {
				// skip output parameters of operators
				if (descr instanceof ALDOpParameterDescriptor) {
					if (((ALDOpParameterDescriptor) descr).getDirection() == Parameter.Direction.OUT)
						continue;
				}
				try {
					// TODO
					if (!(this.isParamLinked.get(descr).booleanValue())) {
//						String linkSource = (String) ALDDataIOManagerSwing
//								.getInstance().readData(descr.getField(),
//										String.class, guiElement);
//						if (linkSource != null && !linkSource.isEmpty()
//								&& linkSource.contains("@")) {
//							if (!this.editorFrame.validateLink(linkSource, link
//									+ "@" + descr.getName())) {
//								failedList.put(descr, "Wrong link received!");
//								this.labelMap.get(descr).setForeground(
//										java.awt.Color.RED);
//							}
//						}
					}
					else {
						// readValue = ALDDataIOManagerSwing.getInstance()
						// .readData(descr.getField(), descr.getMyclass(),
						// guiElement);
						// if (readValue == null && !this.nullReturnsForbidden)
						// this.labelMap.get(descr).setForeground(
						// java.awt.Color.ORANGE);
						// else if (readValue == null)
						// this.labelMap.get(descr).setForeground(
						// java.awt.Color.RED);
						readValue = ALDDataIOManagerSwing.getInstance()
								.readData(descr.getField(), descr.getMyclass(),
										guiElement);
						if (readValue == null && !this.topLevelCall)
							this.labelMap.get(descr).setForeground(
									java.awt.Color.ORANGE);
						else if (readValue == null) {
							failedList.put(descr, "Null value received!");
							this.labelMap.get(descr).setForeground(
									java.awt.Color.RED);
						}
					}
				} catch (ALDDataIOException e) {
					failedList.put(descr, e.getCommentString());
					this.labelMap.get(descr).setForeground(java.awt.Color.RED);
				}
			}
		}
		if (failedList.size() > 0)
			return failedList;
		return null;
	}

	// TODO comments
	public void copyConfiguration(ALDParameterPanel parameterPanel)
			throws ALDDataIOException {
		for (ALDParameterDescriptor descr : this.compMap.keySet()) {
			if (!(this.isParamLinked.get(descr).booleanValue())) {
				Object value = null;
				value = ALDDataIOManagerSwing.getInstance().readData(
						descr.getField(), descr.getMyclass(),
						this.compMap.get(descr));

				for (ALDParameterDescriptor descr2 : parameterPanel.compMap
						.keySet())
					if (descr2.getName().equals(descr.getName()))
						ALDDataIOManagerSwing.getInstance().setValue(
								descr2.getField(), descr2.getMyclass(),
								parameterPanel.compMap.get(descr2), value);
			}
		}
	}
	
	@Override
	public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
		this.fireALDSwingValueChangeEvent(event);
	}
}
