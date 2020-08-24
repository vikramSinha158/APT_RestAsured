package r1.restassured.runner;

import java.io.File;

import org.apache.maven.reporting.MavenReportException;
import org.junit.AfterClass;
import org.junit.runner.RunWith;

import com.cucumber.listener.Reporter;
import com.github.mkolisnyk.cucumber.reporting.CucumberUsageReporting;

import cucumber.api.CucumberOptions;
import cucumber.api.java.After;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(features = { "src/test/resources/features" }, 
		tags = { "@439368, @439367"},
		plugin = { "com.cucumber.listener.ExtentCucumberFormatter:target/cucumber-reports/report.html"},
		// dryRun = true,
		glue = "r1.restassured.stepdefination")

public class testRunner {
	
	 @AfterClass
	 public static void writeExtentReport() {
	 Reporter.loadXMLConfig(new File(System.getProperty("user.dir") + "//extent-config.xml"));
	 }



}
