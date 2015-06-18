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
import javax.swing.tree.*;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;

import de.unihalle.informatik.Alida.annotations.*;
import de.unihalle.informatik.Alida.helpers.ALDEnvironmentConfig;
import de.unihalle.informatik.Alida.operator.ALDOperatorLocation;

/**
 * This class extends <code>JTree</code> to hold <code>ALDOperator</code>s to choose from.
 * There are two levels which determine the operators available, namely
 * the standard and application level.
 *  <p>
 *  The initial level of operators displayed is by default the application level.
 *  This may be overridden by a JVM property alida_oprunner_level or
 *  the environment variable ALIDA_OPRUNNER_LEVEL.
 *  <p>
 *  A list of operators to be unfolded at start up may be supplied in one or more files.
 *  Each file is assumed to contain one operator name per line.
 *  The name(s) of this file(s) may be specified by a JVM property alida_oprunner_favoriteops or
 *  the environment variable ALIDA_OPRUNNER_FAVORITEOPS as a colon separated list of filenames.
 *  <p>
 *  Optionally a string to filter operators (and workflows) may be supplied via
 *  <code>opNameFilter</code>. If null, all annotated operators will we displayed.
 *  If non null and this string contains only alpha numeric letters and dots,
 *  all operators containing the filter string will be displayed where matching
 *  is performed case insensitive.
 *  Otherwise the filter string is interpreted as a regular expression and matching is
 *  performed case sensitive.
 *  
 * @author Stefan Posch
 */

public class ALDOperatorChooserTree extends JTree  {

	/**
	 * Debug flag (not accessible from outside).
	 */
	static boolean debug = false;

	/**
	 * Mode of usage.
	 * <p>
	 * In application mode only operators annotated as applications are
	 * available, otherwise all annotated operators are displayed.
	 */
	protected ALDAOperator.Level level = ALDAOperator.Level.APPLICATION;

	/**
	 * List of favorite operators to be unfolded at startup.
	 */
	Vector<String> favoriteOperators;

	/**
	 * Tree of available operators for standard level.
	 */
	protected DefaultTreeModel standardTreeModel;

	/**
	 * Tree of available operators for standard level.
	 */
	protected DefaultTreeModel applicationTreeModel;

	private Collection<ALDOperatorLocation> standardLocations;
	private Collection<ALDOperatorLocation> applicationLocations;
	/**
	 * if non null regular expression select operators
	 */
	private String opNameFilter = null;

	/**
	 * Constructor.
	 */
	public ALDOperatorChooserTree( Collection<ALDOperatorLocation> standardLocations, Collection<ALDOperatorLocation> applicationLocations) {

		if ( ALDOperatorChooserTree.debug ) {
			System.out.println( "ALDOperatorChooserTree::ALDOperatorChooserTree");
		}
		
		this.standardLocations = standardLocations;
		this.applicationLocations = applicationLocations;
		
		// read configuration
		this.favoriteOperators = initFavoriteOperators();

		this.standardTreeModel = createOpTreeModel( standardLocations, "ALDOperators");
		this.applicationTreeModel = createOpTreeModel( applicationLocations, "ALDOperators");

		String levelString = ALDEnvironmentConfig.getConfigValue("OPRUNNER","LEVEL");
		if (levelString == null || levelString.equalsIgnoreCase("application") ) {
				this.level = ALDAOperator.Level.APPLICATION;
				this.setModel( applicationTreeModel);
		} else {
				this.level = ALDAOperator.Level.STANDARD;
				this.setModel( standardTreeModel);
		}
		
		hideLeaves( new TreePath(this.getModel().getRoot()));
	}

	/** Return current mode of usage.
	 * 
	 * @return current mode
	 */
	public ALDAOperator.Level getLevel() {
		return level;
	}

