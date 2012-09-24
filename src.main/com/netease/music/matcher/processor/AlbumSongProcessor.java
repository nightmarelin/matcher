package com.netease.music.matcher.processor;

import java.util.Map;

import com.netease.music.matcher.Processor;
import com.netease.music.matcher.Utils;

public class AlbumSongProcessor extends Processor {

	public AlbumSongProcessor(String matchedFile) {
		super(matchedFile);
	}

	@Override
	public String process(String tid, Map<String, float[]> scoreMatrix) {
		String topKey = null;
		float topScore = 0.0f;
		for (String key : scoreMatrix.keySet()) {
			float[] value = scoreMatrix.get(key);
			if (value[1] <= 0 || value[2] <= 0) continue;
			float score = value[0] + value[1] + value[2];
			if (score > topScore) {
				topKey = key;
				topScore = score;
			}
			output(Utils.mergeResult(tid, key, value));
		}
		return topKey;
	}

}
