package org.kercheval.gradle.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ExecuteCommand
{
	private final boolean debug = true;

	private final List<String> output = new ArrayList<String>();
	private final int exitValue;

	public ExecuteCommand(final File executeDir, final String... command)
		throws IOException
	{
		debugPrintCommand(command);

		final Process process = new ProcessBuilder().directory(executeDir).command(command).start();
		final BufferedReader input = new BufferedReader(new InputStreamReader(
			process.getInputStream()));
		String line = input.readLine();
		while (line != null)

		{
			output.add(line);
			line = input.readLine();
		}
		input.close();
		exitValue = process.exitValue();

		debugPrintOutput();
	}

	private void debugPrintCommand(final String... command)
	{
		if (debug)
		{
			System.out.print("Executing command: \"");
			boolean doneOnce = false;
			for (final String arg : command)
			{
				if (doneOnce)
				{
					System.out.print(" ");
				}
				else
				{
					doneOnce = true;
				}
				System.out.print(arg);
			}
			System.out.println("\"");
		}
	}

	private void debugPrintOutput()
	{
		if (debug)
		{
			System.out.println("  Returned: " + exitValue);
			for (final String outputLine : output)
			{
				System.out.println("  :-: " + outputLine);
			}
		}
	}

	public int getExitValue()
	{
		return exitValue;
	}

	public List<String> getOutput()
	{
		return output;
	}
}
