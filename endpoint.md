In this demo messages from Alice to Bob are delivered asynchronously by ``org.nasdanika.graph.processor.AsyncInvocableEndpointFactory``.
Messages from Bob to Carol are delivered synchronously.

This is the client (test) code:

```java
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
		null,
		progressMonitor,
		java.util.function.Function.class);
		
System.out.println("Result: " + proxy.apply(33));
		
threadPool.shutdown();
threadPool.awaitTermination(5, TimeUnit.SECONDS);
```

First, a document is loaded.
Then a thread pool and an endpoint factory are created.
After that the document is wrapped into a dynamic proxy which is used to activate Alice processor.
Alice processor sends messages to Bob processor and Bob processor communicates with Carol processor and sends responses to Alice processor.

Click on the diagram shapes for Alice, Bob, and Carol to navigate to descriptions of their processors.
