package com.bridgeit.druv.processor.domains;

public class FreqMinValue {

		double freq;
		double pos_mvx,neg_mvx,pos_mvy,neg_mvy,pos_mvz,neg_mvz;

		public String toString()
		{
			return freq + ":" +
				   pos_mvx + ":" +
				   neg_mvx + ":" +
				   pos_mvy + ":" +
				   neg_mvy + ":" +
				   pos_mvz + ":" +
				   neg_mvz + ":";
		}
		public double getFreq() {
			return freq;
		}
		public void setFreq(double freq) {
			this.freq = freq;
		}
		public double getPos_mvx() {
			return pos_mvx;
		}
		public void setPos_mvx(double pos_mvx) {
			this.pos_mvx = pos_mvx;
		}
		public double getNeg_mvx() {
			return neg_mvx;
		}
		public void setNeg_mvx(double neg_mvx) {
			this.neg_mvx = neg_mvx;
		}
		public double getPos_mvy() {
			return pos_mvy;
		}
		public void setPos_mvy(double pos_mvy) {
			this.pos_mvy = pos_mvy;
		}
		public double getNeg_mvy() {
			return neg_mvy;
		}
		public void setNeg_mvy(double neg_mvy) {
			this.neg_mvy = neg_mvy;
		}
		public double getPos_mvz() {
			return pos_mvz;
		}
		public void setPos_mvz(double pos_mvz) {
			this.pos_mvz = pos_mvz;
		}
		public double getNeg_mvz() {
			return neg_mvz;
		}
		public void setNeg_mvz(double neg_mvz) {
			this.neg_mvz = neg_mvz;
		}
	
}
