
public interface ClusterConfiguration {
	
	public AkkaConfiguration getAkkaConfiguration();
	public static class AkkaConfiguration{
		private String configuration;
		private boolean enabled = true;
	}

}
