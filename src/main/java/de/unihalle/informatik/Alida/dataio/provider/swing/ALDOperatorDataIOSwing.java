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

package de.unihalle.informatik.Alida.dataio.provider.swing;

import javax.swing.*;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import de.unihalle.informatik.Alida.operator.*;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDDataIOProvider;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.Parameter.ExpertMode;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDOperatorParameterPanel;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponentComboBox;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponentComboBoxItem;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponentLabel;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeEvent;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeListener;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.ALDSwingValueChangeReporter;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida.exceptions.ALDException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.gui.ALDOperatorControlFrame;
import de.unihalle.informatik.Alida.gui.OnlineHelpDisplayer;
import de.unihalle.informatik.Alida.helpers.ALDClassInfo;

/**	
 * Data IO provider for operators in GUI.
 * <p>
 * This class provides GUI elements and functionality for configuring 
 * operators in GUI. Note that the class has to operation modes:
 * <ul>
 * <li> invocation from data IO manager:<br>
 * 	in this case a complete frame including buttons is returned
 * <li> call from {@link ALDOperatorControlFrame}:<br>
 * 	in this case just a panel is generated and returned, but no control buttons
 * </ul>
 * While in the first case the default constructor without arguments is used,
 * in the second case the constructor with boolean flag is used, followed by
 * a call of the `getConfigPanel()` method.
 * <p>
 * If you do any changes to this class, please make sure that the panel is 
 * always properly built, independent of how the class object in constructed! 
 * 
 * @author Stefan Posch
 * @author Birgit Moeller
 */
@ALDDataIOProvider
public class ALDOperatorDataIOSwing implements ALDDataIOSwing {

	/**
	 * Local debug flag, not accessible from outside of the class.
	 */
	@SuppressWarnings("unused")
  private boolean debug = false;
	
	/**
	 * Local flag to indicate if object is created on OpRunner top level or not;
	 * flag modules if deep validation is done or not.
	 */
	protected boolean topLevelCall = false;

	/**
	 * Panel to configure sub-classed or abstract operators as parameters. 
	 */
	private OperatorHierarchyConfigPanel subClassHandler = null;
	
	/** 
	 * The operator of this frame, may be abstract!
	 */
	protected ALDOperator op = null;
	
	/**
	 * List of all parameter descriptors associated with operator.
	 */
	protected LinkedList<ALDOpParameterDescriptor> opParamDescrips;
	
	/**
	 * Configure button for operator as parameter.
	 */
	OperatorConfigPanel confPanel;
	
	/**
	 * Default constructor.
	 */
	public ALDOperatorDataIOSwing() {
		// nothing to do here
	}
	
	@Override
  public Object getInitialGUIValue(Field field, Class<?> cl, Object obj, 
  		ALDParameterDescriptor descr) throws ALDDataIOProviderException {		
		
		// check if type of provided object is consistent with provider type
		if (obj != null && !(obj instanceof ALDOperator)) {
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR,
					"[" + this.getClass().getName() + "]" 
						+ ": got invalid object type in getInitialGUIValue()!");
		}

		// figure out which class to use for initial value
		Class<?> valueClass = cl;
		ALDOperator givenOp;
		
		// if object is null we have to initialize a new object, however,  
		// that only works if requested class is not abstract
		if (obj == null) {
			if (Modifier.isAbstract(cl.getModifiers()))
				return null;
			valueClass = cl;
			givenOp = null;
		}
		else {
			valueClass = obj.getClass();
			givenOp = (ALDOperator)obj;
		}

