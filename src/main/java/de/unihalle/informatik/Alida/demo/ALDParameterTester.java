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

package de.unihalle.informatik.Alida.demo;

import java.util.EnumSet;
import java.util.Vector;

import de.unihalle.informatik.Alida.datatypes.ALDDirectoryString;
import de.unihalle.informatik.Alida.datatypes.ALDFileString;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.Parameter;

/**
 * Dummy operator class for demo and testing of input parameter types.
 * <p>
 * Note: this operator has no functionality except printing the values of
 * 				its input parameters to console.
 * 
 * @author moeller
 */
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL)
public class ALDParameterTester extends ALDOperator {

  @Parameter( label= "Info-String", required = false, dataIOOrder = -1,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.",
  		info = true)
	private String infoString = "<i>Info</i> - Set of parameters to test: ";
  @Parameter( label= "String", required = false, dataIOOrder = 0,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private String param_String = null;
  @Parameter( label= "boolean", required = false, dataIOOrder = 1,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private boolean param_boolean = true;
  @Parameter( label= "byte", required = false, dataIOOrder = 2,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private byte param_byte;
  @Parameter( label= "double", required = false, dataIOOrder = 3,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private double param_double;
  @Parameter( label= "float", required = false, dataIOOrder = 4,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private float param_float;
  @Parameter( label= "int", required = false, dataIOOrder = 5,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private int param_int;
  @Parameter( label= "long", required = false, dataIOOrder = 6,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private long param_long;
  @Parameter( label= "short", required = false, dataIOOrder = 7,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private short param_short;
  @Parameter( label= "Boolean", required = false, dataIOOrder = 8,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private Boolean param_Boolean;
  @Parameter( label= "Byte", required = false, dataIOOrder = 9,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private Byte param_Byte;
  @Parameter( label= "Double", required = false, dataIOOrder = 10,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private Double param_Double;
  @Parameter( label= "Float", required = false, dataIOOrder = 11,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private Float param_Float;
  @Parameter( label= "Integer", required = false, dataIOOrder = 12,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private Integer param_Integer;
  @Parameter( label= "Long", required = false, dataIOOrder = 13,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private Long param_Long;
  @Parameter( label= "Short", required = false, dataIOOrder = 14,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private Short param_Short;

  @Parameter( label= "String_array", required = false, dataIOOrder = 15,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private String [] array_String = null;
//  	new String[]{"AAA","BBB","CCC","DDD","EEE"};
  
  @Parameter( label= "Integer_array", required = false, dataIOOrder = 16,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private Integer [] array_Integer = null;
//  	new Integer[]{new Integer(1), new Integer(2), new Integer(3)};

  @Parameter( label= "String_array_2D", required = false, dataIOOrder = 17,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private String [][] array_String_2D = 
  	new String[][]{{"AAA","BBB","CCC","DDD","EEE"},
  									{"aaa","bbb","ccc","ddd","eee"}};
  
  @Parameter( label= "Integer_array_2D", required = false, dataIOOrder = 18,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private Integer [][] array_Intege_2D = 
  	new Integer[][]{
  	{new Integer(1), new Integer(2), new Integer(3) },
  	{new Integer(11),new Integer(22),new Integer(33)}};
  
  private static enum EnumSelection {
  	FIRST_VALUE,
  	SECOND_VALUE,
  	THIRD_VALUE,
  	FORTH_VALUE,
  	FIFTH_VALUE,
  	SIXTH_VALUE,
  	SEVENTH_VALUE,
  	EIGHTH_VALUE,
  	NINETH_VALUE,
  	TENTH_VALUE,
  	ELEVEN,
  	TWELVE
  }
  
  @Parameter( label= "Enum", required = false, dataIOOrder = 19,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private EnumSelection param_enum = null;

  @Parameter( label= "Enum Selection", required = false, dataIOOrder = 19,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private EnumSet<EnumSelection> eSelect =
  	EnumSet.of(EnumSelection.FIRST_VALUE, EnumSelection.THIRD_VALUE);

  @Parameter( label= "Vector_Double", required = false, dataIOOrder = 20,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private Vector<Double> param_vectorDouble = null;

  @Parameter( label= "Vector_Byte", required = false, dataIOOrder = 20,
	  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
	  private Vector<Byte> param_vectorByte = null;

  @Parameter( label= "File", required = false, dataIOOrder = 21,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private ALDFileString param_file = null;
  @Parameter( label= "Directory", required = false, dataIOOrder = 22,
  		direction = Parameter.Direction.IN,	description = "Dummy parameter.")
  private ALDDirectoryString param_dir = null;

  // output parameter
  @Parameter( label= "Output Boolean", required = false, dataIOOrder = 1,
		  direction = Parameter.Direction.OUT,	description = "Dummy output parameter.")
  private Boolean out_Boolean;
  @Parameter( label= "Output Byte", required = false, dataIOOrder = 2,
		  direction = Parameter.Direction.OUT,	description = "Dummy output parameter.")
  private Byte out_Byte;
  @Parameter( label= "Output Double", required = false, dataIOOrder = 3,
		  direction = Parameter.Direction.OUT,	description = "Dummy output parameter.")
  private Double out_Double;
  @Parameter( label= "Output Float", required = false, dataIOOrder = 4,
		  direction = Parameter.Direction.OUT,	description = "Dummy output parameter.")
  private Float out_Float;
  @Parameter( label= "Output Integer", required = false, dataIOOrder = 5,
		  direction = Parameter.Direction.OUT,	description = "Dummy output parameter.")
  private Integer out_Integer;
  @Parameter( label= "Output Long", required = false, dataIOOrder = 6,
		  direction = Parameter.Direction.OUT,	description = "Dummy output parameter.")
  private Long out_Long;
  @Parameter( label= "Output Short", required = false, dataIOOrder = 7,
		  direction = Parameter.Direction.OUT,	description = "Dummy output parameter.")
  private Short out_Short;
  @Parameter( label= "Output double", required = false, dataIOOrder = 8,
		  direction = Parameter.Direction.OUT,	description = "Dummy output parameter.")
  private double out_double;

  @Parameter( label= "Output Vector_Double", required = false, dataIOOrder = 9,
		  direction = Parameter.Direction.OUT,	description = "Dummyoutput parameter.")
  private Vector<Double> out_vectorDouble = null;

  @Parameter( label= "out_Vector_Byte", required = false, dataIOOrder = 10,
		  direction = Parameter.Direction.OUT,	description = "Dummy output parameter.")
  private Vector<Byte> out_vectorByte = null;

  @Parameter( label= "Output String_array", required = false, dataIOOrder = 15,
	  		direction = Parameter.Direction.OUT,	description = "Dummy parameter.")
	  private String [] out_array_String = null;
	  
	  @Parameter( label= "Output Integer_array", required = false, dataIOOrder = 16,
	  		direction = Parameter.Direction.OUT,	description = "Dummy parameter.")
	  private Integer [] out_array_Integer = null;

	  @Parameter( label= "Out Enum Selection", required = false, dataIOOrder = 19,
	  		direction = Parameter.Direction.OUT,	description = "Dummy parameter.")
	  private EnumSet<EnumSelection> outSelect = null;

	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public ALDParameterTester() throws ALDOperatorException {
		super();
	}

	@Override
	protected void operate() {
		System.out.println("Parameter settings:");
		for (String param : this.getParameterNames()) {
			try {
				String value = "null";
				if (this.getParameter(param) != null)
					value = this.getParameter(param).toString();
				System.out.println("- " + param + " = " + value);
			} catch (ALDOperatorException e) {
				System.err.println("=> Problems printing parameter " + param);
				continue;
			}
		}
		if (this.array_String != null) {
			System.out.println("Size of string array = " + this.array_String.length);
			for (int i=0;i<this.array_String.length;++i)
				System.out.println("- i = " + i + " -> " + this.array_String[i]);
		}
		else {
			System.out.println("String array not configured!");
		}
		if (this.array_String_2D != null) {
			System.out.println("2D string array:");
			for (int i=0;i<this.array_String_2D.length;++i) {
				for (int j=0;j<this.array_String_2D[i].length;++j) {
					System.out.println("- i = " + i + " , j = " + j + " -> " 
							+ this.array_String_2D[i][j]);
				}
			}
		}
		else {
			System.out.println("String array not configured!");
		}
		if (this.param_vectorDouble != null) {
			System.out.println("Size of double vector = " 
					+ this.param_vectorDouble.size());
			for (int i=0;i<this.param_vectorDouble.size();++i)
				System.out.println("- i = "+i+ " -> " +this.param_vectorDouble.get(i));
		}
		else {
			System.out.println("Parameter vector double not configured!");
		}
		
		System.out.println("Selected enum values:");
		for (EnumSelection e: this.eSelect) {
			System.out.println(e.toString());
		}
		
		this.out_Boolean = this.param_Boolean;
		this.out_Byte = this.param_Byte;
		this.out_Short = this.param_Short;
		this.out_Integer = this.param_Integer;
		this.out_Float = this.param_Float;
		this.out_Double = this.param_Double;
		this.out_Long = this.param_Long;
		this.out_double = this.param_double;
		this.out_vectorDouble = this.param_vectorDouble;
		this.out_vectorByte = this.param_vectorByte;
		this.out_array_String = this.array_String;
		this.out_array_Integer = this.array_Integer;
		this.outSelect = this.eSelect;
	}
	
	@Override
	public String getDocumentation() {
		return "This operator has a huge amount of input and output parameters of various types.\n" + 
				"<p>\n" + 
				"It basically copies the given input data values to the corresponding output \n" + 
				"parameter variables.\n" + 
				"<p>\n" + 
				"It is mainly intended to be used for testing the functionality of the \n" + 
				"different data I/O providers.\n";
	}
}
