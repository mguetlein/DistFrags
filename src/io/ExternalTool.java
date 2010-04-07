package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Date;

import util.StringUtil;

public class ExternalTool
{

	public static void run(final String processName, File stdOutfile, final String errorOutMatch, String command)
	{
		run(processName, stdOutfile, errorOutMatch, command, null, null);
	}

	public static void run(final String processName, File stdOutfile, final String errorOutMatch, String command,
			String env[], File dir)
	{
		try
		{
			Status.addIndent();

			final File tmpStdOutfile = new File(stdOutfile + ".tmp");
			final long starttime = new Date().getTime();
			final Process child;

			if (env == null && dir == null)
				child = Runtime.getRuntime().exec(command);
			else if (env != null && dir == null)
				child = Runtime.getRuntime().exec(command, env);
			else
				child = Runtime.getRuntime().exec(command, env, dir);

			Thread th = new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						BufferedReader buffy = new BufferedReader(new InputStreamReader(child.getInputStream()));
						PrintStream print = new PrintStream(tmpStdOutfile);
						while (true)
						{
							String s = buffy.readLine();
							if (s != null)
								print.println(s);
							else
								break;
						}
						buffy.close();
						print.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
			Thread thError = new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						BufferedReader buffy = new BufferedReader(new InputStreamReader(child.getErrorStream()));
						// Status.INFO.println();
						while (true)
						{
							String s = buffy.readLine();
							if (s != null)
							{
								if (errorOutMatch == null || s.matches(errorOutMatch))
									Status.INFO.println(Status.INDENT + processName + " "
											+ StringUtil.formatTime(new Date().getTime() - starttime) + " > " + s);
							}
							else
								break;
						}
						buffy.close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});

			th.start();
			thError.start();

			child.waitFor();
			while (thError.isAlive())
			{
				try
				{
					Thread.sleep(50);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}

			if (child.exitValue() != 0)
				throw new Error(processName + " exited with error: " + child.exitValue());

			if (!tmpStdOutfile.renameTo(stdOutfile))
				throw new Error("cannot rename tmp file");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			Status.remIndent();
		}
	}
}
