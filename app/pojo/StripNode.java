package pojo;

public class StripNode {
	
	public String url;
	public String title;
	
	public StripNode(String url, String title) {
		this.url = url;
		if (this.url != null) {
			this.url = this.url.replaceAll("\n", "").trim();
		}
		this.title = title;
	}
	
	public String toString() {
		return this.title + " " + this.url;
	}
	
}
