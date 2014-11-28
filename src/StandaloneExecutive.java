import java.util.ArrayList;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.util.Timeout;
import akka.pattern.Patterns;


public class StandaloneExecutive {
	ActorRef executive;
	
	public void registerWorker(String path) throws Exception{
		//block until success. 
		Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		Future<Object> future = Patterns.ask(executive, new AddWorkerManager(path), timeout);
		Await.result(future, timeout.duration());
	}
	
	public ArrayList<JobStatus> getJobStatus(){
		Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		Future<Object> future = Patterns.ask(executive, new JobStatusQuery(), timeout);
		ArrayList<JobStatus> result = (ArrayList<JobStatus>) Await.result(future, timeout.duration());
		return result;
		
	}
	

}
