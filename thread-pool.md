In this demo Alice -> Bob connection processor does not manage its own thread pool, but obtains the pool from the connection parent element - Thread Pool container.

## Thread pool container

Thread pool container ``processor`` property is set to ``data:java/org.nasdanika.demos.diagrams.concurrent.ThreadPoolProcessor#5``.
Fragment value ``5`` is the thread pool size. 
Without a fragment the processor would use ``ForkJoinPool.commonPool()`` instead of creating its own thread pool.

```java
public class ThreadPoolProcessor implements AutoCloseable, Supplier<Executor> {
	
	protected Executor executor;
	private boolean shutdownExecutor;
	protected long terminationTimeout;
	protected TimeUnit terminationTimeoutUnit;
	
	public ThreadPoolProcessor(
			Loader loader,
			ProgressMonitor loaderProgressMonitor,
			Object data,
			String fragment,
			ProcessorConfig config,
			BiConsumer<Element, BiConsumer<ProcessorInfo<Invocable>, ProgressMonitor>> infoProvider,
			Consumer<CompletionStage<?>> endpointWiringStageConsumer,
			ProgressMonitor wiringProgressMonitor) {

		if (!Util.isBlank(fragment)) {
			this.executor = Executors.newFixedThreadPool(Integer.parseInt(fragment));
			this.terminationTimeout = 1;
			this.terminationTimeoutUnit = TimeUnit.MINUTES;
			shutdownExecutor = true;
			
		}
	}	

	@Override
	public void close() throws Exception {
		if (shutdownExecutor && executor instanceof ExecutorService) {
			ExecutorService executorService = (ExecutorService) executor;
			executorService.shutdown();
			executorService.awaitTermination(terminationTimeout, terminationTimeoutUnit);
		}		
	}

	@Override
	public Executor get() {
		return executor;
	}
	
}
```

## Alice -> Bob connection

Alice -> Bob connection has its ``processor`` property set to ``data:java/org.nasdanika.demos.diagrams.concurrent.ConcurrentConnectionProcessor``.

``CooncurrentConnectionProcessor`` extends ``AsyncInvocableConnectionProcessor`` and retrieves executor from the parent element processor:

```java
public class ConcurrentConnectionProcessor extends AsyncInvocableConnectionProcessor {

	public ConcurrentConnectionProcessor(
			Loader loader, 
			ProgressMonitor loaderProgressMonitor, 
			Object data,
			String fragment, 
			ProcessorConfig config,
			BiConsumer<Element, BiConsumer<ProcessorInfo<Invocable>, ProgressMonitor>> infoProvider,
			Consumer<CompletionStage<?>> endpointWiringStageConsumer, ProgressMonitor wiringProgressMonitor) {
		super(
				loader, 
				loaderProgressMonitor, 
				data, 
				fragment, 
				config, 
				infoProvider, 
				endpointWiringStageConsumer,
				wiringProgressMonitor);
	}
	
	@ParentProcessor
//	@RegistryEntry("#this == #element.parent")
	public void setThreadPool(Supplier<Executor> threadPoolSupplier) {
		executor = threadPoolSupplier.get();
	}

}
```

The thread pool processor gets injected into ``setThreadPool()`` method because it is annotated with ``@ParentProcessor``. 
It could have been annotated with ``@RegistryEntry("#this == #element.parent")`` to achieve the same result. 
``@RegistryEntry`` annotation can be used when the thread pool container is not an immediate ancestor (parent) of the connection.

