package com.bridgeit.druv.processor.tbmodel;

import java.util.ArrayList;
import java.util.Iterator;

import com.bridgeit.druv.processor.domains.FeedRates;
import com.bridgeit.druv.processor.domains.FreqMinValue;
import com.bridgeit.druv.processor.domains.MachineConfiguration;

public class MVTableModel extends BaseTableModel {

	MVTableModel(Object[][] data, Object[] colNames) {
		super(data, colNames);
		// TODO Auto-generated constructor stub
	}
	
	private static MVTableModel instance  = null;
	
	@Override
	public void addRow(Object[] values) {
		// TODO Auto-generated method stub
		super.addRow(values);
		Object[][] newValues = new Object[data.length+1][7];
		for(int i=0;i<data.length;i++)
			newValues[i]=data[i];
		newValues[data.length]=values;
		data = newValues;
	}
	
	public static MVTableModel getInstance(ArrayList<Double> freqs)
	{
		Object colNames[]  = new  String[]{"Frequency","Min X (+ve)","Min X (-ve)","Min Y (+ve)","Min Y (-ve)","Min Z (+ve)","Min Z (-ve)"};
		
		Object data[][] = new Object[freqs.size()][7];
		
		for(int i=0;i<freqs.size();i++)
		{
			data[i][0]= freqs.get(i);
			data[i][1]= 0;
			data[i][2]= 0;
			data[i][3]= 0;
			data[i][4]= 0;
			data[i][5]= 0;
			data[i][6]= 0;
			
		}
		
		instance = new MVTableModel(data, colNames);
		
		return instance;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		// TODO Auto-generated method stub
		return Double.class;

	}
	
	
	@Override
	public void setValueAt(Object value, int row, int col) {
		// TODO Auto-generated method stub
		super.setValueAt(value, row, col);
		data[row][col]=value;
	}
	
	public FreqMinValue[] getMVs()
	{
		FreqMinValue[] mvs = new FreqMinValue[data.length];
		for(int i=0;i<data.length;i++)
		{
			
			FreqMinValue mv = new FreqMinValue();
			mv.setFreq(Double.parseDouble(data[i][0].toString()));
			mv.setPos_mvx(Double.parseDouble(data[i][1].toString()));
			mv.setNeg_mvx(Double.parseDouble(data[i][2].toString()));
			mv.setPos_mvy(Double.parseDouble(data[i][3].toString()));
			mv.setNeg_mvy(Double.parseDouble(data[i][4].toString()));
			mv.setPos_mvz(Double.parseDouble(data[i][5].toString()));
			mv.setNeg_mvz(Double.parseDouble(data[i][6].toString()));
			mvs[i]=mv;
		}
		return mvs;
	}
	
	public boolean validateData() {
		// TODO Auto-generated method stub
		
		for(int i=0;i<data.length;i++)
		{
			Object[] rowValues = data[i];
			for(int j = 0; j<rowValues.length;j++)
			{
				try
				{
					double value=0;
					value=Double.parseDouble(data[i][j].toString());break;
						
					
				}
				catch(Exception e)
				{
					e.printStackTrace();
					return false;
				}
			}
			
		}
		
		return true;
	}
	

}
