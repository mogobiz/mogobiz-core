/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.utils;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class PatternEngine {
    public static void main(String[] args) throws Exception {
        String prefix = "C:/Projects/IPER2010";
        if ("hayssams".equals(System.getProperty("user.name"))) {
            prefix = "/Users/hayssams/workspaces/IPER2010/SNAPSHOT/trunk";
        }
        String inputDir = prefix
                + "/iper2010/web-app/WEB-INF/site/__defaultsite__";
        String outputDir = prefix + "/iper2010/web-app/site";
        String companyCode = "technoagile";
        String paramDir = prefix + "/iper2010/web-app/WEB-INF/site/param";

        PatternEngine engine = new PatternEngine(companyCode,
                new File(inputDir), new File(outputDir), new File(paramDir));
        engine.treat();
    }

	private String companyCode;
	private File inputDir;
	private File outputDir;
	private File paramDir;
	private Properties attributes = new Properties();
	private Set<File> headers = new HashSet<File>();
	private List<String> errors = new ArrayList<String>();
	private int iteratorLevel = 0;

	public PatternEngine(String companyCode, File inputDir, File outputDir,
			File paramDir) throws Exception {
		if (inputDir.exists() && companyCode != null
				&& companyCode.length() > 0) {
			this.companyCode = companyCode;
			this.inputDir = inputDir;
			this.outputDir = new File(outputDir, companyCode);
			this.paramDir = paramDir;
			if (paramDir != null) {
				File attrs = new File(paramDir, "attributes.properties");
				if (attrs.isFile() && attrs.exists()) {
					try {
						attributes.load(new FileInputStream(attrs));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (!this.inputDir.getAbsolutePath().equals(
					this.outputDir.getAbsolutePath())) {
				try {
					FileUtils.deleteDirectory(this.outputDir);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.outputDir.mkdirs();
			}
		} else {
			throw new Exception(
					"Input folder must exist and company code cannot be null");
		}
	}

	public void treat() {
		File inputEventDir = new File(inputDir, "event");
		String[] filenames = inputEventDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String fname) {
				return fname.endsWith(".html");
			}
		});
		String[] properties = inputDir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String fname) {
				return fname.endsWith(".properties");
			}
		});
		try {
			boolean deleteAfter;
			if (!inputDir.getAbsolutePath().equals(outputDir.getAbsolutePath())) {
				outputDir.mkdirs();
				List<String> errs = copyFiles(new File(inputDir, "event"), outputDir, true);
				errors.addAll(errs);
				errs = copyFiles(new File(inputDir, "site/"+this.companyCode), outputDir, true);
				errors.addAll(errs);
				deleteAfter = false;

			} else {
				deleteAfter = true;
			}
			
			for (String filename : filenames) {
				File file = new File(inputEventDir, filename);
				treatFile(file); // on genere le fichier gsp
				File outputFile = new File(this.outputDir, filename);
				outputFile.delete();  // on supprime le fichier html
				if (deleteAfter)
					file.delete();
			}
			
			for (String property : properties) {
				File file = new File(inputDir, property);
				File outputFile = new File(this.outputDir, property);
				copyFile(file, outputFile);
				if (deleteAfter)
					file.delete();
			}
			
			for (File header : headers) {
				File sourceHeaderFile = new File(inputEventDir, header
						.getName().replace(".gsp", ".html"));
				treatFile(sourceHeaderFile);
				File dir = header.getParentFile();
				String name = header.getName();
				File dest = new File(dir, "_" + name);
				header.renameTo(dest);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String error : errors) {
			System.err.println(error);
		}
	}

	private void treatFile(File file) {
		DataInputStream in = null;
		BufferedWriter out = null;
		File outputFile = new File(outputDir, file.getName().replace(".html",
				".gsp"));
		try {
			FileInputStream fstream;
			out = new BufferedWriter(new FileWriter(outputFile));
			fstream = new FileInputStream(file);
			// Get the object of DataInputStream
			in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int linenum = 0;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				strLine = strLine.replaceAll("__defaultsite__", this.companyCode);
				linenum++;
				// On regarde si une balise gsp est presente danslefichier html
				// en entree
				// auquel cas on l'ignore.
				Pattern invalidPattern = Pattern.compile("(<\\w+:.*?>|<%.*)");
				Matcher invalidMatch = invalidPattern.matcher(strLine);
				boolean result = invalidMatch.find();
				if (result) {
					errors.add("ERROR: invalid input in file "
							+ file.getName()
							+ " at line "
							+ linenum
							+ ":"
							+ strLine.substring(invalidMatch.start(),
									invalidMatch.end()));
				} else {
					Pattern p = Pattern
							.compile("\\{/?(block:|value:|message:|page:|include:)[a-zA-Z\\.]+\\}");
					Matcher m = p.matcher(strLine);
					StringBuffer sb = new StringBuffer();
					result = m.find();
					if (!result) {
						out.write(strLine);
					} else {
						while (result) {
							int start = m.start();
							int end = m.end();
							String strMatch = strLine.substring(start, end);
							String replaceTxt = "";
							try {
								replaceTxt = replace(strMatch);
							} catch (Exception e) {
								replaceTxt = "Line " + linenum
										+ ": !!!INVALID TAG!!!" + strMatch.replace("{", "").replace("}", "");
							}
							m.appendReplacement(sb,
									replaceTxt.replaceAll("\\$", "\\\\\\$"));
							result = m.find();
						}
						m.appendTail(sb);

						out.write(sb.toString());
					}
					out.newLine();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// Close the input stream
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			String content;
			content = loadFile(outputDir, outputFile.getName(), "");
			System.out.println(content);
			System.out
					.println("_______________________________________________________________________________________________________");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String handleIterator(String str) {
		if (str.indexOf("<g:each") >=0) {
			iteratorLevel++;
			str = str.replace("<g:each", "<g:each var=\"it"+ iteratorLevel+"\"");
		}
		if (str.indexOf("</g:each>") >=0) {
			iteratorLevel--;
		}
		if (str.indexOf("${it.") >=0) {
			str = str.replaceAll("${it.", "${it"+ iteratorLevel+".");
		}
		
		return str;
	}
	private String replace(String strMatch) throws Exception {
		boolean isEnd = strMatch.startsWith("{/block:");
		boolean isStart = strMatch.startsWith("{block:");
		boolean isValue = strMatch.startsWith("{value:");
		boolean isMessage = strMatch.startsWith("{message:");
		boolean isInclude = strMatch.startsWith("{include:");
		String name;
		try {
			if (isStart) {
				name = strMatch.substring("{block:".length(),
						strMatch.length() - 1);
				return handleIterator(loadFile(paramDir, name, ".start.txt"));
			} else if (isEnd) {
				name = strMatch.substring("{/block:".length(),
						strMatch.length() - 1);
				return handleIterator(loadFile(paramDir, name, ".end.txt"));
			} else if (isValue) {
				name = strMatch.substring("{value:".length(),
						strMatch.length() - 1);
				return handleIterator(loadFile(paramDir, name, ".value.txt"));
				// return "{it." + name + "}";
			} else if (isMessage) {
				name = strMatch.substring("{message:".length(),
						strMatch.length() - 1);
				if (name.contains("__")) {
					errors.add("ERROR: Invalid message code " + name
							+ ". '__' not allowed");
					return "!!!INVALID MESSAGE!!!";
				} else {
					return "<g:message code=\"__" + companyCode + "__" + name
							+ "\" />";
				}
				// name = strMatch.substring(1, strMatch.length() - 1);
				// return "{it." + name + "}";
			} else if (isInclude) {
				name = strMatch.substring("{include:".length(),
						strMatch.length() - 1);
				if (name.endsWith(".html")) {
					name = name.substring(0, name.length() - ".html".length());
				}
				// File includeFile = new File(outputDir, name);
				// if (includeFile.exists())
				headers.add(new File(this.outputDir, name + ".gsp"));
				return "<g:render template=\"/site/" + this.companyCode + "/"
						+ name + "\"/>";
				// return "<g:include view=\"" + name + ".gsp\"/>";
			} else {
				return "!!!INVALID TAG!!!"  + strMatch.replace("{", "").replace("}", "");
			}
		} catch (Exception e) {
			if (isValue) {
				name = strMatch.substring("{value:".length(),
						strMatch.length() - 1);
				return "${it"+iteratorLevel+"." + name + "}";
			} else {
				throw e;
			}
		}
	}

	private String loadFile(File paramDir, String name, String suffix)
			throws Exception {
		String filename;
		filename = name + suffix;
		File paramFile = new File(paramDir, filename);
		if (paramFile.exists()) {
			DataInputStream in = null;
			StringBuffer loadedFile = new StringBuffer();
			try {
				FileInputStream fstream;
				fstream = new FileInputStream(paramFile);
				in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));
				String strLine;
				while ((strLine = br.readLine()) != null) {
					//loadedFile.append(strLine + "\n");
					loadedFile.append(strLine);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return loadedFile.toString();
		} else {
			throw new Exception("!!!INVALID TAG!!!"  + name.replace("{", "").replace("}", ""));
		}
	}
	
	public static void copyFile(File src, File dest) throws IOException {
		// This was not a directory, so lets just copy the file
		FileInputStream fin = null;
		FileOutputStream fout = null;
		byte[] buffer = new byte[4096]; // Buffer 4K at a time (you can
										// change this).
		int bytesRead;
		try {
			// open the files for input and output
			fin = new FileInputStream(src);
			fout = new FileOutputStream(dest);
			// while bytesRead indicates a successful read, lets write...
			while ((bytesRead = fin.read(buffer)) >= 0) {
				fout.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) { // Error copying file...
			IOException wrapper = new IOException(
					"copyFiles: Unable to copy file: "
							+ src.getAbsolutePath() + "to"
							+ dest.getAbsolutePath() + ".");
			wrapper.initCause(e);
			wrapper.setStackTrace(e.getStackTrace());
			throw wrapper;
		} finally { // Ensure that the files are closed (if they were open).
			if (fin != null) {
				fin.close();
			}
			if (fout != null) {
				fout.close();
			}
		}
	}

	public static List<String> copyFiles(File src, File dest,
			boolean excludeHTMLFiles) throws IOException {
		List<String> errors = new ArrayList<String>();
		// Check to ensure that the source is valid...
		if (!src.exists()) {
			throw new IOException("copyFiles: Can not find source: "
					+ src.getAbsolutePath() + ".");
		} else if (!src.canRead()) { // check to ensure we have rights to the
										// source...
			throw new IOException("copyFiles: No right to source: "
					+ src.getAbsolutePath() + ".");
		}
		// is this a directory copy?
		if (src.isDirectory()) {
			if (!dest.exists()) { // does the destination already exist?
				// if not we need to make it exist if possible (note this is
				// mkdirs not mkdir)
				if (!dest.mkdirs()) {
					throw new IOException(
							"copyFiles: Could not create direcotry: "
									+ dest.getAbsolutePath() + ".");
				}
			}
			// get a listing of files...
			String list[] = src.list();
			// copy all the files in the list.
			for (int i = 0; i < list.length; i++) {
				File dest1 = new File(dest, list[i]);
				File src1 = new File(src, list[i]);
				List<String> errs = copyFiles(src1, dest1, excludeHTMLFiles);
				errors.addAll(errs);
			}
		} else if ((!excludeHTMLFiles && src.getName().endsWith(".html"))
				|| isValidResource(src.getName())) {
			copyFile(src, dest);
		} else if (!src.getName().endsWith(".html")) {
			errors.add("WARNING: Ignoring file " + src.getName());
		}
		return errors;
	}

	private static boolean isValidResource(String name) {
		// return name.endsWith(".css") || name.endsWith(".js")
		// || name.endsWith(".png") || name.endsWith(".ico")
		// || name.endsWith(".bmp") || name.endsWith(".properties")
		// || name.endsWith(".jpg") || name.endsWith(".swf")
		// || name.endsWith(".gif");
		return !name.endsWith(".gsp") && !name.endsWith(".jsp");
	}

	public static boolean isValidZip(final File file) {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			Enumeration zipEntries = zipFile.entries();
			while (zipEntries.hasMoreElements()) {
				ZipEntry entry = ((ZipEntry) zipEntries.nextElement());
				String name = entry.getName();
				System.out.println(name);
				if (!entry.isDirectory() && !name.endsWith(".html")
						&& !isValidResource(name)) {
					System.out.println("*********************" + name);
					return false;
				}
			}
			return true;
		} catch (ZipException e) {
			return false;
		} catch (IOException e) {
			return false;
		} finally {
			try {
				if (zipFile != null) {
					zipFile.close();
					zipFile = null;
				}
			} catch (IOException e) {
			}
		}
	}
}
