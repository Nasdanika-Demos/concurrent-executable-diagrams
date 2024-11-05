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
		return responseCF2.join().toString();
	}
	
	@OutgoingEndpoint
	public AsyncChat bobEndpoint;
	//public Invocable bobEndpoint;

}
```
