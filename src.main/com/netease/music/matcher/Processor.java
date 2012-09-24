package com.netease.music.matcher;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public abstract class Processor {

	private BufferedWriter matchedWriter = null;
	
	public Processor(String matchedFile) {
		try {
			matchedWriter = new BufferedWriter(new FileWriter(matchedFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void output(String result) {
		if (matchedWriter != null) {
			try {
				matchedWriter.write(result);
				matchedWriter.write(Character.LINE_SEPARATOR);
			} catch (IOException e) {
				System.err.println(result);
			}
		}
	}

	public abstract String process(String tid, Map<String, float[]> scoreMatrix);

	public void close() {
		try {
			matchedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
