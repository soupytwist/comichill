package models.siena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controllers.Strips;

import pojo.NavigationFrame;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.embed.Embedded;
import util.My;
import util.Serializers;

public class StripQueue extends Model {

	public static final int MAX_QUEUE_SIZE = 100;
	public static final int CUTOFF_COUNT = 12;
	
	@Id(Generator.AUTO_INCREMENT)
	public Long id;	
	
	@Embedded
	private List<Long> queue;
	
	public int pos;

	public StripQueue() {
		// default no-arguments constructor
	}
	
	public void add(Strip s) {
		if (queue == null)
			queue = new ArrayList<Long>();
		queue.add(s.id);
	}
	
	public void add(List<Strip> strips) {
		if (queue == null)
			queue = new ArrayList<Long>();
		// TODO Ignore duplicates
		for (Strip s : strips)
			queue.add(s.id);
	}
	
	public boolean isEmpty() {
		return queue == null || queue.isEmpty();
	}
	
	public int size() {
		return (queue == null) ? 0 : queue.size();
	}
	
	public Strip getCurrent() {
		Long id = queue.get(pos);
		return Strip.getById(id);
	}
	
	public boolean setCurrent(Strip strip) {
		int idx = queue.indexOf(strip.id);
		if (idx != -1)
			pos = idx;
		return idx != -1;
	}
	
	public Strip getRelativePos(int off) {
		if (pos + off >= 0 && pos + off < queue.size()) {
			Long id = queue.get(pos+off);
			return Strip.getById(id);
		} else {
			return null;
		}
	}
	
	public Strip getFirst() {
		if (pos > 0) {
			Long id = queue.get(0);
			return Strip.getById(id);
		} else {
			return null;
		}
	}
	
	public Strip getLast() {
		if (pos < queue.size()) {
			Long id = queue.get(queue.size()-1);
			return Strip.getById(id);
		} else {
			return null;
		}
	}
	
	public NavigationFrame getFrame() {
		return new NavigationFrame(this.getFirst(),this.getRelativePos(-1), this.getRelativePos(1), this.getLast());
	}
	
	public Map<Integer, Object> toJSON() {
		Map<Integer, Strip> map = new HashMap();
		Map<Long, Comic> comics = new HashMap<Long, Comic>();
		int i = 0;
			
		for (Long id : queue) {
			Strip s = Strip.getById(id);
			map.put(i++, s);
			if (!comics.containsKey(s.cid)) {
				comics.put(s.cid, Comic.getById(s.cid));
			}
		}
		
		return My.map(map, comics, new Integer(pos), new Integer(size()));
	}
	
	public void clear() {
		queue.clear();
	}
	
	public void shuffle() {
		// TODO Implement shuffle()
	}
	
	public static StripQueue getById(Long id) {
		return Model.all(StripQueue.class).getByKey(id);
	}
	
	public String toString() {
		return this.toJSON().toString();
	}
}
