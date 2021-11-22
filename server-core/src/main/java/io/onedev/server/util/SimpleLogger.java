package io.onedev.server.util;

import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;

import io.onedev.server.buildspec.job.log.StyleBuilder;

public abstract class SimpleLogger {

	private static final Pattern EOL_PATTERN = Pattern.compile("\r?\n");
	
	public void log(String message, Throwable t) {
		StringBuilder builder = new StringBuilder(message);
		for (String line: Splitter.on(EOL_PATTERN).split(Throwables.getStackTraceAsString(t))) {
			if (builder.length() == 0)
				builder.append(line);
			else
				builder.append("\n    ").append(line);
		}
		log(builder.toString());
	}

	public abstract void log(String message, StyleBuilder styleBuilder);

	public void log(String message) {
		log(message, new StyleBuilder());
	}
	
	public void error(String message, Throwable t) {
	}

	public void error(String message) {
	}
	
	public void warning(String message, Throwable t) {
	}

	public void warning(String message) {
	}

	public void emphasize(String message, Throwable t) {
	}

	public void emphasize(String message) {
	}
	
}
