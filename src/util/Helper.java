package util;

public class Helper {

	public static String printBoolArr(boolean arr[]) {
		
		String s = "{";
		
		for(int idx = 0; idx < arr.length; ++idx) {
			if(idx != arr.length - 1)
				s += (arr[idx] + ", ");
			else
				s += (arr[idx] + "}");
		}
		
		s += "\n";
		
		return s;
	}
	
}
