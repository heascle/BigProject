package turnitup.model;

import turnitup.utils.Fingerprint;

public class Regexer {

    private String regex = null;

    private String type = null;

    private Boolean enabled = true;
    private int fingerprint = Fingerprint.FP_DEFAULT;

    public Regexer(String regex, String type, int fingerprint) {
	// fingerprint.g
	this.regex = regex;
	this.type = type;
	this.fingerprint = fingerprint;
    }
    public Regexer(String regex, String type) {
	this.regex = regex;
	this.type = type;
	
    }
    public String getRegex() {
	return regex;
    }

    public void setRegex(String regex) {
	this.regex = regex;
    }

    public String getType() {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    public int getFingerprint() {
	return fingerprint;
    }

    public void setFingerprint(int fingerprint) {
	this.fingerprint = fingerprint;
    }
    public Boolean getEnabled() {
        return enabled;
    }
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
