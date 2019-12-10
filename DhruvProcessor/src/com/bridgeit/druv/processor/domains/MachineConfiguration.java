package com.bridgeit.druv.processor.domains;

import com.google.gson.Gson;

public class MachineConfiguration {

	private long  created_time, last_updated_time;
	private int spindle_rpm;
	private FeedRates[] feed_rates;
	private Rapid_Frequency rapid_freq;
	private ToolTuret tool_turet;
	private FreqMinValue[] freq_mvs; 
	private String confName;
	
	
	public String getConfName() {
		return confName;
	}

	public void setConfName(String confName) {
		this.confName = confName;
	}

	public String toString()
	{
		return new Gson().toJson(this);
	}
	
	public FreqMinValue[] getFreq_mvs() {
		return freq_mvs;
	}
	public void setFreq_mvs(FreqMinValue[] freq_mvs) {
		this.freq_mvs = freq_mvs;
	}
	
	public long getCreated_time() {
		return created_time;
	}
	public void setCreated_time(long created_time) {
		this.created_time = created_time;
	}
	public long getLast_updated_time() {
		return last_updated_time;
	}
	public void setLast_updated_time(long last_updated_time) {
		this.last_updated_time = last_updated_time;
	}
	public int getSpindle_rpm() {
		return spindle_rpm;
	}
	public void setSpindle_rpm(int spindle_rpm) {
		this.spindle_rpm = spindle_rpm;
	}
	public FeedRates[] getFeed_rates() {
		return feed_rates;
	}
	public void setFeed_rates(FeedRates[] feed_rates) {
		this.feed_rates = feed_rates;
	}
	public Rapid_Frequency getRapid_freq() {
		return rapid_freq;
	}
	public void setRapid_freq(Rapid_Frequency rapid_freq) {
		this.rapid_freq = rapid_freq;
	}
	public ToolTuret getTool_turet() {
		return tool_turet;
	}
	public void setTool_turet(ToolTuret tool_turet) {
		this.tool_turet = tool_turet;
	}
	class ToolTuret
	{
		double rotary_turet,linear_toolpost,single_toolpost;

		public double getRotary_turet() {
			return rotary_turet;
		}

		public void setRotary_turet(double rotary_turet) {
			this.rotary_turet = rotary_turet;
		}

		public double getLinear_toolpost() {
			return linear_toolpost;
		}

		public void setLinear_toolpost(double linear_toolpost) {
			this.linear_toolpost = linear_toolpost;
		}

		public double getSingle_toolpost() {
			return single_toolpost;
		}

		public void setSingle_toolpost(double single_toolpost) {
			this.single_toolpost = single_toolpost;
		}
		
	}
	
}
