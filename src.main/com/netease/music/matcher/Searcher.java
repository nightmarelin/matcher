package com.netease.music.matcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.core.CharacterUtil;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.spreada.utils.chinese.ZHConverter;

public abstract class Searcher {

	private int topDocs;

	private IndexSearcher indexSearcher;
	protected QueryParser queryParser;
	
	public Searcher(int topDocs, String indexDir) throws IOException {
		this.topDocs = topDocs;
		
		queryParser = new QueryParser(Version.LUCENE_34, Config.FIELD_TEXT, new IKAnalyzer());
		queryParser.setDefaultOperator(Operator.AND);
		
		Directory dic = new SimpleFSDirectory(new File(indexDir));
		indexSearcher = new IndexSearcher(IndexReader.open(dic));
	}
	
	public ScoreDoc[] search(String name) throws IOException, ParseException {
		Query query = queryParse(name);
		TopDocs topDocs = indexSearcher.search(query, this.topDocs);
		return topDocs.scoreDocs;
	}
	
	public Document doc(int docID) throws CorruptIndexException, IOException {
		return indexSearcher.doc(docID);
	}
	
	public void close() {
		try {
			indexSearcher.close();
		} catch (IOException e) {
			// nothing todo
		}
	}
	
	public void setParserOperator(Operator operator) {
		queryParser.setDefaultOperator(operator);
	}
	
	public abstract Query queryParse(String name) throws ParseException;
}

class SongSearcher extends Searcher {

	public SongSearcher(int topDocs, String indexDir) throws IOException {
		super(topDocs, indexDir);
	}

	@Override
	public Query queryParse(String name) throws ParseException {
		ZHConverter zhConverter = ZHConverter.getInstance(ZHConverter.SIMPLIFIED);
		Query query = queryParser.parse(QueryParser.escape(zhConverter.convert(name.toLowerCase())));
		return query;
	}
}

class AlbumSearcher extends SongSearcher {

	public AlbumSearcher(int topDocs, String indexDir) throws IOException {
		super(topDocs, indexDir);
	}
	
}

class ArtistSearcher extends Searcher {
	
	public ArtistSearcher(int topDocs, String indexDir) throws IOException {
		super(topDocs, indexDir);
	}

	@Override
	public Query queryParse(String name) throws ParseException {
		ZHConverter zhConverter = ZHConverter.getInstance(ZHConverter.SIMPLIFIED);
		Query query = queryParser.parse(QueryParser.escape(zhConverter.convert(name.toLowerCase())));
		
		return query;
	}
	
	protected String[] parseCJKToken(String name) {
		if (name == null || name.length() == 0)
			return null;
		
		List<String> result = new ArrayList<String>();
		StringBuilder sb = null;
		char[] charList = name.toCharArray();
		for (int i = 0, l = charList.length, v = 0; i < l; ++i) {
			v = CharacterUtil.identifyCharType(charList[i]);
			if (v == CharacterUtil.CHAR_CHINESE || v == CharacterUtil.CHAR_OTHER_CJK) {
				if (sb == null)
					sb = new StringBuilder();
				sb.append(charList[i]);
			} else {
				if (sb != null && sb.length() > 1) {
					result.add(sb.toString());
					sb = null;
				}
			}
		}
		if (sb != null) {
			if (sb.length() > 1 || sb.toString().equals(name.trim()))
				result.add(sb.toString());
//			else
//				System.out.println(sb.toString() + ',' + name);
		}
		
		String[] tmpResult = new String[result.size()];
		return result.toArray(tmpResult);
	}
	
}
