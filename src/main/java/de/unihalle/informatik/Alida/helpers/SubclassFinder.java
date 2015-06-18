/*
 * This file is adapted from jstacs by Stefan Posch
 */

/*
 * This file is part of Jstacs.
 * 
 * Jstacs is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Jstacs is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Jstacs. If not, see <http://www.gnu.org/licenses/>.
 * 
 * For more information on Jstacs, visit http://www.jstacs.de
 */

package de.unihalle.informatik.Alida.helpers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.AbstractList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Utility-class with static methods to
 * <ul>
 * <li>find all sub-classes of a certain class (or interface) within the scope
 * of the current class-loader</li>
 * <li>find all sub-classes of a certain class (or interface) within the scope
 * of the current class-loader that can be instantiated, i.e. that are neither
 * interfaces nor abstract</li>
 * <li>filter a set of classes by inheritance from a super-class</li>
 * <li>obtain the class of an {@link InstanceParameterSet} that can be used to
 * instantiate a sub-class of {@link InstantiableFromParameterSet}.</li>
 * <li>obtain a {@link CollectionParameter} using all possible
 * {@link InstanceParameterSet}s (for classes that are a subclass of a specified
 * superclass) as elements</li>
 * </ul>
 * 
 * The methods may fail to find a certain sub-class, if
 * <ul>
 * <li>it was loaded by another {@link ClassLoader} than the caller</li>
 * <li>it was loaded from a physically other place than the super-class, e.g.
 * another jar-file.
 * </ul>
 * 
 * @author Jan Grau, Jens Keilwagen
 */
public class SubclassFinder {

	/**
	 * Returns all sub-classes of <code>T</code> that can be instantiated, i.e.
	 * are neither an interface nor abstract, and that are located in a package
	 * below <code>startPackage</code>.
	 * 
	 * @param <T>
	 *            The class to obtain the sub-classes for
	 * @param clazz
	 *            the {@link Class} object for T
	 * @param startPackage
	 *            the package under which to search
	 * @return the {@link Class} objects for the sub-classes
	 * @throws ClassNotFoundException
	 *             if one of the classes is present in the file system or jar
	 *             but cannot be loaded by the class loader
	 * @throws IOException
	 *             is thrown if the classes are searched for in a jar file, but
	 *             that file could not be accessed or read
	 */
	public static LinkedList<Class> findInstantiableSubclasses( Class clazz, String startPackage ) 
			throws ClassNotFoundException, IOException {
		LinkedList<Class> list = findSubclasses( clazz, startPackage );
		LinkedList<Class> list2 = new LinkedList<Class>();
		Iterator<Class> it = list.iterator();
		while( it.hasNext() ) {
			Class c = it.next();
			if( !c.isInterface() && !Modifier.isAbstract( c.getModifiers() ) ) {
				list2.add( c );
			}
		}
		return list2;
	}


	/**
	 * Returns all sub-classes of <code>T</code> including interfaces and
	 * abstract classes that are located in a package below
	 * <code>startPackage</code>.
	 * 
	 * @param <T>
	 *            The class to obtain the sub-classes for
	 * @param clazz
	 *            the {@link Class} object for T
	 * @param startPackage
	 *            the package under which to search
	 * @return the {@link Class} objects for the sub-classes
	 * @throws ClassNotFoundException
	 *             if one of the classes is present in the file system or jar
	 *             but cannot be loaded by the class loader
	 * @throws IOException
	 *             is thrown if the classes are searched for in a jar file, but
	 *             that file could not be accessed or read
	 */
	public static LinkedList<Class> findSubclasses( Class clazz, String startPackage ) throws ClassNotFoundException,
	IOException {
		return findSubclasses( startPackage, clazz, startPackage);
	}
	
	public static LinkedList<Class> findSubclasses( String prefix, Class clazz, String startPackage ) throws ClassNotFoundException,
	IOException {

		String name = startPackage;
		if( !name.startsWith( "/" ) ) {
			name = "/" + name;
		}
		name = name.replace( ".", "/" );

		URL url = clazz.getResource( name );

		LinkedList<Class> list = new LinkedList<Class>();

		if( url != null ) {
			File dir = new File( url.getFile() );

			if( dir.exists() ) {
				File[] files = dir.listFiles();
				for( int i = 0; i < files.length; i++ ) {
					if( files[i].isDirectory() ) {
						//System.out.println(startPackage+"."+files[i].getName());
						list.addAll( findSubclasses( prefix, clazz, startPackage + "." + files[i].getName() ) );
					} else if( files[i].isFile() && files[i].getName().endsWith( ".class" ) ) {
						add( clazz, list, startPackage + "." + files[i].getName().substring( 0, files[i].getName().lastIndexOf( "." ) ) );
					}
				}
			} else {
				JarURLConnection con = (JarURLConnection)url.openConnection();
				JarFile jar = con.getJarFile();
				String starts = con.getEntryName();
				Enumeration<JarEntry> en = jar.entries();
				while( en.hasMoreElements() ) {
					JarEntry entry = en.nextElement();
					String entryname = entry.getName();
					if( entryname.startsWith( starts ) && entryname.endsWith( ".class" ) ) {
						String classname = entryname.substring( 0, entryname.length() - 6 );
						if( classname.startsWith( "/" ) ) {
							classname.substring( 1 );
						}
						add( clazz, list, classname.replace( "/", "." ) );
					}
				}
			}
		} else {
			name = startPackage;

			JarFile jar;
			File dir = new File( name);
			if( dir.exists() && dir.isDirectory()) {
				File[] files = dir.listFiles();
				for( int i = 0; i < files.length; i++ ) {
					if( files[i].isDirectory() ) {
						list.addAll( findSubclasses( prefix, clazz, startPackage + "." + files[i].getName() ) );
					} else if( files[i].isFile() && files[i].getName().endsWith( ".class" ) ) {
						add( clazz, list, files[i].getAbsolutePath().substring( prefix.length()+1, files[i].getAbsolutePath().lastIndexOf( "." ) ).replace("/", ".") );
					}
				}
			} else if ( (jar = new JarFile(dir)) != null){
				Enumeration<JarEntry> en = jar.entries();
				while( en.hasMoreElements() ) {
					JarEntry entry = en.nextElement();
					String entryname = entry.getName();
					if( entryname.endsWith( ".class" ) ) {
						String classname = entryname.substring( 0, entryname.length() - 6 );
						if( classname.startsWith( "/" ) ) {
							classname.substring( 1 );
						}
						add( clazz, list, classname.replace( "/", "." ) );
					}
				}
			}

		}

		return list;
	}

	@SuppressWarnings( "unchecked" )
	private static void add( Class clazz, AbstractList<Class> list, String className ) {
		try {
			Class c = Class.forName( className );
			if( clazz.isAssignableFrom( c ) ) {

				list.add( c );
			}
		} catch ( Exception e ) {} catch ( Error e ) {}
	}

}
