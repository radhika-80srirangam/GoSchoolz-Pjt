package com.iskool.event;

import com.iskool.entity.SendEmailDetails;

public class OtpEvent extends SendEmailDetails {

	private static final long serialVersionUID = 1L;

	private String otp;

	public OtpEvent(Object source, String otp) {
		super(source);
		this.otp = otp;

	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

}
