import java.util.ArrayList;
import java.util.List;

import akka.actor.UntypedActor;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import akka.actor.*;


public class WorkerManager extends UntypedActor{
	
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private int currentJobId = -1;
	private long jobSize = -1;
	private long jobReturned = 0;
	
	private List<ActorRef> workers;
	private Router router;
	private ActorRef jobManager;
	private ActorRef executive;
	
	private Status status;
	
	//specify number of worker that needs to run on the mapreduce clone
	//if nothign is specified it takes the number of processors
	private int nWorkers;
	
	WorkerManager(){
		
	}
	
	WorkerManager(Integer nWorkers){
		if(nWorkers == null){
			nWorkers = Runtime.getRuntime().availableProcessors();
			
			this.workers = new ArrayList<ActorRef>();
			
			status = Status.READY;
			
			//need to initate the map Actor
		}
	}
	
	private void createAndRouteWorker(){
		ArrayList<Routee> routees = new ArrayList<Routee>();
		
		for(int i=0; i<this.nWorkers;i++){
			ActorRef worker = getContext().actorOf(Props.create(Mapper.class),"worker"+i);
			routees.add(new ActorRefRoutee(worker));
			workers.add(worker);
			
		}
		
		router = new Router(new RoundRobinRoutingLogic(), routees);
		
		System.out.println("workers are runnign on the system");
		status = Status.READY;
	}
	
	

	@Override
	public void onReceive(Object arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
