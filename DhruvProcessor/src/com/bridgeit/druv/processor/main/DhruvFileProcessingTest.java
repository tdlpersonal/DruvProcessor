/************************************************************************************************
 *    code added By Gurunath patil
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 ****************************************************************************************************/
package com.bridgeit.druv.processor.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.Properties;
import java.io.BufferedReader;
import java.io.FileReader;

public class DhruvFileProcessingTest {
	public static void main(String[] args) throws IOException, ParseException {
		Properties PropertiesPath=new Properties();
		FileInputStream Pathip= new FileInputStream("C:/Dhruv/DruvProcessor/path_config.properties");
		PropertiesPath.load(Pathip);
		System.out.println(PropertiesPath.getProperty("OutPutParentFolderPath"));
		
		 File XmvPath=null;
		 File YmvPath=null;
		 File ZmvPath=null;
		 File MachineConfigPath=null;
		 File NclFilePath=null;
		 /***********************************/
		 String OutPutParentFolderPath=PropertiesPath.getProperty("OutPutParentFolderPath");
		 String XMT_YMV_ZMV_Path=PropertiesPath.getProperty("XMT_YMV_ZMV_Path");
		 String MachineConfigRootPath=PropertiesPath.getProperty("MachineConfigRootPath");
		 String SourceCode_FilePath=PropertiesPath.getProperty("SourceCode_FilePath");
		 /***********************************/
		 
		 final File OutPutNclFilePath= new File(OutPutParentFolderPath);
		 final boolean isDebug = false;
		
		final File MachineConfigfolder = new File(MachineConfigRootPath);
		File[] listOfmachineConfiFiles = MachineConfigfolder.listFiles();
		for (int i = 0; i < listOfmachineConfiFiles.length; i++) {
			if (listOfmachineConfiFiles[i].isFile()) {
				String FileNAME = listOfmachineConfiFiles[i].getName();
				MachineConfigPath=new File(MachineConfigRootPath+"/"+FileNAME);
			}
			else if (listOfmachineConfiFiles[i].isDirectory()) {
			}
		}
		
		final File ConfigXmvYmvZmv = new File(XMT_YMV_ZMV_Path);
		File[] listOfXmvYmvZmvFiles = ConfigXmvYmvZmv.listFiles();
  
		for (int i = 0; i < listOfXmvYmvZmvFiles.length; i++) {
			if (listOfXmvYmvZmvFiles[i].isFile()) {
				String FileNAME = listOfXmvYmvZmvFiles[i].getName();
			///	System.out.println("File " + FileNAME);
				if(FileNAME.contains("XMV")){
					XmvPath=new File(XMT_YMV_ZMV_Path+"/"+FileNAME);
				} 
				else if(FileNAME.contains("YMV")){
					YmvPath=new File(XMT_YMV_ZMV_Path+"/"+FileNAME);
				} 
				else  if(FileNAME.contains("ZMV")){
                 ZmvPath=new File(XMT_YMV_ZMV_Path+"/"+FileNAME);
				} 

			} else if (listOfXmvYmvZmvFiles[i].isDirectory()) {
				//System.out.println("Directory " + listOfXmvYmvZmvFiles[i].getName());
			}
		}
		  //   String DestiPath ="C:/MachineOutPut/";
		          
		
		File OutputfileFolder = new File(OutPutParentFolderPath);
		File[] listOfSourceFolder = OutputfileFolder.listFiles();
		
		for (int Srs = 0; Srs < listOfSourceFolder.length; Srs++) {
			if (!listOfSourceFolder[Srs].isFile()) {
				String RootFolderName = listOfSourceFolder[Srs].getName();
				File OutputfilerootFolder = new File(OutPutParentFolderPath+RootFolderName);
				deleteDirectory(new File(OutputfilerootFolder.toString()));	
			}
		}
			
		final File SourceFilesncl = new File(SourceCode_FilePath);
		File[] listOfSourceFilesnclFiles = SourceFilesncl.listFiles();
				
		for (int i = 0; i < listOfSourceFilesnclFiles.length; i++) {
			if (!listOfSourceFilesnclFiles[i].isFile()) {

				String FolderName = listOfSourceFilesnclFiles[i].getName();
				final File SourceFilefolder = new File(SourceCode_FilePath+"/"+FolderName);
				File[] listOfSourceFilesncl = SourceFilefolder.listFiles();
				
				 String outputDestinationPath = null;
				 File SourceCODEpath=null;
				 File SourceCODE_Filepath=null;
				 File DestinationCODEpath=null;
				 File DestinationCODE_Filepath=null;
				 
				for (int j = 0; j < listOfSourceFilesncl.length; j++) {
					if (listOfSourceFilesncl[j].isFile()) {
						String NclFileNAme = listOfSourceFilesncl[j].getName();
						NclFilePath=new File(SourceCode_FilePath+"/"+FolderName+"/"+NclFileNAme);
						if(isDebug)
						System.out.println("_______________________________"+NclFilePath);
						String ChildFolderNAME ="";
						if (!listOfSourceFilesncl[0].isFile()){  ChildFolderNAME = listOfSourceFilesncl[0].getName();}
						File OutputfilePath = new File(OutPutParentFolderPath + FolderName);
						boolean bool = OutputfilePath.mkdir();  
						if (bool) {
							outputDestinationPath = OutputfilePath.toString();
							String[] arguments = new String[6];
							arguments[0]=MachineConfigPath.toString();
							arguments[1]=XmvPath.toString();
							arguments[2]=YmvPath.toString();
							arguments[3]=ZmvPath.toString();
							arguments[4]=NclFilePath.toString();
							arguments[5]=outputDestinationPath.toString();
							if(isDebug)
							System.out.println("Arguments"+arguments);
							DruvProcessor.main(arguments);
							try{
							SourceCODEpath=new File(SourceCode_FilePath+"/"+FolderName+"/"+ChildFolderNAME);
							DestinationCODEpath=new File(OutPutParentFolderPath + FolderName+"/"+ChildFolderNAME);
							File[] SorceFolderOutPutTXTfiles = SourceCODEpath.listFiles();
							File[] DestinationFolderOutPutTXTfiles = DestinationCODEpath.listFiles();
							for (int DisfileCount = 0; DisfileCount < DestinationFolderOutPutTXTfiles.length; DisfileCount++) {
								if (DestinationFolderOutPutTXTfiles[DisfileCount].isFile()) {
									String DistOutFileName = DestinationFolderOutPutTXTfiles[DisfileCount].getName();
									SourceCODE_Filepath=new File(SourceCode_FilePath+"/"+FolderName+"/"+ChildFolderNAME+"/"+DistOutFileName);
									DestinationCODE_Filepath= new File(OutPutParentFolderPath + FolderName+"/"+ChildFolderNAME+"/"+DistOutFileName);
									if(isDebug)
									System.out.println("________________"+DistOutFileName+"___________OutPut-TXT-FileName__&&___SourceOutPut-TXT-FileName");
								 BufferedReader Source_OutPut_File = new BufferedReader(new FileReader(SourceCODE_Filepath));
						         BufferedReader Destination_OutPut_File = new BufferedReader(new FileReader(DestinationCODE_Filepath));
						         
						         String DestinationLine = Source_OutPut_File.readLine();						         
						         String SourceLine = Destination_OutPut_File.readLine();
						          
						         boolean areEqual = true;						          
						         int lineNum = 1;
						          
						         while (DestinationLine != null || SourceLine != null)
						         {
						             if(DestinationLine == null || SourceLine == null){areEqual = false;break;}
						             else if(! DestinationLine.equalsIgnoreCase(SourceLine)){areEqual = false;break;}
						              
						             DestinationLine = Destination_OutPut_File.readLine();
						             SourceLine = Source_OutPut_File.readLine();
						             lineNum++;
						         }
						          
						         if(areEqual){
						        	 if(isDebug)
						        	 System.out.println("____________________"+DistOutFileName+"_________________Two files have same content.");
						        
						         }
						         else{
						        	 System.err.println("Error:"+DistOutFileName +" - have different content at Input / Output. They differ at line "+lineNum);
						        	 System.err.println("Output file has "+DestinationLine+" and Input file has "+SourceLine+" at line "+lineNum); 
						        	 }
						          
						         Source_OutPut_File.close();
						         Destination_OutPut_File.close();
								}
							}
							} catch (Exception e) {
								
								 e.printStackTrace();
							}
						         
							
						} else {
							if(isDebug)
							System.out.println(FolderName + ":Sorry couldn’t create specified Folder");
						}
					
					}
					
				}
			} else if (listOfSourceFilesnclFiles[i].isDirectory()) {
				if(isDebug)
				System.out.println("Directory " + listOfSourceFilesnclFiles[i].getName());
			}
		}
		if(isDebug){
		System.out.println(MachineConfigPath + "______________MatchinConfigPath");
		System.out.println(XmvPath + "______________XMV");
		System.out.println(YmvPath + "______________YMV");
		System.out.println(ZmvPath + "______________XMV");
		System.out.println(NclFilePath +"______________NCL");
		System.out.println(OutPutNclFilePath + "______________Out Put Folder");
		}
	}

	private static void CopyNCLFile(File source, File dest) throws IOException {
		Files.copy(source.toPath(), dest.toPath());
	}
	
	public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(children[i]);
                if (!success) {
                    return false;
                }
            }
        }
       // System.out.println("removing file or directory : " + dir.getName());
        return dir.delete();
    }
}
