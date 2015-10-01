package org.ispeech.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SilenceDetection {

	private static final int ROLLING_LOOKBACK = 10;
	private static final float DIST_THRESHOLD = 0.4f;
	
	private int n = 0;
	private int min, max, distance;
	private RollingDistance rollingDistance;
	long average;
	
	public SilenceDetection() {
		rollingDistance = new RollingDistance(ROLLING_LOOKBACK);
	}
	
	public int addSound(byte[] input) {
		int energy = avgLevel(input);
		
		rollingDistance.add(energy);
		
		if(n == 0) { // first time, set min, max
			min = energy;
			max = energy;
		}
		
		if(min > energy) 
			min = energy;
		
		if(max < energy)
			max = energy;
		
		distance = max - min;
		
		average = ((average*n)+energy)/(n+1);
		n++;
		
		return energy;
	}
	
	private short[] convertBytesToShortsArray(byte [] data) {    	
		short[] shorts = new short[data.length/2];
		
		// to turn bytes to shorts as either big endian or little endian. 
		ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
	  	
		return shorts;
	}
	
	private int avgLevel(byte[] data) {
		short[] levels = convertBytesToShortsArray(data);
		
		int total = 0;
		
		for(short s : levels) {
			total += Math.abs(s);
		}
		
		return total/levels.length;
	}
	
	public boolean isSilence() { // if the rolling distance is smaller than the max distance
		return rollingDistance.getDistance() < distance*DIST_THRESHOLD;
	}
	
	private class RollingDistance {
		
		int[] values;
		int index = 0;
		
		public RollingDistance(int n) {
			values = new int[n];
		}
		
		public void add(int i) {
			values[index] = i;
			index++;
			
			if(index == values.length)
				index = 0;
		}
		
		public int getDistance() {
			int max, min;
			max = min = values[0];
			
			for(int i = 0; i < values.length; i++) {
				if(min > values[i]) 
					min = values[i];
				
				if(max < values[i])
					max = values[i];
			}
			
			return Math.abs(max - min);
		}
	}
}
