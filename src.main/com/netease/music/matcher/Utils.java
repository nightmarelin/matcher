package com.netease.music.matcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.wltea.analyzer.cfg.DefualtConfig;
import org.wltea.analyzer.core.CharacterUtil;
import org.wltea.analyzer.dic.Dictionary;

import com.spreada.utils.chinese.ZHConverter;

public class Utils {
	
	public static Dictionary getDictionary() {
		Dictionary.initial(DefualtConfig.getInstance());
		Dictionary dictionary = Dictionary.getSingleton();
		
		return dictionary;
	}
	
	public static Dictionary loadExtDic() throws Exception {
		Dictionary dictionary = Utils.getDictionary();
	
		BufferedReader reader = new BufferedReader(new FileReader(Config.artistDicFile));
		String buffer = null;
		while ((buffer = reader.readLine()) != null) {
			buffer = buffer.trim();
			if (buffer.length() > 0)
				dictionary.addWords(Arrays.asList(buffer));
		}
		reader.close();
		return dictionary;
	}
	
	/**
	 * Generate artist dictionary from origin file exported from table 'Music_Artist'
	 * 
	 * @return {void}
	 */
	protected static void generateArtistDic() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(Config.artistOrgFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(Config.artistDicFile));
		BufferedWriter missed = new BufferedWriter(new FileWriter(Config.artistDicFile + ".miss"));
		
		ZHConverter zhConverter = ZHConverter.getInstance(ZHConverter.SIMPLIFIED);
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		String buffer = null;
		while ((buffer = reader.readLine()) != null) {
			buffer = buffer.trim();
			if (isCJKName(buffer)) {
				map.put(zhConverter.convert(buffer), true);
			} else {
				missed.write(buffer);
				missed.write(Character.LINE_SEPARATOR);
			}
		}
		Set<String> keys = map.keySet();
		for (String key : keys) {
			writer.write(key);
			writer.write(Character.LINE_SEPARATOR);
		}
		reader.close();
		writer.close();
		missed.close();
	}
	
	protected static boolean isCJKName(String name) {
		if (name == null || name.length() == 0) return false;
		
		int count = 0;
		char[] array = name.toCharArray();
		for (int i = 0, l = array.length, v = 0; i < l; ++i) {
			v = CharacterUtil.identifyCharType(array[i]);
			if (v == CharacterUtil.CHAR_CHINESE || v == CharacterUtil.CHAR_OTHER_CJK)
				count++;
		}
		
		return count > 1;
	}
	
	public static String mergeResult(String tid, String sid, float[] values) {
		StringBuilder sb = new StringBuilder();
		sb.append(tid);
		sb.append('\t');
		sb.append(sid);
		sb.append('\t');
		sb.append(String.valueOf(values[0]));
		sb.append('\t');
		sb.append(String.valueOf(values[1]));
		sb.append('\t');
		sb.append(String.valueOf(values[2]));
		return sb.toString();
	} 
	
	public static void main(String[] args) throws Exception {
		generateArtistDic();
	}
}
