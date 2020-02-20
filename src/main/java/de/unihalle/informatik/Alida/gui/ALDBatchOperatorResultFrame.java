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
import de.unihalle.informatik.Alida.batch.ALDBatchOutputManagerSwing;
import de.unihalle.informatik.Alida.batch.ALDBatchRunResultInfo;
import de.unihalle.informatik.Alida.batch.ALDBatchOutputManagerSwing.ProviderInteractionLevel;
import de.unihalle.informatik.Alida.batch.provider.output.swing.ALDBatchOutputSummarizerSwing;
import de.unihalle.informatik.Alida.exceptions.ALDBatchIOManagerException;

/**
 * Frame to display results of a batch run of an operator.
 * 
 * @author Birgit Moeller
 */
public class ALDBatchOperatorResultFrame extends JFrame 
	implements ActionListener {

	/**
	 * Local debug flag.
	 */
	@SuppressWarnings("unused")
	private boolean debug = true;

//	/**
//	 * Map of compenents displayed in frame.
//	 */
//	private HashMap<ALDOpParameterDescriptor,JComponent> componentMap;
//	
//	/**
//	 * Associated operator.
//	 */
//	private ALDOperator op = null;
	
	/**
	 * Constructor.
	 * @param op						Associated operator.
	 * @param batchInfoMap	Batch mode result data and its meta data.
	 * @param pLevel				Level of interaction the providers should obey to.
	 */
	public ALDBatchOperatorResultFrame(ALDOperator op,
			HashMap<String, ALDBatchRunResultInfo> batchInfoMap, 
			ProviderInteractionLevel pLevel) {
		
		JPanel mainPanel = new JPanel();
		BoxLayout ylayout = new BoxLayout( mainPanel, BoxLayout.Y_AXIS );
		mainPanel.setLayout( ylayout);

		int numPanels = 0;
		// configure provider level according to context
		ALDBatchOutputManagerSwing.getInstance().setProviderInteractionLevel(pLevel);
		// set up window
		Set<String> keys = batchInfoMap.keySet();
		for ( String pName : keys ) {
			try {
				ALDOpParameterDescriptor descr= op.getParameterDescriptor( pName);
				Class<?> descrClass = descr.getMyclass();
				JComponent guiElement = null;
				try {
					ALDBatchOutputSummarizerSwing summarizer =
							(ALDBatchOutputSummarizerSwing)ALDBatchOutputManagerSwing.
											getInstance().getProvider(descrClass, 
																				ALDBatchOutputSummarizerSwing.class);
					if (summarizer == null)
						continue;
					guiElement = summarizer.writeData(batchInfoMap.get(pName), descr);
					if ( guiElement != null ) {
						numPanels++;

						JPanel paramPanel = new JPanel( new FlowLayout());
						paramPanel.add( new JLabel( descr.getLabel()));

						//						this.componentMap.put( descr, guiElement);
						paramPanel.add( guiElement);
						mainPanel.add( paramPanel);
					}
				} catch (ALDBatchIOManagerException exp) {
					Object[] options = { "OK" };
					JOptionPane.showOptionDialog(null, 
							"No provider found! \n " + exp.getCommentString(), "Warning", 
							JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
							null, options, options[0]);
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
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
//		JButton showButton = new JButton("Show Input Parameter");
//		showButton.setActionCommand( "showParams");
//		showButton.addActionListener( this);
//		showButton.setBounds(50, 60, 80, 30);

		// quit button
		JButton quitButton = new JButton("Quit");
		quitButton.setActionCommand( "quit");
		quitButton.addActionListener( this);
		quitButton.setBounds(50, 60, 80, 30);

		JPanel buttonPanel = new JPanel();
//		buttonPanel.add(showButton);
		buttonPanel.add(quitButton);

		mainPanel.add(buttonPanel);

		JScrollPane scrollPane = new JScrollPane( mainPanel);
		this.add( scrollPane);

		this.setTitle("Operator Results for " + op.getName());
		if (numPanels == 0)
			this.getContentPane().setPreferredSize(new Dimension(400, 200));
		else
			this.getContentPane().setPreferredSize(new Dimension(400, 250));
		this.pack();
		this.setDefaultCloseOperation( DISPOSE_ON_CLOSE);
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
			System.out.println( "I will show you your inputs soon....");
//			this.inputFrame.setVisible( true);
		}
	}
}
