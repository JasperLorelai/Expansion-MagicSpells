package eu.jasperlorelai.mspapiexpansion;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.nisovin.magicspells.util.config.ConfigData;

public class Util {

	protected static String setPrecision(String str, String precision) {
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

	protected static String miniMessage(String string) {
		return com.nisovin.magicspells.util.Util.getMiniMessageFromLegacy(string);
	}

	/**
	 * Version compatibility method.
	 */
	protected static float getFloatData(Object object, String getter, Class<?> clazz) {
		try {
			return switch (clazz.getMethod(getter).invoke(object)) {
				case Float data -> data;
				case ConfigData<?> data when data.isConstant() -> (float) data.get();
				case null, default -> 0;
			};
		} catch (Exception ignored) {}
		return 0;
	}

}
