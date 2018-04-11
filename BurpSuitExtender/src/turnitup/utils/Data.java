package turnitup.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import turnitup.model.GathererMatcher;
import turnitup.model.Regexer;

public class Data {

    /**
     * matcher's id
     */
    private static int MATCHERER_ID = 0;

    public  static  int getMatcherID() {
	 MATCHERER_ID+=1;
	 return MATCHERER_ID;
	//return 1;
    }

    public  static  int getMatcherID2() {
	 return MATCHERER_ID;
   }
    
    
    /**
     * Regexer list
     */
    public static List<Regexer> REGEXER_LIST = new ArrayList<Regexer>();;
    
    /**
     * match items
     */
    public static List<GathererMatcher> MATCHER_LIST = new LinkedList<GathererMatcher>();
    public static boolean addToMatcherList(GathererMatcher gathererMatcher){
	
	if(gathererMatcher.getHost()==null||gathererMatcher.getId()<=0
		||gathererMatcher.getMatch()==null||gathererMatcher.getMethod()==null
		||gathererMatcher.getType()==null||gathererMatcher.geturiq()==null)
	    return false;
	MATCHER_LIST.add(gathererMatcher);
	return true;
	//Setting.uiTableModel.fireTableRowsInserted(0, 0);
	//Setting.uiTableModel.fireTableDataChanged();
	//return rb;
    }
    
    private static Set<String> FINGERPRINT_SET = new HashSet<String>();
    public static boolean addFingerprintSet(String str){
	try {
	    MessageDigest md5 = MessageDigest.getInstance("MD5");
	    byte[] bytes = md5.digest(str.getBytes());
	    System.out.println(bytes.toString());
	    return FINGERPRINT_SET.add(toHex(bytes));
	    //System.out.println(toHex(bytes));
	} catch (NoSuchAlgorithmException e) {
	    e.printStackTrace();
	}
	return false;
    }
    
    private static String toHex(byte[] bytes) {

	    final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
	    StringBuilder ret = new StringBuilder(bytes.length * 2);
	    for (int i=0; i<bytes.length; i++) {
	        ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
	        ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
	    }
	    return ret.toString();
	}
}
