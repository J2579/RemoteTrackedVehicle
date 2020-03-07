package util;

public class SBTest {
	
	public static void main(String[] args) {
		
		boolean[] list = {true, true, false, false, true};
		String string = SBAssist.btoa(list);
		String expected1 = "[true,true,false,false,true]";
		
		System.out.println(string.equals(expected1) ? "PASSED" : "FAILED");
		
		try {
			boolean[] b = SBAssist.atob(null);
			System.out.println(SBAssist.btoa(b));
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
		
		try {
			boolean[] b = SBAssist.atob("");
			System.out.println(SBAssist.btoa(b));
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
		
		try {
			boolean[] b = SBAssist.atob("[true,true,false,false,true");
			System.out.println(SBAssist.btoa(b));
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
		
		try {
			boolean[] b = SBAssist.atob("true,true,false,false,true]");
			System.out.println(SBAssist.btoa(b));
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
		
		try {
			boolean[] b = SBAssist.atob("[true,banana,false,false,true]");
			System.out.println(SBAssist.btoa(b));
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
		
		try {
			boolean[] b = SBAssist.atob("[true,true,false,pear,true]");
			System.out.println(SBAssist.btoa(b));
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
	}
}