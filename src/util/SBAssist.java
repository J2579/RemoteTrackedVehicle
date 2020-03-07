package util;

public class SBAssist {
	
	public static String btoa(boolean[] b) {
		String s = "[";
		for(int idx = 0; idx < b.length; ++idx) {
			s += b[idx];
			s += (idx == b.length-1 ? "]" : ",");
		}
		return s;
	}	
	
	/**
	* Parse boolean array from String. Return null on fail.
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
	
	public static boolean parseBoolean(String s) {
		if(s.equals("false"))
			return false;
		else if(s.equals("true"))
			return true;
		throw new IllegalArgumentException("BAD VALUE");
	}
	
}