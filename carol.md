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
