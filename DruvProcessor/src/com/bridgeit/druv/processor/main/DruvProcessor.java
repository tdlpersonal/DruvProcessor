package com.bridgeit.druv.processor.main;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.math.util.MathUtils;

import com.bridgeit.druv.processor.domains.FeedRates;
import com.bridgeit.druv.processor.domains.FreqMinValue;
import com.bridgeit.druv.processor.domains.MVValue;
import com.bridgeit.druv.processor.domains.MachineConfiguration;
import com.bridgeit.druv.processor.domains.Rapid_Frequency;
import com.bridgeit.druv.processor.utils.Circles;
import com.bridgeit.druv.processor.utils.Constants;
import com.bridgeit.druv.processor.utils.Point;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class DruvProcessor {
	private static DruvFileGenerator gen = null;
	MachineConfiguration conf = null;
	HashMap<String, String> flags = new HashMap<String, String>();
	HashMap<String, Boolean> setFlags = new HashMap<String, Boolean>();
	static NumberFormat numberFormat = NumberFormat.getInstance();
	boolean isVFDxWritten = false, isVFDyWritten = false, isVFDzWritten = false;
	double currentX, currentY, currentZ, selectedG1FeedRate = 0;
	int lineNum = 0;
	FreqMinValue[] mvs = null;
	HashMap<Double, FreqMinValue> mapMV;
	double currentG1Feed = -1;
	DecimalFormat df = new DecimalFormat("#####.###");

	public static void main(String[] args) throws ParseException {

		try {
			numberFormat.setMaximumFractionDigits(3);
			numberFormat.setMinimumFractionDigits(3);
			numberFormat.setMaximumIntegerDigits(5);
			numberFormat.setMinimumIntegerDigits(5);
			numberFormat.setGroupingUsed(false);
			long start = Calendar.getInstance().getTimeInMillis();
			DruvProcessor dp = new DruvProcessor();
			dp.init(args);
			dp.loadMVFiles(args);
			dp.validateGMCodeFile(args[4]);
			dp.processGM(args[4], args[5]);
			log("Completed in :" + (Calendar.getInstance().getTimeInMillis() - start) + " ms");
		} catch (Exception e) {
			e.printStackTrace();
			logExit("Clearing code folder as  we received error");
		}
	}

	public void runJob(MachineConfiguration conf, String gmFile,String destn) throws Exception {
		numberFormat.setMaximumFractionDigits(3);
		numberFormat.setMinimumFractionDigits(3);
		numberFormat.setMaximumIntegerDigits(5);
		numberFormat.setMinimumIntegerDigits(5);
		numberFormat.setGroupingUsed(false);
		long start = Calendar.getInstance().getTimeInMillis();
		DruvProcessor dp = new DruvProcessor();
		dp.conf = conf;
		mvs = conf.getFreq_mvs();
		mapMV = getMVMap(mvs);
		dp.validateGMCodeFile(gmFile);
		dp.processGM(gmFile, destn);
		log("Completed in :" + (Calendar.getInstance().getTimeInMillis() - start) + " ms");
	

	}

	private void processGM(String gmFile, String path) throws Exception {

		String currentCode = "";
		BufferedReader ignored = new BufferedReader(new FileReader("conf\\TOBEIGNORED.TXT"));
		ArrayList<String> ignoredList = new ArrayList<String>();
		while (ignored.ready()) {
			ignoredList.add(ignored.readLine());
		}
		ignored.close();
		ignored = null;
		// log(ignoredList);
		// TODO Auto-generated method stub
		boolean isInSideNBlock = false;
		log("PROESSING GM CODE FILE");
		gen = DruvFileGenerator.genInstance(path);
		
		try
		
		{
			gen.init();
		
		}
		catch(Exception e)
		{
			return;
		}
		BufferedReader reader = new BufferedReader(new FileReader(gmFile));
		while (reader.ready()) {
			String line = reader.readLine().trim();
			if (line.contains("(")) {
				do {
					if (!line.contains(")"))
						logExit("ERROR in GM CODE FILE, Line : " + lineNum
								+ ", Invalid Comment. missing closing bracket");
					int first = line.indexOf("(");
					int second = line.indexOf(")");
					line = line.substring(0, first) + line.substring(second + 1);

				} while (line.contains("("));

			}
			log("PROCESSING GM LINE: " + line);
			boolean toIgnore = false;
			for (String ignore : ignoredList) {
				if (line.startsWith(ignore)) {
					toIgnore = true;
					break;
				}
			}
			if (toIgnore)
				continue;
			lineNum++;
			if (line.contains("(")) {
				if (!line.contains(")"))
					logExit("ERROR in GM CODE FILE, Line : " + lineNum + ", Invalid Comment. missing closing bracket");
				line = line.substring(0, line.indexOf('(')).trim();
			}
			if (line.startsWith("N")) {
				resetVFDWritten();
				gen.complete();
				isInSideNBlock = true;
				continue;
			} else if (line.startsWith("T")) {
				if (line.startsWith("T0101")) {
					gen.WriteLine("(LxxX+00000.000,)");
					gen.WriteLine("(LxxZ+00000.000,)");
					continue;
				} else
					logExit("T Option : " + line + " is not supported in this version");
			} else if (line.length() >= 3) {
				String code = line.substring(0, 3);
				if (flags.containsKey(code)) {
					if (!isInSideNBlock)
						continue;
					else {
						int codeLength = line.length();
						// log("codeLength:"+codeLength);
						if (codeLength % 3 != 0)
							logExit("ERROR in GM CODE FILE, Line : " + lineNum + ", Invalid line : " + line);
						int parts = codeLength / 3;
						ArrayList<String> codes = new ArrayList<String>();
						for (int i = 0, j = 0; i < parts; i++, j = j + 3) // 0123456789ABCDE
						{
							codes.add(line.substring(j, j + 3));
						}
						log("Flags:" + codes);
						for (Iterator iterator = codes.iterator(); iterator.hasNext();) {
							String codepart = (String) iterator.next();
							// log("---FOUND FLAG---" + codepart);
							// log(flags.get(codepart));
							String flagAction = flags.get(codepart);
							if ("SETFLAG".equals(flagAction))
								setFlags.put(codepart, true);
							else if ("TOGGLEFLAG".equals(flagAction))
								setFlags.put(codepart, !setFlags.get(codepart));
						}
					}

				}

			}
			if (line.startsWith("M00")) {
				gen.WriteLine("(CPxx+00000.000,)");
			} else if (line.startsWith("G28")) {
				if (!line.trim().equals("G28"))
					logExit("ERROR: G28 with explicit u,v,w are not supported. use ONLY G28");
				writeG28();
			} else if ((line.startsWith("G0") || line.startsWith("G00")) && !line.startsWith("G01")) {
				currentCode = "G0";
				if (line.startsWith("G00"))
					processGO(line.substring(3));
				else
					processGO(line.substring(2));
			} else if (line.startsWith("X") || line.startsWith("Y") || line.startsWith("Z")) {
				if (currentCode == "G0")
					processGO(line);
				else if (currentCode == "G1")
					processG1(line);
			} else if (line.startsWith("G01") || (line.startsWith("G1") && !line.startsWith("G18"))) {
				currentCode = "G1";
				if (line.startsWith("G01"))
					processG1(line.substring(3));
				else
					processG1(line.substring(2));
				// log(line);
			} else if (line.startsWith("M8") || line.startsWith("M08")) {
				System.err.println("Alert : Please link electrically with Spindle Relays, and press 'N' to continue");
				waitForEnter();
			} else if (line.startsWith("M4") || line.startsWith("M04")) {

				String freq = "TBD";
				if (line.startsWith("M4"))
					freq = line.substring(2);
				else
					freq = line.substring(3);
				if (freq.contains("s"))
					freq = freq.substring(1);
				if (freq != null && (freq.trim().equals("") || freq.trim().equals(" ")))
					logExit("Invalid Line Number: " + lineNum + ", Incorect Spindle Frequency: "
							+ (freq == null || freq.equals("") || freq.equals(" ") ? "BLANK" : freq));
				if (conf.getSpindle_rpm() != Integer.parseInt(freq))
					logExit("Frequency Mismatch, Spindle Freq from conf :  " + conf.getSpindle_rpm()
							+ ", whereas M4S line has : " + freq);
				else
					gen.WriteLine("(SDON+00000.000,)");
				log(line);
			} else if (line.startsWith("M05") || line.startsWith("M5")) {
				gen.WriteLine("(SDOF+00000.000,)");
			} else if (line.startsWith("M9") || line.startsWith("M09")) {
				System.err.println("Alert : Please link electrically with Spindle Relays, and press 'N' to continue");
				waitForEnter();
			} else if ((line.startsWith("M3") && !line.startsWith("M30")) || line.startsWith("M03")) {
				log(line);
				String freq = line.substring(3);
				log(freq);
				if (line.startsWith("M03"))
					freq = freq.substring(1);
				log(freq);
				if (conf.getSpindle_rpm() != Integer.parseInt(freq))
					logExit("Frequency Mismatch, Spindle Freq from conf :  " + conf.getSpindle_rpm()
							+ ", whereas M3S line has : " + freq);
				else
					gen.WriteLine("(SDON-00000.000,)");
				log(line);
			} 
			else if ( (line.startsWith("G2") || line.startsWith("G02")) && !line.startsWith("G20") && !line.startsWith("G21") && !line.startsWith("G28")
					
					) {
				currentCode = "G2";
				if (line.startsWith("G02"))
					processG2G3(line.substring(3),"G2");
				else
					processG2G3(line.substring(2),"G3");
			}else if ( (line.startsWith("G3") || line.startsWith("G03")) && !line.startsWith("G20") && !line.startsWith("G21") && !line.startsWith("G28")
					
					) {
				currentCode = "G3";
				if (line.startsWith("G03"))
					processG2G3(line.substring(3),"G3");
				else
					processG2G3(line.substring(2),"G3");
			}
			else
				System.err.println("TO BE IMPLEMENTED: " + line);
		}
		gen.end();
	}

	private void processG2G3(String line, String mode) throws  Exception {
		// TODO Auto-generated method stub
		resetVFDWritten();
		log("Processing line in " + mode +  ":  " + line);
		int op = determineXYZ(line);
		if (op == 0)
			logExit("ERROR: IncorrectLine : " + line);
		FeedRates feedRate = getFeedRate(line);
		boolean isG18 = setFlags.containsKey("G18");
		switch (op) {
		case Constants.X_Z: 
			writeG2G3Projections('X', 'Z', currentX, currentZ, line, feedRate, mode);
			break;
		case Constants.Z_X: 
			writeG2G3Projections('Z', 'X', currentZ, currentX, line, feedRate, mode);
			break;
		
		}
	}

	private void writeG28() throws Exception {
		// TODO Auto-generated method stub
		gen.WriteLine("(HxxZ+00000.000,)");
		if (!setFlags.containsKey("G18"))
			gen.WriteLine("(HxxY+00000.000,)");
		gen.WriteLine("(HxxX+00000.000,)");
		gen.WriteLine("(SZAZ+00000.000,)");
		if (!setFlags.containsKey("G18"))
			gen.WriteLine("(SZAY+00000.000,)");
		gen.WriteLine("(SZAX+00000.000,)");
		currentX = 0;
		if (!setFlags.containsKey("G18"))
			currentY = 0;
		currentZ = 0;

	}

	private void waitForEnter() {
		// TODO Auto-generated method stub
		Scanner sc = new Scanner(System.in);
		char c = sc.next().charAt(0);
		if (c == 'N' || c == 'n')
			return;
		waitForEnter();
	}

	private void resetVFDWritten() {
		// TODO Auto-generated method stub
		isVFDxWritten = false;
		isVFDyWritten = false;
		isVFDzWritten = false;

	}

	private void writeGO_MRA(char axis, String line) throws Exception {
		if (line.startsWith("" + axis))
			line = line.substring(1);

		char sign = '+';
		if (line.contains("-"))
			sign = '-';
		if (sign == '-')
			line = line.substring(1);
		log("Line:" + line);
		double value = Double.parseDouble(line);
		if (setFlags.containsKey("G20"))
			value = value * 25.4;
		switch (axis) {
		case 'X':
			currentX = !setFlags.containsKey("G18") ? value : value / 2;
			format("MRAX", sign, currentX);
			break;
		case 'Y':
			currentY = value;
			format("MRAY", sign, value);
			break;
		case 'Z':
			currentZ = value;
			format("MRAZ", sign, value);
			break;
		}
	}

	private FeedRates getFeedRate(String  line) throws Exception {
		double feedRate = 0;
		if (line.indexOf("F") > -1) {
			
			feedRate = Double.parseDouble(line.substring(line.indexOf("F") + 1));
			selectedG1FeedRate = feedRate;
		} else
			feedRate = selectedG1FeedRate;
		FeedRates[] rates = conf.getFeed_rates();
		FeedRates selectedRate = null;
		boolean foundRate = false;
		double xFreq = 0, yFreq = 0, zFreq = 0;
		int xDelay = 0, yDelay = 0, zDelay = 0;

		for (int i = 0; i < rates.length; i++) {
			FeedRates rate = rates[i];
			if (rate.getFeed_rate() == feedRate) {
				foundRate = true;
				selectedRate = rate;
				xFreq = rate.getFreq_x();
				yFreq = rate.getFreq_y();
				zFreq = rate.getFreq_z();
				xDelay = rate.getDelay_x();
				yDelay = rate.getDelay_y();
				zDelay = rate.getDelay_z();
				break;
			}

		}
		if (!foundRate)
			logExit("ERROR in Line : " + lineNum + ", FeedRate from line " + feedRate
					+ " is not present in machine configuration feedrates");
		return selectedRate;
		
	}
	
	private void processG1(String line) throws Exception {

		resetVFDWritten();
		log("Processing line in G1 :  " + line);
		int op = determineXYZ(line);
		if (op == 0)
			logExit("ERROR: IncorrectLine : " + line);
		FeedRates feedRate = getFeedRate(line);
		
		boolean isG18 = setFlags.containsKey("G18");
		switch (op) {
		// X2.5F0.025
		
		case Constants.ONLY_X:
			double destX = 0;
			if (line.indexOf("F") > -1)
				destX = Double.parseDouble(line.substring(1, line.indexOf("F")));
			else
				destX = Double.parseDouble(line.substring(1));
			if (setFlags.containsKey("G20"))
				destX = destX * 25.4;
			if (isG18)
				destX = destX / 2;
			format("VFDX", '+', feedRate.getFreq_x());
			if (feedRate.getDelay_x()== 1) {
				format("WTNX", '+', 1);
				
				if (destX < 0)
					format("MFAX", '-', Math.abs(destX));
				else
					format("MFAX", '+', destX);			
				
				format("WTFX", '+', 0);
			} else {
				format("WTNX", '+', feedRate.getDelay_x());
				writeG1Projections('X', '+', currentX, destX, feedRate);
				format("WTFX", '+', 0);
			}
			log("INSIDE G1 ONLY_X");
			break;			
		case Constants.ONLY_Y:
			double destY = 0;
			if (line.indexOf("F") > -1)
				destY = Double.parseDouble(line.substring(1, line.indexOf("F")));
			else
				destY = Double.parseDouble(line.substring(1));
			if (setFlags.containsKey("G20"))
				destY = destY * 25.4;

			format("VFDY", '+', feedRate.getFreq_y());
			if (feedRate.getDelay_y() == 1) {
				format("WTNY", '+', 1);
				
				if (destY < 0)
					format("MFAY", '-', Math.abs(destY));
				else
					format("MFAY", '+', destY);
				
				format("WTFY", '+', 0);
			} else {
				format("WTNY", '+', feedRate.getDelay_y());
				writeG1Projections('Y', '+', currentY, destY, feedRate);
				format("WTFY", '+', 0);
			}
			log("INSIDE G1 ONLY_Y");
			break;			
		case Constants.ONLY_Z:
			double destZ = 0;
			if (line.indexOf("F") > -1)
				destZ = Double.parseDouble(line.substring(1, line.indexOf("F")));
			else
				destZ = Double.parseDouble(line.substring(1));
			if (setFlags.containsKey("G20"))
				destZ = destZ * 25.4;

			format("VFDZ", '+', feedRate.getFreq_z());
			if (feedRate.getDelay_z()== 1) {
				format("WTNZ", '+', 1);				
				if (destZ < 0)
					format("MFAZ", '-', Math.abs(destZ));
				else
					format("MFAZ", '+', destZ);
				
				format("WTFZ", '+', 0);
			} else {
				format("WTNZ", '+', feedRate.getDelay_z());
				writeG1Projections('Z', '+', currentZ, destZ, feedRate);
				format("WTFZ", '+', 0);
			}
			log("INSIDE G1 ONLY_Z");
			break;
		case Constants.X_Y:
			// X20.0Y10.0F0.1
			
			writeG1ProjectionsTwoAxis('X', 'Y', currentX, currentY, line, feedRate);
			break;
		case Constants.X_Z:
			
			writeG1ProjectionsTwoAxis('X', 'Z', currentX, currentZ, line, feedRate);
			break;

		case Constants.Y_Z:
			
			writeG1ProjectionsTwoAxis('Y', 'Z', currentY, currentZ, line, feedRate);
			break;
		case Constants.Y_X:
			
			writeG1ProjectionsTwoAxis('Y', 'X', currentY, currentX, line, feedRate);
			break;

		case Constants.Z_X:
			
			writeG1ProjectionsTwoAxis('Z', 'X', currentZ, currentX, line, feedRate);
			break;
		case Constants.Z_Y:
		
			writeG1ProjectionsTwoAxis('Z', 'Y', currentZ, currentY, line, feedRate);
			break;

		default:
			log("NOT SUPPORTED: " + op);
			break;

		}
	}

	private void writeG2G3Projections(char first,char second,double firstOrigin, double secondOrigin,
			String line, FeedRates selectedRate,String mode) throws Exception {
		log("SRC: " + currentX + ":" + currentY + ":" + currentZ);
		double firstDest = 0, secondDest = 0;
		firstDest = Double.parseDouble(line.substring(1, line.indexOf(second)));
		secondDest = Double.parseDouble(line.substring(line.indexOf(second) + 1, line.indexOf("R")));
		double radius = Double.parseDouble(line.substring(line.indexOf("R")+1,line.indexOf("F")));
		if (setFlags.containsKey("G18")) {
			if (first == 'X')
				firstDest = firstDest / 2;
			if (second == 'X')
				secondDest = secondDest / 2;
		}
		if (setFlags.containsKey("G20")) {
			firstDest = firstDest * 25.4;
			secondDest = secondDest * 25.4;
		}
		log("Procesing  "+mode +": (" + first + "," + second + ") -> (" + firstOrigin + "," + secondOrigin
				+ ") -> (" + firstDest + "," + secondDest + ")");
		double firstFreq = 'X' == first ? selectedRate.getFreq_x()
				: ('Y' == first ? selectedRate.getFreq_y() : selectedRate.getFreq_z());
		double secondFreq = 'X' == second ? selectedRate.getFreq_x()
				: ('Y' == second ? selectedRate.getFreq_y() : selectedRate.getFreq_z());

		format("VFD" + first, '+', firstFreq);
		format("VFD" + second, '+', secondFreq);
		int firstdelay = 'X' == first ? selectedRate.getDelay_x()
				: ('Y' == first ? selectedRate.getDelay_y() : selectedRate.getDelay_z());
		int seconddelay = 'X' == second ? selectedRate.getDelay_x()
				: ('Y' == second ? selectedRate.getDelay_y() : selectedRate.getDelay_z());

		format("WTN" + first, '+', firstdelay);
		format("WTN" + second, '+', seconddelay);
		
		FreqMinValue[] mins = conf.getFreq_mvs();
		FreqMinValue selectedMVF1 = null;
		for (int i = 0; i < mins.length; i++) {
			FreqMinValue mvF = mins[i];
			if (mvF.getFreq() == (first == 'X' ? selectedRate.getFreq_x()
					: (first == 'Y' ? selectedRate.getFreq_y() : selectedRate.getFreq_z()))) {
				selectedMVF1 = mvF;
				break;
			}

		}
		if (selectedMVF1 == null)
			logExit("ERROR: No MV Frequency " + (first == 'X' ? selectedRate.getFreq_x()
					: (first == 'Y' ? selectedRate.getFreq_y() : selectedRate.getFreq_z())) + " found in XMV");

		FreqMinValue selectedMVF2 = null;
		for (int i = 0; i < mins.length; i++) {
			FreqMinValue mvF = mins[i];
			if (mvF.getFreq() == (second == 'X' ? selectedRate.getFreq_x()
					: (second == 'Y' ? selectedRate.getFreq_y() : selectedRate.getFreq_z()))) {
				selectedMVF2 = mvF;
				break;
			}

		}
		if (selectedMVF2 == null)
			logExit("ERROR: No MV Frequency " + (second == 'X' ? selectedRate.getFreq_x()
					: (second == 'Y' ? selectedRate.getFreq_y() : selectedRate.getFreq_z())) + " found in XMV");

		double firstPos = 0, firstNeg = 0, secondPos = 0, secondNeg = 0;

		firstPos = first == 'X' ? selectedMVF1.getPos_mvx()
				: (first == 'Y' ? selectedMVF1.getPos_mvy() : selectedMVF1.getPos_mvz());
		firstNeg = first == 'X' ? selectedMVF1.getNeg_mvx()
				: (first == 'Y' ? selectedMVF1.getNeg_mvy() : selectedMVF1.getNeg_mvz());

		secondPos = second == 'X' ? selectedMVF1.getPos_mvx()
				: (second == 'Y' ? selectedMVF1.getPos_mvy() : selectedMVF1.getPos_mvz());
		firstNeg = first == 'X' ? selectedMVF1.getNeg_mvx()
				: (first == 'Y' ? selectedMVF1.getNeg_mvy() : selectedMVF1.getNeg_mvz());

		ArrayList<Point> pointsList = new Circles().getPointsOfCircle(new Point(firstOrigin,secondOrigin), new Point(firstDest,secondDest), radius, 0.001, mode, firstPos, firstNeg, secondPos, secondNeg);
		System.out.println(pointsList.size());
		
		for(int i=0;i<pointsList.size();i++)
		{
			
			Point p = pointsList.get(i);
			if(i>0)
			{
				Point tmp = pointsList.get(i-1);
				if(tmp.equals(p))
					continue;
			}
			formatG2G3(first, p.x<0?'-':'+', p.x);
			formatG2G3(second, p.y<0?'-':'+', p.y);
		}
		
		format("WTF" + first, '+', 0);
		format("WTF" + second, '+', 0);

		switch (first) {
		case 'X':
			currentX = firstDest;
			break;
		case 'Y':
			currentY = firstDest;
			break;
		case 'Z':
			currentZ = firstDest;
			break;
		}

		switch (second) {
		case 'X':
			currentX = secondDest;
			break;
		case 'Y':
			currentY = secondDest;
			break;
		case 'Z':
			currentZ = secondDest;
			break;
		}

		log("Dest: " + currentX + ":" + currentY + ":" + currentZ);

		
		
	}
	
	private void writeG1ProjectionsTwoAxis(char first, char second, double firstOrigin, double secondOrigin,
			String line, FeedRates selectedRate) throws Exception {
		// TODO Auto-generated method stub
		// X20.0Y10.0F0.1

		log("SRC: " + currentX + ":" + currentY + ":" + currentZ);
		double firstDest = 0, secondDest = 0;
		firstDest = Double.parseDouble(line.substring(1, line.indexOf(second)));
		if (line.indexOf("F") > -1)
			secondDest = Double.parseDouble(line.substring(line.indexOf(second) + 1, line.indexOf("F")));
		else
			secondDest = Double.parseDouble(line.substring(line.indexOf(second)));
		if (setFlags.containsKey("G18")) {
			if (first == 'X')
				firstDest = firstDest / 2;
			if (second == 'X')
				secondDest = secondDest / 2;
		}
		if (setFlags.containsKey("G20")) {
			firstDest = firstDest * 25.4;
			secondDest = secondDest * 25.4;
		}
		log("Procesing Two Axis  G1 : axis  : (" + first + "," + second + ") -> (" + firstOrigin + "," + secondOrigin
				+ ") -> (" + firstDest + "," + secondDest + ")");
		/*
		 * (VFDX+00002.000,) (VFDZ+00005.000,) (WTNX+00300.000,)
		 * (WTNZ+00200.000,) (MFAX+00010.000,) (MFAZ+00010.000,)
		 * (WTFX+00000.000,) (WTFZ+00000.000,)
		 */

		double firstFreq = 'X' == first ? selectedRate.getFreq_x()
				: ('Y' == first ? selectedRate.getFreq_y() : selectedRate.getFreq_z());
		double secondFreq = 'X' == second ? selectedRate.getFreq_x()
				: ('Y' == second ? selectedRate.getFreq_y() : selectedRate.getFreq_z());

		format("VFD" + first, '+', firstFreq);
		format("VFD" + second, '+', secondFreq);
		int firstdelay = 'X' == first ? selectedRate.getDelay_x()
				: ('Y' == first ? selectedRate.getDelay_y() : selectedRate.getDelay_z());
		int seconddelay = 'X' == second ? selectedRate.getDelay_x()
				: ('Y' == second ? selectedRate.getDelay_y() : selectedRate.getDelay_z());

		format("WTN" + first, '+', firstdelay);
		format("WTN" + second, '+', seconddelay);

		double slope = round((secondDest - secondOrigin) / (firstDest - firstOrigin));		

		FreqMinValue[] mins = conf.getFreq_mvs();
		FreqMinValue selectedMVF1 = null;
		for (int i = 0; i < mins.length; i++) {
			FreqMinValue mvF = mins[i];
			if (mvF.getFreq() == (first == 'X' ? selectedRate.getFreq_x()
					: (first == 'Y' ? selectedRate.getFreq_y() : selectedRate.getFreq_z()))) {
				selectedMVF1 = mvF;
				break;
			}

		}
		if (selectedMVF1 == null)
			logExit("ERROR: No MV Frequency " + (first == 'X' ? selectedRate.getFreq_x()
					: (first == 'Y' ? selectedRate.getFreq_y() : selectedRate.getFreq_z())) + " found in XMV");

		FreqMinValue selectedMVF2 = null;
		for (int i = 0; i < mins.length; i++) {
			FreqMinValue mvF = mins[i];
			if (mvF.getFreq() == (second == 'X' ? selectedRate.getFreq_x()
					: (second == 'Y' ? selectedRate.getFreq_y() : selectedRate.getFreq_z()))) {
				selectedMVF2 = mvF;
				break;
			}

		}
		if (selectedMVF2 == null)
			logExit("ERROR: No MV Frequency " + (second == 'X' ? selectedRate.getFreq_x()
					: (second == 'Y' ? selectedRate.getFreq_y() : selectedRate.getFreq_z())) + " found in XMV");

		double firstPos = 0, firstNeg = 0, secondPos = 0, secondNeg = 0;

		firstPos = first == 'X' ? selectedMVF1.getPos_mvx()
				: (first == 'Y' ? selectedMVF1.getPos_mvy() : selectedMVF1.getPos_mvz());
		firstNeg = first == 'X' ? selectedMVF1.getNeg_mvx()
				: (first == 'Y' ? selectedMVF1.getNeg_mvy() : selectedMVF1.getNeg_mvz());

		secondPos = second == 'X' ? selectedMVF1.getPos_mvx()
				: (second == 'Y' ? selectedMVF1.getPos_mvy() : selectedMVF1.getPos_mvz());
		firstNeg = first == 'X' ? selectedMVF1.getNeg_mvx()
				: (first == 'Y' ? selectedMVF1.getNeg_mvy() : selectedMVF1.getNeg_mvz());

		/* TDL : Commented out to cimplyfy the interpolations 
			double next1, next2, nextStep1 = round(firstOrigin + firstPos), nextStep2 = round(secondOrigin + secondPos);
		    if (firstPos <= secondPos) {
			// know x, calculate y
			next1 = round(firstOrigin + firstPos); // we have x2			
					
			do {
				// y2 = m(x2-x1) + y1
				next2 = round(slope * (next1 - firstOrigin) + secondOrigin);
				
				if (next2 > secondDest)
					next2 = secondDest;
				if (next2 >= nextStep2) {
					// write
					if (next1 < 0)
						formatG1(first, '-', next1);
					else
						formatG1(first, '+', next1);
					if (next2 < 0)
						formatG1(second, '-', next2);
					else
						formatG1(second, '+', next2);
					nextStep2 = round(next2 + secondPos);
					log("WROTE: (" + next1 + "," + next2 + ")");
				} else {

				}
				next1 = round(next1 + firstPos); // get nextX2
			} while (next1 <= firstDest);

		} else {

			next2 = round(secondOrigin + secondPos); // we have y2
			do {
				// x2= (y2-y1)/m + x1

				next1 = round(((next2 - secondOrigin) / slope) + firstOrigin);
				if (next1 > firstDest)
					next1 = firstDest;
				if (next1 >= nextStep1) {
					// write
					if (next1 < 0)
						formatG1(first, '-', next1);
					else
						formatG1(first, '+', next1);
					if (next2 < 0)
						formatG1(second, '-', next2);
					else
						formatG1(second, '+', next2);
					nextStep1 = round(next1 + firstPos);

				} else {

				}
				next2 = round(next2 + secondPos); // get nextX2
			} while (next2 <= secondDest);
		}
*/		
		
		/**
		 * Logic for interpolation 
		 * We have the following values with us 
		 * a. (firstOrigin,secondOrigin) -> (firstDest,SecondDest)
		 * b. slope : 
		 * c. firstPos,firstNeg : positive and negative min values for first axis
		 * d. secondPos,secondNeg : positive and negative min values for second axis
		 */
		
		Point source = new Point(firstOrigin,secondOrigin);
		Point dest = new Point(firstDest,secondDest);
		ArrayList<Point> allPoints = new ArrayList<Point>();
		ArrayList<Point> selectedPoints = new ArrayList<Point>();
		allPoints.add(source);
		// polulate all points using // y2 = m(x2-x1) + y1 with 0.001 increment on x 
		double increment = 0.25;
		
		if(dest.x-source.x<0) // we are moving to left ( e.g from 5 to 2 OR from -10  to -11... so next incremental value has to be greater than dest
		{
			increment = increment*-1;
			for(double d = source.x; d>=dest.x;d+=increment)
				allPoints.add(new Point(d, slope * (d - firstOrigin) + secondOrigin));
		}
		else
			for(double d = source.x; d<=dest.x;d+=increment)
				allPoints.add(new Point(d, slope * (d - firstOrigin) + secondOrigin));
		
		allPoints.add(dest);
		
		// now iterate through arrayList and select only those points that fit within the MV
		if(allPoints.size()>0)
		{
			
			Point prev = allPoints.get(0);
			for(int i=1;i<allPoints.size();i++)
			{
				
				Point next = allPoints.get(i);
				boolean rejected = false; // set this boolean to true if next point should not be written because it has distance 
				
				if(next.x-prev.x<0 && Math.abs(next.x-prev.x) < firstNeg)
					rejected = true; // ( next.x-prev.x negative means you are moving to left, neg MV should be considered. reject if diff is less than minMV
				if(next.x-prev.x>=0 && Math.abs(next.x-prev.x) < firstPos)
					rejected = true; // ( next.x-prev.x positive means you are moving to right , pos MV should be considered. reject if diff is less than minMV
				if(next.y-prev.y<0 && Math.abs(next.y-prev.y) < secondNeg)
					rejected = true; // ( next.x-prev.x negative means you are moving to left, neg MV should be considered. reject if diff is less than minMV
				if(next.y-prev.y>=0 && Math.abs(next.y-prev.y) < secondPos)
					rejected = true; // ( next.x-prev.x positive means you are moving to right , pos MV should be considered. reject if diff is less than minMV
							
				if(!rejected)
				{
					if (next.x < 0)
						formatG1(first, '-', next.x);
					else
						formatG1(first, '+', next.x);
					if (next.y < 0)
						formatG1(second, '-', next.y);
					else
						formatG1(second, '+', next.y);
					prev = next; // assign written point to prev so that next comparison will be done referring to it 
				}
			}
		}
		
		
		format("WTF" + first, '+', 0);
		format("WTF" + second, '+', 0);

		switch (first) {
		case 'X':
			currentX = firstDest;
			break;
		case 'Y':
			currentY = firstDest;
			break;
		case 'Z':
			currentZ = firstDest;
			break;
		}

		switch (second) {
		case 'X':
			currentX = secondDest;
			break;
		case 'Y':
			currentY = secondDest;
			break;
		case 'Z':
			currentZ = secondDest;
			break;
		}

		log("Dest: " + currentX + ":" + currentY + ":" + currentZ);
	}

	private double findMatch(ArrayList<Double> listValues, double value) {
		// TODO Auto-generated method stub

		double diff = Double.MAX_VALUE;
		double closeMatch = 0;

		for (Iterator iterator = listValues.iterator(); iterator.hasNext();) {
			Double fromList = (Double) iterator.next();
			double currentDiff = Math.abs(fromList - value);
			if (currentDiff == 0)
				return fromList;
			if (currentDiff < diff) {
				diff = currentDiff;
				closeMatch = fromList;

			}

		}
		return closeMatch;
	}

	private double round(double value) throws Exception {
		return MathUtils.round(value, 2);
		// return Math.round(value*1000d/1000d);
	}

	private void writeG1Projections(char axis, char sign, double current, double dest, FeedRates selectedRate)
			throws Exception {
		// TODO Auto-generated method stub
		FreqMinValue selectedMVF = null;
		FreqMinValue[] mins = conf.getFreq_mvs();
		for (int i = 0; i < mins.length; i++) {
			FreqMinValue mvF = mins[i];
			if (mvF.getFreq() == (axis == 'X' ? selectedRate.getFreq_x()
					: (axis == 'Y' ? selectedRate.getFreq_y() : selectedRate.getFreq_z()))) {
				selectedMVF = mvF;
				break;
			}

		}
		if (selectedMVF == null)
			logExit("ERROR: No MV Frequency " + selectedRate.getFreq_x() + " found in XMV");

		if (current < dest)
			for (double d = current + selectedMVF.getPos_mvx(); d <= dest; d = d + selectedMVF.getPos_mvx()) {
				if (d < 0)
					sign = '-';
				else
					sign = '+';
				formatG1(axis, sign, d);
			}
		else
			for (double d = current - selectedMVF.getNeg_mvx(); d >= dest; d = d - selectedMVF.getNeg_mvx()) {
				
				if (d < 0)
					sign = '-';
				else
					sign = '+';
				formatG1(axis, sign, d);
			}

	}

	private void formatG2G3(char axis, char sign, double value) throws Exception {
		formatG1(axis, sign, value); 
	}
	
	private void formatG1(char axis, char sign, double value) throws Exception {
		value = round(value);
		String strCommand = "MFAX";
		switch (axis) {
		case 'X':
			strCommand = "MFAX";
			currentX = value;
			break;
		case 'Y':
			strCommand = "MFAY";
			currentY = value;
			break;
		case 'Z':
			strCommand = "MFAZ";
			currentZ = value;
			break;
		}
		format(strCommand, sign, value);
	}

	private void processGO(String line) throws Exception {
		// TODO Auto-generated method stub

		log("Processing line in G0 :  " + line);
		boolean isG28 = false;
		if (line.contains("G28")) {
			// G00 G28  Z5.0 X3.0
			isG28 = true;
			String linepart1 = line.substring(0, line.indexOf("G28")).trim();// G00
			String linepart2 = line.substring(line.indexOf("G28") + 3).trim();// G00
			line = linepart1 + linepart2;
			log("Line with G28: " + line);
		}
		int op = determineXYZ(line);
		if (op == 0)
			logExit("ERROR: Incorrect Line , Line Number : " + lineNum + ", invalid Data after GO code: " + line);
		else {
			String strXValue = "", strYValue = "", strZValue = "";
			switch (op) {
			case Constants.ONLY_X:
				writeVFD('X');
				writeGO_MRA('X', line);
				break;

			case Constants.ONLY_Y:
				writeVFD('Y');
				writeGO_MRA('Y', line);
				break;
			case Constants.ONLY_Z:
				writeVFD('Z');
				writeGO_MRA('Z', line);
				break;
			case Constants.X_Y:
				writeVFD('X');
				strXValue = line.substring(1, line.indexOf('Y'));
				writeGO_MRA('X', strXValue);
				writeVFD('Y');
				strYValue = line.substring(line.indexOf('Y') + 1);
				writeGO_MRA('Y', strYValue);
				break;
			case Constants.X_Z:
				writeVFD('X');
				strXValue = line.substring(1, line.indexOf('Z'));
				writeGO_MRA('X', strXValue);
				writeVFD('Z');
				strZValue = line.substring(line.indexOf('Z') + 1);
				writeGO_MRA('Z', strZValue);
				break;
			case Constants.Y_X:
				writeVFD('Y');
				strYValue = line.substring(1, line.indexOf('X'));
				writeGO_MRA('Y', strYValue);
				writeVFD('X');
				strXValue = line.substring(line.indexOf('X') + 1);
				writeGO_MRA('X', strXValue);
				break;

			case Constants.Y_Z:
				writeVFD('Y');
				strYValue = line.substring(1, line.indexOf('Z'));
				writeGO_MRA('Y', strYValue);
				writeVFD('Z');
				strZValue = line.substring(line.indexOf('Z') + 1);
				writeGO_MRA('X', strZValue);
				break;
			case Constants.Z_X:
				writeVFD('Z');
				strZValue = line.substring(1, line.indexOf('X'));
				writeGO_MRA('Z', strZValue);
				writeVFD('X');
				strXValue = line.substring(line.indexOf('X') + 1);
				writeGO_MRA('X', strXValue);
				break;

			case Constants.Z_Y:
				writeVFD('Z');
				strZValue = line.substring(1, line.indexOf('Y'));
				writeGO_MRA('Z', strZValue);
				writeVFD('Y');
				strYValue = line.substring(line.indexOf('Y') + 1);
				writeGO_MRA('Y', strYValue);
				break;

			case Constants.X_Y_Z:
				writeVFD('X');
				strXValue = line.substring(1, line.indexOf('Y'));
				writeGO_MRA('X', strXValue);
				writeVFD('Y');
				strYValue = line.substring(line.indexOf('Y') + 1, line.indexOf('Z'));
				writeGO_MRA('Y', strYValue);
				writeVFD('Z');
				strZValue = line.substring(line.indexOf('Z') + 1);
				writeGO_MRA('Z', strZValue);
				break;

			case Constants.X_Z_Y:
				writeVFD('X');
				strXValue = line.substring(1, line.indexOf('Z'));
				writeGO_MRA('X', strXValue);
				writeVFD('Z');
				strZValue = line.substring(line.indexOf('Z') + 1, line.indexOf('Y'));
				writeGO_MRA('Z', strZValue);
				writeVFD('Y');
				strYValue = line.substring(line.indexOf('Y') + 1);
				writeGO_MRA('Y', strYValue);
				break;
			case Constants.Y_X_Z:
				writeVFD('Y');
				strYValue = line.substring(1, line.indexOf('Y') + 1);
				writeGO_MRA('Y', strYValue);
				writeVFD('X');
				strXValue = line.substring(line.indexOf('Y') + 1, line.indexOf('Z'));
				writeGO_MRA('X', strXValue);
				writeVFD('Z');
				strZValue = line.substring(line.indexOf('Z') + 1);
				writeGO_MRA('Z', strZValue);
				break;
			case Constants.Y_Z_X:
				writeVFD('Y');
				strYValue = line.substring(1, line.indexOf('Y') + 1);
				writeGO_MRA('Y', strYValue);
				writeVFD('Z');
				strZValue = line.substring(line.indexOf('Z') + 1, line.indexOf('Y'));
				writeGO_MRA('Z', strZValue);
				writeVFD('X');
				strXValue = line.substring(line.indexOf('Y') + 1);
				writeGO_MRA('X', strXValue);
				break;

			case Constants.Z_X_Y:
				writeVFD('Z');
				strZValue = line.substring(1, line.indexOf('X'));
				writeGO_MRA('Z', strZValue);
				writeVFD('X');
				strXValue = line.substring(line.indexOf('X') + 1, line.indexOf('Y'));
				writeGO_MRA('X', strXValue);
				writeVFD('Y');
				strYValue = line.substring(1, line.indexOf('Y') + 1);
				writeGO_MRA('Y', strYValue);
				break;

			case Constants.Z_Y_X:
				writeVFD('Z');
				strZValue = line.substring(1, line.indexOf('Y'));
				writeGO_MRA('Z', strZValue);
				writeVFD('Y');
				strYValue = line.substring(line.indexOf('Y') + 1, line.indexOf('Y'));
				writeGO_MRA('Y', strYValue);
				writeVFD('X');
				strXValue = line.substring(line.indexOf('X') + 1);
				writeGO_MRA('X', strXValue);
				break;

			}
			if (isG28)
				writeG28();
		}
	}

	private void writeVFD(char c) throws Exception {
		// TODO Auto-generated method stub
		switch (c) {
		case 'X':
			if (!isVFDxWritten) {
				format("VFDX", '+', conf.getRapid_freq().getRapid_x());
				isVFDxWritten = true;
			}
			break;
		case 'Y':
			if (!isVFDyWritten) {
				format("VFDY", '+', conf.getRapid_freq().getRapid_y());
				isVFDyWritten = true;
			}
			break;
		case 'Z':
			if (!isVFDzWritten) {
				format("VFDZ", '+', conf.getRapid_freq().getRapid_z());
				isVFDzWritten = true;
			}
			break;
		default:
			logExit("ERROR: internal programming error:  char " + c + " is not valid in writeVFD method");

		}

	}

	private void format(String string, char c, double value) throws Exception {
		// TODO Auto-generated method stub

		String toWrite = "(" + string + c + numberFormat.format(value) + ",)";
		gen.WriteLine(toWrite);

	}

	private int determineXYZ(String line) {
		// TODO Auto-generated method stub
		int xIndex = line.indexOf('X');
		int yIndex = line.indexOf('Y');
		int zIndex = line.indexOf('Z');

		if (!(xIndex == 0 || yIndex == 0 || zIndex == 0))
			return 0;

		if (xIndex < 0 && yIndex < 0)
			return Constants.ONLY_Z;
		if (yIndex < 0 && zIndex < 0)
			return Constants.ONLY_X;
		if (xIndex < 0 && zIndex < 0)
			return Constants.ONLY_Y;
		if (zIndex < 0 && xIndex > -1 && yIndex > -1) {
			if (xIndex < yIndex)
				return Constants.X_Y;
			else
				return Constants.Y_X;
		}
		if (zIndex > -1 && xIndex > -1 && yIndex < 0) {
			if (xIndex < zIndex)
				return Constants.X_Z;
			else
				return Constants.Z_X;
		}
		if (zIndex > -1 && xIndex < 0 && yIndex > -1) {
			if (yIndex < zIndex)
				return Constants.Y_Z;
			else
				return Constants.Z_Y;
		}
		if (xIndex < yIndex && yIndex < zIndex)
			return Constants.X_Y_Z;
		if (xIndex < zIndex && zIndex < yIndex)
			return Constants.X_Z_Y;
		if (yIndex < xIndex && xIndex < zIndex)
			return Constants.Y_X_Z;
		if (yIndex < zIndex && zIndex < xIndex)
			return Constants.Y_Z_X;
		if (zIndex < xIndex && xIndex < yIndex)
			return Constants.Z_X_Y;
		if (zIndex < yIndex && yIndex < xIndex)
			return Constants.Z_Y_X;
		return 0;
	}

	private void validateGMCodeFile(String gmFile) throws Exception {
		/*
		 * BufferedReader buff = new BufferedReader(new
		 * FileReader("conf\\INVALID_CODES.TXT")); HashSet<String> invalidCodes
		 * = new HashSet<String>(); while(buff.ready()) {
		 * invalidCodes.add(buff.readLine()); } buff.close();
		 */
		// TODO Auto-generated method stub
		log("VALIDATING GM CODE FILE: " + gmFile);
		BufferedReader reader = new BufferedReader(new FileReader(gmFile));
		if (!reader.readLine().startsWith("%"))
			logExit("ERROR in GM CODE FILE : " + gmFile + ", File does not start with %");
		boolean nFound = false;
		while (reader.ready()) {
			String line = reader.readLine();

			if (line.startsWith("N")) {
				nFound = true;
				break;
			}
		}
		reader.close();
		if (!nFound)
			logExit("ERROR: No BLOCK START COMMAND (N) Found in GM Code File :  " + gmFile);

		reader = new BufferedReader(new FileReader(gmFile));
		int lineNum = 1;
		BufferedReader buff = new BufferedReader(new FileReader("conf\\FLAGS.TXT"));

		while (buff.ready()) {
			String flagLine = buff.readLine();
			String flagCode = flagLine.substring(0, flagLine.indexOf("="));
			String flagAction = flagLine.substring(flagLine.indexOf("=") + 1);
			flags.put(flagCode, flagAction);
		}
		buff.close();
		// log(flags.size());

		while (reader.ready()) {
			String line = reader.readLine().trim();
			/*
			 * if(line.length()>=2)
			 * if(invalidCodes.contains(line.substring(0,2)))
			 * logExit("ERROR IN GM CODE FILE: Line " + lineNum + " has CODE " +
			 * line.substring(0,2) +", which is not supported.");
			 */
			if (line.contains("(")) {
				do {
					if (!line.contains(")"))
						logExit("ERROR in GM CODE FILE, Line : " + lineNum
								+ ", Invalid Comment. missing closing bracket");
					int first = line.indexOf("(");
					int second = line.indexOf(")");
					line = line.substring(0, first) + line.substring(second + 1);
					line = line.trim();
				} while (line.contains("("));

			}
			if (line.length() >= 3) {
				String code = line.substring(0, 3);
				if (flags.containsKey(code)) {

					int codeLength = line.length();
					// log("codeLength:"+codeLength);
					if (codeLength % 3 != 0)
						logExit("ERROR in GM CODE FILE, Line : " + lineNum + ", Invalid line : " + line);

					int parts = codeLength / 3;					
					ArrayList<String> codes = new ArrayList<String>();
					for (int i = 0, j = 0; i < parts; i++, j = j + 3) // 0123456789ABCDE
					{
						codes.add(line.substring(j, j + 3));

					}
					log("Flags:" + codes);
					for (Iterator iterator = codes.iterator(); iterator.hasNext();) {
						String codepart = (String) iterator.next();
						String flagAction = flags.get(codepart);

						if ("SETFLAG".equals(flagAction) || "TOGGLEFLAG".equals(flagAction))
							setFlags.put(codepart, true);
						if ("INVALID".equals(flagAction))
							logExit("ERROR in GM CODE FILE, Line : " + lineNum + ", Invalid  Code : " + codepart);
					}
				}
			}
			lineNum++;
		}
		if (!setFlags.containsKey("G20") && !setFlags.containsKey("G21"))
			logExit("ERROR IN GM FILE: Either G20 or G21 should be provided , none found");
		if (setFlags.containsKey("G20") && setFlags.containsKey("G21"))
			logExit("ERROR IN GM FILE: Either G20 or G21 should be provided , both found");
		log("GM CODE FILE VALIDATED SUCCSSFULLY");
	}

	private void loadMVFiles(String[] args) throws Exception {
		if (mvs == null)
			mapMV = new HashMap<Double, FreqMinValue>();
		else
			mapMV = getMVMap(mvs);
		log("LOADING MV FILES");
		if (!"NONE".equals(args[1])) {
			log("LOADING XMV FILE:" + args[1]);
			mapMV = loadMV('X', args[1], mapMV);
		} else {
			log("XMV FILE SKIPPED");
		}
		if (!"NONE".equals(args[2])) {
			log("LOADING YMV FILE:" + args[2]);
			mapMV = loadMV('Y', args[2], mapMV);
		} else {
			log("YMV FILE SKIPPED");
		}
		if (!"NONE".equals(args[3])) {
			log("LOADING ZMV FILE:" + args[3]);
			mapMV = loadMV('Z', args[3], mapMV);
		} else {
			log("ZMV FILE SKIPPED");
		}

		if (validateConfMV(mapMV))
			log("MV VALIDATED SUCCEEDED");
		else
			logExit("MV VALIDATED FAILED");

		log("MV FILES LOADED SUCCESSFULLY");

	}

	private boolean validateConfMV(HashMap<Double, FreqMinValue> mapMV) {
		// TODO Auto-generated method stub
		FeedRates[] feedRates = conf.getFeed_rates();

		HashSet<Double> xFreq = new HashSet<Double>();
		HashSet<Double> yFreq = new HashSet<Double>();
		HashSet<Double> zFreq = new HashSet<Double>();
		for (int i = 0; i < feedRates.length; i++) {
			FeedRates rate = feedRates[i];
			xFreq.add((double) rate.getFreq_x());
			yFreq.add((double) rate.getFreq_y());
			zFreq.add((double) rate.getFreq_z());
		}

		Set<Double> freqs = mapMV.keySet();
		FreqMinValue[] fs = new FreqMinValue[freqs.size()];
		int i = 0;
		for (Iterator iterator = freqs.iterator(); iterator.hasNext();) {
			Double freq = (Double) iterator.next();
			FreqMinValue mv = mapMV.get(freq);
			if (mv.getNeg_mvx() != 0 && mv.getPos_mvx() != 0) {
				if (!xFreq.contains(freq)) {
					for (Iterator iterator2 = freqs.iterator(); iterator2.hasNext();) {
						Double double1 = (Double) iterator2.next();
						// log(double1);
					}
					logExit("CONFIGURATION MISMATCH : FEEDRATE for X Frequency :" + freq + "is not present in conf");
				}
			}
			if (mv.getNeg_mvy() != 0 && mv.getPos_mvy() != 0) {
				if (!yFreq.contains(freq)) {
					logExit("CONFIGURATION MISMATCH : FEEDRATE for Y Frequency :" + freq + "is not present in conf");
				}
			}
			if (mv.getNeg_mvz() != 0 && mv.getPos_mvz() != 0) {
				if (!zFreq.contains(freq)) {
					logExit("CONFIGURATION MISMATCH : FEEDRATE for Z Frequency :" + freq + "is not present in conf");
				}
			}
			fs[i++] = mv;
		}
		conf.setFreq_mvs(fs);

		return true;
	}

	private HashMap<Double, FreqMinValue> getMVMap(FreqMinValue[] mvs) {
		// TODO Auto-generated method stub
		HashMap<Double, FreqMinValue> map = new HashMap<Double, FreqMinValue>();
		for (int i = 0; i < mvs.length; i++) {
			map.put(mvs[i].getFreq(), mvs[i]);
		}
		return map;

	}

	private MVValue validateMVFileLineFormat(String str, String file, int line) {
		MVValue mvValue = new MVValue();

		boolean oddline = line % 2 == 1 ? true : false;
		if (!str.startsWith("["))
			logExit("ERROR: Invalid MV FILE: " + file + ": line #:" + line + " does not start with [");
		if (!str.endsWith("]"))
			logExit("ERROR: Invalid MV FILE: " + file + ": line #:" + line + " does not end with ]");
		if (str.charAt(4) != ',')
			logExit("ERROR: Invalid MV FILE: " + file + ": line #:" + line + " does not have ',' ");
		if (oddline && !(str.charAt(5) == '+')) {
			logExit("ERROR: Invalid MV FILE: " + file + ": No + sign in line #:" + line);
		} else if (!oddline && !(str.charAt(5) == '-')) {
			logExit("ERROR: Invalid MV FILE: " + file + ": No - sign in line #:" + line);
		}
		// [002,+0000.010]
		mvValue.setSignMV(str.charAt(5) == '+' ? true : false);
		try {
			double freq = Double.parseDouble(str.substring(1, 4));
			mvValue.setFreq(freq);
		} catch (Exception e) {
			logExit("ERROR: Invalid MV FILE: " + file + ": Incorrcet Frequency at line #:" + line);
		}
		try {
			double value = Double.parseDouble(str.substring(6, 14));
			mvValue.setValueMV(value);
		} catch (Exception e) {
			logExit("ERROR: Invalid MV FILE: " + file + ": Incorrcet min  value  at line #:" + line);
		}
		return mvValue;
	}

	private HashMap<Double, FreqMinValue> loadMV(char c, String file, HashMap<Double, FreqMinValue> mapMVFreq)
			throws Exception {
		BufferedReader buff = new BufferedReader(new FileReader(file));
		ArrayList<String> lines = new ArrayList<>();
		int line = 1;
		boolean oddline = true;
		MVValue mvValue = null;
		double currFreq = 0;
		while (buff.ready()) {
			String str = buff.readLine();
			mvValue = validateMVFileLineFormat(str, file, line);
			if (!oddline) {
				if (currFreq != mvValue.getFreq())
					logExit("ERROR IN " + c + "MV FILE:" + file + ", Line:" + (line - 1) + " has frequency : "
							+ currFreq + " whereas Line: " + line + " has frequency:" + mvValue.getFreq());
			} else
				currFreq = mvValue.getFreq();
			lines.add(str);
			oddline = !oddline;
			line++;
			FreqMinValue mvFreq = null;
			if (mapMVFreq.containsKey(mvValue.getFreq())) {
				mvFreq = mapMVFreq.get(mvValue.getFreq());

			} else {
				mvFreq = new FreqMinValue();
				mvFreq.setFreq(mvValue.getFreq());
				mapMVFreq.put(mvValue.getFreq(), mvFreq);
			}

			switch (c) {
			case 'X':
				if (oddline) {
					mvFreq.setPos_mvx(mvValue.getValueMV());
				} else {
					mvFreq.setNeg_mvx(mvValue.getValueMV());
				}
				break;
			case 'Y':
				if (oddline) {
					mvFreq.setPos_mvy(mvValue.getValueMV());
				} else {
					mvFreq.setNeg_mvy(mvValue.getValueMV());
				}
				break;
			case 'Z':
				if (oddline) {
					mvFreq.setPos_mvz(mvValue.getValueMV());
				} else {
					mvFreq.setNeg_mvz(mvValue.getValueMV());
				}
				break;
			}

			// log(mvFreq);
		}
		buff.close();
		// log(mapMVFreq.get(2.0).toString());
		mapMVFreq.remove(mvValue.getFreq());
		Rapid_Frequency rf = conf.getRapid_freq();
		switch (c) {
		case 'X':
			if (mvValue.getFreq() != rf.getRapid_x())
				logExit("ERROR: RAPID FREQ X MISMATCH: CONFIG HAS: " + rf.getRapid_x() + " and XMV file " + file
						+ " has : " + mvValue.getFreq());
			break;
		case 'Y':
			if (mvValue.getFreq() != rf.getRapid_y())
				logExit("ERROR: RAPID FREQ Y MISMATCH: CONFIG HAS: " + rf.getRapid_y() + " and YMV file " + file
						+ " has : " + mvValue.getFreq());
			break;
		case 'Z':
			if (mvValue.getFreq() != rf.getRapid_z())
				logExit("ERROR: RAPID FREQ Z MISMATCH: CONFIG HAS: " + rf.getRapid_z() + " and ZMV file " + file
						+ " has : " + mvValue.getFreq());
			break;
		}
		return mapMVFreq;
	}

	public static void log(Object str) {
		System.out.println(str);
	}

	public static void logExit(Object str) {
		try {
			if (gen != null)
				gen.clearCodeFolder();
		} catch (Exception e) {
			e.printStackTrace();
		}
		showErrorinUI(str);
		System.err.println(str);
		
		//System.exit(0);
	}
	
	public static void showErrorinUI(Object str)
	{
		try
		{
			Frame [] frames = JFrame.getFrames();
			for (int i = 0; i < frames.length; i++) {
				Frame f = frames[i];
				if(f.isVisible())
				{
					JOptionPane.showMessageDialog(f, "Error : " + str);
				}
			}
		}
		catch(Exception e)
		{
			
		}
	}

	private void init(String[] args) throws Exception {
		log("INITIALIZING");
		JsonReader reader = new JsonReader(new FileReader(args[0]));
		conf = (MachineConfiguration) new Gson().fromJson(reader, MachineConfiguration.class);
		if (conf.getRapid_freq() == null)
			logExit("ERROR: NO RAPID FREQUENCY SET in Config");
		log("INITIALIZATION COMPLETED");
	}
}
