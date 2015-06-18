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

package de.unihalle.informatik.Alida.helpers;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Singleton class to provide access to Alida icon in graphical environments.
 * 
 * @author moeller
 */
public class ALDIcon {

	/**
	 * Singleton reference.
	 */
	private static ALDIcon instance = null;
	
	/**
	 * The icon by itself.
	 */
	private ImageIcon alidaIcon;

	/**
	 * Get singleton instance.
	 * @return	Singleton instance.
	 */
	public static ALDIcon getInstance() {
		if (instance == null) {
			instance = new ALDIcon();
		}
		return instance;
	}
	
	/**
	 * Get reference to the icon.
	 * @return	The Alida icon.
	 */
	public ImageIcon getIcon() {
		return this.alidaIcon;
	}
	
	/**
	 * Default constructor.
	 */
	protected ALDIcon() {

		// initialize the icon
		String iconDataName = "/share/logo/Alida_logo.png";
		Image img = null;
		BufferedImage bi = null;
		Graphics g = null;
		InputStream is = null;
		try {
			ImageIcon icon;
			File iconDataFile = new File("./" + iconDataName);
			if(iconDataFile.exists()) {
				icon = new ImageIcon("./" + iconDataName);
				img = icon.getImage();
			}
			// try to find it inside a jar archive....
			else {
				is = ALDIcon.class.getResourceAsStream(iconDataName);
				if (is == null) {
					System.err.println("Warning - cannot find icons...");
					img = new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);
				}
				else {
					img = ImageIO.read(is);
				}
				bi= new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);
				g = bi.createGraphics();
				g.drawImage(img, 0, 0, 20, 20, null);
			}
		} catch (IOException ex) {
			System.err.println("ALDChooseOpNameFrame - problems loading icons...!");
			img = new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);
			bi= new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);
			g = bi.createGraphics();
			g.drawImage(img, 0, 0, 20, 20, null);
		}
		this.alidaIcon = new ImageIcon(img);
	}
}