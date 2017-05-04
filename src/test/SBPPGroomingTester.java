package test;

import network.Layer;
import network.Network;
import trafficGrooming.GroomingwithSBPP;

public class SBPPGroomingTester {
	public static void main(String[] args) {
		Network network=new Network("ip over wdm", 0, "");
		network.readPhysicalTopology("E:\\����\\RPtopology\\NODE24.csv");
		network.copyNodes();
		network.createNodepair();
		
		Layer ipLayer=network.getLayerlist().get("Layer0");
		Layer optLayer=network.getLayerlist().get("Physical");
		
		GroomingwithSBPP grooming=new GroomingwithSBPP();
		grooming.SBPPgrooming(ipLayer, optLayer);
		
		System.out.println("\n\n����Ҫ��Transponder������Ϊ"+grooming.getTransponderNum());
		
		
		System.out.println("Max Slot="+grooming.getMaxSlotNum());
	}
}
