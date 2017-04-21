package trafficGrooming;

import java.util.ArrayList;
import java.util.Iterator;

import demand.Request;
import graphalgorithms.RouteSearching;
import graphalgorithms.SearchConstraint;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import subgraph.LinearRoute;

public class EONTrafficGrooming {
private  int transponderNum=0;
	
	public int getTransponderNum() {
		return transponderNum;
	}

	public void setTransponderNum(int transponderNum) {
		this.transponderNum = transponderNum;
	}
/*
 * 用link中的virtualinkList进行
 * 这里的
 */
	/*public void gooming_VL_Sharable(Layer ipLayer,Layer optLayer){
		ArrayList<NodePair> nodePairList = Request.sortNodePair(ipLayer);
		for (int i = 0; i < nodePairList.size(); i++){
			NodePair currentNodePair = nodePairList.get(i);
			Node srcNode = currentNodePair.getSrcNode();
			Node destNode = currentNodePair.getDesNode();
			System.out.println("\n\n当前操作的节点对是：" + srcNode.getName() + "--" + destNode.getName() + "\t其流量需求为："
					+ currentNodePair.getTrafficdemand());
			//在IP层应当保留的链路应当具有以下属性：1，剩余容量够用就OK了
			SearchConstraint constraint0=new SearchConstraint();
			Iterator<String> itr0=ipLayer.getLinklist().keySet().iterator();
			while(itr0.hasNext()){//只要虚链路列表中存在一条容量足够即可，反之，如果没有，则排除
				boolean flag=false;
				Link link=(Link) ipLayer.getLinklist().get(itr0.next());
				for(VirtualLink vtLink:link.getVirtualLinkList()){
					if(vtLink.getRemanCapacity()>currentNodePair.getTrafficdemand()){
						flag=true;
						break;
					}
				}
				
				if(!flag){
					constraint0.getExcludedLinklist().add(link);
				}
			}
			
			RouteSearching rS0=new RouteSearching();
			LinearRoute newRoute0=new LinearRoute("", 0, "");
			rS0.Dijkstras(srcNode, destNode, ipLayer, newRoute0, constraint0);
			constraint0.getExcludedLinklist().clear();
			
			
			if(newRoute0.getLinklist().size()!=0){
				System.out.println("1.在IP层路由成功工作路径");
				newRoute0.OutputRoute_node(newRoute0);
				
				for(Link link:newRoute0.getLinklist()){
					VirtualLink tempVrtLink=null;
					for(VirtualLink vtLink:link.getVirtualLinkList()){
						
					}
				}
			}
			
		}
	}*/
	public  void trafficGroominginEON(Layer ipLayer, Layer optLayer) {
		

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
				if(((link.getCapacity()-link.getSumFlow())<currentNodePair.getTrafficdemand())||(link.getNature()==1)){
					tempDelLinkList.add(link);
					
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
			
			
			LinearRoute newWorkRouteIP=new LinearRoute("IPWorkingRoute",0,"");
			RouteSearching rsIPWork=new RouteSearching();
			rsIPWork.Dijkstras(srcNode, destNode, ipLayer, newWorkRouteIP, null);

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

	
}
