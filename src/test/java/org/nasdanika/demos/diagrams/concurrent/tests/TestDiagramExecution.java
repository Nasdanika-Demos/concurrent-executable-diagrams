package org.nasdanika.demos.diagrams.concurrent.tests;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.Test;
import org.nasdanika.common.PrintStreamProgressMonitor;
import org.nasdanika.common.ProgressMonitor;
import org.nasdanika.drawio.Document;
import org.nasdanika.drawio.processor.ElementInvocableFactory;
import org.nasdanika.graph.processor.AsyncInvocableEndpointFactory;

public class TestDiagramExecution {

	/**
	 * Tests concurrent execution with {@link AsyncInvocableEndpointFactory} 
	 * @throws Exception
	 */
	@Test
	public void testEndpoint() throws Exception {
		Function<URI, InputStream> uriHandler = null;				
		Function<String, String> propertySource = Map.of("my-property", "Hello")::get;		
		Document document = Document.load(
				new File("diagram.drawio"),
				uriHandler,
				propertySource);

		ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();				
		
		ElementInvocableFactory elementInvocableFactory = new ElementInvocableFactory(
				document.getPages().stream().filter(p -> "Endpoint".equals(p.getName())).findFirst().get(), 
				"processor");
		
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
		
}