	/**
	 * Set the level of the operators in this tree.
	 * Keep the current expansion state of the tree.
	 * 
	 * @param level new level
	 */
	public void setLevel( ALDAOperator.Level level) {
		if ( ALDOperatorChooserTree.debug ) {
			System.out.println( "ALDOperatorChooserTree::setLevel to " + level);
		}
		this.level = level;

		HashSet<String> expandedPackageNames = this.collectExpandedPackages( new TreePath(this.getModel().getRoot()), "");
		if ( debug ) {
			for ( String str : expandedPackageNames) {
				System.out.println("ALDOperatorChooserTree::setLevel unfolded package: " + str);
			}
		}

		if ( level == ALDAOperator.Level.STANDARD) {
			this.setModel( standardTreeModel);
		} else {
			this.setModel( applicationTreeModel);
		}
		
		this.makeVisibleNodes(new TreePath(this.getModel().getRoot()), "", expandedPackageNames);
		this.updateUI();
	}
	
	/**
	 * @return the opNameFilter
	 */
	public String getOpNameFilter() {
		return opNameFilter;
	}

	/**
	 * @param opNameFilter the opNameFilter to set
	 */
	public void setOpNameFilter(String opNameFilter) {
		if ( opNameFilter.isEmpty())
			this.opNameFilter = null;
		else
			this.opNameFilter = opNameFilter;

		if (level == ALDAOperator.Level.APPLICATION) {
			this.applicationTreeModel = createOpTreeModel( applicationLocations, "ALDOperators");
			this.setModel( applicationTreeModel);
		} else {
			this.standardTreeModel = createOpTreeModel( standardLocations, "ALDOperators");
			this.setModel( standardTreeModel);
		}
		
		if ( opNameFilter.isEmpty())
			hideLeaves( new TreePath(this.getModel().getRoot()));
		else
			unhideLeaves( new TreePath(this.getModel().getRoot()));
		
		this.updateUI();
	}

	@Override
	public ALDOperatorChooserTreeNode getLastSelectedPathComponent() {
		return  (ALDOperatorChooserTreeNode) super.getLastSelectedPathComponent();
	}

	private DefaultTreeModel createOpTreeModel( Collection<ALDOperatorLocation> operators, String nameOfRootNode) {
		if (ALDOperatorChooserTree.debug)
			System.out.println("ALDOperatorChooserTree::createOpTreeeModel " +
					"Looking up all annotated @ALDAOperators for level " + level);

		// first we build the tree with our own data structure 
		AuxNode treeOfOpNames = new AuxNode();

		for (ALDOperatorLocation location : operators) {
			String className = location.getName();
			if ( opNameFilter != null &&
					! this.matches(className, opNameFilter)) {
				if ( ALDOperatorChooserTree.debug) {
					System.out.println("ALDOperatorChooserTree::createOpTreeModel do not add "
									+ className);
				}
				continue;
			}
			if ( ALDOperatorChooserTree.debug) {
				System.out.println("ALDOperatorChooserTree::createOpTreeModel adding "
								+ className);
			}

			// build a tree of opNames according to packages
			String[] parts = location.getPartsOfName();
			AuxNode currentNode = treeOfOpNames;
			// loop over the part but the last one
			int i;
			for (i = 0; i < parts.length; i++) {
				String part = parts[i];
				if (currentNode.children.get(part) == null) {
					AuxNode newChild = new AuxNode(null);
					currentNode.children.put(part, newChild);
				}
				currentNode = currentNode.children.get(part);
				
				if ( i == (parts.length-1)) {
					currentNode.location = location;
				}
			}
		}

		ALDOperatorChooserTreeNode node = treeOfOpNames.createTree(nameOfRootNode);
		return new DefaultTreeModel(node);
	}

	/** Does <code>className</code>  match the opNameFilter
	 * @param className
	 * @param opNameFilter
	 * @return
	 */
	private boolean matches(String className, String opNameFilter) {
		if ( opNameFilter.matches("^[a-zA-Z.]*$") ) {
			return className.toLowerCase().matches(".*"+opNameFilter.toLowerCase() + ".*");
		} else {
			return className.matches(opNameFilter);
		}
	}

