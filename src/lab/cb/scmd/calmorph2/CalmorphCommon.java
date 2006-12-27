package lab.cb.scmd.calmorph2;

public class CalmorphCommon {
	
	public static void errorExit(String method_name, String error_reason) {
		System.err.println("Method : " + method_name);
		System.err.println("Reason : " + error_reason);
		System.exit(1);
	}
	
}
