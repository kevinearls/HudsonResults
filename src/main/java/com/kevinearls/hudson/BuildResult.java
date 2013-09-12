package com.kevinearls.hudson;

public class BuildResult {
	private String name;
	private String runDate;
	private String result;		// from MatrixRunType.getResult..I think this includes SUCCESS, FAILURE.
	private Integer testsRun;
	private Integer failedTests;
    private Integer duration;
	private JDK jdk;
	private PLATFORM platform;	// i.e. Ubuntu, Windows, AIX
	
	
	
	/**
	 * 
	 * @param name
	 * @param runDate
	 * @param jdk
	 * @param platform
	 * @param result
	 * @param testsRun
	 * @param testsFailed
	 */
	public BuildResult(String name, String runDate, JDK jdk, PLATFORM platform, String result, int testsRun, int testsFailed, int duration) {
		this.name = name;
		this.runDate = runDate;
		this.jdk = jdk;
		this.platform = platform;
		this.result = result;
		this.testsRun = testsRun;
		this.failedTests = testsFailed;
        this.duration = duration;
	}
	
	
	/**
	 *
	 */
	@Override
	public String toString() {
		String s = name + "," + runDate + ", " + jdk + ", " + platform + ", " + getResult() + ", Tests run, " + testsRun + ", Failed ," + failedTests + " ,duration, " + getFormattedDuration();
		return s;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRunDate() {
		return runDate;
	}
	public void setRunDate(String runDate) {
		this.runDate = runDate;
	}
	public String getResult() {
        if (result.equals("SUCCESS")) {
            return result.toLowerCase();
        } else {
		    return result;
        }
	}
	public void setResult(String result) {
		this.result = result;
	}
	public Integer getTestsRun() {
		return testsRun;
	}
	public void setTestsRun(Integer testsRun) {
		this.testsRun = testsRun;
	}
	public Integer getFailedTests() {
		return failedTests;
	}
	public void setFailedTests(Integer failedTests) {
		this.failedTests = failedTests;
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

    public Integer getDuration() {
        return this.duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    /**
     *
     * @return
     */
    public String getFormattedDuration() {
        int hours = (int) (duration / (60 * 60 * 1000));
        int minutes = (int) (duration / (60 * 1000)) % 60;
        int seconds = (int) (duration / 1000) % 60;
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }
	
	

	

}
