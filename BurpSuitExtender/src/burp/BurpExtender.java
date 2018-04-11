package burp;

import java.awt.Component;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import turnitip.ui.*;
import turnitup.model.GathererMatcher;
import turnitup.model.Regexer;
import turnitup.utils.Fingerprint;
import turnitup.utils.Setting;
import turnitup.utils.Data;

//shift + end  ctrl + d
//ctrl + 1
/**
 * Gatherer
 * 
 * @author Turn it up 1)When Get a HTTP Resopne,put it in an queue 2)
 *         "block comment" first to match
 *
 */
public class BurpExtender implements IBurpExtender, IHttpListener, ITab {
    private IBurpExtenderCallbacks callbacks = null;
    private IExtensionHelpers extHelpers = null;
    private PrintWriter extPrint = null;

    private GathererUI gethererUI = null;

    /**
     * Set tab name
     */
    @Override
    public String getTabCaption() {
	return "Gatherer";
    }

    @Override
    public Component getUiComponent() {
	return gethererUI;
    }
    //<E>
    //ArrayList<GathererMatcher> MATCHER_LIST ;
    //AbstractTableModel uiTableModel ;
    //JTable matcherTable;
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
	// pass the callback object
	this.callbacks = callbacks;
	// register the http listener
	
	this.callbacks.setExtensionName("GathererExtender");
	this.extHelpers = this.callbacks.getHelpers();
	this.extPrint = new PrintWriter(this.callbacks.getStdout(), true);
	
