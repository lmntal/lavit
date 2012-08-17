package lavit.util;

public final class IntUtils
{
	private IntUtils() { }

	public static int clamp(int x, int min, int max)
	{
		return Math.max(min, Math.min(x, max));
	}
}
