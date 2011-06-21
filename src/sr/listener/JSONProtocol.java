package sr.listener;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONProtocol {
	
	 public String processInput(String theInput) {
		 JSONObject jsobj = null;
		 try {
			jsobj = new JSONObject(theInput);

		 } catch (JSONException e) {
			e.printStackTrace();
			return "ERROR: Not a JSON string";
		 }
		return theInput;
	 }
}
