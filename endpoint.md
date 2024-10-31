In this demo messages from Alice to Bob are delivered asynchronously by ``org.nasdanika.graph.processor.AsyncInvocableEndpointFactory``.
Messages from Bob to Carol are delivered synchronously.

## Client code

This is the client (test) code:

```java
Document document = loadDocument();
ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();				
		
ElementInvocableFactory elementInvocableFactory = new ElementInvocableFactory(
		document.getPages().stream().filter(p -> "Endpoint".equals(p.getName())).findFirst().get(), 
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
```

First, a document is loaded.
Then a thread pool and an endpoint factory are created.
After that the document is wrapped into a dynamic proxy which is used to activate Alice processor.
Alice processor sends messages to Bob processor and Bob processor communicates with Carol processor and sends responses to Alice processor.

## Alice

Alice node has ``bind`` property set to ``apply``, which binds its processor, which implements ``Invocable`` to the invocation of the dynamic proxy's ``apply()`` method.
Alice node ``processor`` property is set to ``data:java/org.nasdanika.demos.diagrams.concurrent.AliceProcessor``.

Alice processor communicates with Bob processor via ``bobEndpoint`` which is wired to perform asynchronous invocation of Bob processor ``chat()`` method.
The type of the endpoint is ``AsyncChat``, which is a functional interface. 
The wiring process detects that the endpoint type is not compatible with ``Inovocable`` and is a functional interface and creates a proxy.

The endpoint type could have been ``Invocable`` as shown in the commented out line - it would spare us from creating ``AsynChat`` interface. 
However, ``AsyncChat`` interface introduces strong typing and makes ``AliceProcessor`` code more robust.

```java
public class AliceProcessor implements Invocable {

	public AliceProcessor(
			Loader loader,
			ProgressMonitor loaderProgressMonitor,
			Object data,
			String fragment,
			ProcessorConfig config,
			BiConsumer<Element, BiConsumer<ProcessorInfo<Invocable>, ProgressMonitor>> infoProvider,
			Consumer<CompletionStage<?>> endpointWiringStageConsumer,
			ProgressMonitor wiringProgressMonitor) {
		
		System.out.println("I got constructed " + this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String invoke(Object... args) {
		Message m1 = new Message(
				"Alice", 
				"Bob", 
				"SMS", 
				"Hi, Bob! How are you?", 
				Thread.currentThread().getName(), 
				new Date(), 
				null, 
				null);
		
		CompletableFuture<Message> responseCF1 = bobEndpoint.chat(m1);		
		responseCF1.thenAccept(response -> {
			System.out.println("[" + Thread.currentThread().getName() + "] Response: " + response);			
		});
		
		Message m2 = new Message(
				"Alice", 
				"Bob", 
				"SMS", 
				"By the way, say Hi to Carol! How is she doing?", 
				Thread.currentThread().getName(), 
				new Date(), 
				null, 
				null);
		
		
		CompletableFuture<Message> responseCF2 = bobEndpoint.chat(m2);		
		responseCF2.thenAccept(response -> {
			System.out.println("[" + Thread.currentThread().getName() + "] Response: " + response);			
		});
		
		return "Here I'm";
	}
	
	@OutgoingEndpoint
	public AsyncChat bobEndpoint;
	//public Invocable bobEndpoint;

}
```

## Bob

Bob node ``processor`` property is set to ``data:java/org.nasdanika.demos.diagrams.concurrent.BobProcessor``.

Bob processor communicates with Carol processor via ``carolEndpoint`` which is wired to perform synchronous invocation of Carol processor ``chat.chat()`` method.
Bob processor ``chat`` method is annotated with ``@IncomingHandler(wrap = HandlerWrapper.ASYNC_INVOCABLE)`` and therefore it gets wrapped into ``AsyncInvocable``, 
which tells the endpoint factory to perform asynchronous invocation.
The handler could have been explicitly of type ``AsyncInvocable`` - it would result in the same behavior.

```java
public class BobProcessor {

	public BobProcessor(
			Loader loader,
			ProgressMonitor loaderProgressMonitor,
			Object data,
			String fragment,
			ProcessorConfig config,
			BiConsumer<Element, BiConsumer<ProcessorInfo<Invocable>, ProgressMonitor>> infoProvider,
			Consumer<CompletionStage<?>> endpointWiringStageConsumer,
			ProgressMonitor wiringProgressMonitor) {
		
		System.out.println("I got constructed " + this);
	}

	@IncomingHandler(wrap = HandlerWrapper.ASYNC_INVOCABLE)
	public Message chat(Message request) throws InterruptedException {
		System.out.println("[Bob in " + Thread.currentThread().getName() + "] Got this from Alice: " + request);
		Thread.sleep(200);
		
		if (request.text().contains("Carol")) {
			Message toCarol = new Message(
					"Bob", 
					"Carol", 
					"Voice", 
					"Hey Carol, Alice says Hi and asks how you are!", 
					Thread.currentThread().getName(), 
					new Date(), 
					null, 
					request);
			
			Message carolResponse = carolEndpoint.chat(toCarol);		
			return new Message(
					"Bob", 
					"Alice", 
					"SMS", 
					"She is fine, says Hi back!", 
					Thread.currentThread().getName(), 
					new Date(), 
					request, 
					carolResponse);					
		}
		
		return new Message(
				"Bob", 
				"Alice", 
				"SMS", 
				"I'm fine", 
				Thread.currentThread().getName(), 
				new Date(), 
				request, 
				null);		
	}
	
	@OutgoingEndpoint
	public Chat carolEndpoint;
	
}
```

## Carol

Carol node ``processor`` property is set to ``data:java/org.nasdanika.demos.diagrams.concurrent.CarolProcessor``.
``chat`` field is annotated with ``@IncomingHandler`` and is wired to Bob processor ``carolEndpoint``.

```java
public class CarolProcessor {

	public CarolProcessor(
			Loader loader,
			ProgressMonitor loaderProgressMonitor,
			Object data,
			String fragment,
			ProcessorConfig config,
			BiConsumer<Element, BiConsumer<ProcessorInfo<Invocable>, ProgressMonitor>> infoProvider,
			Consumer<CompletionStage<?>> endpointWiringStageConsumer,
			ProgressMonitor wiringProgressMonitor) {
		
		System.out.println("I got constructed " + this);
	}

	@IncomingHandler	
	public Chat chat = new Chat() {

		@Override
		public Message chat(Message request) {
			System.out.println("[Carol in " + Thread.currentThread().getName() + "] Got this from Bob: " + request);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				return new Message(
						"Carol", 
						"Bob", 
						"Voce", 
						"Got interrupted, sorry!", 
						Thread.currentThread().getName(), 
						new Date(), 
						request, 
						null);		
			}
			return new Message(
					"Carol", 
					"Bob", 
					"Voce", 
					"I'm fine, say Hi back!", 
					Thread.currentThread().getName(), 
					new Date(), 
					request, 
					null);		
		}
		
	};

}
```
