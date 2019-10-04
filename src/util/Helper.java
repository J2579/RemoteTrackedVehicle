package util;

public class Helper {

	public static void printBoolArr(boolean arr[]) {
		
		System.out.print("{");
		
		for(int idx = 0; idx < arr.length; ++idx) {
			if(idx != arr.length - 1)
				System.out.print(arr[idx] + ", ");
			else
				System.out.print(arr[idx] + "}");
		}
		System.out.println();
	}
	
}
