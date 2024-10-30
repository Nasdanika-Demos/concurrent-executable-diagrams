package org.nasdanika.demos.diagrams.concurrent.tests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.Test;
import org.nasdanika.common.PrintStreamProgressMonitor;
import org.nasdanika.common.ProgressMonitor;
import org.nasdanika.drawio.Document;
import org.nasdanika.drawio.Element;
import org.nasdanika.drawio.Node;
import org.nasdanika.drawio.processor.ElementInvocableFactory;
import org.nasdanika.graph.processor.AsyncInvocableEndpointFactory;
import org.xml.sax.SAXException;

public class TestDiagramExecution {

	/**
	 * Tests concurrent execution with {@link AsyncInvocableEndpointFactory} 
	 * @throws Exception
	 */
	@Test
	public void testAsyncInvocableEndpoint() throws Exception {
		Document document = loadDocument();
		ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();				
		
		ElementInvocableFactory elementInvocableFactory = new ElementInvocableFactory(
				document.getPages().stream().filter(p -> "AsyncInvocableEndpoint".equals(p.getName())).findFirst().get(), 
				"processor");
		
		ExecutorService threadPool = Executors.newFixedThreadPool(5);
		
		AsyncInvocableEndpointFactory endpointFactory = new AsyncInvocableEndpointFactory(threadPool);		
		
		java.util.function.Function<Object,Object> proxy = elementInvocableFactory.createProxy(
				"bind",
				endpointFactory,
				null,
				info -> {
					System.out.println("Info: " + info);
					return info.getProcessor();
				},
				progressMonitor,
				java.util.function.Function.class);
		
		System.out.println("Result: " + proxy.apply(33));
		
		threadPool.shutdown();
		threadPool.awaitTermination(5, TimeUnit.SECONDS);
	}
	
	@Test
	public void testTraversal() throws Exception {
		Document document = loadDocument();
		
		Node bobNode = document
			.stream()
			.filter(Node.class::isInstance)
			.map(Node.class::cast)
			.filter(n -> "Bob".equals(n.getLabel()))
			.findFirst()
			.get();
				
		System.out.println(bobNode);
		Consumer<Element> collector = System.out::println;
		Consumer<Element> traverser = org.nasdanika.drawio.Util.traverser(collector, null);
		bobNode.accept(traverser, null);
	}

	protected Document loadDocument() throws IOException, ParserConfigurationException, SAXException {
		Function<URI, InputStream> uriHandler = null;				
		Function<String, String> propertySource = Map.of("my-property", "Hello")::get;		
		Document document = Document.load(
				new File("diagram.drawio"),
				uriHandler,
				propertySource);
		return document;
	}	
		
}
