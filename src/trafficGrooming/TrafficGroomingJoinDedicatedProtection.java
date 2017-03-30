package trafficGrooming;

import java.util.ArrayList;
import java.util.Iterator;

import demand.Request;
import graphalgorithms.RouteSearching;
import network.Layer;
import network.Link;
import network.Network;
import network.Node;
import network.NodePair;
import subgraph.LinearRoute;

public class TrafficGroomingJoinDedicatedProtection {
	private  int transponderNum=0;
	
	public int getTransponderNum() {
		return transponderNum;
	}

	public void setTransponderNum(int transponderNum) {
		this.transponderNum = transponderNum;
	}





	public  void jointlyDesignedTGAndP(Network network, Layer ipLayer, Layer optLayer) {
		

		// 现将节点对按照流量从大到小的顺序对节点对排序ArrayList<NodePair>
		ArrayList<NodePair> nodePairList = Request.sortNodePair(ipLayer);
		for (int i = 0; i < nodePairList.size(); i++) {
			// 获取当前操作的节点对的元节点和目的节点
			NodePair currentNodePair = nodePairList.get(i);
			Node srcNode = currentNodePair.getSrcNode();
			Node destNode = currentNodePair.getDesNode();
			System.out.println("\n\n当前操作的节点对是：" + srcNode.getName() + "--" + destNode.getName() + "\t其流量需求为："
					+ currentNodePair.getTrafficdemand());
			/*
			 * //遍历iplayer，排除容量不足以及保护属性的链路，
			 * 然后在IP虚拟拓扑上路由当前拓扑
			 */			
			ArrayList<Link> tempDelLinkList = new ArrayList<Link>();
			Iterator<String> itr0=ipLayer.getLinklist().keySet().iterator();
			while(itr0.hasNext()){
				Link link=(Link) ipLayer.getLinklist().get(itr0.next());
//				System.out.println("IP层存储的链路："+link.getName()+"\t剩余容量为："+(link.getCapacity()-link.getSumFlow()));
				if(((link.getCapacity()-link.getSumFlow())<currentNodePair.getTrafficdemand())||(link.getNature()==1)){
					tempDelLinkList.add(link);
					
					//ipLayer.removeLink(link.getName());
				}
			}
			for(Link link:tempDelLinkList){
				ipLayer.removeLink(link.getName());			   
			}

			/*
			 * 输出剩余的链路
			 */
			Iterator<String> itr10=ipLayer.getLinklist().keySet().iterator();
			while(itr10.hasNext()){
				Link linkn=(Link) ipLayer.getLinklist().get(itr10.next());
				System.out.println("剩余的链路："+(linkn.getName())+"\t"+(linkn.getCapacity()-linkn.getSumFlow())+"\t"+linkn.getCost()+"\t"+linkn.getLength());
			}
			
			/*
			 * 输出节点
			 */
			/*Iterator<String> itr11=ipLayer.getNodelist().keySet().iterator();
			while(itr11.hasNext()){
				Node node=(Node) ipLayer.getNodelist().get(itr11.next());
				System.out.println("\n\n剩余节点为："+node.getName()+"\t其相邻的节点为：");
				for(Node node1:node.getNeinodelist()){
					System.out.print("\t"+node1.getName());
				}
			}*/
			LinearRoute newWorkRouteIP=new LinearRoute("IPWorkingRoute",0,"");
			RouteSearching rsIPWork=new RouteSearching();
			rsIPWork.Dijkstras(srcNode, destNode, ipLayer, newWorkRouteIP, null);
//			newWorkRouteIP.OutputRoute_node(newWorkRouteIP);
			//在Ip层上路由成功以后，恢复排除的节点
			for(Link link: tempDelLinkList){
				ipLayer.addLink(link);
			}
			tempDelLinkList.clear();
		
			/*
			 * 在虚拟拓扑上路由工作路径成功
			 * 
			 */
			
			if(newWorkRouteIP.getLinklist().size()!=0){
				System.out.println("\n\n******************在IP层路成功的由工作路径及分配资源！***********************\n\n");
				newWorkRouteIP.OutputRoute_node(newWorkRouteIP);
				for(Link link: newWorkRouteIP.getLinklist()){
					link.setNature(0);
					link.setSumFlow(link.getSumFlow()+currentNodePair.getTrafficdemand());					
				}
			}
			
			/*
			 * 在虚拟拓扑上路由工作路径失败
			 */
			else{
			/*
			 * 在光层路由，新建光路，排除容量不足，所有的工作链路，以及本次工作路径在物理链路有重叠的链路
			 */
				System.out.println("在IP层路由工作路径失败，转入光层处理");
				Node srcNode_OP=optLayer.getNodelist().get(srcNode.getName());
				Node destNode_OP=optLayer.getNodelist().get(destNode.getName());
				LinearRoute newWorkRouteOL=new LinearRoute("OptWorkingRoute",1,"");
				RouteSearching rsOLWork=new RouteSearching();
				rsOLWork.Dijkstras(srcNode_OP, destNode_OP, optLayer, newWorkRouteOL, null);
				if(newWorkRouteOL.getLinklist().size()==0){
					System.out.println("工作路径在光层路由失败，想新建光路，但是歇菜了，没辙，哈哈哈哈");
				}
				/*
				 * 在光层路由工作路径，建立光路成功啦，来吧，我们来分配资源，考虑保护路径吧
				 */
				else{
					this.setTransponderNum(this.getTransponderNum()+2);
					
					//根据Traffic Demand以及路径长度来确定啥呢，哈哈，是slot的个数
					double l=newWorkRouteOL.getLength();
					double X=1;
					if(l<500){
						X=50;
					}else if(l<1000){
						X=37.5;
					}else if(l<2000){
						X=25;
					}else{
						X=12.5;
					}
					int slotNum=(int)Math.ceil(currentNodePair.getTrafficdemand()/X);
				
//					int slotNum=slotNumByDemand(currentNodePair.getTrafficdemand(), newWorkRouteOL.getLength());
//					System.out.println("当前节点对工作路径占用的光层的slot个数是"+slotNum);
					
					
					newWorkRouteOL.setSlotsnum(slotNum);
					ArrayList<Integer> slotIndex=Request.spectrumAllocationOneRoute_ReqList(newWorkRouteOL);
					/*
					 * 查看光层资源够不够用
					 */
					
					if(slotIndex.size()==0){
						System.out.println("堵塞了，没办法，不分配资源了");
					}else{
						//更新光层占用的链路的信息
						for(Link link:newWorkRouteOL.getLinklist()){
							for(int index=slotIndex.get(0);index<slotNum+slotIndex.get(0);index++){
								link.getSlotsArray().get(index).setStatus(1);
								
							}
							link.setMaxSlot(slotNum+link.getMaxSlot());
						}						
					}
					
//					//public Link(String name, int index, String comments, Layer associatedLayer, Node nodeA, Node nodeB, double length,
//					double cost)
					
					String name=srcNode.getName()+"-"+destNode.getName();
					Link newLink=new Link(name, ipLayer.getLinklist().size(), "", ipLayer, srcNode, destNode, newWorkRouteOL.getLength(), newWorkRouteOL.getCost());
					
					newLink.setCapacity(X*slotNum);
					newLink.setSumFlow(currentNodePair.getTrafficdemand());
					newLink.setIpRemainFlow(newLink.getCapacity()-newLink.getSumFlow());
					newLink.setNature(0);
					newLink.setPhysicalLink(newWorkRouteOL.getLinklist());
					ipLayer.addLink(newLink);
					System.out.println("新光路:"+newLink.getName()+"\tslot数目："+slotNum+"\tCapacity:"+newLink.getCapacity()+"\t剩余流量是："+(newLink.getCapacity()-newLink.getSumFlow()));
					System.out.print("所经过的物理链路为");
					newWorkRouteOL.OutputRoute_node(newWorkRouteOL);
				}
					
				
			}
		}
		
	}


	
/*	public static int slotNumByDemand(double demand,double routeLength){
		int slot=0;
		double l=routeLength;
		double X=1;
		if(l<500){
			X=50;
		}else if(l<1000){
			X=37.5;
		}else if(l<2000){
			X=25;
		}else{
			X=12.5;
		}
		slot=(int)Math.ceil(demand/X);
		return slot;
	}*/
}
