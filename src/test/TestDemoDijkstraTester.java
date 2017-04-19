package test;

import graphalgorithms.RouteSearching;
import network.Layer;
import network.Network;
import network.Node;
import subgraph.LinearRoute;

public class TestDemoDijkstraTester {
	public static void main(String[] args) {
		long now1=System.currentTimeMillis();
		Network network = new Network("workNet", 0, "");
		network.readPhysicalTopology("E:\\ÆäËû\\RPtopology\\NODE6.csv");	
//		network.copyNodes();
//		network.createNodepair();
		
		Layer layer=network.getLayerlist().get("Physical");
//		Layer ipLayer=network.getLayerlist().get("Layer0");
		Node node1=layer.getNodelist().get("N0");
		Node node2=layer.getNodelist().get("N1");
		LinearRoute newRoute=new LinearRoute("", 0, "");
		
		
		System.out.println(node1.getName()+"---"+node2.getName());
		RouteSearching tdd=new RouteSearching();	
		
		tdd.Dijkstras(node1,node2, layer, newRoute, null);
		
		newRoute.OutputRoute_node(newRoute);
	    long now2=System.currentTimeMillis();
	    System.out.println("Time cosume: "+(now2-now1)+" ms");
//		SearchConstraint constraint=null;
		
		
	}

}