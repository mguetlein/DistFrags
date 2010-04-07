package io;

import java.io.OutputStream;
import java.io.PrintStream;

public class Status
{
	public static PrintStream NULL_PRINT_STREAM = new PrintStream(new OutputStream()
	{
		public void write(int b)
		{}
	});

	public static final String TAB = "  ";

	public static String INDENT = "";

	public static void addIndent()
	{
		INDENT += TAB;
	}

	public static void remIndent()
	{
		INDENT = INDENT.substring(0, INDENT.length() - TAB.length());
		INFO.println();
	}

	public static PrintStream INFO = System.out;

	public static PrintStream WARN = System.err;

	public static SmartIOInfo SMART_INFO = new SmartIOInfo(INFO);

	static
	{
		// System.setOut(NULL_PRINT_STREAM);
	}

}
