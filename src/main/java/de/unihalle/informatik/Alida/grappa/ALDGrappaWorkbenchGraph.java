/*
 * This file is part of MiToBo, the Microscope Image Analysis Toolbox.
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
 * Fore more information on MiToBo, visit
 *
 *    http://www.informatik.uni-halle.de/mitobo/
 *
 */

package de.unihalle.informatik.Alida.grappa;

import com.mxgraph.view.mxGraph;

/**
 * Graph class augmented for Grappa workflows.
 * <p>
 * This class extends JGraph's super class {@link mxGraph} 
 * mainly for handling tooltips.
 * 
 * @author moeller
 */
public class ALDGrappaWorkbenchGraph extends mxGraph {

	/**
	 * Reference to the workbench tab which is handling tooltip requests.
	 */
	private ALDGrappaWorkbenchTab tooltipHandler = null;

	/**
	 * Specify workbench tab associated with this graph.
	 * @param tab		Workbench tab associated with graph.		
	 */
	public void setWorkbenchTab(ALDGrappaWorkbenchTab tab) {
		this.tooltipHandler = tab;
	}

	@Override
	public String getToolTipForCell(Object cell) {
		if (this.tooltipHandler != null )
			return this.tooltipHandler.getTooltipText(cell);
		return "";
	}
}
