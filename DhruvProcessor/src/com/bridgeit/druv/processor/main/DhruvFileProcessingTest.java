/************************************************************************************************
 *    Code added By Gurunath patil
 * 
 *    Code Updated Date 20/12/2019
 *    Details:-XMV,YMV,ZMV,MACHIN_Config and NCL File in Every folder
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
/***************************** FILE PATH VARIABLES START *******************************************/
		 File XmvPath=null;
		 File YmvPath=null;
		 File ZmvPath=null;
		 File MachineConfigPath=null;
		 File NclFilePath=null;
		 String outputDestinationPath = null;
		 File SourceCODEpath=null;
		 File SourceCODE_Filepath=null;
		 File DestinationCODEpath=null;
		 File DestinationCODE_Filepath=null;
 /***************************** FILE PATH VARIABLES START *******************************************/

 /*********************************** FILE PATH ***************************************************/
		 String OutPutParentFolderPath=PropertiesPath.getProperty("OutPutParentFolderPath");
		 String SourceCode_FilePath=PropertiesPath.getProperty("SourceCode_FilePath");
 /**********************************FILE PATH END*******************************************************/
		 
 /********** OUTPUT FILE PATH FOLDER,IF ANY CODE FOLDER IS THERE THEN CODE WILL REMOVE IT ****************************************/
		 final File OutPutNclFilePath= new File(OutPutParentFolderPath);
		 final boolean isDebug = false;
		 File OutputfileFolder = new File(OutPutParentFolderPath);
		 File[] listOfSourceFolder = OutputfileFolder.listFiles();
		
		for (int Srs = 0; Srs < listOfSourceFolder.length; Srs++) {
			if (!listOfSourceFolder[Srs].isFile()) {
				String RootFolderName = listOfSourceFolder[Srs].getName();
				File OutputfilerootFolder = new File(OutPutParentFolderPath+RootFolderName);
				deleteDirectory(new File(OutputfilerootFolder.toString()));	
			}
		} 
 /******************* *********    CODE FOLDER REMOVE CODE END  **************************************************/			
	
 /******************* *********   GETTING SOURCE ROOT FOLDER OF ALL CODE AND XMV YMV ZMV CONFIG NCL FILE  **************************************************/			

		final File SourceFilesncl = new File(SourceCode_FilePath);
		File[] listOfSourceFilesnclFiles = SourceFilesncl.listFiles();
	
		for (int i = 0; i < listOfSourceFilesnclFiles.length; i++) {
			if (!listOfSourceFilesnclFiles[i].isFile()) {

				String FolderName = listOfSourceFilesnclFiles[i].getName();
				final File SourceFilefolder = new File(SourceCode_FilePath+"/"+FolderName);
				File[] listOfSourceFiles = SourceFilefolder.listFiles();
				
	/***************** GETTING ALL NCL CONFIG XMV YMV ZMV EACH TIME START *********************  */
				for (int j = 0; j < listOfSourceFiles.length; j++) {
					if (listOfSourceFiles[j].isFile()) {
						
						for (int AllFilesCount = 0; AllFilesCount < listOfSourceFiles.length; AllFilesCount++) {
							if (listOfSourceFiles[AllFilesCount].isFile()) {
							String SourceFileName = listOfSourceFiles[AllFilesCount].getName();
							if(SourceFileName.contains("XMV")){
								XmvPath=new File(SourceCode_FilePath+"/"+FolderName+"/"+SourceFileName);
							} 
							else if(SourceFileName.contains("YMV")){
								YmvPath=new File(SourceCode_FilePath+"/"+FolderName+"/"+SourceFileName);
							} 
							else  if(SourceFileName.contains("ZMV")){
			                 ZmvPath=new File(SourceCode_FilePath+"/"+FolderName+"/"+SourceFileName);
							} 
							else  if(SourceFileName.contains(".ncl")){
								NclFilePath=new File(SourceCode_FilePath+"/"+FolderName+"/"+SourceFileName);
							}
							else  if(SourceFileName.contains("MACHINE_CONF")){
								MachineConfigPath=new File(SourceCode_FilePath+"/"+FolderName+"/"+SourceFileName);
							}
							}							
							}
		/************************  ALL NCL CONFIG XMV YMV ZMV EACH TIME END *********************  */				
						if(isDebug)
						System.out.println("_______________________________"+NclFilePath);
						String ChildFolderNAME ="";
						if (!listOfSourceFiles[0].isFile()){  ChildFolderNAME = listOfSourceFiles[0].getName();}
						File OutputfilePath = new File(OutPutParentFolderPath + FolderName);
		/*****************!!!!!!  PASSING ALL  NCL CONFIG XMV YMV ZMV FILE EACH TIME TO DHRUV MAIN CLASS FILE START !!!!!!!!!!!*********************  */				
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
	/*****************!!!!!!  PASSING ALL  NCL CONFIG XMV YMV ZMV FILE EACH TIME TO DHRUV MAIN CLASS FILE END !!!!!!!!!!!*********************  */				
							
		/*****************!!!!!!  COMPARING SOURCE OUT PUT FILES AND NEWLY GENERATED OUT PUT FILES START !!!!!!!!!!!*********************  */				
				
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
/*****************!!!!!!  COMPARING SOURCE OUT PUT FILES AND NEWLY GENERATED OUT PUT FILES START !!!!!!!!!!!*********************  */				

					
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
        return dir.delete();
    }
}
