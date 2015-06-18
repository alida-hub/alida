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

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import de.unihalle.informatik.Alida.operator.*;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing.ProviderInteractionLevel;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;

/**
 * Frame with result data displayed on termination of an operator run.
 * 
 * @author Stefan Posch
 * @author Birgit Moeller
 */
public class ALDOperatorResultFrame extends JFrame 
	implements ActionListener {

	/**
	 * Local debug flag.
	 */
	private boolean debug = true;

	/**
	 * Map of compenents displayed in frame.
	 */
	private HashMap<ALDOpParameterDescriptor,JComponent> componentMap;
	
	/**
	 * Associated operator.
	 */
	private ALDOperator op = null;
	
	/**
	 * Window to display associated parameter configuration.
	 */
	private ALDOperatorParamDisplayFrame paramFrame;
	
	/**
	 * Constructor.
	 * @param operator	Associated operator.
	 * @param pLevel		Level of interaction the providers should obey to.
	 */
	public ALDOperatorResultFrame(ALDOperator operator, 
			ProviderInteractionLevel pLevel) {
		
		this.op = operator;
		this.componentMap = new HashMap<ALDOpParameterDescriptor, JComponent>();

		// init the configuration window
		this.paramFrame = new ALDOperatorParamDisplayFrame(this.op, 
				ProviderInteractionLevel.WARNINGS_ONLY);		

		JPanel mainPanel = new JPanel();
		BoxLayout ylayout = new BoxLayout( mainPanel, BoxLayout.Y_AXIS );
		mainPanel.setLayout( ylayout);

		int numPanels = 0;
		// configure provider level according to context
		ALDDataIOManagerSwing.getInstance().setProviderInteractionLevel(pLevel);
		
		// sort descriptors according to GUI order into hash table
		HashMap<Integer, Vector<ALDOpParameterDescriptor>> guiOrderHash = 
				new HashMap<Integer, Vector<ALDOpParameterDescriptor>>();
		for ( String pName : this.op.getOutInoutNames() ) {
			try {
				ALDOpParameterDescriptor descr= this.op.getParameterDescriptor( pName);
				Integer order = new Integer(descr.getDataIOOrder());
				if (guiOrderHash.containsKey(order)) {
					guiOrderHash.get(order).add(descr);
				} else {
					Vector<ALDOpParameterDescriptor> paramVec = 
							new Vector<ALDOpParameterDescriptor>();
					paramVec.add(descr);
					guiOrderHash.put(order, paramVec);
				}
			}
			catch (Exception e) {
				System.err.println("Warning! Problems with result parameter " + 
						"descriptor: " + e.getMessage());
				e.printStackTrace();
				System.out.println("Skipping...");
			}
		}
		// TODO Birgit - Maybe sorting the keys can be done easier...
		Set<Integer> keys = guiOrderHash.keySet();
		LinkedList<Integer> keyList = new LinkedList<Integer>();
		for (Integer key : keys) {
			keyList.add(key);
		}
		Collections.sort(keyList);

		// set up window
		for (Integer key : keyList) {
			Vector<ALDOpParameterDescriptor> descrips = guiOrderHash.get(key);
			for ( ALDOpParameterDescriptor descr : descrips ) {
				Object value;
        try {
	        value = this.op.getParameter( descr.getName() );
        } catch (ALDOperatorException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	        continue;
        }

				if ( this.debug ) 
					System.out.println(
							"ALDExecuteOperatorFrame output value of parameter " 
									+ descr.getName() +	" = " + value);

				if ( value != null ) {
					JComponent guiElement = null;
					try {
						guiElement = 
								ALDDataIOManagerSwing.getInstance().writeData( value, descr);
						if ( guiElement != null ) {
							numPanels++;

							JPanel paramPanel = new JPanel( new FlowLayout());
							paramPanel.add( new JLabel( descr.getLabel()));

							this.componentMap.put( descr, guiElement);
							paramPanel.add( guiElement);

							mainPanel.add( paramPanel);
						}
					} catch (ALDDataIOManagerException exp) {
						Object[] options = { "OK" };
						JOptionPane.showOptionDialog(null, 
								"No provider found! \n " + exp.getCommentString(), "Warning", 
								JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
								null, options, options[0]);
					} catch (ALDDataIOException e) {
	          // TODO Auto-generated catch block
	          e.printStackTrace();
          }
				}
			}
		}

		if ( numPanels == 0 ) {
			JLabel notifyLabel = 
				new JLabel("No additional result parameters to display...");
			notifyLabel.setAlignmentX(LEFT_ALIGNMENT);
			mainPanel.add(notifyLabel);
		}
			
		// ===========================================
		// show input parameters button
		JButton showButton = new JButton("Show Input Parameters");
		showButton.setActionCommand( "showParams");
		showButton.addActionListener( this);
		showButton.setBounds(50, 60, 80, 30);

		// quit button
		JButton quitButton = new JButton("Quit");
		quitButton.setActionCommand( "quit");
		quitButton.addActionListener( this);
		quitButton.setBounds(50, 60, 80, 30);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(showButton);
		buttonPanel.add(quitButton);

		mainPanel.add(buttonPanel);

		JScrollPane scrollPane = new JScrollPane( mainPanel);
		this.add( scrollPane);

		this.setTitle("Operator Results for " + this.op.getName());
		if (numPanels == 0)
			this.getContentPane().setPreferredSize(new Dimension(400, 200));
		else
			this.getContentPane().setPreferredSize(new Dimension(400, 250));
		this.pack();
		this.setDefaultCloseOperation( DISPOSE_ON_CLOSE);
	}
	
	@Override
	public void dispose() {
		if (this.paramFrame != null)
			this.paramFrame.dispose();
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
  public void actionPerformed(ActionEvent e) {

		// local variables
		String command = e.getActionCommand();

		// quit
		if (command.equals("quit")) {
			this.setVisible( false);
		}
		// show params
		else if (command.equals("showParams")) {
			this.paramFrame.setVisible( true);
		}
	}
}
