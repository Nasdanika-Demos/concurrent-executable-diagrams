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
