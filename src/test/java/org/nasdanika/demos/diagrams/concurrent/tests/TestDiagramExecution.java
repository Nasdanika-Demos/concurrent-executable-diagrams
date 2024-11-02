package org.nasdanika.demos.diagrams.concurrent.tests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
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
import org.nasdanika.drawio.Page;
import org.nasdanika.drawio.processor.ElementInvocableFactory;
import org.nasdanika.graph.processor.AsyncInvocableConnectionProcessor;
import org.nasdanika.graph.processor.AsyncInvocableEndpointFactory;
import org.nasdanika.graph.processor.ProcessorConfig;
import org.nasdanika.graph.processor.ProcessorInfo;
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
				document.getPages().stream().filter(p -> "AsyncInvocableEndpointFactory".equals(p.getName())).findFirst().get(), 
				"processor");
		
		ExecutorService threadPool = Executors.newFixedThreadPool(5);
		
		AsyncInvocableEndpointFactory endpointFactory = new AsyncInvocableEndpointFactory(threadPool);		
		
		java.util.function.Function<Object,Object> proxy = elementInvocableFactory.createProxy(
				"bind",
				endpointFactory,
				null,
				progressMonitor,
				java.util.function.Function.class);
		
		System.out.println("Result: " + proxy.apply(33));
		
		threadPool.shutdown();
		threadPool.awaitTermination(5, TimeUnit.SECONDS);
	}
	
	/**
	 * Tests concurrent execution with {@link AsyncInvocableConnectionProcessor} 
	 * @throws Exception
	 */
	@Test
	public void testAsyncInvocableConnectionProcessor() throws Exception {
		Document document = loadDocument();
		ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();				
		
		Page page = document.getPages().stream().filter(p -> "AsyncInvocableConnectionProcessor".equals(p.getName())).findFirst().get();

		Map<org.nasdanika.graph.Element,AutoCloseable> toClose = new HashMap<>();
		ElementInvocableFactory elementInvocableFactory = new ElementInvocableFactory(page, "processor") {

			/**
			 * This override is needed to collect processors implementing {@link AutoCloseable}
			 */
			@Override
			protected Object doCreateProcessor(
					ProcessorConfig config, 
					boolean parallel,
					BiConsumer<org.nasdanika.graph.Element, BiConsumer<ProcessorInfo<Object>, ProgressMonitor>> infoProvider,
					Consumer<CompletionStage<?>> endpointWiringStageConsumer, 
					ProgressMonitor progressMonitor) {
				
				Object processor = super.doCreateProcessor(config, parallel, infoProvider, endpointWiringStageConsumer, progressMonitor);
				if (processor instanceof AutoCloseable) {
					toClose.put(config.getElement(), (AutoCloseable) processor);
				}
				return processor;
			}
			
		};
		
		java.util.function.Function<Object,Object> proxy = elementInvocableFactory.createProxy(
				"bind",
				null,
				progressMonitor,
				java.util.function.Function.class);
		
		System.out.println("Result: " + proxy.apply(33));		
		
		// Closing children first
		System.out.println("Closing");
		page.accept(e -> {
			AutoCloseable tc = toClose.get(e);
			if (tc != null) {
				try {
					tc.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}
		
	/**
	 * Tests concurrent execution with {@link AsyncInvocableConnectionProcessor} 
	 * @throws Exception
	 */
	@Test
	public void testThreadPoolContainer() throws Exception {
		Document document = loadDocument();
		ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();				
		
		Page page = document.getPages().stream().filter(p -> "Thread pool container".equals(p.getName())).findFirst().get();

		Map<org.nasdanika.graph.Element,AutoCloseable> toClose = new HashMap<>();
		ElementInvocableFactory elementInvocableFactory = new ElementInvocableFactory(page, "processor") {

			/**
			 * This override is needed to collect processors implementing {@link AutoCloseable}
			 */
			@Override
			protected Object doCreateProcessor(
					ProcessorConfig config, 
					boolean parallel,
					BiConsumer<org.nasdanika.graph.Element, BiConsumer<ProcessorInfo<Object>, ProgressMonitor>> infoProvider,
					Consumer<CompletionStage<?>> endpointWiringStageConsumer, 
					ProgressMonitor progressMonitor) {
				
				Object processor = super.doCreateProcessor(config, parallel, infoProvider, endpointWiringStageConsumer, progressMonitor);
				if (processor instanceof AutoCloseable) {
					toClose.put(config.getElement(), (AutoCloseable) processor);
				}
				return processor;
			}
			
		};
		
		java.util.function.Function<Object,Object> proxy = elementInvocableFactory.createProxy(
				"bind",
				null,
				progressMonitor,
				java.util.function.Function.class);
		
		System.out.println("Result: " + proxy.apply(33));		
		
		// Closing children first
		System.out.println("Closing");
		page.accept(e -> {
			AutoCloseable tc = toClose.get(e);
			if (tc != null) {
				try {
					tc.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
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
