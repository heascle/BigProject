package turnitup.model;

import java.io.ObjectInputStream.GetField;

public class GathererMatcher {
    private int id=0;
    private String type=null;
    private String host=null;
    private String uriq=null;
    private String method=null;
    private String match=null;

    public String getStr(){
	return "id:"+this.id+"_type:"+this.type+"_match:"+this.match;
    }
    
    public GathererMatcher(int id, String type, String host, String uriq, String method, String match) {
	this.id = id;
	this.type = type;
	this.host = host;
	this.uriq = uriq;
	this.method = method;
	this.match = match;
    }
    
    //public getByteCount()
    
    public GathererMatcher(GathererMatcher gathererMatcher) {
	this.id = gathererMatcher.id;
	this.type = gathererMatcher.type;
	this.host = gathererMatcher.host;
	this.uriq = gathererMatcher.uriq;
	this.method = gathererMatcher.method;
	this.match = gathererMatcher.match;
    }
    public GathererMatcher(String host, String uriq, String method) {
 	this.host = host;
 	this.uriq = uriq;
 	this.method = method;
 	
     }
     

    public int getId() {
	return id;
    }

    public String getType() {
	return type;
    }

    public String getHost() {
	return host;
    }

    public String geturiq() {
	return uriq;
    }

    public String getMethod() {
	return method;
    }

    public String getMatch() {
	return match;
    }

    public void setId(int id) {
	this.id = id;
    }

    public void setType(String type) {
	this.type = type;
    }

    public void setHost(String host) {
	this.host = host;
    }

    public void seturiq(String uriq) {
	this.uriq = uriq;
    }

    public void setMethod(String method) {
	this.method = method;
    }

    public void setMatch(String match) {
	this.match = match;
    }

}
