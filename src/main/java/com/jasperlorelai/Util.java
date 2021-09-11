package com.jasperlorelai;

import java.util.Arrays;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class Util {

	private static final Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([0-9A-Fa-f]{4})");

	public static String setPrecision(String str, String precision) {
		// Return value if value isn't a floating point - can't be scaled.
		try {
			float floatValue = Float.parseFloat(str);

			// Return value if precision isn't a floating point.
			int toScale = Integer.parseInt(precision);

			// Return the scaled value.
			return BigDecimal.valueOf(floatValue).setScale(toScale, RoundingMode.HALF_UP).toString();
		} catch (NumberFormatException | NullPointerException ignored) {
			return str;
		}
	}

	public static String joinArgs(String[] args, int index) {
		return StringUtils.join(Arrays.copyOfRange(args, index, args.length), '_');
	}

	public static String unescapeUnicode(String string) {
		Matcher matcher = UNICODE_PATTERN.matcher(string);
		while (matcher.find()) {
			char unicode = (char) Integer.parseInt(matcher.group(1), 16);
			string = string.replace(matcher.group(), unicode + "");
			matcher = UNICODE_PATTERN.matcher(string);
		}
		return string;
	}

}
