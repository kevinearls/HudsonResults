package com.kevinearls.hudson;

import generated.hudson.build.ActionsType;
import generated.hudson.build.HudsonTasksJunitTestResultActionType;
import generated.hudson.build.MatrixRunType;
import org.apache.commons.io.FileUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create a summary of result of the Platform builds on Hudson in a single html page
 * 
 * @author kearls
 *
 */
public class SummarizeBuildResults {

    private static final String passedTdOpenTag = "<td style=\"background-color: #2E8B57;\">";
    private static final String failedTestsTdOpenTag = "<td style=\"background-color: #ffd700;\">";
    private static final String failedBuildTdOpenTag =  "<td style=\"background-color: #dc143c;\">";
    private static final String tdCloseTag = "</td>";
    public static final String NEW_LINE = "\n";

    // RE to select which test directories we want results from.
    private static final String ACCEPT_STRING_RH_6_1 = ".*6[-\\.]1.*platform";

    /**
     *
     */
    private static Unmarshaller unmarshaller = null;
	static {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(MatrixRunType.class);
			unmarshaller = jaxbContext.createUnmarshaller();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Use JAXB to create a MatrixRunType object from the file
	 * 
	 * @param buildFileName
	 * @return
	 * @throws JAXBException
	 */
	private MatrixRunType getTestSuiteFromFile(String buildFileName) throws JAXBException {
		File buildResultsFile = new File(buildFileName);
		StreamSource source = new StreamSource(buildResultsFile);
		JAXBElement<MatrixRunType> root = unmarshaller.unmarshal(source, MatrixRunType.class); 
		return root.getValue(); 
	}


	/**
	 * Return the latest build in a given build directory
	 * 
	 * @param targetDirectory Something like: cxf-2.6.0.fuse-7-1-x-stable-platform/configurations/axis-jdk/jdk6/axis-label/ubuntu/builds/
	 * @return cxf-2.6.0.fuse-7-1-x-stable-platform/configurations/axis-jdk/jdk6/axis-label/ubuntu/builds/2012-11-02_21-09-35
	 */
    private File getLatestBuildDirectory(File targetDirectory) throws IOException {
        if (targetDirectory != null && targetDirectory.listFiles() != null) {
            List<File> fud = Arrays.asList(targetDirectory.listFiles());

            // Remove symlinks, which are just numbered.  We want to be able to use the date.
            List<File> contents = new ArrayList<File>();
            for (File f : fud) {
                if (!FileUtils.isSymlink(f)) {
                    contents.add(f);
                }
            }

            Collections.sort(contents, new BuildDirectoryComparator());
            Collections.reverse(contents);

            if (!contents.isEmpty()) {
                return contents.get(0);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


	/**
	 * Return a list of platform test result directories matching directoryMatchExpression
	 * 
	 * @param root this should be the root of the Hudson jobs directory
     * @param directoryMatchExpression regular expression for // TODO explain and update everywhere.
	 */
	private List<File> getPlatformDirectories(File root, String directoryMatchExpression) {
		PlatformDirectoryFilter pdf = new PlatformDirectoryFilter(directoryMatchExpression);
        File[] files = root.listFiles(pdf);
		List<File> directories = Arrays.asList(files);
        Collections.sort(directories);
		
		return directories;
	}

    /**
     * Write the report to a file.
     *
     * @param root
     * @throws JAXBException
     * @throws IOException
     */
    public void createHTMLSummary(File root, String directoryMatchExpression) throws JAXBException, IOException {
        FileWriter writer = getResultFileWriter();
        writer.write("<html>" + NEW_LINE);
        writer.write("<body>" + NEW_LINE);
        String style = "<style><!--\n" +
                "table { border-collapse: collapse; font-family: Futura, Arial, sans-serif; } caption { font-size: larger; margin: 1em auto; } th, td { padding: .65em; } th, thead { background: #000; color: #fff; border: 1px solid #000; } td { border: 1px solid #777; }\n" +
                "--></style>";
        writer.write(style + NEW_LINE);
        writer.write("<table border=\"1\">"  + NEW_LINE);
        writer.write("<caption>JBoss Fuse 6.1 Platform Test Results as of " + new Date().toString() + "</caption>" + NEW_LINE);
        printHtmlHeaders(writer);

        Map<String, List<BuildResult>> allResults = getAllResults(root, directoryMatchExpression);

        // Write one row for each project
        List<String> projectNames = new ArrayList<String>(allResults.keySet());
        Collections.sort(projectNames);
        for (String projectName : projectNames) {
            writer.write("    <tr>");
            List<BuildResult> buildResults = allResults.get(projectName);
            Collections.reverse(buildResults);
            Collections.sort(buildResults, new BuildResultComparator());

            writer.write("<td>" + projectName + "</td>");
            for (PLATFORM platform : PLATFORM.values()) {
                for (JDK jdk : JDK.values()) {
                    for (BuildResult br: buildResults) {
                        if (platform.equals(br.getPlatform()) && jdk.equals(br.getJdk()) && !(jdk.equals(JDK.jdk6) && platform.equals(PLATFORM.rhel))) {


                            // Link needs to be something like:
                            // http://ci.fusesource.com/hudson/view/JBoss%206.1/job/activemq-5.9.0.redhat-6-1-x-stable-platform/11/jdk=jdk7,label=ubuntu/
                            // starting from http://localhost:8080/job/BuildResults/HTML_Report/ or
                            String testResult = br.getFailedTests() + "/" + br.getTestsRun()     // TODO turn into a link back to test result?
                                    + "<br/><small>(" + br.getFormattedDuration() + ")</small>";    // TODO this needs to be smaller.
                            if (br.getResult().equalsIgnoreCase("success")) {
                                writer.write(passedTdOpenTag + testResult + tdCloseTag);
                            } else if (br.getTestsRun().equals(0)) {
                                writer.write(failedBuildTdOpenTag + testResult + tdCloseTag);
                            } else {
                                writer.write(failedTestsTdOpenTag + testResult + tdCloseTag);
                            }
                        }
                    }

                }
            }
            writer.write("</tr> " + NEW_LINE);
        }

        writer.write("<table>" + NEW_LINE);
        writer.write("<br/>" + NEW_LINE);
        writer.write("<p>Red cells indicate build failures, yellow cells indicate builds with test failures, green cells indicate successful builds.  ");
        writer.write("Cell results N/M show N test failures out of M tests run</p>" + NEW_LINE);
        writer.write("<p></p>" + NEW_LINE);
        writer.write("</body>" + NEW_LINE);
        writer.write("</html>" + NEW_LINE);
        writer.close();
    }

    /**
     *
     * @return
     * @throws IOException
     */
    private FileWriter getResultFileWriter() throws IOException {
        File resultsDir = new File("results");
        resultsDir.mkdir();
        String resultsFileName = "results/results.html";
        return new FileWriter(resultsFileName);
    }

    /**
     * Print the headers for the HTML summary
     */
    private void printHtmlHeaders(FileWriter writer) throws IOException {
        // Print headers
        writer.write("<thead>");
        writer.write("<tr>");
        writer.write("<td>Platform</td>");
        for (PLATFORM platform : PLATFORM.values()) {
            for (JDK jdk : JDK.values()) {
                if (!(jdk.equals(JDK.jdk6) && platform.equals(PLATFORM.rhel))) {
                    writer.write("<td>" + platform + " " + jdk + "</td>");
                }
            }
        }
        writer.write("</tr>");
        writer.write("</thead>" + NEW_LINE);
    }

    /**
     * TODO summarize; exactly what does this return?
     *
     * @param root
     * @return
     */
    private Map<String, List<BuildResult>> getAllResults(File root, String directoryMatchExpression) throws IOException{
        Map<String, List<BuildResult>> allResults = new HashMap<>();
        List<File>platformDirectories = getPlatformDirectories(root, directoryMatchExpression);
        for (File platformDirectory : platformDirectories) {
            for (PLATFORM platform : PLATFORM.values()) {
                for (JDK jdk : JDK.values()) {
                    String targetDirectoryName = platformDirectory.getAbsolutePath() + "/configurations/axis-jdk/" + jdk + "/axis-label/" + platform + "/builds/";
                    File latestBuildDirectory = getLatestBuildDirectory(new File(targetDirectoryName));
                    if (latestBuildDirectory != null) {
                        try {
                            String buildDateTime = latestBuildDirectory.getName(); 	// directory name of the build is date time in the format 2012-11-02_21-09-35
                            String latestBuildFileName = latestBuildDirectory.getAbsolutePath() + "/build.xml";
                            MatrixRunType mrt = getTestSuiteFromFile(latestBuildFileName);
                            ActionsType actions = mrt.getActions();
                            HudsonTasksJunitTestResultActionType junitResults = actions.getHudsonTasksJunitTestResultAction();

                            BuildResult buildResult;
                            if (junitResults != null) {
                                buildResult = new BuildResult(platformDirectory.getName(),  buildDateTime, jdk, platform, mrt.getResult(), junitResults.getTotalCount(), junitResults.getFailCount(), mrt.getDuration());
                            } else {
                                buildResult = new BuildResult(platformDirectory.getName(),  buildDateTime, jdk, platform, mrt.getResult(), 0, 0, 0);
                            }
                            // TODO need to store by platformDirectory.getName() (which is projectname) jdk, platform
                            List<BuildResult> platformResults = allResults.get(platformDirectory.getName());
                            if (platformResults == null) {
                                platformResults = new ArrayList<>();
                                allResults.put(platformDirectory.getName(), platformResults);
                            }
                            platformResults.add(buildResult);
                        } catch(Exception e) {
                            // TODO this could occur if the build is still running.
                            System.err.println("************ Exception " + e.getMessage() + " on " + latestBuildDirectory.getAbsolutePath());
                        }

                    }
                }

            }
        }
        return allResults;
    }


	/**
	 * @param args
	 * @throws JAXBException 
	 */
	public static void main(String[] args) throws JAXBException, IOException {
		String testRoot ="/mnt/hudson/jobs";
        String directoryMatchString = ACCEPT_STRING_RH_6_1;

		if (args.length > 0) {
            testRoot = args[0];
            if (args.length > 1) {
                directoryMatchString = args[1];
            }
		} 

		System.out.println("Starting at " + testRoot + " matchings on [" + directoryMatchString + "]");
		SummarizeBuildResults me = new SummarizeBuildResults();
		File theRoot = new File(testRoot);
        me.createHTMLSummary(theRoot, directoryMatchString);
	}
}


/**
 * Filter to select directories which contain desired results
 *
 * @author kearls
 *
 */
class PlatformDirectoryFilter implements FileFilter {
    final String DEFAULT_MATCH_STRING = "/*7[-\\.]1.*platform"; // TODO should this be removed?
    String matchRegularExpression = "";

    public PlatformDirectoryFilter() {
        this.matchRegularExpression = DEFAULT_MATCH_STRING;
    }

    public PlatformDirectoryFilter(String target) {
        this.matchRegularExpression = target;
    }

    @Override
	public boolean accept(File pathname) {
		String name = pathname.getName();
        if (name.matches(matchRegularExpression)) {
            return true;
		} else {
	        return false;
		}
	}
}


/**
 * Comparator to help find the newest subdirectory in the builds directory
 * @author kearls
 *
 */
class BuildDirectoryComparator implements Comparator<File> {
    @Override
	public int compare(File first, File second) {
		if (first.lastModified() > second.lastModified()) {
			return 1;
		} else {
			return -1;
		}
	}
}


/**
 * Comparator to sort a list of builds results on name, platform, and jdk
 * @author kearls
 *
 */
class BuildResultComparator implements Comparator<BuildResult> {
	@Override
	public int compare(BuildResult b1, BuildResult b2) {
		int nameValue = b1.getName().compareTo(b2.getName());
		if (nameValue != 0) {
			return nameValue;
		} else {
			// TODO sort on date too?
			int platformValue = b1.getPlatform().compareTo(b2.getPlatform());
			if (platformValue != 0) {
				return platformValue;
			} else {
				return b1.getJdk().compareTo(b2.getJdk());
			}
		}
	}
	
}