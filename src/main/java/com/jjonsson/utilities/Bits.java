package com.jjonsson.utilities;

public final class Bits
{
	private Bits(){}

	/**
	 * Utility method for checking bit masks against a byte
	 * @param input a byte that is checked against the given mask
	 * @param mask a mask with masking bits set
	 * @return true if the bits set in the given {@link mask} is set in the {@link input} as well
	 */
	public static boolean containBits(final byte input, final byte mask)
	{
		return (input & mask) == mask;
	}
}
