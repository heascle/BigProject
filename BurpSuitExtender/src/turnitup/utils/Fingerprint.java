package turnitup.utils;

public class Fingerprint {
    // FP_DEFAULT,FP_1,FP_2;

    /**
     * HOST + URI + REQUEST METHOD
     */
    public final static int FP_DEFAULT = 0x0000;
    /**
     * HOST + URI + REQUEST METHOD + REQUEST BODY
     */
    public final int FP_1 = 0x0001;
    /**
     * HOST + URI
     */

}