	printBanner();
	initRegexs();
	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		// setUI();
		//matcherTable = new JTable(uiTableModel);
		gethererUI = new GathererUI(BurpExtender.this.callbacks);
		// gethererUI.setLayout((LayoutManager)null);
		//BurpExtender.this.callbacks.customizeUiComponent(gethererUI);
		BurpExtender.this.callbacks.addSuiteTab(BurpExtender.this);
		BurpExtender.this.callbacks.registerHttpListener(BurpExtender.this);
	    }
	});

    }

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
	if (Setting.isGATHERER_STATUS_RUNNING()
		&& (toolFlag == IBurpExtenderCallbacks.TOOL_PROXY || toolFlag == IBurpExtenderCallbacks.TOOL_SPIDER)
		&& messageIsRequest == false) {
	    GathererMatcher gathererMatcher = getRequestInfo(messageInfo.getRequest());
	    if (gathererMatcher==null)  return;
	    
	    boolean isOnlyOne = Data.addFingerprintSet(gathererMatcher.getHost()+gathererMatcher.geturiq()+gathererMatcher.getMethod());
	    if(!isOnlyOne) return;
	    
	    String responseBody = null;
	    try {
		responseBody = getResopnseBody(messageInfo.getResponse());
	    } catch (UnsupportedEncodingException e) {
		e.printStackTrace();
		return;
	    }
	    
	    for (Regexer regexer : Data.REGEXER_LIST) {
		regexMatchers(responseBody, regexer, getRequestInfo(messageInfo.getRequest()));
	    }
	    gethererUI.update();
	    gethererUI.setCountLabel(""+Data.getMatcherID2());
	  
	}
    }

    public String getResopnseBody(byte[] responseByte) throws UnsupportedEncodingException{
	  String responseBody=null;
	    //byte[] responseByte =messageInfo.getResponse();
	  IResponseInfo responseInfo = extHelpers.analyzeResponse(responseByte);
	  List<String> headerList = responseInfo.getHeaders();
	  String coding=null;
	  if(headerList!=null){
	  for (String string : headerList) {
	      if(string==null&&!"".equals(string)) continue;
	      if(string.length()>17 && string.substring(0, 14).equals("Content-Type: ")){
		 
		   coding = string.substring(string.lastIndexOf("=")+1);
//		  try{
//		  Pattern pattern = Pattern.compile("charset=(.*)");
//		  Matcher matcher = pattern.matcher(string);
//		  if(matcher.find()){
//		      //extPrint.println(string);
//		      //extPrint.println(matcher.group(1));
//		      coding = matcher.group(1);
//		  }
//		  }catch (Exception e){
//		      extPrint.println("eeeeeee");
//		  }
		  break;
	      }
	    }}
	    int bodyOffset = responseInfo.getBodyOffset();
	    byte[] bodyByte = new byte[responseByte.length-bodyOffset];
	    if(bodyByte.length<2) return null;
	    System.arraycopy(responseByte, bodyOffset, bodyByte, 0, bodyByte.length);
	    try {
		if(coding==null) coding = "UTF-8";
		responseBody = new String(bodyByte,coding);
	    } catch (UnsupportedEncodingException e) {
		responseBody = new String(bodyByte,"UTF-8");
		return "";
	    }
	    return responseBody;
	    //if (responseBody==null) return;
    	
    }
    
    /** filter file with those suffix
     */
    String[] filterFileSuffix = {"jpg","png"};
    public GathererMatcher getRequestInfo(byte[] requestByte) {
	//Matcher matcher = null;
	String host=null;
	String method=null;
	String uriq=null;
	String content = null;
	//BurpExtender.this.extHelpers.analyzeResponseKeywords(keywords, responses)
	try {
	    content = new String(requestByte, "utf-8");
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	Pattern pattern = Pattern.compile("(.*\r\n)*?\r\n");
	Matcher headersMatcher = pattern.matcher(content);
	String headers;
	if (!headersMatcher.find())
	    return null;
	headers = headersMatcher.group();
	String[] headerArray = headers.split("\r\n");
	boolean first_line = true;
	for (String string : headerArray) {
	    if (first_line) {
		first_line = false;
		String[] strA = string.split("\\s");
		method = strA[0];
		uriq = strA[1];
	    } else {
		if (string.substring(0, 5).equals("Host:")) {
		    host = string.substring(6);
		}
	    }
	}
	//extPrint.println("host"+host);
	//extPrint.println("method"+method);
	//extPrint.println("uriq"+uriq);
	if (host==null||method==null||uriq==null) return null;
	
	int commonIndex = uriq.lastIndexOf(".");
	if(commonIndex!=-1 ){
	    String fileSuffix = uriq.substring(commonIndex);
	    if (!fileSuffix.contains("/")){
		for (String string : filterFileSuffix) {
		    if(fileSuffix.contains(string)) return null;
		}
	    }
	}
	
	return new GathererMatcher(host, uriq, method);
    }

    // private String[] regexs={
    // // /**/
    // "/\\*(.|\r\n|\n)*?\\*/"
    // // <!-- -->
    // ,"<!--(.|\r\n|\n)*?>"
    // // //
    // ,"[\\s|](//.{2,})"
    // // e-mail
    // ,"\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*"
    // };

    

    // 后面改成从XML文档读取
    public void initRegexs() {
	int regexsNum = 4;
	Regexer regexer_1 = new Regexer("/\\*(.|\r\n|\n)*?\\*/", "COMMENT", Fingerprint.FP_DEFAULT);
	Data.REGEXER_LIST.add(regexer_1);
	Regexer regexer_2 = new Regexer("[\\s|](//.{2,})", "COMMENT", Fingerprint.FP_DEFAULT);
	Data.REGEXER_LIST.add(regexer_2);
	Regexer regexer_3 = new Regexer("<!--(.|\r\n|\n)*?>", "COMMENT", Fingerprint.FP_DEFAULT);
	Data.REGEXER_LIST.add(regexer_3);
	Regexer regexer_4 = new Regexer("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*", "e-mail", Fingerprint.FP_DEFAULT);
	Data.REGEXER_LIST.add(regexer_4);
    }
   
  
    /**
     * 根据正则提权出字符串 并组成列表
     * 
     * @param content
     * @param regexs
     * @return 
     * @return List<String>
     */
    public void regexMatchers(String content,Regexer regexer,GathererMatcher gathererMatcher) {
	    Pattern pattern = Pattern.compile(regexer.getRegex());
	    Matcher matcher = pattern.matcher(content);
	    //List<GathererMatcher> list = new ArrayList<GathererMatcher>();
	    while (matcher.find()) {
		//synchronized (Setting.SYNCHRONIZED) {
		GathererMatcher g = new GathererMatcher(gathererMatcher);
			g.setMatch(matcher.group());
			g.setId(Data.getMatcherID());
			g.setType(regexer.getType());
			if(!Data.addToMatcherList(g)){
			   extPrint.println("add result false!!!");}
	}
    }

    /**
     * Print banner in Extender UI
     */
    public void printBanner() {
	
    }

    public void getRequestFingerprint(byte[] request) {
	String elementHost = "";
	String elementMethodURIEtc = "";
	IRequestInfo requestInfo = this.extHelpers.analyzeRequest(request);
	List<String> headers = requestInfo.getHeaders();
	for (String string : headers) {
	    if (string.substring(0, 1) == "Host") {

	    }

	    // this.extPrint.println(string);
	}
    }

}
