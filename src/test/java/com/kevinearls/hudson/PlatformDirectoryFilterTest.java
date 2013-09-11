package com.kevinearls.hudson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: kearls
 * Date: 9/11/13
 * Time: 9:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class PlatformDirectoryFilterTest {
    protected static final Logger LOG = LoggerFactory.getLogger(PlatformDirectoryFilterTest.class);
    private static final String ACCEPT_STRING_7_1 = ".*7[-\\.]1.*platform";     // note: watch out for shipped and stable versions
    private static final String ACCEPT_STRING_7_2 = ".*7[-\\.]2.*platform";
    private static final String ACCEPT_STRING_RH_6_1 = ".*6[-\\.]1.*platform";

    /**
     * Load list of test file names
     * @return
     * @throws Exception
     */
    private List<String> loadTestFileNames() throws Exception{
        URL url = this.getClass().getResource("/testDirectoryNames.txt");
        File testDirectoryNamesFile = new File(url.getFile());
        FileInputStream fisTargetFile = new FileInputStream(testDirectoryNamesFile);
        List<String> testFilesNames = IOUtils.readLines(fisTargetFile);
        return testFilesNames;
    }

    @Test
    public void test61() throws Exception {      // TODO rename
        List<String> testDirectoryNames = loadTestFileNames();
        System.out.println(">>>> Got " + testDirectoryNames.size() + " file names");

        int acceptedCount = 0;
        PlatformDirectoryFilter pdf = new PlatformDirectoryFilter(ACCEPT_STRING_RH_6_1);
        for (String testDirectoryName : testDirectoryNames) {
            File testDirectory = new File(testDirectoryName);
            if (pdf.accept(testDirectory))  {
                //System.out.println(">>>> Accepted " + testDirectoryName);
                acceptedCount++;
            }
        }

        System.out.println(">>>> Accepted " + acceptedCount);
        assertEquals(23, acceptedCount);
    }


    @Test
    public void testAccept() {
        PlatformDirectoryFilter pdf = new PlatformDirectoryFilter(ACCEPT_STRING_7_2);
        String[] directoryNames = {
            "archetypes-2012.01.0.fuse-7-2-x-stable-platform",
            "aries-jmx-1.0.1.fuse-7-2-x-stable-platform",
            "aries-util-1.0.1.fuse-7-2-x-stable-platform",
            "felix-framework-7.2.x.fuse-stable-platform",
            "smx4-specs-2.0.0.fuse-7-2-x-stable-platform"
        };

        List<String> testDirectoryNames = Arrays.asList(directoryNames);
        for (String testDirectoryName : testDirectoryNames) {
            LOG.info(">>>> Accept: " + testDirectoryName);
            System.out.println(">>>> Accept? " + testDirectoryName + "?");
            File testDirectory = new File(testDirectoryName);
            boolean accepted = pdf.accept(testDirectory);
            assertTrue(accepted);
        }
    }

    @Test
    public void testReject() {
        PlatformDirectoryFilter pdf = new PlatformDirectoryFilter(ACCEPT_STRING_7_2);
        String[] directoryNames = {
                "activemq-5.7.0.fuse-7-1-x-stable-platform",
                "archetypes-2012.01.0.fuse-7-1-x-stable-platform",
                "aries-transaction-1.0.0.fuse-7-1-x-stable-platform",
                "aries-blueprint-1.0.0.fuse-7-1-x-stable-platform",
                "eraseMe.txt"
        };
        List<String> testDirectoryNames = Arrays.asList(directoryNames);
        for (String directoryName : testDirectoryNames) {
            assertFalse(pdf.accept(new File(directoryName)));
        }
    }
}
