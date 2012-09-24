package com.netease.music.matcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.spreada.utils.chinese.ZHConverter;

public class Indexer {
	
	private String srcFile;
	private String indexDir;

	public Indexer(String srcFile, String indexDir) {
		this.srcFile = srcFile;
		this.indexDir = indexDir;
	}
	
	public void make(Analyzer analyzer) throws IOException {
		long startTime = new Date().getTime();
		
		Directory directory = new SimpleFSDirectory(new File(indexDir));
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_34, analyzer);
		indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
		
		long wrong = 0, i = 0;
		String buffer = null;
		ZHConverter zhConverter = ZHConverter.getInstance(ZHConverter.SIMPLIFIED);
		BufferedReader reader = new BufferedReader(new FileReader(srcFile));
		while ((buffer = reader.readLine()) != null) {
			if (i++ % 10000 == 0) {
				System.out.println("index row : " + (i - 1));
			}
			buffer = buffer.trim();
			if (buffer.length() <= 0) continue;
			String[] tmpArray = buffer.split("\t");
			if (tmpArray.length != 2 || tmpArray[0].length() == 0 || tmpArray[1].length() == 0) {
				wrong++;
			} else {
				Document doc = new Document();
				doc.add(new Field(Config.FIELD_KEY, tmpArray[0], Field.Store.YES, Field.Index.NOT_ANALYZED));
				doc.add(new Field(Config.FIELD_TEXT, zhConverter.convert(tmpArray[1]), Field.Store.YES, Field.Index.ANALYZED));
				indexWriter.addDocument(doc);
			}
		}
		reader.close();
		indexWriter.close();
		
		long endTime = new Date().getTime();
		System.out.println(String.format("Takes time: %d, wrong data: %d", endTime - startTime, wrong));
	}
	
	public static void main(String[] args) throws Exception {
		Utils.loadExtDic();
		Analyzer analyzer = new IKAnalyzer();
		
		Indexer indexer1 = new Indexer("C:/orpheus/origin/taobao_song", Config.songIndexDir);
		indexer1.make(analyzer);
		
		Indexer indexer2 = new Indexer("C:/orpheus/origin/taobao_album", Config.albumIndexDir);
		indexer2.make(analyzer);
		
		Indexer indexer3 = new Indexer("C:/orpheus/origin/taobao_artist", Config.artistIndexDir);
		indexer3.make(analyzer);
	}
}
