package trafficGrooming;

import java.util.ArrayList;
import java.util.Iterator;

import demand.Request;
import general.Constant;
import graphalgorithms.RouteSearching;
import graphalgorithms.SearchConstraint;
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
			System.out.println("\n\n\n----------------------------------------------------");
			System.out.println("当前操作的节点对是：" + srcNode.getName() + "--" + destNode.getName() + "\t其流量需求为："
					+ currentNodePair.getTrafficdemand());
			System.out.println("-------------------------------------------------");
/*
 * //遍历iplayer，排除容量不足以及保护属性的链路，
 * 然后在IP虚拟拓扑上路由当前拓扑
 */			
			SearchConstraint constraint1=new SearchConstraint();
			Iterator<String> itr0=ipLayer.getLinklist().keySet().iterator();
			while(itr0.hasNext()){
				Link link=(Link) ipLayer.getLinklist().get(itr0.next());
				if(((link.getCapacity()-link.getSumFlow())<currentNodePair.getTrafficdemand())||(link.getNature()==1)){//属性：0 工作 1 保护
					constraint1.getExcludedLinklist().add(link);
					
				}
			}			
			LinearRoute newWorkRouteIP=new LinearRoute("IPWorkingRoute",0,"");
			RouteSearching rsIPWork=new RouteSearching();
			rsIPWork.Dijkstras(srcNode, destNode, ipLayer, newWorkRouteIP, constraint1);

			/*
			 * 在虚拟拓扑上路由工作路径成功
			 * 
			 */
			
			if(newWorkRouteIP.getLinklist().size()!=0){
				System.out.print("1.工作路径IP层路成功的由及分配资源，路由如下***********************\n\t");
				newWorkRouteIP.OutputRoute_node(newWorkRouteIP);
				
				//更新IP层链路信息
				for(Link link: newWorkRouteIP.getLinklist()){
					link.setNature(Constant.WORK);
					link.setSumFlow(link.getSumFlow()+currentNodePair.getTrafficdemand());	
					}
/*
 * IP路由保护路径，注意排除容量不足以及与工作链路在物理链路上有重叠的链路，以及所有的工作路径
 */
				SearchConstraint constraint2=new SearchConstraint();			
				Iterator<String> itr1=ipLayer.getLinklist().keySet().iterator();
				while(itr1.hasNext()){
					Link link=(Link)(ipLayer.getLinklist().get(itr1.next()));
					boolean flag=false;
					//当该IP链路的物理链路包含工作路径所经过的物理路径  flag=true,否则为 false
					out:for(Link link1: newWorkRouteIP.getLinklist()){
						for(Link link2:link1.getPhysicalLink()){
							if(link.getPhysicalLink().contains(link2)){
								flag=true;
								break out;
							}
						}
					}
					if((link.getCapacity()-link.getSumFlow()<currentNodePair.getTrafficdemand())||link.getNature()==Constant.WORK
							||flag){
						constraint2.getExcludedLinklist().add(link);
					}
				}
				
				LinearRoute protectionRouteIP=new LinearRoute("", 1, "");
				RouteSearching rsIPProt=new RouteSearching();
				rsIPProt.Dijkstras(srcNode, destNode, ipLayer, protectionRouteIP, constraint2);
				 
				if(protectionRouteIP.getLinklist().size()!=0){
					System.out.print("\n1.IP层路由保护路径成功！路径如下：***********************\n\t");
					protectionRouteIP.OutputRoute_node(protectionRouteIP);
					System.out.println("\n\n");
					
					
					//更新IP层链路信息
					for(Link link:protectionRouteIP.getLinklist()){
						link.setNature(Constant.PROTECTION);
						link.setSumFlow(link.getSumFlow()+currentNodePair.getTrafficdemand());
					}
				}else{
					System.out.print("\n1.保护路径IP层路由失败*****************\n");
/****************************************************************
 * 在光层路由保护路径,删除工作路径所经过的物理链路
 * **************************************************************
 */
//					排除工作路径所经过的物理链路
					SearchConstraint constraint3=new SearchConstraint();
					for(Link link:newWorkRouteIP.getLinklist()){
						for(Link link1:link.getPhysicalLink()){
							constraint3.getExcludedLinklist().add(link1);
						}
					}
										
					Node srcNode_OPP=optLayer.getNodelist().get(srcNode.getName());
					Node destNode_OPP=optLayer.getNodelist().get(destNode.getName());
					LinearRoute protectionRouteOP=new LinearRoute("optProtection",2,"");
					RouteSearching rsOPProt=new RouteSearching();
					rsOPProt.Dijkstras(srcNode_OPP, destNode_OPP, optLayer, protectionRouteOP, constraint3);
					
				
					if(protectionRouteOP.getLinklist().size()==0){
						System.out.println("\t保护路径在光层路由失败，歇菜了");
					}else{
						this.setTransponderNum(this.getTransponderNum()+2);
						double l=protectionRouteOP.getLength();
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
						protectionRouteOP.setSlotsnum(slotNum);
						ArrayList<Integer> indexArr=Request.spectrumAllocationOneRoute_ReqList(protectionRouteOP);
						if(indexArr.size()==0){
							System.out.println("\t堵塞，不分配频谱");
						}else{
							System.out.println("\t不堵塞，分配频谱资源");
							for(Link link:protectionRouteOP.getLinklist()){
								for(int index=indexArr.get(0);index<slotNum+indexArr.get(0);index++){
									link.getSlotsArray().get(index).setStatus(1);
									
								}
								link.setMaxSlot(slotNum+link.getMaxSlot());
							}						
						}
						
						
						String name=srcNode.getName()+"-"+destNode.getName();
						Link newLinkP=new Link(name, ipLayer.getLinklist().size(), "", ipLayer, srcNode, destNode, protectionRouteOP.getLength(),protectionRouteOP.getCost());
						
						newLinkP.setCapacity(slotNum*X);
						newLinkP.setSumFlow(currentNodePair.getTrafficdemand());
						newLinkP.setIpRemainFlow(newLinkP.getCapacity()-newLinkP.getSumFlow());
						newLinkP.setNature(Constant.PROTECTION);
						newLinkP.setPhysicalLink(protectionRouteOP.getLinklist());
						ipLayer.addLink(newLinkP);
						System.out.println("\t新的保护光路为"+newLinkP.getName()+"\tCapacity"+newLinkP.getCapacity()+"\t剩余："+newLinkP.getIpRemainFlow());
						System.out.print("\t物理路由为\n\t");
						protectionRouteOP.OutputRoute_node(protectionRouteOP);
					}
				}
			}
		
			else{
/*
 * 2.在光层路由工作路径，新建光路，排除容量不足，所有的工作链路，以及本次工作路径在物理链路有重叠的链路
 */
				System.out.println("2.工作路径IP层路由失败，转入光层处理");
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
				
					newWorkRouteOL.setSlotsnum(slotNum);
					ArrayList<Integer> slotIndex=Request.spectrumAllocationOneRoute_ReqList(newWorkRouteOL);
					/*
					 * 查看光层资源够不够用
					 */
					
					if(slotIndex.size()==0){
						System.out.println("堵塞了，没办法，不分配资源了");
					}else{
						System.out.println("\t不堵塞，分配资源");
						for(Link link:newWorkRouteOL.getLinklist()){
							for(int index=slotIndex.get(0);index<slotNum+slotIndex.get(0);index++){
								link.getSlotsArray().get(index).setStatus(1);
								
							}
							link.setMaxSlot(slotNum+link.getMaxSlot());
						}						
					}
					
				
					String name=srcNode.getName()+"-"+destNode.getName();
					Link newLink=new Link(name, ipLayer.getLinklist().size(), "", ipLayer, srcNode, destNode, newWorkRouteOL.getLength(), newWorkRouteOL.getCost());
					
					newLink.setCapacity(X*slotNum);
					newLink.setSumFlow(currentNodePair.getTrafficdemand());
					newLink.setIpRemainFlow(newLink.getCapacity()-newLink.getSumFlow());
					newLink.setNature(Constant.WORK);
					newLink.setPhysicalLink(newWorkRouteOL.getLinklist());
					ipLayer.addLink(newLink);
					System.out.println("\t新光路:"+newLink.getName()+"\tslot数目："+slotNum+"\tCapacity:"+newLink.getCapacity()+"\t剩余流量是："+(newLink.getCapacity()-newLink.getSumFlow()));
					System.out.print("\t所经过的物理链路为");
					newWorkRouteOL.OutputRoute_node(newWorkRouteOL);
					
					
/*
 * 2.IP层路由保护路径
 */
					SearchConstraint constraint4=new SearchConstraint();
					Iterator<String> itr3=ipLayer.getLinklist().keySet().iterator();
					while(itr3.hasNext()){
						Link link=(Link) (ipLayer.getLinklist().get(itr3.next()));
						boolean flag=false;
						for(Link link1:newLink.getPhysicalLink()){
							if(link.getPhysicalLink().contains(link1)){
								flag=true;
								break;
							}
						}
						if(flag||(link.getCapacity()-link.getSumFlow()<currentNodePair.getTrafficdemand())||(link.getNature()==Constant.WORK)){
							constraint4.getExcludedLinklist().add(link);
						}
					}
					LinearRoute protectionRouteIP=new LinearRoute("IPprotection",0,"");
					RouteSearching rsIPP=new RouteSearching();
					rsIPP.Dijkstras(srcNode, destNode, ipLayer, protectionRouteIP, constraint4);
					
					if(protectionRouteIP.getLinklist().size()!=0){
						System.out.println("2保护路径IP层路由成功，路由如下**********************");
						System.out.print("\t");
						protectionRouteIP.OutputRoute_node(protectionRouteIP);
						
						for(Link link:protectionRouteIP.getLinklist()){
							link.setNature(Constant.PROTECTION);
							link.setSumFlow(link.getSumFlow()+currentNodePair.getTrafficdemand());
						}
					}else{
						System.out.println("2保护路径IP层路由失败***************************");
/************************
 *2.光层路由保护路径 
 ***************************
 */

						//排除新建的工作路径所经过的物理链路
						SearchConstraint constraint5=new SearchConstraint();
						for(Link link:newLink.getPhysicalLink()){
							constraint5.getExcludedLinklist().add(link);
						}
						
						
						Node srcPNode_OP=optLayer.getNodelist().get(srcNode.getName());
						Node destPNode_OP=optLayer.getNodelist().get(destNode.getName());
						LinearRoute protectionOPRoute=new LinearRoute("", 4,"");
						RouteSearching rsPOP=new RouteSearching();
						rsPOP.Dijkstras(srcPNode_OP, destPNode_OP, optLayer, protectionOPRoute, constraint5);
						
						if(protectionOPRoute.getLinklist().size()==0){
							System.out.println("2光层路由失败，哈哈哈");
						}else{
							this.setTransponderNum(this.getTransponderNum()+2);
							double l1=protectionOPRoute.getLength();
							double X1=1;
							if(l1<500){
								X1=50;
							}else if(l1<1000){
								X1=37.5;
							}else if(l1<2000){
								X1=25;
							}else{
								X1=12.5;
							}
							int slotNum1=(int)(Math.ceil(currentNodePair.getTrafficdemand()/X1));
							protectionOPRoute.setSlotsnum(slotNum1);
							ArrayList<Integer> slotIndex1=Request.spectrumAllocationOneRoute_ReqList(protectionOPRoute);
							if(slotIndex1.size()==0){
								System.out.println("\t堵塞，不分配资源");
							}else{
								System.out.println("\t不堵塞，分配资源");
								for(Link link:protectionOPRoute.getLinklist()){
									for(int j=slotIndex1.get(0);j<slotNum1+slotIndex1.get(0);j++){
										link.getSlotsArray().get(j).setStatus(1);
									}
									link.setMaxSlot(link.getMaxSlot()+slotNum1);
								}
							}
						
							String name1=srcPNode_OP.getName()+"-"+destPNode_OP.getName();
							Link newLink1=new Link(name1, ipLayer.getLinklist().size(), "", ipLayer, srcNode, destNode, protectionOPRoute.getLength(), protectionOPRoute.getCost());
							
							newLink1.setCapacity(slotNum1*X1);
							newLink1.setSumFlow(currentNodePair.getTrafficdemand());
							newLink1.setIpRemainFlow(newLink1.getCapacity()-newLink1.getSumFlow());
							newLink1.setNature(Constant.PROTECTION);
							newLink1.setPhysicalLink(protectionOPRoute.getLinklist());
							ipLayer.addLink(newLink1);
							System.out.println("\t新的保护光路为："+newLink1.getName()+"\tslotnum="+slotNum1+"\t剩余"+newLink1.getIpRemainFlow());
							System.out.print("\t物理路由为：");
							protectionOPRoute.OutputRoute_node(protectionOPRoute);
						}
					
					}
				}
					
				
			}
		}
		
	}


}
