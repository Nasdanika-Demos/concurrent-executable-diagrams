package org.nasdanika.demos.diagrams.concurrent;

import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.nasdanika.capability.CapabilityFactory.Loader;
import org.nasdanika.common.Invocable;
import org.nasdanika.common.ProgressMonitor;
import org.nasdanika.graph.Element;
import org.nasdanika.graph.processor.ConnectionProcessorConfig;
import org.nasdanika.graph.processor.NodeProcessorConfig;
import org.nasdanika.graph.processor.ProcessorConfig;
import org.nasdanika.graph.processor.ProcessorInfo;

/**
 * This processor's is invoked from a dynamic proxy apply(). 
 */
public class AliceProcessor implements Invocable {

	/**
	 * This is the constructor signature for graph processor classes which are to e instantiated by URIInvocableCapabilityFactory (org.nasdanika.capability.factories.URIInvocableCapabilityFactory).
	 * Config may be of specific types {@link ProcessorConfig} - {@link NodeProcessorConfig} or {@link ConnectionProcessorConfig}.  
	 * @param loader
	 * @param loaderProgressMonitor
	 * @param data
	 * @param fragment
	 * @param config
	 * @param infoProvider
	 * @param endpointWiringStageConsumer
	 * @param wiringProgressMonitor
	 */
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
		// TODO Auto-generated method stub
		return "Here I'm";
	}

}
