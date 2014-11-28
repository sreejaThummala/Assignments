import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;

import akka.actor.UntypedActor;
import akka.cluster.*;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.event.slf4j.Logger;

public class ClusterSingleton extends UntypedActor {

	private static final org.slf4j.Logger log = LoggerFactory
			.getLogger(ClusterSingleton.class);
	private final Cluster cluster;
	private final SingletonHealthCheck healthCheck;

	public ClusterSingleton(SingletonHealthCheck healthCheck,
			Supplier<Runnable> action) {
		// notify the health check that I am now the master
		this.healthCheck = healthCheck;
		this.healthCheck.setMaster(true);
		this.cluster = Cluster.get(getContext().system());
		if (action.get() == null) {
			throw new IllegalStateException(
					"No action has been specified to run as singleton!");
		}
		action.get().run();
	}

	@Override
	public void onReceive(Object message) throws Exception {
		// TODO Auto-generated method stub
		if(message instanceof MemberUp){
			
		} else if(message instanceof UnreachableMember){
			
		} else if(message instanceof MemberRemoved){
			
		} else if (message instanceof MemberEvent){
			
		} else {
			unhandled(message);
		}

	}
	
	 @Override
	 public void preStart() {
	 // subscribe to cluster changes
	 cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class);
	 }
	 @Override
	 public void postStop() {
	 // un-subscribe on stop
	 cluster.unsubscribe(getSelf());
	 }

}
