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
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URISyntaxException;

import de.unihalle.informatik.Alida.operator.*;

/**
 * Frame to show documentation for operators and tools.
 * 
 * @author Birgit Moeller
 */
public class ALDOperatorDocumentationFrame extends JFrame 
	implements ActionListener {

	/**
	 * Name of associated operator.
	 */
	protected String opName = null;
	
	/**
	 * Package of associated operator/tool.
	 */
	protected String opPackage = null;
	
	/**
	 * Constructor with title parameter.
	 * @param title			Window title.
	 * @param operator	Associated operator.
	 * @param pLevel		Level of interaction the providers should obey to.
	 */
	public ALDOperatorDocumentationFrame(String name, String pack, 
			String docText) {

		// init the frame
		super();
		
		// remember the operator
		this.opName = name;
		this.opPackage = pack;

		this.setTitle("Documentation - " + this.opName);
		this.setSize(750, 800);
		
		JPanel docuPanel = new JPanel();
		docuPanel.setLayout(new BorderLayout());
		
		String opPackage = this.opPackage.replace(".", "/");
		opPackage += ".html";
		
		String text = "<h1> Documentation of " + this.opName + "</h1><br/>" 
				+ docText + "<br/><p><hr>";
		text += "<a href=\"http://alida.informatik.uni-halle.de/api/java/" + 
				opPackage + "\">Class API at alida.informatik.uni-halle.de</a>";
		JEditorPane textPane = new JEditorPane("text/html", text);
		
		textPane.setEditable(false);
		textPane.addHyperlinkListener(new HyperlinkListener() {
	    public void hyperlinkUpdate(HyperlinkEvent e) {
	        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {			        	
	    			if(Desktop.isDesktopSupported()) {
	  			    try {
								Desktop.getDesktop().browse(e.getURL().toURI());
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (URISyntaxException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
	    			}
	        }
	    }
		});

		JScrollPane scrollPane = new JScrollPane(textPane);
		docuPanel.add(scrollPane, BorderLayout.CENTER);
		scrollPane.getVerticalScrollBar().setValue(0);
		scrollPane.getHorizontalScrollBar().setValue(0);
		
		JPanel buttonPanel = new JPanel();
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(this);

		buttonPanel.add(closeButton);
		docuPanel.add(buttonPanel, BorderLayout.SOUTH);
		docuPanel.updateUI();
		
		this.add(docuPanel);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
  public void actionPerformed(ActionEvent e) {
		this.dispose();
	}
}
