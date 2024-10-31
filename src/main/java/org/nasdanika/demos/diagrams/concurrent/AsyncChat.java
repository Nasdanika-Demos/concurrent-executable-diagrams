package org.nasdanika.demos.diagrams.concurrent;

import java.util.concurrent.CompletableFuture;

public interface AsyncChat {
			
	CompletableFuture<Message> chat(Message request); 

}
