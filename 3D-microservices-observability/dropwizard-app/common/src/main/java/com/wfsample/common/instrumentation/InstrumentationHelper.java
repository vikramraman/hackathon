package com.wfsample.common.instrumentation;

import com.wavefront.sdk.jaxrs.client.WavefrontJaxrsClientFilter;
import com.wavefront.sdk.jersey.WavefrontJerseyFactory;
import com.wavefront.sdk.jersey.WavefrontJerseyFilter;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import com.wavefront.sdk.common.WavefrontSender;
import com.codahale.metrics.MetricRegistry;
import com.wavefront.dropwizard.metrics.DropwizardMetricsReporter;

import javax.annotation.Nullable;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class InstrumentationHelper {
    private static DropwizardMetricsReporter dwReporter = null;

    private static MetricRegistry metricRegistry = null;

    private static String applicationTagsYamlFile = "app-tags.yml";

    private static String wfReportingConfigYamlFile = "wf-reporting-config.yml";

    public static MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public static void instrument(JerseyEnvironment jersey) {   // Instantiate the WavefrontJerseyFactory
        if(new File(applicationTagsYamlFile).exists() &&
                new File(wfReportingConfigYamlFile).exists()) {
            com.wavefront.sdk.jersey.WavefrontJerseyFactory wjf = new WavefrontJerseyFactory(
                    applicationTagsYamlFile, wfReportingConfigYamlFile);
            WavefrontJerseyFilter f  = wjf.getWavefrontJerseyFilter();
            jersey.register(f);
            System.out.println("Jersey filter registered");

            synchronized (InstrumentationHelper.class) {

                // We'll borrow the first sender created to use for DWM.

                if(dwReporter == null) {
                    // Use the same sender to set up DropWizardMetrics.
                    WavefrontSender s = wjf.getWavefrontSender();

                    // Create a registry
                    metricRegistry = new MetricRegistry();

                    // Create a builder instance for the registry
                    DropwizardMetricsReporter.Builder builder = DropwizardMetricsReporter.forRegistry(metricRegistry);
                    try {
                        String source = InetAddress.getLocalHost().getHostAddress();
                        builder.withSource(source);
                    } catch(UnknownHostException e) {
                        System.err.println("Error obtaining local address");
                    }
                    builder.reportMinuteDistribution().reportHourDistribution().reportDayDistribution();
                    DropwizardMetricsReporter reporter = builder.build(s);
                    reporter.start(30, TimeUnit.SECONDS);
                }
            }
        } else {
            System.out.println("Jersey filter not registered. Configuration files not found!");
        }
    }

    @Nullable
    public static WavefrontJaxrsClientFilter getJaxrsFilter()  {
        if(new File(applicationTagsYamlFile).exists() &&
                new File(wfReportingConfigYamlFile).exists()) {
            com.wavefront.sdk.jersey.WavefrontJerseyFactory wjf = new WavefrontJerseyFactory(
                    applicationTagsYamlFile, wfReportingConfigYamlFile);
            return wjf.getWavefrontJaxrsClientFilter();
        }
        return null;
    }
}
