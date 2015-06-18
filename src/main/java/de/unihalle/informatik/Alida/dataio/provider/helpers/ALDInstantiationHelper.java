package de.unihalle.informatik.Alida.dataio.provider.helpers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ALDInstantiationHelper {

	static public Object newInstance( Class clazz) 
			throws IllegalArgumentException, InstantiationException, IllegalAccessException, 
			       InvocationTargetException, SecurityException, NoSuchMethodException {
		
		try {
			return clazz.newInstance();
		} catch ( Exception ex ) {
			@SuppressWarnings({ "rawtypes", "unchecked" })

			Constructor constructor = clazz.getDeclaredConstructor(null);
			constructor.setAccessible(true);
			return constructor.newInstance(new Object[0]);
		}

	}
}
