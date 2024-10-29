package org.nasdanika.demos.diagrams.proxy.tests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;

import org.apache.groovy.groovysh.Groovysh;
import org.codehaus.groovy.tools.shell.IO;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.Test;
import org.nasdanika.capability.CapabilityLoader;
import org.nasdanika.capability.ServiceCapabilityFactory;
import org.nasdanika.capability.requirements.DiagramRequirement;
import org.nasdanika.capability.requirements.URIInvocableRequirement;
import org.nasdanika.common.Invocable;
import org.nasdanika.common.PrintStreamProgressMonitor;
import org.nasdanika.common.ProgressMonitor;
import org.nasdanika.drawio.Document;
import org.nasdanika.drawio.processor.ElementInvocableFactory;

import groovy.lang.Binding;

public class TestDiagramExecution {

	@Test
	public void testDynamicProxy() throws Exception {
		Function<URI, InputStream> uriHandler = null;				
		Function<String, String> propertySource = Map.of("my-property", "Hello")::get;		
		Document document = Document.load(
				new File("diagram.drawio"),
				uriHandler,
				propertySource);

		ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();				
		
		ElementInvocableFactory elementInvocableFactory = new ElementInvocableFactory(document, "processor");
		java.util.function.Function<Object,Object> proxy = elementInvocableFactory.createProxy(
				"bind",
				null,
				info -> {
					System.out.println("Info: " + info);
					return info.getProcessor();
				},
				progressMonitor,
				java.util.function.Function.class);
		
		System.out.println("Result: " + proxy.apply(33));
	}	
	
	// implementation 'org.freemarker:freemarker:2.3.33'
	
	@Test
	public void testDiagramRequirement() throws IOException {
		CapabilityLoader capabilityLoader = new CapabilityLoader();
		ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();
		URI specUri = URI.createFileURI(new File("diagram.drawio").getCanonicalPath());
		DiagramRequirement requirement = new DiagramRequirement(
				specUri, 
				null,
				null,
				null, 
				null,
				null, 
				"processor", 
				"bind", 
				getClass().getClassLoader(), 
				new Class<?>[] { java.util.function.Function.class });
		
		Function<String,Object> result = capabilityLoader.loadOne(requirement, progressMonitor);
		System.out.println(result);
		System.out.println(result.apply("YAML"));
	}
	
	@Test
	public void testYAMLSpecWithFragment() throws IOException {
		CapabilityLoader capabilityLoader = new CapabilityLoader();
		ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();
		URI specUri = URI.createFileURI(new File("diagram-function.yml").getCanonicalPath()).appendFragment("my-property=Hello");
		Invocable invocable = capabilityLoader.loadOne(
				ServiceCapabilityFactory.createRequirement(Invocable.class, null, new URIInvocableRequirement(specUri)),
				progressMonitor);
		Function<String,Object> result = invocable.invoke();
		System.out.println(result);
		System.out.println(result.apply("15"));
	}
	
	@Test
	public void testYAMLSpec() throws IOException {
		CapabilityLoader capabilityLoader = new CapabilityLoader();
		ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();
		URI specUri = URI.createFileURI(new File("diagram-function.yml").getCanonicalPath());
		Invocable invocable = capabilityLoader.loadOne(
				ServiceCapabilityFactory.createRequirement(Invocable.class, null, new URIInvocableRequirement(specUri)),
				progressMonitor);
		Function<String,Object> result = invocable.invoke();
		System.out.println(result);
		System.out.println(result.apply("YAML"));
	}
	
	@Test
	public void testYAMLSpecInline() throws IOException {
		CapabilityLoader capabilityLoader = new CapabilityLoader();
		ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();
		URI specUri = URI.createFileURI(new File("diagram-function-inline.yml").getCanonicalPath());
		Invocable invocable = capabilityLoader.loadOne(
				ServiceCapabilityFactory.createRequirement(Invocable.class, null, new URIInvocableRequirement(specUri)),
				progressMonitor);
		Function<String,Object> result = invocable.invoke();
		System.out.println(result);
		System.out.println(result.apply("YAML"));
	}
	
	// --- Not related to diagrams ---
		
	@Test
	public void testGroovy() throws IOException {
		CapabilityLoader capabilityLoader = new CapabilityLoader();
		ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();
		URI requirement = URI.createFileURI(new File("test.groovy").getCanonicalPath());
		Invocable invocable = capabilityLoader.loadOne(
				ServiceCapabilityFactory.createRequirement(Invocable.class, null, requirement),
				progressMonitor);
		Object result = invocable.invoke();
		System.out.println(result);
	}
	
	@Test
	public void testGroovyWithArguments() throws IOException {
		CapabilityLoader capabilityLoader = new CapabilityLoader();
		ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();
		URI requirement = URI.createFileURI(new File("test.groovy").getCanonicalPath()).appendFragment("Hello");
		Invocable invocable = capabilityLoader.loadOne(
				ServiceCapabilityFactory.createRequirement(Invocable.class, null, requirement),
				progressMonitor);
		Object result = invocable.invoke("Universe");
		System.out.println(result);
	}
	
	@Test
	public void testGroovysh() {		
		Binding binding = new Binding();
		binding.setProperty("test", "Test");
		Groovysh groovysh = new Groovysh(binding, new IO());
		groovysh.run("2 + 3");
	}
		
}
