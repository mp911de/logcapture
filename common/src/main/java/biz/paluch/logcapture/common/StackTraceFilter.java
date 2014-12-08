package biz.paluch.logcapture.common;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Filtering Facility for Stack-Traces. This is to shorten very long Traces. It leads to a very short Trace containing only the
 * interesting parts.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class StackTraceFilter {

    private static final Logger LOG = Logger.getLogger(StackTraceFilter.class);
    private static final String INDENT = "\t";
    private static final String FILTER_SETTINGS = "/" + StackTraceFilter.class.getSimpleName() + ".packages";

    /**
     * List of Surpressed Packages.
     */
    private static Set<String> suppressedPackages;

    static {

        InputStream is = null;
        try {
            is = getStream();
            if (is == null) {
                LOG.info("No " + FILTER_SETTINGS + " resource present, using defaults");
                suppressedPackages = new HashSet<String>(getDefaults());
            } else {
                Properties p = new Properties();
                p.load(is);
                suppressedPackages = (Set) p.keySet();
            }

        } catch (IOException e) {
            LOG.info("Could not parse " + FILTER_SETTINGS + " resource, using defaults");
            suppressedPackages = new HashSet<String>(getDefaults());

            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {

                }
            }
        }

    }

    private static InputStream getStream() {

        Thread thread = Thread.currentThread();
        InputStream is = StackTraceFilter.class.getResourceAsStream(FILTER_SETTINGS);
        if (is == null && thread.getContextClassLoader() != null) {
            is = thread.getContextClassLoader().getResourceAsStream(FILTER_SETTINGS);
        }
        return is;
    }

    public static List<String> getDefaults() {

        return Arrays.asList("org.h2", "org.apache.catalina", "org.apache.coyote", "org.apache.tomcat", "com.arjuna",
                "org.apache.cxf", "org.hibernate", "org.junit", "org.jboss", "java.lang.reflect.Method", "sun.", "com.sun",
                "org.eclipse", "junit.framework", "com.sun.faces", "javax.faces", "org.richfaces", "org.apache.el",
                "javax.servlet");
    }

    /**
     * Utility-Constructor.
     */
    private StackTraceFilter() {

    }

    /**
     * Filter Stack-Trace
     * 
     * @param t
     * @return String containing the Stack-Trace.
     */
    public static String getFilteredStackTrace(Throwable t) {
        return getFilteredStackTrace(t, true);
    }

    /**
     * Filter Stack-Trace
     * 
     * @param t
     * @param shouldFilter
     * @return String containing the Stack-Trace.
     */
    public static String getFilteredStackTrace(Throwable t, boolean shouldFilter) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            writeCleanStackTrace(t, pw, shouldFilter);
            return sw.getBuffer().toString();
        } catch (Exception e) {
            LOG.error("Error filtering StackTrace: " + e.getMessage(), e);
            return e.toString();
        }
    }

    private static void writeCleanStackTrace(Throwable t, PrintWriter s, boolean wantsFilter) {
        s.print("Exception: ");
        printExceptionChain(t, s);
        Set<String> skippedPackages = new HashSet<String>();
        int skippedLines = 0;
        boolean shouldFilter = wantsFilter && filtersEnabled();
        boolean first = true;
        for (StackTraceElement traceElement : getBottomThrowable(t).getStackTrace()) {
            String forbiddenPackageName = null;
            if (shouldFilter && !first) {
                forbiddenPackageName = tryGetForbiddenPackageName(traceElement);
            }

            first = false;
            if (forbiddenPackageName == null) {
                if (!skippedPackages.isEmpty()) {
                    // 37 lines skipped for [org.h2, org.hibernate, sun., java.lang.reflect.Method, $Proxy]
                    s.println(getSkippedPackagesMessage(skippedPackages, skippedLines));
                }
                // at hib.HibExample.test(HibExample.java:18)
                s.println(INDENT + "at " + traceElement);
                skippedPackages.clear();
                skippedLines = 0;
            } else {
                skippedLines++;
                skippedPackages.add(forbiddenPackageName);
            }
        }
        if (skippedLines > 0) {
            s.println(getSkippedPackagesMessage(skippedPackages, skippedLines));
        }
    }

    // 37 lines skipped for [org.h2, org.hibernate, sun., java.lang.reflect.Method, $Proxy]
    private static String getSkippedPackagesMessage(Set<String> skippedPackages, int skippedLines) {
        return INDENT + INDENT + skippedLines + " line" + (skippedLines == 1 ? "" : "s") + " skipped for " + skippedPackages;
    }

    private static Throwable getBottomThrowable(Throwable t) {
        Throwable result = t;
        while (result.getCause() != null) {
            result = result.getCause();
        }
        return result;
    }

    /**
     * Check configuration to see if filtering is enabled system-wide
     * 
     * @return
     */
    private static boolean filtersEnabled() {
        return true;
    }

    private static void printExceptionChain(Throwable t, PrintWriter s) {
        s.println(t);
        if (t.getCause() != null) {
            s.print("Caused by: ");
            printExceptionChain(t.getCause(), s);
        }
    }

    /**
     * Checks to see if the class is part of a forbidden package. If so, it returns the package name from the list of suppressed
     * packages that matches, otherwise it returns null.
     * 
     * @param traceElement
     * @return
     */
    private static String tryGetForbiddenPackageName(StackTraceElement traceElement) {
        String classAndMethod = traceElement.getClassName() + "." + traceElement.getMethodName();
        for (String pkg : suppressedPackages) {
            if (classAndMethod.startsWith(pkg)) {
                return pkg;
            }
        }
        return null;
    }
}
