package pojo;

import models.siena.Comic;
import models.siena.Strip;

public class NavigationFrame {

	public Strip first, prev, next, last;
	
	public NavigationFrame(Strip cur) {
		Comic comic = cur.getComic();
		if (cur.sid > 1) {
			first = Strip.get(comic.id, 1);
			if (cur.sid > 2) {
				prev = Strip.get(comic.id, cur.sid - 1);
			} else {
				prev = first;
			}
		}
		
		if (cur.sid < comic.numStrips) {
			last = Strip.get(comic.id, comic.numStrips);
			
			if (cur.sid != comic.numStrips-1) {
				next = Strip.get(comic.id, cur.sid + 1);
			} else {
				next = last;
			}
		}
	}
	
	public NavigationFrame(Strip first, Strip prev, Strip next, Strip last) {
		this.first = first;
		this.prev = prev;
		this.next = next;
		this.last = last;
		if (this.first != null) { this.first.get(); }
		if (this.prev != null)  { this.prev.get();  }
		if (this.next != null)  { this.next.get();  }
		if (this.last != null)  { this.last.get();  }
	}
	
}