	/**
	 * Hide all leaves of this tree below the given path.
	 * The exception to this rule is that all favorite operators are unfolded 
	 * which also unfolds there siblings.
	 * 
	 * @param treePath Path below which leaves are to be hidden.
	 */
	private void hideLeaves( TreePath treePath) {
		ALDOperatorChooserTreeNode lastNode = (ALDOperatorChooserTreeNode) treePath.getLastPathComponent();
		
		if (!lastNode.isLeaf())
			this.collapsePath(treePath);
		
		if ( lastNode.getLocation() != null) {
			if (this.favoriteOperators.contains(lastNode.getLocation().getName())) {
				this.makeVisible(treePath);
			}
		}

		TreeModel treeModel = this.getModel();
		if (treeModel.getChildCount(lastNode) >= 0) {
			for (int i = 0; i < treeModel.getChildCount(lastNode); i++) {
				TreeNode child = (TreeNode) treeModel.getChild(lastNode, i);
				TreePath extendedPath = treePath.pathByAddingChild(child);
				hideLeaves( extendedPath);
			}
		}
	}

	/**
	 * Unhide all leaves of this tree below the given path.
	 * 
	 * @param treePath Path below which leaves are to be hidden.
	 */
	private void unhideLeaves( TreePath treePath) {
		ALDOperatorChooserTreeNode lastNode = (ALDOperatorChooserTreeNode) treePath.getLastPathComponent();
		
		this.makeVisible(treePath);

		TreeModel treeModel = this.getModel();
		if (treeModel.getChildCount(lastNode) >= 0) {
			for (int i = 0; i < treeModel.getChildCount(lastNode); i++) {
				TreeNode child = (TreeNode) treeModel.getChild(lastNode, i);
				TreePath extendedPath = treePath.pathByAddingChild(child);
				unhideLeaves( extendedPath);
			}
		}
	}

	/** Collect the package names (for operators) and path (for workflows) which are
	 * currently expanded.
	 * Use a <code>+</code> to separate the parts.
	 * 
	 * @param treePath
	 * @param base
	 * @return
	 */
	private HashSet<String> collectExpandedPackages(TreePath treePath, String base) {
		HashSet<String> res = null;
		
		ALDOperatorChooserTreeNode lastNode = (ALDOperatorChooserTreeNode) treePath.getLastPathComponent();
		
		if ( this.isExpanded(treePath)) {
			res = new HashSet<String>();

			String name = new String();
			if ( String.class.isAssignableFrom(lastNode.getUserObject().getClass()) )
				name = (String)lastNode.getUserObject();

			String packageName;
			if ( base.isEmpty() )
				packageName = name;
			else
				packageName = base + "+" + name;
			
			res.add(packageName);
			
			TreeModel treeModel = this.getModel();
			if (treeModel.getChildCount(lastNode) >= 0) {
				for (int i = 0; i < treeModel.getChildCount(lastNode); i++) {
					TreeNode child = (TreeNode) treeModel.getChild(lastNode, i);
					TreePath extendedPath = treePath.pathByAddingChild(child);
					HashSet<String> recursiveResult = collectExpandedPackages( extendedPath, packageName);
					if (recursiveResult != null) {
						res.addAll(recursiveResult);
					}
				}
			}
		}
		
		return res;
	}
	
