
public class Messaging{
	
	private class Progress{
		private long count = 0;
		
		public synchronized long getCount(){
				return count;
			
		}
		
		public synchronized void setCount(long l){
			count = l;
		}
	}
	
	private Messaging(){
		
	}
	

}
