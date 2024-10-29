import org.nasdanika.capability.CapabilityFactory.Loader
import org.nasdanika.common.ProgressMonitor

// Script arguments for reference
Loader loader = args[0];
ProgressMonitor loaderProgressMonitor = args[1];
Object data = args[2];

System.out.println(args);
"I've got " + args.length + " arguments!"