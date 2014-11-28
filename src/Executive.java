import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;


public class Executive extends UntypedActor{
	int taskOut;
	int jobId = 0;
	Map<Integer, ArrayList<WorkResult>> jobResults;
	Map<String, ActorRef> wmPathActorRefs;
	Map<String, Integer> workerManagers;
	Map<String, ActorRef> jobManagers;
	
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	
	Executive(){
		jobResults = new HashMap<Integer, ArrayList<WorkResult>>();
		
	}

}
