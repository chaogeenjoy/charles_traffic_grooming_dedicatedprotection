package graphalgorithms;

import java.util.HashMap;
import java.util.Iterator;

import general.SWP;
import network.Layer;
import network.Network;
import network.Node;
import network.NodePair;
import subgraph.LinearRoute;

/*
 * Requirements: 1、给定FS的数量
                           2、给定源节点和目的节点
                           3、实现路由和频谱分配
 */
public class TestForSWPBasedRSA {
	public static void main(String[] args) {
		Network network=new Network("network",0,"");
		network.readPhysicalTopology("E:\\其他\\RPtopology\\NODE6.csv");
		network.copyNodes();
		network.createNodepair();
		Layer optLayer=network.getLayerlist().get("Physical");
		Layer ipLayer=network.getLayerlist().get("Layer0");
		HashMap<String, NodePair> nodePairList=ipLayer.getNodepairlist();
		Iterator<String> itr=nodePairList.keySet().iterator();
		while(itr.hasNext()){
			NodePair nodePair=(NodePair) nodePairList.get(itr.next());
			Node srcNode=optLayer.getNodelist().get(nodePair.getSrcNode().getName());
			Node destNode=optLayer.getNodelist().get(nodePair.getDesNode().getName());
			
			int trafficDemand=nodePair.getTrafficdemand();			
			double X = 25;// 2000-4000 BPSK,1000-2000 QBSK,500-1000
							// 8QAM,0-500 16QAM

			int slotNum = (int) Math.ceil(trafficDemand / X);
			System.out.println(nodePair.getName()+" "+trafficDemand+"  "+slotNum);
			SWP RSAOnSWP=new SWP();
			LinearRoute newRoute=new LinearRoute("route", 0, "");
			newRoute=RSAOnSWP.minCost_RSABasedOnSWP(optLayer, slotNum, srcNode, destNode, new SearchConstraint(), 1);
//			                              (optLayer, FSNum, srcNode, destNode, newRoute, constraint, nature);
			if(newRoute.getLinklist().size()!=0){
				System.out.println("成功！");
				newRoute.OutputRoute_node(newRoute);
				System.out.println("");
				
				
			}else{
				System.out.println("建立新的光路失败！");
			}
			
			RouteSearching rs=new RouteSearching();
			LinearRoute newRoute1=new LinearRoute("", 0, "");
			rs.Dijkstras(srcNode, destNode, optLayer, newRoute1, null);
			newRoute1.OutputRoute_node(newRoute1);
			System.out.println("\n\n");
			
			
		}
		
	}	

}
