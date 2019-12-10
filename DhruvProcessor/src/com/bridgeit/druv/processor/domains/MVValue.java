package com.bridgeit.druv.processor.domains;

public class MVValue {
	double freq,valueMV;
	public double getFreq() {
		return freq;
	}
	public void setFreq(double freq) {
		this.freq = freq;
	}
	public double getValueMV() {
		return valueMV;
	}
	public void setValueMV(double valueMV) {
		this.valueMV = valueMV;
	}
	public boolean isSignMV() {
		return signMV;
	}
	public void setSignMV(boolean signMV) {
		this.signMV = signMV;
	}
	boolean  signMV;
	
}
