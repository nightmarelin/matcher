package com.netease.music.matcher;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

public class Matcher {
	
	private Searcher songSearcher = null;
	private Searcher albumSearcher = null;
	private Searcher artistSearcher = null;

	public Matcher() throws Exception {
		this(100, 100, 100);
	}
	
	public Matcher(int artistDocs, int albumDocs, int songDocs) throws Exception {
		songSearcher = new SongSearcher(songDocs, Config.songIndexDir);
		albumSearcher = new AlbumSearcher(albumDocs, Config.albumIndexDir);
		artistSearcher = new ArtistSearcher(artistDocs, Config.artistIndexDir);
	}
	
	public Map<String, float[]> match(String artistName, String albumName, String songName) throws Exception {
		ScoreDoc[] songScoreDocs = null;
		ScoreDoc[] albumScoreDocs = null;
		ScoreDoc[] artistScoreDocs = null;
		
		artistName = artistName.trim();
		if (artistName != null && artistName.length() > 0) {
			artistScoreDocs = artistSearcher.search(artistName);
		}
		albumName = albumName.trim();
		if (albumName != null && albumName.length() > 0) {
			albumScoreDocs = albumSearcher.search(albumName);
		}
		songName = songName.trim();
		if (songName != null && songName.length() > 0) {
			songScoreDocs = songSearcher.search(songName);
		}
		return mergeScoreDocs(artistScoreDocs, albumScoreDocs, songScoreDocs);
	}
	
	protected Map<String, float[]> mergeScoreDocs(ScoreDoc[] artistScoreDocs, ScoreDoc[] albumScoreDocs, 
			ScoreDoc[] songScoreDocs) throws Exception {
		Map<String, float[]> map = new HashMap<String, float[]>();
		
		if (artistScoreDocs != null && artistScoreDocs.length > 0) {
			for (int i = 0, l = artistScoreDocs.length; i < l; ++i) {
				Document doc = artistSearcher.doc(artistScoreDocs[i].doc);
				String id = doc.get("id");
				float[] tmpArray = map.get(id);
				if (tmpArray == null) {
					tmpArray = new float[3];
					map.put(id, tmpArray);
				}
				tmpArray[0] = artistScoreDocs[i].score;
			}
		}
		if (albumScoreDocs != null && albumScoreDocs.length > 0) {
			for (int i = 0, l = albumScoreDocs.length; i < l; ++i) {
				Document doc = albumSearcher.doc(albumScoreDocs[i].doc);
				String id = doc.get("id");
				float[] tmpArray = map.get(id);
				if (tmpArray == null) {
					tmpArray = new float[3];
					map.put(id, tmpArray);
				}
				tmpArray[1] = albumScoreDocs[i].score;
			}
		}
		if (songScoreDocs != null && songScoreDocs.length > 0) {
			for (int i = 0, l = songScoreDocs.length; i < l; ++i) {
				Document doc = songSearcher.doc(songScoreDocs[i].doc);
				String id = doc.get("id");
				float[] tmpArray = map.get(id);
				if (tmpArray == null) {
					tmpArray = new float[3];
					map.put(id, tmpArray);
				}
				tmpArray[2] = songScoreDocs[i].score;
			}
		}
		
		return map;
	}
	
	public void close() throws Exception {
		songSearcher.close();
		albumSearcher.close();
		artistSearcher.close();
	}
	
	public Searcher getSongSearcher() {
		return songSearcher;
	}

	public Searcher getAlbumSearcher() {
		return albumSearcher;
	}

	public Searcher getArtistSearcher() {
		return artistSearcher;
	}
	
	public static void main(String[] args) throws Exception {
		Utils.loadExtDic();
		
		Matcher matcher = new Matcher(2000, 2000, 2000);
		Map<String, float[]> scoreMatrix = matcher.match("蔡依林", "Live concert 演唱会全纪录2", "说爱你");
		System.out.println("size : " + scoreMatrix.size());
		for (String key : scoreMatrix.keySet()) {
			float[] values = scoreMatrix.get(key);
			if (values[1] > 0 && values[2] > 0 && values[0] > 0)
				System.out.println(Utils.mergeResult("", key, values));
		}
	}
}
