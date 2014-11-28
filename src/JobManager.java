import java.util.HashSet;
import java.util.Set;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;


public class JobManager extends UntypedActor{

	private Set<ActorRef> workerManagersOut;
	
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);
	private ActorRef executive;
	private int jobId;
	private JobItemCallback callback;
	private HashSet<ActorRef> workerManagersReady;
	
	//an element for the vodlermort
	
	
	JobManager(Boolean workOffline) {
	//initate storage client
	workerManagersReady = new HashSet<ActorRef>();
	workerManagersOut = new HashSet<ActorRef>();
	}
	@Override
	public void onReceive(Object msg) throws Exception {
	if (msg instanceof ActorRef) {
	onMsgActorRef((ActorRef) msg);
	} else if (msg instanceof JobSpec) {
	onMsgJobSpec((JobSpec) msg);
	} else if(msg instanceof WorkResult){
	onMsgWorkResult((WorkResult) msg);
	} else if(msg instanceof JobSliceDone){
	onMsgJobSliceDone();
	} else if(msg instanceof RemoveWorkerManager){
	onMsgRemoveWorkerManager((RemoveWorkerManager)msg);
	} else if(msg instanceof Terminated){
	unhandled(msg);
	} else if(msg instanceof CancelJob){
	onMsgCancelJob((CancelJob)msg);
	} else {
	unhandled(msg);
	}
	}
	private void onMsgCancelJob(CancelJob msg) throws Exception {
	cancelAndReturn();
	}
	private void onMsgRemoveWorkerManager(RemoveWorkerManager msg) throws Exception {
	ActorRef dead = msg.workerManager;
	if( workerManagersOut.contains( dead ) ){
	workerManagersOut.remove( dead );
	cancelAndReturn();
	return;
	}
	boolean removedFromReady = workerManagersReady.remove(dead);
	if(!removedFromReady){
	log.error("attepting to remove workermanager not on the jobmanager's roster");
	//TODO raise error more sternly?
	}
	}
	private void cancelAndReturn() throws Exception {
	// cancel every WorkerManager still on a job
	Timeout timeout = new Timeout(Duration.create(5, "seconds"));
	Set<Future<Object>> futures = new HashSet<Future<Object>>();
	for(ActorRef wm : workerManagersOut ){
	futures.add( Patterns.ask(wm, new CancelJob(), timeout) );
	}
	for(Future<Object> future : futures){
	Await.result(future, timeout.duration());
	}
	// move them all the the ready set
	workerManagersReady.addAll( workerManagersOut );
	workerManagersOut.clear();
	// report to supervisor
	executive.tell( new JobDone(JobDone.Status.CANCELLED, workerManagersReady), getSelf());
	}
	private void onMsgJobSliceDone() {
	ActorRef mng = getSender();
	workerManagersOut.remove(mng);
	workerManagersReady.add(mng);
	if (workerManagersOut.isEmpty()){
	executive.tell(new JobDone(jobId, workerManagersReady), getSelf());
	}
	}
	private void onMsgWorkResult(WorkResult res) throws IOException {
	res.jobId = jobId;
	if(callback != null){
	this.callback.onWorkResult( res );
	}
	executive.tell(res, getSelf());
	}
	private void onMsgJobSpec(JobSpec js) throws Exception {
	this.jobId = js.jobId;
	this.callback = js.callback;
	// bond to the executive that sent this
	this.executive = getSender();
	log.debug( "get origin pointset: {}",js.fromPtsLoc );
	PointSet fromPts = s3Store.getPointset( js.fromPtsLoc );
	log.debug( "got origin pointset: {}",fromPts.featureCount() );
	TimeZone tz = TimeZone.getTimeZone(js.tz);
	Date date = DateUtils.toDate(js.date, js.time, tz);
	if(js.subsetIds != null && js.subsetIds.size() > 0) {
	Joiner joiner = Joiner.on(",").skipNulls();
	log.debug( "restricting analysis to ids: {}", joiner.join(js.subsetIds));
	// split the job evenly between managers
	float seglen = js.subsetIds.size() / ((float) workerManagersReady.size());
	int i=0;
	for(ActorRef workerManager : workerManagersReady){
	int start = Math.round(seglen * i);
	int end = Math.round(seglen * (i + 1));
	workerManager.tell(new JobSliceSpec(js.fromPtsLoc,js.subsetIds,js.toPtsLoc,js.graphId,date,js.mode), getSelf());
	workerManagersOut.add( workerManager );
	i++;
	}
	}
	else {
	// split the job evenly between managers
	float seglen = fromPts.featureCount() / ((float) workerManagersReady.size());
	int i=0;
	for(ActorRef workerManager : workerManagersReady){
	int start = Math.round(seglen * i);
	int end = Math.round(seglen * (i + 1));
	workerManager.tell(new JobSliceSpec(js.fromPtsLoc,start,end,js.toPtsLoc,js.graphId,date, js.mode), getSelf());
	workerManagersOut.add( workerManager );
	i++;
	}
	}
	// since we added every WorkerManager in the ready set to the out set, we can empty it
	workerManagersReady.clear();
	}
	private void onMsgActorRef(ActorRef asel) {
	//getContext().watch(asel);
	workerManagersReady.add(asel);
	getSender().tell(new Boolean(true), getSelf());
	}


}
