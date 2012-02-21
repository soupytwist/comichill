package pojo;

public class StripNode {
	
	public String url;
	public String title;
	
	public StripNode(String url, String title) {
		this.url = url;
		this.title = title;
	}
	
	public String toString() {
		return this.title + " " + this.url;
	}
	
}
