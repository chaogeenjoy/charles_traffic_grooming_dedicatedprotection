package designSBPP;

import demand.Request;
import general.SWP;
import graphalgorithms.SearchConstraint;
import network.Layer;
import network.Node;
import network.NodePair;
import subgraph.LinearRoute;

public class SBPPRSA {
	public void WorkingRSA(Layer optLayer,NodePair nodePair, Request request){
		Node srcNode=nodePair.getSrcNode();
		Node destNode=nodePair.getDesNode();
		
		double[] modulationCapacity={37.5,25,12.5};
		double[] transparentLen={1000,2000,4000};
		for(int i=0;i<modulationCapacity.length;i++){
			int slotNum=(int)Math.ceil(request.getRequestRate()/modulationCapacity[i]);
			SWP workSWP=new SWP();
			SearchConstraint constraint1=new SearchConstraint();
			LinearRoute newRoute=workSWP.minCost_RSABasedOnSWP(optLayer, slotNum, srcNode, destNode, constraint1, 1);
			if(newRoute.getLength()>transparentLen[i]){
				System.out.println("OK");
			}
			
		}
	}

}
