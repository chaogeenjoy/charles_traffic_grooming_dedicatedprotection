package test;

import java.util.HashMap;
import java.util.Iterator;

import network.Layer;
import network.Link;
import network.Network;
import trafficGrooming.TrafficGroomingJoinDedicatedProtection;

public class TGAndDPTester {
	public static void main(String[] args) {

		Network network=new Network("ip over wdm", 0, "");
		network.readPhysicalTopology("E:\\ÆäËû\\RPtopology\\NODE6.csv");
		network.copyNodes();
		network.createNodepair();
		
		Layer ipLayer=network.getLayerlist().get("Layer0");
		Layer optLayer=network.getLayerlist().get("Physical");
		

		TrafficGroomingJoinDedicatedProtection tfAP=new TrafficGroomingJoinDedicatedProtection();
		tfAP.jointlyDesignedTGAndP(network, ipLayer, optLayer);
		int transponderNum=tfAP.getTransponderNum();
		System.out.println("\ntransponder num="+transponderNum);
		
		int maxSlot=0;
		HashMap<String, Link> map=optLayer.getLinklist();
		Iterator<String> itr=map.keySet().iterator();
		while(itr.hasNext()){
			Link link=(Link) (map.get(itr.next()));
			if(link.getMaxSlot()>maxSlot){
				maxSlot=link.getMaxSlot();
			}
		}
		System.out.println("MaxSlot="+maxSlot);
	}

}
