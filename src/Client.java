import java.util.HashMap;
import java.util.Map;

import akka.actor.UntypedActor;


public class Client extends UntypedActor{
	
	//create a class
	Map<String,Integer> commands;
	
	{
		commands = new HashMap<String,Integer>();
		commands.put("send", 1);
		commands.put("receive", 2);
	}

	@Override
	public void onReceive(Object arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
