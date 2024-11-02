This demo has the same functionality as the [AsyncInvocableEndpointFactory](endpoint.html) demo. 
However, instead of ``org.nasdanika.graph.processor.AsyncInvocableEndpointFactory`` it uses
``org.nasdanika.graph.processor.AsyncInvocableConnectionProcessor`` for the connection from Alice to Bob to deliver messages asynchronously.

## Client code

The client code doesn't manage a thread pool and doesn't create an endpoint factory. 
The thread pool is managed by the connection processor. 
The client code takes care of closing the processor, which ensures proper termination of the thread pool and processing of all messages.

```java
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
```

## Alice -> Bob connection

Alice -> Bob connection has its ``processor`` property set to ``data:java/org.nasdanika.graph.processor.AsyncInvocableConnectionProcessor#5``. 
Fragment value ``5`` is used as the size of the thread pool.
Without the fragment the connection processor would use the ``ForkJoinPool.commonPool()``.
