package eu.jasperlorelai.mspapiexpansion;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Util {

	public static String setPrecision(String str, String precision) {
		if (precision == null) return str;
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

	public static String miniMessage(String string) {
		return com.nisovin.magicspells.util.Util.getMiniMessageFromLegacy(string);
	}

}