	/**
	 * Makes all nodes of the tree visible where the corresponding package names (of workflow paths)
	 * are contained in <code>packageNames</code>.
	 * 
	 * @param treePath
	 * @param base
	 * @param packageNames
	 * @return
	 */
	private void makeVisibleNodes(TreePath treePath, String base, HashSet<String> packageNames) {
		ALDOperatorChooserTreeNode lastNode = (ALDOperatorChooserTreeNode) treePath.getLastPathComponent();

		String name = new String();
		if ( String.class.isAssignableFrom(lastNode.getUserObject().getClass()) )
			name = (String)lastNode.getUserObject();

		String packageName;
		if ( base.isEmpty() )
			packageName = name;
		else
			packageName = base + "+" + name;

		if ( packageNames.contains(packageName)) {
			if ( debug ) {
				System.out.println( "ALDOperatorChooserTree::makeVisibleNodes  make visible " + packageName);
			}
			
			this.expandPath(treePath);
			
			TreeModel treeModel = this.getModel();

			if (treeModel.getChildCount(lastNode) >= 0) {
				for (int i = 0; i < treeModel.getChildCount(lastNode); i++) {
					TreeNode child = (TreeNode) treeModel.getChild(lastNode, i);
					TreePath extendedPath = treePath.pathByAddingChild(child);
					makeVisibleNodes( extendedPath, packageName, packageNames);
				}
			}
		} else {			
			if ( debug ) {
				System.out.println( "ALDOperatorChooserTree::makeVisibleNodes  make invisible " + packageName);
			}

			this.collapsePath(treePath);
		}
	}

	/**
	 * Initialize list of favorite operators to be unfolded at startup.
	 * <p>
	 *  The list of operators to be unfolded at start up may be supplied in one or more files.
	 *  Each file is assumed to contain one operator name per line.
	 *  The name(s) of this file(s) may be specified by a JVM property alida_oprunner_favoriteops or
	 *  the environment variable ALIDA_OPRUNNER_FAVORITEOPS as a colon separated list of filenames.
	 * 
	 * @return List of favorite operators.
	 */
	private Vector<String> initFavoriteOperators() {
		String favoriteOpsConfigFiles = ALDEnvironmentConfig.getConfigValue(
				"OPRUNNER", "FAVORITEOPS");

		if ( favoriteOpsConfigFiles == null) {
			favoriteOpsConfigFiles = System.getProperty("user.home") + "/.alida/favoriteops";
		}

		Vector<String> favoriteOps = new Vector<String>();
		if (ALDOperatorChooserTree.debug) {
			System.out.println("ALDChooseOpNameFrame::initFavoriteOperators configfile = "
							+ favoriteOpsConfigFiles);
		}

		if (favoriteOpsConfigFiles != null) {
			for ( String filename : favoriteOpsConfigFiles.split(":"))
				try {
					BufferedReader reader = new BufferedReader(new FileReader(
							filename));
					String line;
					while ((line = reader.readLine()) != null) {
						favoriteOps.add(line);
						if ( ALDOperatorChooserTree.debug ) {
							System.out.println("   added favorite operator " + line);
						}
					}

				} catch (Exception e) {
					// nothing to be done
			}
		}
		return favoriteOps;
	}

	// ========================================================================================

	/**
	 * Node to build up a tree of the package structure of <code>ALDOperator</code>s.
	 * Useful as we have a hash map of children.
	 */
	private class AuxNode {

		/**
		 * Children of this node.
		 */
		HashMap<String, AuxNode> children;

		/**
		 * location object for this node. Is null for inner nodes, i.e. nodes not representing an operator
		 */
		ALDOperatorLocation location;
		
		/**
		 * Constructor.
		 * 
		 * @param fullName  Full name of root operator.
		 */
		AuxNode(ALDOperatorLocation location) {
			this.children = new HashMap<String, AuxNode>();
			this.location = location;
		}

		/**
		 * Default constructor.
		 */
		AuxNode() {
			this( null);
		}

		/**
		 * Generates the ModelTree using the tree represented with <code>AuxNode</code>s.
		 * 
		 * @param name name of node.
		 * @return Created tree.
		 */
		ALDOperatorChooserTreeNode createTree(String name) {
			// create a TreeNode for this AuxName
			ALDOperatorChooserTreeNode node = 
					new ALDOperatorChooserTreeNode(name, this.location);

			LinkedList<String> keysSorted = new LinkedList<String>(
					this.children.keySet());
			java.util.Collections.sort(keysSorted);
			for (String str : keysSorted) {
				ALDOperatorChooserTreeNode child = this.children.get(str).createTree(str);
				node.add(child);
			}

			return node;
		}
	}
}
