package org.ofbiz.msaccomanagement;

import org.ofbiz.entity.GenericValue;

public class MSaccoStatus {
	
	private String status;
	private GenericValue msaccoApplication;
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public GenericValue getMsaccoApplication() {
		return msaccoApplication;
	}
	public void setMsaccoApplication(GenericValue msaccoApplication) {
		this.msaccoApplication = msaccoApplication;
	}
	
	

}
