package zdream.rockchronicle.platform.region;

public class RegionDef {
	
	public String name;
	public String path;
	public String data;

	@Override
	public String toString() {
		
		return String.format("RegionDef:%s", name == null ? path : name);
	}


}
