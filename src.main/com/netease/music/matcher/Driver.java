package com.netease.music.matcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.solr.util.StrUtils;

import com.netease.music.matcher.processor.AlbumSongProcessor;
import com.netease.music.matcher.processor.AllProcessor;
import com.netease.music.matcher.processor.ArtistSongProcessor;

public class Driver {
	
	private String srcFilename;
	
	private Matcher matcher = null;
	private Processor processor = null;
	private BufferedWriter ignoreWriter = null;
	private BufferedWriter missedWriter = null;
	private BufferedWriter findedWriter = null;
	private BufferedWriter uncompleteWriter = null;
	
	public Driver(String srcFilename, Matcher matcher, Processor processor) throws Exception {
		this.srcFilename = srcFilename;
		this.matcher = matcher;
		this.processor = processor;
		
		ignoreWriter = new BufferedWriter(new FileWriter(this.srcFilename + ".ignore"));
		missedWriter = new BufferedWriter(new FileWriter(this.srcFilename + ".missed"));
		findedWriter = new BufferedWriter(new FileWriter(this.srcFilename + ".finded"));
		uncompleteWriter = new BufferedWriter(new FileWriter(this.srcFilename + ".uncomplete")); 
	}
	
	public String getSrcFilename() {
		return this.srcFilename;
	}
	
	public void work() throws Exception {
		String buffer = null;
		BufferedReader reader = new BufferedReader(new FileReader(srcFilename));
		
		long count = 0, emptyData = 0;
		while ((buffer = reader.readLine()) != null) {
			if (count++ %1000 == 0)
				System.out.println("process for : " + (count - 1));
			if (buffer.length() == 0) {
				emptyData += 1;
				continue;
			}
			
			String[] tokens = buffer.split("\t");
			if (tokens.length < 4 || StringUtils.isBlank(tokens[1]) || StringUtils.isBlank(tokens[3])) {
//			if (tokens.length < 4 || tokens[1].length() == 0 || tokens[3].length() == 0) {
				ignoreWriter.write(buffer);
				ignoreWriter.write(Character.LINE_SEPARATOR);
				continue;
			}
			Map<String, float[]> scoreMatrix = matcher.match(tokens[1], tokens[2], tokens[3]);
			if (scoreMatrix.size() <= 0) {
				missedWriter.write(buffer);
				missedWriter.write(Character.LINE_SEPARATOR);
			} else {
				String topKey = processor.process(tokens[0], scoreMatrix);
				if (topKey != null && topKey.length() > 0) {
					findedWriter.write(Utils.mergeResult(tokens[0], topKey, scoreMatrix.get(topKey)));
					findedWriter.write(Character.LINE_SEPARATOR);
				} else {
					uncompleteWriter.write(buffer);
					uncompleteWriter.write(Character.LINE_SEPARATOR);
				}
			}
		}
		reader.close();
		System.out.println("empty data : " + emptyData);
	}
	
	public void close() throws Exception {
		matcher.close();
		processor.close();
		
		ignoreWriter.close();
		missedWriter.close();
		findedWriter.close();
		uncompleteWriter.close();
	}
	
	public static void main(String[] args) throws Exception {
		Utils.loadExtDic();
		String target = Config.ROOTPATH + "/origin/1k_1k_1k_and/meta_all.uncomplete";
		
		Matcher matcher = new Matcher(2000, 2000, 2000);
		matcher.getSongSearcher().setParserOperator(Operator.OR);
		matcher.getAlbumSearcher().setParserOperator(Operator.OR);
		matcher.getArtistSearcher().setParserOperator(Operator.OR);
		
		Processor processor = new AllProcessor(target + ".matched");
		
		Driver driver = new Driver(target, matcher, processor);
		driver.work();
		driver.close();
	}
}
