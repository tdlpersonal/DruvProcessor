package com.bridgeit.druv.processor.tbmodel;

import java.util.ArrayList;
import java.util.Iterator;

import com.bridgeit.druv.processor.domains.FeedRates;
import com.bridgeit.druv.processor.domains.MachineConfiguration;

public class FeedRatesTableModel extends BaseTableModel {

	FeedRatesTableModel(Object[][] data, Object[] colNames) {
		super(data, colNames);
		// TODO Auto-generated constructor stub
	}
	
	private static FeedRatesTableModel instance  = null;
	
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
	
	public static FeedRatesTableModel getInstance(FeedRates[] feedRates)
	{
		Object colNames[]  = new  String[]{"Feed Rate","Freq X","Delay X","Freq Y","Delay Y","Freq Z","Delay Z"};
		
		if(feedRates==null  || feedRates.length<1)
			return new FeedRatesTableModel(new Object[][]{},colNames);
		
		Object data[][] = new Object[feedRates.length][7];
		
		for(int i=0;i<feedRates.length;i++)
		{
			FeedRates rate = feedRates[i];
			data[i][0]= rate.getFeed_rate();
			data[i][1]= rate.getFreq_x();
			data[i][2]= rate.getDelay_x();
			data[i][3]= rate.getFreq_y();
			data[i][4]= rate.getDelay_y();
			data[i][5]= rate.getFreq_z();
			data[i][6]= rate.getDelay_z();
			
		}
		instance = new FeedRatesTableModel(data, colNames);
		
		return instance;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		// TODO Auto-generated method stub
		switch(col)
		{
		case 2:
		case 4:
		case 6: return Integer.class;
		default : return Double.class;

		}
	}
	
	
	@Override
	public void setValueAt(Object value, int row, int col) {
		// TODO Auto-generated method stub
		super.setValueAt(value, row, col);
		data[row][col]=value;
	}
	public FeedRates[] getRates()
	{
		FeedRates[] rates = new FeedRates[data.length];
		for(int i=0;i<data.length;i++)
		{
			FeedRates rate = new FeedRates();
			rate.setFeed_rate(Double.parseDouble(data[i][0].toString()));
			rate.setFreq_x(Double.parseDouble(data[i][1].toString()));
			rate.setDelay_x(Integer.parseInt(data[i][2].toString()));
			rate.setFreq_y(Double.parseDouble(data[i][3].toString()));
			rate.setDelay_y(Integer.parseInt(data[i][4].toString()));
			rate.setFreq_z(Double.parseDouble(data[i][5].toString()));
			rate.setDelay_z(Integer.parseInt(data[i][6].toString()));
			rates[i]=rate;
		}
		return rates;
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
					switch(j)
					{
						case 2:
						case 4:
						case 6: value=Integer.parseInt(data[i][j].toString());break;
						default : value=Double.parseDouble(data[i][j].toString());break;
						
					}
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
