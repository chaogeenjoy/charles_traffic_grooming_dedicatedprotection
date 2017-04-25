package test;

import java.util.Iterator;

import network.Layer;
import network.Link;
import network.Network;
import trafficGrooming.GroomingwithSBPP;

public class SBPPGroomingTester {
	public static void main(String[] args) {
		Network network=new Network("ip over wdm", 0, "");
		network.readPhysicalTopology("E:\\其他\\RPtopology\\NODE24.csv");
		network.copyNodes();
		network.createNodepair();
		
		Layer ipLayer=network.getLayerlist().get("Layer0");
		Layer optLayer=network.getLayerlist().get("Physical");
		
		GroomingwithSBPP grooming=new GroomingwithSBPP();
		grooming.SBPPgrooming(ipLayer, optLayer);
		
		System.out.println("\n\n所需要的Transponder的数量为"+grooming.getTransponderNum());
		
		
		int maxSlot=0;
		Iterator<String> itr=optLayer.getLinklist().keySet().iterator();
		while(itr.hasNext()){
			Link link=(Link) (optLayer.getLinklist().get(itr.next()));
			if(maxSlot<link.getMaxSlot()){
				maxSlot=link.getMaxSlot();
			}
		}
		
		
		System.out.println("Max Slot="+maxSlot);
	}
}
