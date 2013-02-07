package com.kevinearls.hudson;

public class JdkPlatformTuple {
	
	private JDK jdk;
	private PLATFORM platform;
	
	public JdkPlatformTuple(JDK jdk, PLATFORM platform) {
		this.jdk = jdk;
		this.platform = platform;
	}
	
	public JDK getJdk() {
		return jdk;
	}
	public void setJdk(JDK jdk) {
		this.jdk = jdk;
	}
	public PLATFORM getPlatform() {
		return platform;
	}
	public void setPlatform(PLATFORM platform) {
		this.platform = platform;
	}
	

	

}
