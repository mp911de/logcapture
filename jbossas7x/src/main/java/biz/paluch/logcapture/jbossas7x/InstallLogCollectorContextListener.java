package biz.paluch.logcapture.jbossas7x;

import biz.paluch.logcapture.common.jul.InstallJulCollector;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class InstallLogCollectorContextListener implements ServletContextListener {

    private String CONFIGURATION_KEY = "logcapture-configuration";
    private String FORMAT_KEY = "logcapture-format-pattern";
    private InstallJulCollector install;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String configurationKey = getProperty(sce, CONFIGURATION_KEY);
        String formatPattern = getProperty(sce, FORMAT_KEY);

        install = new InstallJulCollector(configurationKey, formatPattern);
        install.install();
    }

    protected String getProperty(ServletContextEvent sce, String propertyName) {
        String configurationKey = sce.getServletContext().getInitParameter(propertyName);
        if (configurationKey == null || configurationKey.trim().equals("")) {
            try {
                configurationKey = InitialContext.doLookup(propertyName);
            } catch (NamingException e) {

            }
        }

        if (configurationKey == null || configurationKey.trim().equals("")) {
            configurationKey = System.getProperty(propertyName);
        }
        return configurationKey;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (install != null) {
            install.uninstall();
        }
    }
}
