package util;

/**
 * Assists in conversion between String and boolean array.
 * 
 * @author J2579
 */
public class SBAssist {
	
	/**
	 * Converts a boolean array into a string literal.
	 * The string begins and ends with "[" and "]", respectively, with
	 * commas between every pair of values.
	 * 
	 * e.g. [true,false,true,true]
	 * @param b Initial boolean array.
	 * @return Converted string.
	 */
	public static String btoa(boolean[] b) {
		String s = "[";
		for(int idx = 0; idx < b.length; ++idx) {
			s += b[idx];
			s += (idx == b.length-1 ? "]" : ",");
		}
		return s;
	}	
	
	/**
	* Parse boolean array from String of form:
	* [bool,bool,bool]
	* 
	* @param a The string to parse
	* @throws IllegalArgumentException If: a is null, a is empty, a does not begin and end with "[" and "]", respectively, the array values are not 'true' or 'false' literal
	* @return The parsed boolean array
	*/
	public static boolean[] atob(String a) {
		if(a == null || a.isEmpty()) //string exists
			throw new IllegalArgumentException("DNE");;
		
		if(a.charAt(0) != '[' || a.charAt(a.length()-1) != ']')
			throw new IllegalArgumentException("BAD HEADER OR FOOTER");
		
		String[] raw = a.substring(1,a.length()-1).split(",");
		boolean[] cooked = new boolean[raw.length];

		for(int idx = 0; idx < raw.length; ++idx) {
			try {
				cooked[idx] = parseBoolean(raw[idx]);
			}
			catch(IllegalArgumentException e) {
				throw new IllegalArgumentException("IDX " + idx + ": " + e.getMessage());
			}
		}
		return cooked;
	}
	
	/**
	 * Parses a boolean value from a string. Value must
	 * be "true" or "false", literal.
	 * 
	 * @param s The string to parse from
	 * @throws IllegalArgumentException If !(s.equals("true") || s.equals("false"))
	 * @return The parsed boolean value
	 */
	public static boolean parseBoolean(String s) {
		if(s.equals("false"))
			return false;
		else if(s.equals("true"))
			return true;
		throw new IllegalArgumentException("BAD VALUE");
	}
	
}