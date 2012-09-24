package com.netease.music.matcher;

import java.io.File;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.dic.Hit;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.spreada.utils.chinese.ZHConverter;

public class Test {
	
	public static void testDic() {
		Dictionary dic = Utils.getDictionary();
		Hit hit = dic.matchInMainDict("".toCharArray());
		System.out.println(hit.isMatch());
		
		hit = dic.matchInMainDict("".toCharArray());
		System.out.println(hit.isMatch());
		
		hit = dic.matchInMainDict("".toCharArray());
		System.out.println(hit.isMatch());
		
		hit = dic.matchInMainDict("".toCharArray());
		System.out.println(hit.isMatch());
	}
	
	public static void testParser() throws Exception {
		Analyzer analyzer = new IKAnalyzer();
		QueryParser queryParser = new QueryParser(Version.LUCENE_34, Config.FIELD_TEXT, analyzer);
		queryParser.setDefaultOperator(Operator.AND);
		
		TermQuery termQuery1 = new TermQuery(new Term(Config.FIELD_TEXT, ""));
//		TermQuery termQuery2 = new TermQuery(new Term(Config.FIELD_TEXT, ""));
//		TermQuery termQuery3 = new TermQuery(new Term(Config.FIELD_TEXT, ""));
		BooleanQuery query = new BooleanQuery();
		query.add(termQuery1, Occur.MUST);
//		query.add(termQuery2, Occur.MUST);
//		query.add(termQuery3, Occur.MUST);
		System.out.println(query.toString());
		Directory dic0 = new SimpleFSDirectory(new File(Config.artistIndexDir));
		IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(dic0));
		TopDocs topDocs = indexSearcher.search(query, 4000000);
		System.out.println("----------------------" + topDocs.scoreDocs.length);
		for (int i = 0, l = topDocs.scoreDocs.length; i < l; ++i) {
			Document doc = indexSearcher.doc(topDocs.scoreDocs[i].doc);
			String id = doc.get("id");
			String text = doc.get("text");
		}
		indexSearcher.close();
	}
	
	public static void testQuery() throws Exception {
		Analyzer analyzer = new IKAnalyzer();
		QueryParser queryParser = new QueryParser(Version.LUCENE_34, Config.FIELD_TEXT, analyzer);
		queryParser.setDefaultOperator(Operator.AND);
		
		Query query1 = queryParser.parse("");
		System.out.println(query1.toString());
		System.out.println(query1.toString(Config.FIELD_TEXT));
		Directory artistDic = new SimpleFSDirectory(new File(Config.artistIndexDir));
		IndexSearcher artistIndexSearcher = new IndexSearcher(IndexReader.open(artistDic));
		TopDocs topDocs1 = artistIndexSearcher.search(query1, 100000);
		System.out.println(topDocs1.scoreDocs.length);
		artistIndexSearcher.close();
		
		Query query2 = queryParser.parse(QueryParser.escape("Jolin超级珍藏世纪�?Disc 1"));
		System.out.println(query2.toString());
		System.out.println(query2.toString(Config.FIELD_TEXT));
		Directory albumDic = new SimpleFSDirectory(new File(Config.albumIndexDir));
		IndexSearcher albumIndexSearcher = new IndexSearcher(IndexReader.open(albumDic));
		TopDocs topDocs2 = albumIndexSearcher.search(query2, 1000);
		System.out.println(topDocs2.scoreDocs.length);
		albumIndexSearcher.close();

		Query query3 = queryParser.parse(QueryParser.escape(""));
		System.out.println(query3.toString());
		System.out.println(query3.toString(Config.FIELD_TEXT));
		Directory songDic = new SimpleFSDirectory(new File(Config.songIndexDir));
		IndexSearcher songIndexSearcher = new IndexSearcher(IndexReader.open(songDic));
		TopDocs topDocs3 = songIndexSearcher.search(query3, 1000);
		System.out.println(topDocs3.scoreDocs.length);
		songIndexSearcher.close();
	}

	public static void main(String[] args) throws Exception {
//		testParser();
		ZHConverter c = ZHConverter.getInstance(ZHConverter.TRADITIONAL);
		System.out.println(c.convert("翁 虹什么变了模样"));

	}
}
