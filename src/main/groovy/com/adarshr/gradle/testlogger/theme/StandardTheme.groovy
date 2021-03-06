package com.adarshr.gradle.testlogger.theme

import groovy.transform.InheritConstructors
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult

import static java.lang.System.lineSeparator
import static org.gradle.api.tasks.testing.TestResult.ResultType.*

@InheritConstructors
class StandardTheme extends AbstractTheme {

    @Override
    String suiteText(TestDescriptor descriptor) {
        "[erase-ahead,bold,bright-yellow]${escape(descriptor.className)}[/]${lineSeparator()}"
    }

    @Override
    String testText(TestDescriptor descriptor, TestResult result) {
        def line = new StringBuilder("[erase-ahead,bold]  Test [bold-off]${escape(descriptor.name)}")

        switch (result.resultType) {
            case SUCCESS:
                line << '[green] PASSED'
                showDurationIfSlow(result, line)
                break
            case FAILURE:
                line << '[red] FAILED'
                showDurationIfSlow(result, line)
                line << exceptionText(descriptor, result)
                break
            case SKIPPED:
                line << '[yellow] SKIPPED'
                break
        }

        line << '[/]'
    }

    private void showDurationIfSlow(TestResult result, StringBuilder line) {
        if (tooSlow(result)) {
            line << "[red] (${duration(result)})"
        } else if (mediumSlow(result)) {
            line << "[yellow] (${duration(result)})"
        }
    }

    @Override
    String exceptionText(TestDescriptor descriptor, TestResult result) {
        def exceptionText = super.exceptionText(descriptor, result)

        exceptionText ? "[red]${exceptionText}" : ''
    }

    @Override
    String summaryText(TestDescriptor descriptor, TestResult result) {
        if (!showSummary) {
            return ''
        }

        def colour = result.resultType == FAILURE ? 'red' : 'green'
        def line = new StringBuilder()

        line << "[erase-ahead,bold,${colour}]${result.resultType}: "
        line << "[default]Executed ${result.testCount} tests in ${duration(result)}"

        def breakdown = getBreakdown(result)

        if (breakdown) {
            line << ' (' << breakdown.join(', ') << ')'
        }

        line << "[/]${lineSeparator()}"
    }

    private static List getBreakdown(TestResult result) {
        def breakdown = []

        if (result.failedTestCount) {
            breakdown << "${result.failedTestCount} failed"
        }

        if (result.skippedTestCount) {
            breakdown << "${result.skippedTestCount} skipped"
        }

        breakdown
    }

    @Override
    String suiteStandardStreamText(String lines) {
        standardStreamText(lines, 2)
    }

    @Override
    String testStandardStreamText(String lines) {
        standardStreamText(lines, 4)
    }

    private String standardStreamText(String lines, int indent) {
        if (!showStandardStreams || !lines) {
            return ''
        }

        lines = lines.replace('[', '\\[')

        def indentation = ' ' * indent
        def line = new StringBuilder("[default]${lineSeparator()}")

        line << lines.split($/${lineSeparator()}/$).collect {
            "${indentation}${it}"
        }.join(lineSeparator())

        line << "[/]${lineSeparator()}"
    }
}
