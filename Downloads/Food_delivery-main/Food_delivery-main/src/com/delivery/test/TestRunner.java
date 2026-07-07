package com.delivery.test;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("=== RUNNING JUNIT 5 TESTS FOR ORDER REPOSITORY ===");

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(OrderRepositoryTest.class))
                .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();
        System.out.println("\n=== TEST RESULTS SUMMARY ===");
        System.out.println("Tests started: " + summary.getTestsStartedCount());
        System.out.println("Tests succeeded: " + summary.getTestsSucceededCount());
        System.out.println("Tests failed: " + summary.getTestsFailedCount());
        System.out.println("Tests aborted: " + summary.getTestsAbortedCount());
        System.out.println("Tests skipped: " + summary.getTestsSkippedCount());

        if (summary.getTestsFailedCount() > 0) {
            System.out.println("\n!!! FAILURES DETECTED !!!");
            summary.getFailures().forEach(failure -> {
                System.out.println("\nTest failed: " + failure.getTestIdentifier().getDisplayName());
                System.out.println("Exception message: " + failure.getException().getMessage());
                failure.getException().printStackTrace();
            });
            System.exit(1);
        } else {
            System.out.println("\n>>> ALL TESTS PASSED SUCCESSFULLY! <<<");
            System.exit(0);
        }
    }
}
