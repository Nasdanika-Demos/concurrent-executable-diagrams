package org.nasdanika.demos.diagrams.concurrent;

import java.util.Date;

public interface Chat {
	
	record Message(
			String sender,
			String recipient,
			String channel,
			String text, 
			String thread, 
			Date time, 
			Message inResponseTo,
			Message relatedTo) {}
		
	Message chat(Message request); 

}