		// initialize initial object
		ALDOperator initialOp;
    try {
    	initialOp = (ALDOperator)valueClass.newInstance();
    	// if no operator was given, use the one created
    	if (givenOp == null)
    		givenOp = initialOp;
    } catch (Exception e) {
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
					"[" + this.getClass().getName() + "]" 
							+ ": could not instantiate initial operator "
							+ "of class <" + cl + ">...");
    }
    
		// check if operator is released for GUI usage
		if (!this.guiUsageAllowed(initialOp.getClass()))
			return null;

		// iterate over all parameters and request default values
		Collection<String> params = initialOp.getInInoutNames();
		// copy the collection, because iterating over a collection
		// which could be meanwhile modified is a very bad idea!
		String[] paramArray = new String[params.size()];
		int i=0;
		for (String s: params) {
			paramArray[i] = s;
			++i;
		}

		ALDParameterDescriptor paramDescr = null; 
		Class<?> paramClass = null;
		for (i=0; i<paramArray.length; ++i) {
			String pname = paramArray[i];
			// if the parameter does not exist anymore (e.g., due to callback
			// changes), skip it
			if (!initialOp.hasParameter(pname))
				continue;
			try {
				paramDescr = initialOp.getParameterDescriptor(pname);
				paramClass = paramDescr.getMyclass();
				Object initialValue = 
						ALDDataIOManagerSwing.getInstance().getInitialGUIValue(
								paramDescr.getField(), paramClass, 
								givenOp.getParameter(pname), paramDescr);
				initialOp.setParameter(pname, initialValue);
			} catch (Exception e) {
				e.printStackTrace();
				throw new ALDDataIOProviderException(
						ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
						"[" + this.getClass().getName() + "]" 
								+ ": could not get initial GUI value for "
								+ "parameter <" + pname + "> of class <" + paramClass
								+ ">!");
			}
		}
		return initialOp;
	}
	
	@Override
  public ALDSwingComponent createGUIElement(
  		Field field, Class<?> cl, Object obj, ALDParameterDescriptor descr) {
		return this.createGUIElement(field, cl, obj, descr, true);
	}
	
  /**
   * Method to actually create the GUI element for the parameter requested.
   * <p>
   * Most important here is the boolean to enable or disable checks of 
   * derived classes. Particularly nested usage of operators or, e.g., using 
   * operators as elements of collections requires to handle the existence
   * of derived classes differently.  
   * 
   * @param field									Field of corresponding parameter.
   * @param cl										Class of corresponding parameter.
   * @param obj										Initial parameter value.
   * @param descr									Corresponding parameter descriptor.
   * @param checkDerivedClasses		If true, derived classes are checked.
   * @return GUI element for configuration of the corresponding parameter.
   */
  public ALDSwingComponent createGUIElement(
  		Field field, Class<?> cl, Object obj, ALDParameterDescriptor descr,
  		boolean checkDerivedClasses) {
		this.topLevelCall = false;
		try {
			if (obj != null) {
				// we have a default object given...
				if (cl.equals(obj.getClass())) {
					// same class like parameter, just use given object
					this.op = (ALDOperator)obj;
				}
				else {
					// class is different, should be a subclass
					this.op = (ALDOperator)obj;
		    	// instantiating operator failed, maybe an abstract class...
		    	this.subClassHandler = new OperatorHierarchyConfigPanel();
		    	return this.subClassHandler.createGUIElement(field, cl, obj,descr);
				}
			}
			else {
				this.op = null;
				// try to instantiate object of given class to figure out if 
				// operator class is abstract or not
				cl.newInstance();
			}
    } catch (Exception e) {
    	// instantiating operator failed, maybe an abstract class...
    	this.subClassHandler = new OperatorHierarchyConfigPanel();
    	return this.subClassHandler.createGUIElement(field, cl, obj, descr);
    }
    // check if the operator has derived classes (if requested)
		if (checkDerivedClasses) {
			@SuppressWarnings("rawtypes")
			Collection<Class> extClasses = ALDClassInfo.lookupExtendingClasses(cl);
			if (extClasses.size() > 1) { // class itself is also found...
				this.subClassHandler = new OperatorHierarchyConfigPanel();
				return this.subClassHandler.createGUIElement(field, cl, obj, descr);
			}
		}
		// check if operator class is released for GUI usage
		if (!this.guiUsageAllowed(cl))
			return new ALDSwingComponentLabel(
				"     Parameter data type is not approved for GUI usage!     ");
    // return configuration button for this class
	  this.confPanel = new OperatorConfigPanel(cl, descr);
	  return this.confPanel;
  }

	@Override
	public void setValue(
			Field field, Class<?> cl, ALDSwingComponent guiElement,	Object value) 
	throws ALDDataIOProviderException {
		if (value != null && !(value instanceof ALDOperator)) {
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR,
					this.getClass().getName()+": got invalid object type in setValue()");
		}
		if (guiElement instanceof OperatorConfigPanel) {
			if (value == null)
				return;
			OperatorConfigPanel confB = (OperatorConfigPanel)guiElement;
			this.op = (ALDOperator)value;
			HashMap<ALDOpParameterDescriptor, String> failedParams =
					confB.updateOperator(this.op);
			if (failedParams != null) {
				StringBuffer msg = new StringBuffer();
				Set<ALDOpParameterDescriptor> keys = failedParams.keySet();
				for (ALDOpParameterDescriptor descr: keys) {
					msg.append("Setting parameter "+descr.getLabel()+" failed!\n");
					msg.append("--> " + failedParams.get(descr));
				}
				throw new ALDDataIOProviderException(
						ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
						"Problems setting parameters...\n" + msg);				
			}
		}
		else if (guiElement instanceof 
				OperatorHierarchyConfigPanel.AbstrOpClassPanel) {
			OperatorHierarchyConfigPanel.AbstrOpClassPanel p=
				(OperatorHierarchyConfigPanel.AbstrOpClassPanel)guiElement;
			p.setValue(field, cl, guiElement, value);
			guiElement.getJComponent().updateUI();
		}
	}

	@Override
  public Object readData(
  		Field field, Class<?> cl, ALDSwingComponent guiElement) 
		throws ALDDataIOProviderException {
		if (guiElement instanceof OperatorConfigPanel) {
			OperatorConfigPanel confB = (OperatorConfigPanel)guiElement;
			return confB.readData(field, cl);
		}
		else if (guiElement instanceof 
				OperatorHierarchyConfigPanel.AbstrOpClassPanel) {
			OperatorHierarchyConfigPanel.AbstrOpClassPanel p=
				(OperatorHierarchyConfigPanel.AbstrOpClassPanel)guiElement;
			return p.readData(field, cl);
		}
		return null;
  }

	@Override
  public JComponent writeData(Object obj, ALDParameterDescriptor descr) {
		if (obj instanceof ALDOperator)
			return new OperatorShowButton(obj, descr).getJComponent();
		return null;
  }

	@Override
	public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
		classes.add(ALDOperator.class);
		return classes;
	}

	/**
	 * Internal helper to request GUI-suitability of an operator.
	 * @param c Operator class to check.
	 * @return	If true, usage in GUI is granted.
	 */
	private boolean guiUsageAllowed(Class<?> c) {
		try {
			Annotation anno = c.getAnnotation(ALDAOperator.class);
			Class<? extends Annotation> type = anno.annotationType();
			// request the value of the generic execution mode
			Method m = type.getDeclaredMethod("genericExecutionMode");
			Object value = m.invoke(anno, (Object[])null);
			if (   value == ALDAOperator.ExecutionMode.ALL
					|| value == ALDAOperator.ExecutionMode.SWING) {
				return true;
			}
		}	catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * GUI element for displaying configuration of a single operator.
	 * <p>
	 * This button has an operator display window attached to it. 
	 * 
	 * @author moeller
	 */
	private class OperatorShowButton extends ALDSwingComponent {

		/**
		 * Button to open configuration window.
		 */
		private JButton mainButton;
		
		/**
		 * Operator configuration window.
		 */
		OperatorConfigWindow confWin;

		/**
		 * Constructor.
		 * 
		 * @param obj			Default object.
		 * @param descr		Optional descriptor to provide additional information.
		 */
		public OperatorShowButton(Object obj, ALDParameterDescriptor descr) {
			ALDOperator operator = (ALDOperator)obj;
			this.confWin = 	new OperatorConfigWindow(operator, operator.getClass(), 
				descr, false);
			this.mainButton = new JButton("Show Configuration...");
			this.mainButton.setActionCommand("buttonPressed");
			this.mainButton.addActionListener(this.confWin);
			this.mainButton.setVisible(true);
		}		

		@Override
		public JComponent getJComponent() {
			return this.mainButton;
		}

		@Override
    public void disableComponent() {
			if (this.confWin != null)
				this.confWin.disableComponent();
    }

		@Override
    public void enableComponent() {
			if (this.confWin != null)
				this.confWin.enableComponent();
    }
		
		@Override
    public void dispose() {
			if (this.confWin != null)
				this.confWin.dispose();
    }
	}	

	/**
	 * GUI element for configuring a single operator.
	 * <p>
	 * This button has an operator configuration window attached to it. 
	 * 
	 * @author moeller
	 */
	private class OperatorConfigPanel extends ALDSwingComponent 
		implements ALDSwingValueChangeListener, ActionListener {

		/**
		 * Panel containing buttons.
		 */
		private JPanel configPanel;
		
		/**
		 * Button to open configuration window.
		 */
		private JButton configButton;
		
		/**
		 * Button to reset operator to null.
		 */
		private JButton resetButton;

		/**
		 * Operator configuration window.
		 */
		OperatorConfigWindow confWin;

		/**
		 * Class of operator to configure.
		 */
		private Class<?> operatorClass;
		
		/**
		 * Descriptor of parameter linked to operator. 
		 */
		private ALDParameterDescriptor operatorDescriptor;
		
		/**
		 * Constructor.
		 * 
		 * @param cl			Class of collection.
		 * @param d		Optional descriptor to provide additional information.
		 */
		public OperatorConfigPanel(Class<?> cl, ALDParameterDescriptor d) {
			this.operatorClass = cl;
			this.operatorDescriptor = d;
			this.configPanel = new JPanel();
			this.confWin = 
					new OperatorConfigWindow(ALDOperatorDataIOSwing.this.op, cl, 
							d, true);
			this.confWin.addValueChangeEventListener(this);
			this.configButton = new JButton("Configure Operator...");
			this.configButton.setActionCommand("configButtonPressed");
			this.configButton.addActionListener(this);
			this.configButton.setVisible(true);
			this.resetButton = new JButton("Reset");
			this.resetButton.setActionCommand("resetButtonPressed");
			this.resetButton.addActionListener(this);
			this.resetButton.setVisible(true);
			this.configPanel.add(this.configButton);
			this.configPanel.add(this.resetButton);
		}		
		
		/**
		 * Read operator parameter values.
		 * 
		 * @param field
		 * @param cl
		 * @return
		 * @throws ALDDataIOProviderException
		 */
		protected Object readData(Field field, Class<?> cl) 
			throws ALDDataIOProviderException {
			try {
				if (this.confWin == null)
					return null;
				return this.confWin.setOperatorParameters(field, cl);
			} catch (ALDOperatorException ex) {
				throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.OBJECT_INSTANTIATION_ERROR, 
						ex.getCommentString());
			}
		}

		/**
		 * Update window with new operator parameter values.
		 * @param _op		New operator to be linked to window.
		 * @return	Null in case of success, otherwise list of failed parameters.
		 * @throws ALDDataIOProviderException
		 */
		protected HashMap<ALDOpParameterDescriptor, String> updateOperator(
																															ALDOperator _op) 
			throws ALDDataIOProviderException {
			return this.confWin.updateOperator(_op);
		}

		@Override
		public JComponent getJComponent() {
			return this.configPanel;
		}

		@Override
		public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
			this.fireALDSwingValueChangeEvent(event);
		}

		@Override
    public void disableComponent() {
			if (this.confWin != null)
				this.confWin.disableComponent();
    }

		@Override
    public void enableComponent() {
			if (this.confWin != null)
				this.confWin.enableComponent();
    }
		
		@Override
    public void dispose() {
			if (this.confWin != null)
				this.confWin.dispose();
    }

		@Override
    public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmd.equals("resetButtonPressed")) {
				ALDOperatorDataIOSwing.this.op = null;
				this.confWin.dispose();
				this.confWin = null;
				this.handleValueChangeEvent(
						new ALDSwingValueChangeEvent(this, this.operatorDescriptor));
			}
			else if (cmd.equals("configButtonPressed")) {
				if (this.confWin != null)
					this.confWin.setVisible(true);
				else { 
					// request initial GUI values for operator
					ALDOperator givenOp = ALDOperatorDataIOSwing.this.op;
					ALDOperator initialOp;
          try {
	          initialOp = (ALDOperator)ALDDataIOManagerSwing.
	          		getInstance().getInitialGUIValue(null, 
	          				givenOp.getClass(), givenOp, null);
						// copy parameter settings
						Collection<String> params = initialOp.getParameterNames();
						for (String pname: params) {
							givenOp.setParameter(pname, initialOp.getParameter(pname));
						}
          } catch (ALDException e1) {
	          // TODO Auto-generated catch block
	          e1.printStackTrace();
          }
					this.confWin = 
							new OperatorConfigWindow(ALDOperatorDataIOSwing.this.op, 
									this.operatorClass, this.operatorDescriptor, true);
					this.confWin.addValueChangeEventListener(this);
					this.confWin.setVisible(true);
					this.handleValueChangeEvent(new ALDSwingValueChangeEvent(this, 
							this.operatorDescriptor));
				}
			}
    }
	}	

	/**
	 * Configuration window for operator.
	 * 
	 * @author moeller
	 */
	protected class OperatorConfigWindow extends ALDSwingValueChangeReporter
		implements ActionListener, ALDSwingValueChangeListener {

		/**
		 * Main configuration window.
		 */
		JFrame window;
		
		/**
		 * Fixed width of window.
		 */
		private final int frameWidth = 500;
		
		/**
		 * Main panel of main frame.
		 */
		private JPanel mainPanel = null;
		
		/**
		 * Sub-panel containing operator parameters.
		 */
		private ALDOperatorParameterPanel configPanel = null;

		/**
		 * Corresponding operator to be configured.
		 */
		private ALDOperator operator;
		
		/**
		 * Class of the operator to be configured.
		 */
		private Class<?> operatorClass;
		
		/**
		 * Optional descriptor granting access to additional parameter information.
		 */
		private ALDParameterDescriptor descriptor;
		
		/**
		 * Mode for displaying parameters.
		 */
		protected Parameter.ExpertMode displayMode= Parameter.ExpertMode.STANDARD;

		/**
		 * Flag to enable/disable editing.
		 */
		protected boolean editsAllowed;
		
		/**
		 * Default constructor.
		 * 
		 * @param ops		Corresponding operator to configure.
		 * @param cl 
		 * @param descr 
		 * @param editable 
		 */
		public OperatorConfigWindow(ALDOperator ops, Class<?> cl,
									ALDParameterDescriptor descr, boolean editable) {
			this.operatorClass = cl;
			this.descriptor = descr;
			this.editsAllowed = editable;
			if (ops != null) {
				this.operator = ops;
				// setup window if we have an operator object given
				this.buildWindow();
				this.configPanel.updateConfigurationStatus(
						this.operator.unconfiguredItems());
				// if editable, enable events
				if (this.editsAllowed)
					this.handleValueChangeEvent(new ALDSwingValueChangeEvent(this,descr));
				// if not editable, make sure that all parameters are displayed
				if (!this.editsAllowed)
					this.displayMode = ExpertMode.ADVANCED;
			}
		}
		
    /**
     * Disables the operator configuration panel.
     */
    public void disableComponent() {
    	if (this.configPanel != null)
    		this.configPanel.disableComponents();
    }

    /**
     * Enables the operator configuration panel.
     */
    public void enableComponent() {
    	if (this.configPanel != null)
    		this.configPanel.enableComponents();
    }

    /**
     * Disposes all resources of this window and its sub-windows.
     */
    public void dispose() {
    	if (this.configPanel != null)
    		this.configPanel.dispose();
			if (this.window != null)
				this.window.dispose();
    }
    
    @SuppressWarnings("unused")
    protected ALDOperator setOperatorParameters(Field field, Class<?> cl) 
    	throws ALDOperatorException {
    	if (this.configPanel == null) {
    		return null;
    	}
//			HashMap<ALDOpParameterDescriptor,String> failedParams =
//						this.configPanel.setOperatorParameters();
//			if (failedParams != null && failedParams.size() > 0) {
//				StringBuffer msg = new StringBuffer();
//				Set<ALDOpParameterDescriptor> keys = failedParams.keySet();
//				for (ALDOpParameterDescriptor descr: keys) {
//					msg.append("Setting parameter "+descr.getLabel()+"failed!\n");
//					msg.append("--> " + failedParams.get(descr) + "\n");
//				}
//				throw 
//					new ALDOperatorException(OperatorExceptionType.OPERATE_FAILED, 
//																		msg.toString());
//			}
			return this.operator;
		}
    
    /**
     * Update operator linked to window.
     * @param oper	New operator to be linked to the window.
     * @return Null in case of success, otherwise list of failed parameters.
     * @throws ALDDataIOProviderException 
     */
    protected HashMap<ALDOpParameterDescriptor, String> updateOperator(
    		ALDOperator oper) throws ALDDataIOProviderException {
    	this.operator = oper;
    	// if window was never opened before, it needs to be initialized first
			if (this.window == null) {
				// setup window
				this.buildWindow();
			}
    	HashMap<ALDOpParameterDescriptor, String> failedParams = 
					this.configPanel.setNewOperator(this.operator);
//			this.configPanel.updateConfigurationStatus(
//					this.operator.unconfiguredItems());
//			this.handleValueChangeEvent(new ALDSwingValueChangeEvent(this));
    	return failedParams;
    }
    
    /**
     * Allows to check if window is linked to operator object or not.
     * @return	True if operator object is present.
     */
    protected boolean isOperatorNull() {
    	if (this.operator == null)
    		return true;
    	return false;
    }
		
    /**
     * Initializes a new operator object with default configuration.
     */
    protected void initOperator() {
			try {
				this.operator = (ALDOperator)this.operatorClass.newInstance();
				// add the corresponding window
				if (this.window == null) {
					// setup window
					this.buildWindow();
					this.configPanel.updateConfigurationStatus(
																					this.operator.unconfiguredItems());
				}
			} catch (InstantiationException inex) {
				// TODO Auto-generated catch block
				inex.printStackTrace();
			} catch (IllegalAccessException ilex) {
				// TODO Auto-generated catch block
				ilex.printStackTrace();
			}
    }
    
		/**
		 * Generates the window.
		 */
		private void buildWindow() {
			this.window = new JFrame();
			String paramName = "unknown";
			if (this.descriptor != null)
				paramName = this.descriptor.getLabel();
			this.window.setTitle(
					"Configure parameter <" + paramName + ">, type: " 
																		+ this.operatorClass.getSimpleName());
	//		this.window.setSize(this.frameWidth,300);
			
			this.mainPanel = new JPanel();
			this.window.add(this.mainPanel);
			
			BoxLayout ylayout = new BoxLayout( this.mainPanel, BoxLayout.Y_AXIS );
	    this.mainPanel.setLayout( ylayout);

	    ALDSwingValueChangeListener listener = null;
	    if (this.editsAllowed)
	    	listener = this;
	    this.configPanel = new ALDOperatorParameterPanel(this.operator,
 																					this.displayMode, false, listener);
	    // panel should be non-editable
	    if (!this.editsAllowed)
	    	this.configPanel.disableComponents();
	    JScrollPane scroller = new JScrollPane(this.configPanel.getJPanel());  
//	    this.window.getContentPane().add(scroller, BorderLayout.CENTER);
			this.mainPanel.add(scroller);

			// add buttons
			JPanel buttonPanel = new JPanel(new GridLayout(1,2));
			JPanel valPanel = new JPanel();
			valPanel.setLayout(new BoxLayout(valPanel, BoxLayout.LINE_AXIS));
			valPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
			JButton validateButton = new JButton("Validate");
			validateButton.setActionCommand( "validate");
			validateButton.addActionListener( this);
			validateButton.setBounds(50, 60, 80, 30);
			valPanel.add(validateButton);
			
			JPanel closePanel = new JPanel();
			closePanel.setLayout(new BoxLayout(closePanel, BoxLayout.LINE_AXIS));
			closePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
			JButton closeButton = new JButton("Close");
			closeButton.setActionCommand("close");
			closeButton.addActionListener(this);
			closePanel.add(closeButton);
//			buttonPanel.add(validateButton);
//			buttonPanel.add(closeButton);
			buttonPanel.add(valPanel);
			buttonPanel.add(closePanel);
			this.mainPanel.add(buttonPanel);
			
			// wrap up with a scrolling panel
//	    JScrollPane scrollPane = new JScrollPane(this.mainPanel);
//	    this.window.add(scrollPane);
			
	    // add a menu bar
			JMenuBar mainWindowMenu = new JMenuBar();
			JMenu fileM = new JMenu("File");
			JMenuItem itemSave = new JMenuItem("Save Settings");
			itemSave.setActionCommand("fileM_save");
			itemSave.addActionListener(this);
			JMenuItem itemLoad = new JMenuItem("Load Settings");
			itemLoad.setActionCommand("fileM_load");
			itemLoad.addActionListener(this);
			fileM.add(itemSave);
			fileM.add(itemLoad);
			
			JMenu actionsM = new JMenu("Actions");
			JMenuItem itemValidate = new JMenuItem("Validate");
			itemValidate.setActionCommand("actionsM_validate");
			itemValidate.addActionListener(this);
			actionsM.add(itemValidate);
			
			JMenu viewM = new JMenu("View");
			ButtonGroup operatorLevelGroup = new ButtonGroup();
			// default
			JRadioButtonMenuItem radioItemStandard = 
				new JRadioButtonMenuItem("Standard");
			if (this.displayMode.equals(Parameter.ExpertMode.STANDARD))
				radioItemStandard.setSelected(true);
			radioItemStandard.setActionCommand("viewM_standard");
			radioItemStandard.addActionListener(this);
			JRadioButtonMenuItem radioItemAdvanced =
				new JRadioButtonMenuItem("Advanced");
			if (this.displayMode.equals(Parameter.ExpertMode.ADVANCED))
				radioItemAdvanced.setSelected(true);
			radioItemAdvanced.setActionCommand("viewM_advanced");
			radioItemAdvanced.addActionListener(this);
			operatorLevelGroup.add(radioItemStandard);
			operatorLevelGroup.add(radioItemAdvanced);
			viewM.add(radioItemStandard);
			viewM.add(radioItemAdvanced);

			// generate help menu
			JMenu helpM = new JMenu("Help");
			JMenuItem itemHelp = new JMenuItem("Online Help");
			itemHelp.addActionListener(OnlineHelpDisplayer.getHelpActionListener(
					itemHelp,this.operator.getClass().getName(),this.window));
			JMenuItem itemAbout = new JMenuItem("About Alida");
			itemAbout.setActionCommand("helpM_about");
			itemAbout.addActionListener(this);
			helpM.add(itemHelp);
			helpM.add(itemAbout);

			mainWindowMenu.add(fileM);
			mainWindowMenu.add(actionsM);
			mainWindowMenu.add(viewM);
			mainWindowMenu.add(Box.createHorizontalGlue());
			mainWindowMenu.add(helpM);

			this.window.setJMenuBar(mainWindowMenu);
			this.window.pack();
			// resize window 
			this.window.setSize(this.frameWidth, 400);
		}

		/**
		 * Displays the window and inits an operator object (if not done before).
		 * @param visible
		 */
		public void setVisible(boolean visible) {
			// init an operator object if not done so far
			if (this.operator == null) {
				try {
					this.operator = (ALDOperator)this.operatorClass.newInstance();
				} catch (InstantiationException inex) {
					// TODO Auto-generated catch block
					inex.printStackTrace();
				} catch (IllegalAccessException ilex) {
					// TODO Auto-generated catch block
					ilex.printStackTrace();
				}
			}
			if (visible) {
				if (this.window == null) {
					// setup window
					this.buildWindow();
					this.configPanel.updateConfigurationStatus(
																					this.operator.unconfiguredItems());
					this.handleValueChangeEvent(
						new ALDSwingValueChangeEvent(this, this.descriptor));
				}
				this.window.setVisible(true);
			}
			else {
				if (this.window != null)
					this.window.setVisible(false);
			}
		}
		
		@Override
    public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand(); 
			if (cmd.equals("close")) {
				this.window.setVisible(false);
			}
			else if (cmd.equals("validate") || cmd.equals("actionsM_validate")) {
				try {
					this.configPanel.validateOperatorParameters(true);
				} catch (ALDOperatorException ex) {
					Object[] options = { "OK" };
					JOptionPane.showOptionDialog(null, 
							"Call of operator's validate() method failed! Reason:\n" + 
									ex.getCommentString(), "Warning", 
							JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
							null, options, options[0]);
				}
			}
			else if (cmd.equals("fileM_save")) {
				Object[] options = { "OK" };
				JOptionPane.showOptionDialog(null, 
						"Function will be provided soon...\n", "Warning",
						JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
						null, options, options[0]);
			}
			else if (cmd.equals("fileM_load")) {
				Object[] options = { "OK" };
				JOptionPane.showOptionDialog(null, 
						"Function will be provided soon...\n", "Warning",
						JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
						null, options, options[0]);
			}
			else if (cmd.equals("viewM_standard")) {
				this.configPanel.changeViewMode(Parameter.ExpertMode.STANDARD);
				this.window.repaint();
			}
			else if (cmd.equals("viewM_advanced")) {
				this.configPanel.changeViewMode(Parameter.ExpertMode.ADVANCED);
				this.window.repaint();
			}
			else if (cmd.equals("helpM_about")) {
				Object[] options = { "OK" };
				JOptionPane.showOptionDialog(null, 
						"Alida / MiToBo Graphical Operator Runner - \n" +
						"Chosen Operator: " + this.operator.getName() + "\n" +
						" @2012 Martin Luther University Halle-Wittenberg", 
						"About Alida / MiToBo",
						JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
						null, options, options[0]);
			}
	  }		
		
		@Override
		public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
			this.fireALDSwingValueChangeEvent(event);
		}
	}
	
	/**	
	 * Element for handling operator inheritance hierarchies in configuration.
	 * 
	 * @author Birgit Moeller
	 */
	private class OperatorHierarchyConfigPanel extends ALDSwingComponent 
		implements ALDSwingValueChangeListener {

		/**
		 * GUI configuration element.
		 */
		private AbstrOpClassPanel paramPanel;

		/**
		 * Default constructor.
		 */
		public OperatorHierarchyConfigPanel() {
			// nothing to do here
		}

		/**
		 * Generates the GUI element.
		 * @param field		Field for associated parameter/operator.
		 * @param cl			Class of associated operator.
		 * @param obj			Default object.
		 * @param descr		Optional descriptor for additional information.
		 * @return	Generated GUI component for operator object.
		 */
		public ALDSwingComponent createGUIElement(
				Field field, Class<?> cl, Object obj, ALDParameterDescriptor descr) {
			this.paramPanel = new AbstrOpClassPanel(field, cl, obj, descr);
			this.paramPanel.addValueChangeEventListener(this);
			return this.paramPanel;
		}

		@SuppressWarnings("unused")
    public Object readData(
				Field field, Class<?> cl, ALDSwingComponent guiElement) 
		throws ALDDataIOProviderException {
			if (!(guiElement instanceof AbstrOpClassPanel))
				throw new ALDDataIOProviderException(
						ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
						"OperatorDataIO: readData received invalid GUI element!");
			AbstrOpClassPanel paramGUIPanel = (AbstrOpClassPanel)guiElement;
			return paramGUIPanel.readData(field, cl);
		}

		@SuppressWarnings("unused")
    public JComponent writeData(Object obj) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public JComponent getJComponent() {
			return this.paramPanel.getJComponent();
		}

		@Override
		public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
			this.fireALDSwingValueChangeEvent(event);
		}

		@Override
    public void disableComponent() {
			this.paramPanel.disableComponent();
    }

		@Override
    public void enableComponent() {
			this.paramPanel.enableComponent();
    }

		@Override
    public void dispose() {
			this.paramPanel.dispose();
    }

		/**
		 * GUI element class for parametrized class parameters.
		 * 
		 * @author moeller
		 */
		private class AbstrOpClassPanel extends ALDSwingComponent 
			implements ActionListener, ItemListener, ALDSwingValueChangeListener {

			/**
			 * Main panel.
			 */
			private JPanel mainPanel;
			
			/**
			 * Combobox containing available classes to select.
			 */
			private ALDSwingComponentComboBox classSelection;

			/**
			 * Button to open configuration window.
			 */
			private JButton configButton;
			
			/**
			 * List of available classes.
			 */
			@SuppressWarnings("rawtypes")
			private Collection<Class> availableClasses;

			/**
			 * Hashmap to remember short class names.
			 */
			@SuppressWarnings("rawtypes")
			private HashMap<String, Class> shortNames = new HashMap<String, Class>();

			/**
			 * Hashmap to store configuration windows for classes.
			 */
			@SuppressWarnings("rawtypes")
			private HashMap<Class, OperatorConfigPanel> configWins = 
				new HashMap<Class, OperatorConfigPanel>();

			/**
			 * Descriptor of the associated (operator) parameter.
			 */
			protected ALDParameterDescriptor paramDescriptor;
			
			/**
			 * Default constructor.
			 * @param field		Field of associated object.
			 * @param cl			Class of associated object.
			 * @param obj			Current value of object, maybe null.
			 * @param descr		Optional descriptor for additional information.
			 */
			public AbstrOpClassPanel(Field field, Class<?> cl, Object obj,
																	ALDParameterDescriptor descr) {
				this.paramDescriptor = descr;
				this.mainPanel = new JPanel();
				this.mainPanel.setLayout(new FlowLayout());

				// combo box for possible classes
				this.availableClasses = ALDClassInfo.lookupExtendingClasses(cl);
				@SuppressWarnings("rawtypes")
				LinkedList<Class> filteredClasses =	new LinkedList<Class>();
 				// check if all these classes allow for GUI usage, if not, skip
				for (Class<?> c: this.availableClasses) {
					// skip abstract classes
					if (Modifier.isAbstract(c.getModifiers()))
						continue;
					try {
						// get the ALDAOperator annotation and its type
						Annotation anno = c.getAnnotation(ALDAOperator.class);
						Class<? extends Annotation> type = anno.annotationType();
						// request the value of the generic execution mode
						Method m = type.getDeclaredMethod("genericExecutionMode");
						Object value = m.invoke(anno, (Object[])null);
						if (   value == ALDAOperator.ExecutionMode.ALL
								|| value == ALDAOperator.ExecutionMode.SWING)
							filteredClasses.add(c);
					}	catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
				
				// make sure that class itself is also included if not abstract
				if (   !Modifier.isAbstract(cl.getModifiers())
						&& !filteredClasses.contains(cl))
					filteredClasses.add(cl);
				
				// replace old set with new filtered set of classes 
				this.availableClasses = filteredClasses;
				Vector<ALDSwingComponentComboBoxItem> comboFields = 
						new Vector<ALDSwingComponentComboBoxItem>();
				for (Class<?> c : this.availableClasses) {
					String shortName = c.getSimpleName();
					// add item object to combobox fields
					comboFields.add(new ALDSwingComponentComboBoxItem(c, shortName,
							c.getCanonicalName()));
					this.shortNames.put(shortName, c);
					// generate configuration window for each class
					ALDOperator winOp = null;
					try {
						// init a default operator
	          winOp = (ALDOperator)c.newInstance();
	          // request default GUI values
						ALDOperator initialOp = (ALDOperator)ALDDataIOManagerSwing.
								getInstance().getInitialGUIValue(null, c, winOp, null);
						// copy parameter settings
						Collection<String> params = initialOp.getParameterNames();
						for (String pname: params) {
							winOp.setParameter(pname, initialOp.getParameter(pname));
						}
          } catch (InstantiationException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
          } catch (IllegalAccessException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
          } catch (ALDDataIOException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
          } catch (ALDOperatorException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
          }
					this.configWins.put(c, 
							(OperatorConfigPanel)new ALDOperatorDataIOSwing().
									createGUIElement(field, c, winOp, descr, false));
					this.configWins.get(c).addValueChangeEventListener(this);
				}
				// sort (short) class names lexicographically
				Collections.sort(comboFields);
				// insert default entry as first element
				comboFields.add(0,
						new ALDSwingComponentComboBoxItem(null,"none",null));
				this.classSelection = new ALDSwingComponentComboBox(
						this.paramDescriptor, comboFields);
				this.classSelection.addValueChangeEventListener(this);
				if ( obj != null ) {
//					this.classSelection.getJComponent().setSelectedItem( obj);
					this.classSelection.setSelectedItem( obj);
				}
				else {
					this.classSelection.getJComponent().setSelectedIndex(0);
				}
				
				// configuration button
				this.configButton = new JButton("Configure...");
				this.configButton.setActionCommand("configure");
				this.configButton.addActionListener(this);

				this.mainPanel.add(this.classSelection.getJComponent());
				this.mainPanel.add(this.configButton);
				
				// init with given value
				try {
					if (obj != null)
						this.setValue(field, cl, null, obj);
        } catch (ALDDataIOProviderException e) {
        	System.err.println(
        			"[ALDOperatorDataIOSwing] Warning! setValue() failed!");
        	return;
        }
			}

			public HashMap<ALDOpParameterDescriptor, String> setValue(
					@SuppressWarnings("unused") Field field, 
					@SuppressWarnings("unused") Class<?> cl, 
					@SuppressWarnings("unused") ALDSwingComponent guiElement,	
					Object value) 
				throws ALDDataIOProviderException {
				// set selected item
//				this.classSelection.getJComponent().setSelectedItem(
//																						value.getClass().getSimpleName());
				this.classSelection.setSelectedItem(value);
				if (value == null) {
					// close all config windows that might be open
					@SuppressWarnings("rawtypes")
          Set<Class> keys = this.configWins.keySet();
					for (@SuppressWarnings("rawtypes") Class c: keys) {
						if (   this.configWins.get(c) != null
								&& this.configWins.get(c).confWin != null )
						(this.configWins.get(c)).confWin.setVisible(false);
					}
					return new HashMap<ALDOpParameterDescriptor, String>();
				}
				if (this.configWins.get(value.getClass()) == null) {
					throw new ALDDataIOProviderException(
							ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR, 
					"OperatorDataIO: readData found invalid class in GUI?!");
				}
				OperatorConfigPanel cButton = this.configWins.get(value.getClass()); 
				return cButton.updateOperator((ALDOperator)value);
			}

			/**
			 * Function to read parameter values from GUI. 
			 * 
			 * @param field		Field of object.
			 * @param cl			Class of object.
			 * @return	Current object value.
			 */
			public Object readData(Field field, 
					@SuppressWarnings("unused") Class<?> cl) 
			throws ALDDataIOProviderException {
				// get selected item
				Object item = this.classSelection.getJComponent().getSelectedItem();
//				Class<?> selectedClass = null;
//				if (item instanceof String)
//					selectedClass = this.shortNames.get(item);
//				else
//					selectedClass = (Class<?>)item;
				Class<?> selectedClass = 
						(Class<?>)((ALDSwingComponentComboBoxItem)item).getObject();
				if (this.configWins.get(selectedClass) == null) {
					return null;
				}
				return new ALDOperatorDataIOSwing().readData(field, selectedClass, 
						this.configWins.get(selectedClass));
			}

			@Override
			public void actionPerformed(ActionEvent e) {

				String command = e.getActionCommand();
				// pop-up the parametrized class configuration window
				if (command.equals("configure")) {
					// get selected item
					Object item = this.classSelection.getJComponent().getSelectedItem();
//					Class<?> selectedClass = null;
//					if (item instanceof String)
//						selectedClass = this.shortNames.get(item);
//					else
//						selectedClass = (Class<?>)item;
					Class<?> selectedClass = 
							(Class<?>)((ALDSwingComponentComboBoxItem)item).getObject();
					if (selectedClass == null)
						return;
//					if (this.configWins.get(selectedClass).confWin.window == null)
//						System.out.println("no window...");
					this.configWins.get(selectedClass).confWin.setVisible(true);
//					this.handleValueChangeEvent(new ALDSwingValueChangeEvent(this));
				}
			}

			@Override
			public void itemStateChanged(ItemEvent e) {
				this.classSelection.getJComponent().removeAllItems();

				// generate list of available classes for sorting 
//				Vector<String> comboFields = new Vector<String>();
//				for (Class<?> c : this.availableClasses) {
//					comboFields.add(c.getSimpleName());
//				}
				
				LinkedList<ALDSwingComponentComboBoxItem> comboFields = 
						new LinkedList<ALDSwingComponentComboBoxItem>();
				for (Class<?> c : this.availableClasses) {
					String shortName = c.getSimpleName();
					// add item object to combobox fields
					comboFields.add(new ALDSwingComponentComboBoxItem(c, shortName, 
																												c.getCanonicalName()));
				}
				// sort list of classes lexicographically
				Collections.sort(comboFields);
				//			comboFields.add(0, "none");
				comboFields.add(0,
						new ALDSwingComponentComboBoxItem(null,"none",null));

				// add default selection
//				this.classSelection.getJComponent().addItem("none");

				// check which name convention to use and add other items
//				if (this.longNameCheckBox.isSelected()) {
//					for (String s: comboFields) {
//						for (@SuppressWarnings("rawtypes") Class c: this.availableClasses) {
//							if (c.getSimpleName().equals(s)) {
//								this.classSelection.getJComponent().addItem(c);
//								break;
//							}
//						}
//					}
//				}
//				else {
//					for (String s: comboFields) {
//						this.classSelection.getJComponent().addItem(s);
//					}
				for (ALDSwingComponentComboBoxItem item: comboFields) {
					this.classSelection.getJComponent().addItem(item);
				}
			}

			@Override
			public void handleValueChangeEvent(ALDSwingValueChangeEvent event) {
				
				// Note: if this method is called, the combobox noticed a change in 
				//       the selection of the operator classes
				
				// check which item was selected
				Object item = this.classSelection.getJComponent().getSelectedItem();
				Class<?> selectedClass = 
						(Class<?>)((ALDSwingComponentComboBoxItem)item).getObject();
				if (selectedClass == null) {
					// "none" was selected, just fire an event...
					this.fireALDSwingValueChangeEvent(event);
					return;
				}
				// if an object class was selected, configure operator and window
				if (this.configWins.get(selectedClass).confWin.isOperatorNull()) {
					this.configWins.get(selectedClass).confWin.initOperator();
				}
				this.fireALDSwingValueChangeEvent(event);
			}

			@Override
			public JComponent getJComponent() {
				return this.mainPanel;
			}

			@Override
      public void disableComponent() {
				this.classSelection.disableComponent();
				@SuppressWarnings("rawtypes")
        Set<Class> keys = this.configWins.keySet();
				for (@SuppressWarnings("rawtypes") Class key: keys)
					this.configWins.get(key).disableComponent();
      }

			@Override
      public void enableComponent() {
				this.classSelection.enableComponent();
				@SuppressWarnings("rawtypes")
        Set<Class> keys = this.configWins.keySet();
				for (@SuppressWarnings("rawtypes") Class key: keys)
					this.configWins.get(key).enableComponent();
      }

			@Override
      public void dispose() {
				this.classSelection.dispose();
				@SuppressWarnings("rawtypes")
        Set<Class> keys = this.configWins.keySet();
				for (@SuppressWarnings("rawtypes") Class key: keys)
					this.configWins.get(key).dispose();
      }

		}
	}
}
