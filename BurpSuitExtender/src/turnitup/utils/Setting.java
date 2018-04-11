package turnitup.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;
import turnitup.model.*;
import turnitup.model.GathererMatcher;
import turnitup.model.Regexer;
public class Setting {
    
    public static final String SYNCHRONIZED = "SYNCHRONIZED";
    /**
     * gatherer's status : running/pausing
     */
    private static boolean GATHERER_STATUS_RUNNING = true;

    /**
     * need to reload the regex or not
     */
    public static boolean REFLASH_REGEXS = false;
   

    public static boolean isGATHERER_STATUS_RUNNING() {
        return GATHERER_STATUS_RUNNING;
    }

    public static void setGATHERER_STATUS_RUNNING(boolean gATHERER_STATUS_RUNNING) {
        GATHERER_STATUS_RUNNING = gATHERER_STATUS_RUNNING;
    }
    
}
