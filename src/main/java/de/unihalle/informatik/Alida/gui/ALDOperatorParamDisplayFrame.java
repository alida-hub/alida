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

import de.unihalle.informatik.Alida.operator.*;
import de.unihalle.informatik.Alida.annotations.Parameter.ExpertMode;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing.ProviderInteractionLevel;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDOperatorParameterPanel;

/**
 * Frame to display the operator configuration used to calculate results.
 * 
 * @author Birgit Moeller
 */
public class ALDOperatorParamDisplayFrame extends JFrame 
	implements ActionListener {

	/**
	 * Associated operator.
	 */
	private ALDOperator op = null;
	
	/**
	 * Constructor.
	 * @param operator	Associated operator.
	 * @param pLevel		Level of interaction the providers should obey to.
	 */
	public ALDOperatorParamDisplayFrame(ALDOperator operator, 
			ProviderInteractionLevel pLevel) {
		
		this.op = operator;
		ALDDataIOManagerSwing.getInstance().setProviderInteractionLevel(pLevel);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		ALDOperatorParameterPanel opPanel = new ALDOperatorParameterPanel(this.op, 
			ExpertMode.ADVANCED, false, null);
		opPanel.disableComponents();
		mainPanel.add(opPanel.getJPanel(),BorderLayout.CENTER);

		// quit button
		JButton quitButton = new JButton("Quit");
		quitButton.setActionCommand( "quit");
		quitButton.addActionListener( this);
		quitButton.setBounds(50, 60, 80, 30);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(quitButton);

		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		JScrollPane scrollPane = new JScrollPane( mainPanel);
		this.add( scrollPane);

		this.setTitle("Operator configuration for results of "+this.op.getName());
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
	}
}
