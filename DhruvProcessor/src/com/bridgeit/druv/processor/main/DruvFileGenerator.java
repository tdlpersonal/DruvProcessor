package com.bridgeit.druv.processor.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.swing.JOptionPane;

import com.bridgeit.druv.processor.utils.Constants;

public class DruvFileGenerator {
	
	private static String basePath=null;
	private static DruvFileGenerator gen = null;
	private int currentFileCount = 0;
	private int currentLineCount = 0;
	BufferedWriter writer = null;
	
	private  DruvFileGenerator()
	{
		
	}
	
	public void WriteLine(String str) throws Exception
	{
		if(writer==null || currentLineCount==Constants.max_line_count-1)
		{
			createFile();
		}
		writer.write(str+"\r\n");
		currentLineCount++;
		//DruvProcessor.log("Wrote line :" + str + " in file: " + currentFileCount +":"+ currentLineCount);
	}
	
	private String getFileName()
	{
		String fileName;
		currentFileCount++;
		if(currentFileCount<10)
			fileName ="000"+currentFileCount+".TXT";
		else if(currentFileCount<100)
			fileName = "00"+currentFileCount+".TXT";
		else if(currentFileCount<1000)
			fileName = "0"+currentFileCount+".TXT";
		else 
			fileName = currentFileCount+".TXT";
		DruvProcessor.log("File: " + fileName );
		return basePath + fileName;
		
	}
	
	public void createFile() throws Exception
	{
		if(currentFileCount==9999)
			DruvProcessor.logExit("File count exceeds 9999");
		currentLineCount=0;
		if(writer!=null)
		{
			writer.write("(ENDF+00000.000,)\r\n#");
			writer.flush();
			writer.close();
			writer = null;
			
		}
		writer = new BufferedWriter(new FileWriter(getFileName()));
		writer.write("*\r\n");
	}
	
	public void complete() throws Exception
	{
		DruvProcessor.log("writer: " + (writer==null));
		if(writer!=null)
		{			
			writer.write("(ENDF+00000.000,)\r\n#");
			writer.flush();
			writer.close();
			writer = null;
		}
		
	}
	
	public void end() throws Exception
	{
		DruvProcessor.log("writer: " + (writer==null));
		if(writer!=null)
		{
			
			writer.write("(ENDO+00000.000,)\r\n#");
			writer.flush();
			writer.close();
			writer = null;
		}
		currentFileCount=0;
	}
	
	public void clearCodeFolder() throws  Exception
	{
		gen.complete();
		File f = new File(basePath);
		File[] files = f.listFiles();
//		for (int i = 0; files!=null && i < files.length; i++) {
//			files[i].delete();
//		}
		f = new File(basePath);
		System.out.println("_______________________________________________");
		
		if(!f.delete())
			DruvProcessor.log("Unabled to Delete code  folder : " + basePath);
		else 
			DruvProcessor.log("CLEARED CODE FOLDER FROM: " + basePath);
	}
	
	public static DruvFileGenerator genInstance(String strPath)
	{
		File f = new File(strPath);
		if(!f.exists() || !f.isDirectory())
			return null;
		
		basePath = strPath;
		if(gen==null)
			gen = new DruvFileGenerator();
		return gen;
	}
	
	public void init() 
	{
		File base = new File(basePath +"\\code");
		if (base.mkdir() == false)
		{	
			DruvProcessor.showErrorinUI("code folder alrady present inside " + basePath +", please delete and re-run");
			
			throw new RuntimeException("Code Folder Present");
			
		}
		basePath = basePath +"\\code\\";
		
	}

}
