/*
JVerifier2 an hash code verifier
Copyright (C) 2017-2018 Davide Sestili

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package it.dsestili.jverifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import it.dsestili.jhashcode.core.Core;
import it.dsestili.jhashcode.core.Utils;

public class JVerifier 
{
	public static void main(String[] args) 
	{
		if(args.length == 3)
		{
			new JVerifier().verify(args[0], args[1], args[2]);
		}
		else
		{
			System.out.println("Usage: param 1: file name, param 2: algorithm, param 3: baseDir");
		}
	}
	
	private void verify(String param1, String param2, String param3)
	{
		long start = System.currentTimeMillis();
		
		File file = new File(param1);
		
		if(!file.exists())
		{
			System.out.println("File does not exist");
			return;
		}
		
		if(!file.isFile())
		{
			System.out.println("Is not a file");
			return;
		}
		
		File baseDir = new File(param3);
		
		if(!baseDir.exists())
		{
			System.out.println("baseDir does not exist");
			return;
		}
		
		if(baseDir.isFile())
		{
			System.out.println("baseDir is a file");
			return;
		}
		
		try 
		{
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader reader = new BufferedReader(isr);
			
			int okCount = 0, doesNotMatchCount = 0, notFoundCount = 0, errorCount = 0;

			File currentFile = null;
			
			String lineOfText;
			while((lineOfText = reader.readLine()) != null)
			{
				try
				{
					StringBuilder hashStringBuilder = new StringBuilder();
					StringBuilder fileNameStringBuilder = new StringBuilder();
					int i;
					for(i = 0; i < lineOfText.length(); i++)
					{
						char c = lineOfText.charAt(i);
						
						if(c == ' ')
						{
							i += 2;
							break;
						}
						
						hashStringBuilder.append(c);
					}

					String hash = hashStringBuilder.toString();
					
					while(i < lineOfText.length())
					{
						char c = lineOfText.charAt(i);
						fileNameStringBuilder.append(c);
						i++;
					}

					String fileName = fileNameStringBuilder.toString();
					
					currentFile = new File(param3 + fileName);

					System.out.print(currentFile.getAbsolutePath());

					Core core = new Core(currentFile, param2);
					String generatedHash = core.generateHash();
					
					if(generatedHash.equalsIgnoreCase(hash))
					{
						System.out.println(" OK");
						okCount++;
					}
					else
					{
						System.out.println(" Does not match");
						doesNotMatchCount++;
					}
				}
				catch(FileNotFoundException e)
				{
					System.out.println(" not found");
					notFoundCount++;
				}
				catch(Throwable e)
				{
					System.out.println(" error");
					errorCount++;
				}
			}
			
			reader.close();
			System.out.println("OK: " + okCount + " - Does not match: " + doesNotMatchCount + " - Not found: " + notFoundCount + " - Errors: " + errorCount);
		} 
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		
		long elapsed = System.currentTimeMillis() - start;
		System.out.println("Elapsed time: " + Utils.getElapsedTime(elapsed, true));

		File ascSignatureLowerCase = new File(file.getAbsolutePath() + ".asc");
		File gpgSignatureLowerCase = new File(file.getAbsolutePath() + ".gpg");
		
		if(ascSignatureLowerCase.exists())
		{
			try
			{
				checkSign(ascSignatureLowerCase);
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
		}
		else if(gpgSignatureLowerCase.exists())
		{
			try
			{
				checkSign(gpgSignatureLowerCase);
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
		}
		else
		{
			System.out.println("Signature missing");
		}
	}
	
	private void checkSign(File file) throws Throwable
	{
		System.out.print(file.getAbsolutePath());
		ProcessBuilder builder = new ProcessBuilder("gpg", "--verify", file.getAbsolutePath());
		Process process = builder.start();
		int exitCode = process.waitFor();
		if(exitCode == 0)
		{
			System.out.println(" Good signature");
		}
		else
		{
			System.out.println(" BAD Signature");
		}
	}
}
