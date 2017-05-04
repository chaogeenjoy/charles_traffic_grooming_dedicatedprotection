package trafficGrooming;

import java.util.ArrayList;
import java.util.Iterator;

import demand.Request;
import general.Constant;
import general.Slot;
import graphalgorithms.RouteSearching;
import graphalgorithms.SearchConstraint;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import subgraph.LinearRoute;

public class GroomingwithSBPP {

	private int transponderNum = 0;
	private int maxSlotNum=0;

	public int getTransponderNum() {
		return transponderNum;
	}

	public void setTransponderNum(int transponderNum) {
		this.transponderNum = transponderNum;
	}
	

	public int getMaxSlotNum() {
		return maxSlotNum;
	}

	public void setMaxSlotNum(int maxSlotNum) {
		this.maxSlotNum = maxSlotNum;
	}

	public void SBPPgrooming(Layer ipLayer, Layer optLayer) {
		ArrayList<NodePair> nodePairList = Request.sortNodePair(ipLayer);
		for (int i = 0; i < nodePairList.size(); i++) {
			NodePair currentNodePair = nodePairList.get(i);
			Node srcNode = currentNodePair.getSrcNode();
			Node destNode = currentNodePair.getDesNode();
			System.out.println("-------------------------------------------------------\n" + "当前操作的节点对是："
					+ srcNode.getName() + "--" + destNode.getName() + "\t其流量需求为：" + currentNodePair.getTrafficdemand()
					+ "\n-------------------------------------------------------");

			/**
			 * 在Ip层路由工作路径：
			 *         1：在IP层的每个链路，查找该链路的虚拟链路中的剩余容量最大的链路
			 *         2：如果该容量大于当前结点对的容量需求，则将对应虚拟链路的cost和length信息复制到链路中去 
			 *         3：否则将该链路排除
			 *         4：在剩余的拓扑上路由该需求 
			 *         5:如果路由成功，更新虚拟链路的容量信息，否则的话，看着办 
			 *         6：否则的话，新建光路
			 */
			SearchConstraint constraint0 = new SearchConstraint();
			Iterator<String> itr0 = ipLayer.getLinklist().keySet().iterator();
			while (itr0.hasNext()) {
				Link link = (Link) (ipLayer.getLinklist().get(itr0.next()));
				double maxRemain = 0.0;
				VirtualLink maxVTLink = null;
				for (VirtualLink vtLink : link.getVirtualLinkList()) {
					if (vtLink.getRemanCapacity() > maxRemain) {
						maxRemain = vtLink.getRemanCapacity();
						maxVTLink = vtLink;
					}
				} 

				if (maxVTLink != null) {
					if (maxVTLink.getRemanCapacity() > currentNodePair.getTrafficdemand()) {
						link.setCost(maxVTLink.getCost());
						link.setLength(maxVTLink.getLength());
					} else {
						constraint0.getExcludedLinklist().add(link);
					}
				} else {
					constraint0.getExcludedLinklist().add(link);
				}
			}

			LinearRoute newRoute0 = new LinearRoute("", 0, "");
			RouteSearching rS0 = new RouteSearching();
			rS0.Dijkstras(srcNode, destNode, ipLayer, newRoute0, constraint0);

			if (newRoute0.getLinklist().size() != 0) {
				System.out.println("1.路由成功");
				System.out.print("\t");
				newRoute0.OutputRoute_node(newRoute0);
				for (Link link : newRoute0.getLinklist()) {
					double maxRemain = 0.0;
					VirtualLink maxVTLink = null;
					for (VirtualLink vtLink : link.getVirtualLinkList()) {
						if (vtLink.getRemanCapacity() > maxRemain) {
							maxRemain = vtLink.getRemanCapacity();
							maxVTLink = vtLink;
						}
					}

					maxVTLink.setRemanCapacity(maxVTLink.getRemanCapacity() - currentNodePair.getTrafficdemand());

				}
				
				this.protectionGrooming(currentNodePair, ipLayer, optLayer, newRoute0);
			} else {
				/**
				 * 新建工作路径： 
				 *      1：在光层上路由 
				 *      2：分配频谱资源 
				 *      3：新建光路
				 */
				Node optSrcNode = optLayer.getNodelist().get(srcNode.getName());
				Node optDestNode = optLayer.getNodelist().get(destNode.getName());
				LinearRoute optNewRoute0 = new LinearRoute("optwork", 1, "");
				RouteSearching optRS0 = new RouteSearching();
				optRS0.Dijkstras(optSrcNode, optDestNode, optLayer, optNewRoute0, null);

				if (optNewRoute0.getLinklist().size() == 0) {
					System.out.println("2.光层建立工作失败");
				} else {
					System.out.println("2.光层建立工作成功");
					this.setTransponderNum(this.getTransponderNum() + 2);
					double len0 = optNewRoute0.getLength();
					double X0 = 1;
					if (len0 < 500) {
						X0 = 50;
					} else if (len0 < 1000) {
						X0 = 37.5;
					} else if (len0 < 2000) {
						X0 = 25;
					} else {
						X0 = 12.5;
					}
					int slotNum0 = (int) (Math.ceil(currentNodePair.getTrafficdemand() / X0));

					optNewRoute0.setSlotsnum(slotNum0);
					ArrayList<Integer> index0 = Request.spectrumAllocationOneRoute_ReqList(optNewRoute0);

					if (index0.size() == 0) {
						System.out.println("\t阻塞，不分配频谱");
					} else {
						System.out.println("\t不阻塞，分配频谱");
						for (Link link : optNewRoute0.getLinklist()) {
							for (int index = index0.get(0); index < slotNum0 + index0.get(0); index++) {
								link.getSlotsArray().get(index).setStatus(1);

							}
//							link.setMaxSlot(slotNum0 + link.getMaxSlot());
						}
					}

					String name = srcNode.getName() + "-" + destNode.getName();
					Link newLink0 = new Link(name, ipLayer.getLinklist().size(), "", ipLayer, srcNode, destNode, 1, 1);
					VirtualLink newVTLink = new VirtualLink(optNewRoute0.getCost(), optNewRoute0.getLength(),
							X0 * slotNum0, X0 * slotNum0 - currentNodePair.getTrafficdemand());
					newVTLink.setNature(Constant.PROTECTION);
					newVTLink.setPhyLinkList(optNewRoute0.getLinklist());
					newLink0.getVirtualLinkList().add(newVTLink);
					ipLayer.addLink(newLink0);
					System.out.println("\t新建光路成功\t" + newLink0.getName() + "\tslot num:" + slotNum0);
					System.out.print("\t");
					optNewRoute0.OutputRoute_node(optNewRoute0);
					
					LinearRoute newWorkRoute=new LinearRoute("opt wor route", 0, "");
					newWorkRoute.getLinklist().add(newLink0);
					this.protectionGrooming(currentNodePair, ipLayer, optLayer, newWorkRoute);
				}

			}
			System.out.println("\n\n\n");

		}
		
		int maxSlot=0;
		Iterator<String> it=optLayer.getLinklist().keySet().iterator();
		while(it.hasNext()){
			Link link=(Link) (optLayer.getLinklist().get(it.next()));
			link.setMaxSlot(0);
		
			for(int s=0;s<link.getSlotsArray().size();s++){
				if(link.getSlotsArray().get(s).getStatus()!=0){
					link.setMaxSlot(link.getMaxSlot()+1);
				}
			}
			if(maxSlot<link.getMaxSlot()){
				maxSlot=link.getMaxSlot();
			}
		}
		this.setMaxSlotNum(maxSlot);

	}
	
	public void protectionGrooming(NodePair currentNodePair,Layer ipLayer,Layer optLayer,LinearRoute worRoute){
		 /*
			 *算法：对每个链路的所有虚拟链路按照剩余容量大小排序后，取出第一条虚拟链路：
		     *         1：查看剩余容量是否够当前结点对用，如果够用，则执行2，否则执行4
		     *         2：查看链路和工作链路所经过的物理层链路是否有重叠，如果没有，执行3，否则，取出下一条虚拟链路，执行1，若无剩余虚拟链路，执行4
		     *         3：标记当前虚拟链路
		     *         4：当前链路没有可用的虚拟链路,添加到constraint中
		     * 这里的IP层是1+1的
			 */
		Node srcNode=currentNodePair.getSrcNode();
		Node destNode=currentNodePair.getDesNode();
		
		
		SearchConstraint constraint1=new SearchConstraint();
		Iterator<String> itr1=ipLayer.getLinklist().keySet().iterator();
		while(itr1.hasNext()){
			Link link=(Link)(ipLayer.getLinklist().get(itr1.next()));
			VirtualLink chosenVLink=null;
			this.sortVTLink(link);
						
			NE:for(int k=0;k<link.getVirtualLinkList().size();k++){
				VirtualLink tempVLink=link.getVirtualLinkList().get(k);
				if(tempVLink.getRemanCapacity()>currentNodePair.getTrafficdemand()){
					//物理链路有重叠					
					Iterator<Link> itr_work=worRoute.getLinklist().iterator();
					while(itr_work.hasNext()){
						Link cuLink=(Link) itr_work.next();//current work link
						this.sortVTLink(cuLink);
						VirtualLink worVLink=cuLink.getVirtualLinkList().get(0);
						         //工作链路用的是虚拟链路中剩余容量最大的那个，他不用考虑物理重叠的问题
												
						for(int k2=0;k2<worVLink.getPhyLinkList().size();k2++){
							if(!(tempVLink.getPhyLinkList().contains(worVLink.getPhyLinkList().get(k2)))){//物理链路不重叠
    							chosenVLink=tempVLink;    							
								break NE;
							}
						}
					}
				}else{
					break NE;//not enough remaining capacity
				}
			}
						
			
			if(chosenVLink==null){
				constraint1.getExcludedLinklist().add(link);
			}else{
				link.setCost(chosenVLink.getCost());
				link.setLength(chosenVLink.getLength());
				link.setTempVirtualLink(chosenVLink);//标记该链路所选择的虚拟链路
			}
		}
		
		
		
		LinearRoute newRoute1=new LinearRoute("protection route",1,"");
		RouteSearching rS1=new RouteSearching();
		rS1.Dijkstras(srcNode, destNode, ipLayer, newRoute1, constraint1);
		
		
		
		
		if(newRoute1.getLinklist().size()!=0){
			System.out.println("1.IP保护建立成功");
			System.out.print("\t");
			newRoute1.OutputRoute_node(newRoute1);
			//更新链路的容量信息
			for(Link link:newRoute1.getLinklist()){
				for(VirtualLink vLink:link.getVirtualLinkList()){
					if(vLink.getNature()==link.getTempVirtualLink().getNature()){//找到所用的虚拟链路
						vLink.setRemanCapacity(vLink.getRemanCapacity()-currentNodePair.getTrafficdemand());
						link.setTempVirtualLink(null);
						break;
					}
				}
			}
			
			//将tempvirtuallink设置为空，以便回收
			Iterator<String> itr1_d=ipLayer.getLinklist().keySet().iterator();
			while(itr1_d.hasNext()){
				Link link=(Link) (ipLayer.getLinklist().get(itr1_d.next()));
				link.setTempVirtualLink(null);
			}
			
		}else{//在光层建立光通道，考虑SBPP
			/*
			 * 在光层建立保护路径：
			 *        1.在路由的时候，排除工作路径所经过的物理链路
			 *        2.在频谱分配的时候，考虑可以共享的情况
			 */
			
			
			Node optSrcNode=optLayer.getNodelist().get(srcNode.getName());
			Node optDestNode=optLayer.getNodelist().get(destNode.getName());
			
			
			SearchConstraint optConstraint1=new SearchConstraint();
			Iterator<String> itr1_pro=optLayer.getLinklist().keySet().iterator();
			while(itr1_pro.hasNext()){
				Link link=(Link)(optLayer.getLinklist().get(itr1_pro.next()));
				for(int i1=0;i1<worRoute.getLinklist().size();i1++){
					this.sortVTLink(worRoute.getLinklist().get(i1));
					if(worRoute.getLinklist().get(i1).getVirtualLinkList().get(0).getPhyLinkList().contains(link)){
						optConstraint1.getExcludedLinklist().add(link);
						break;
					}
				}
			}
			
			
			LinearRoute optNewRoute1=new LinearRoute("opt pro route",1,"");
			RouteSearching optRS1=new RouteSearching();
			optRS1.Dijkstras(optSrcNode, optDestNode, optLayer, optNewRoute1, optConstraint1);
			
			if(optNewRoute1.getLinklist().size()==0){
				System.out.println("2.保护光层建立失败");
			}else{
				System.out.println("2.保护光层建立成功");
				this.setTransponderNum(this.getTransponderNum()+2);
				
				double len1=optNewRoute1.getLength();
				double X1=1;
				if (len1 < 500) {
					X1 = 50;
				} else if (len1 < 1000) {
					X1 = 37.5;
				} else if (len1 < 2000) {
					X1 = 25;
				} else {
					X1 = 12.5;
				}
				int slotNum1 = (int) (Math.ceil(currentNodePair.getTrafficdemand() / X1));
				optNewRoute1.setSlotsnum(slotNum1);
				
				
				ArrayList<VirtualLink> workVLinkList=new ArrayList<VirtualLink>();
				Iterator<Link> itr_workRoute=worRoute.getLinklist().iterator();
				while(itr_workRoute.hasNext()){
					Link link=(Link) (itr_workRoute.next());
					this.sortVTLink(link);
					workVLinkList.add(link.getVirtualLinkList().get(0));
				}
				
				
				
				ArrayList<Integer> index1=this.spectrumAllocation_1Route_SBPP(optNewRoute1, workVLinkList);
				
				
				if(index1.size()==0){
					System.out.println("\t频谱堵塞，不分配");
				}else{
					System.out.println("\t频谱不堵塞，分配资源");
					
					//update slot information
					for(Link link:optNewRoute1.getLinklist()){
						for(int i=index1.get(0);i<slotNum1+index1.get(0);i++){
							Slot occupySlot=link.getSlotsArray().get(i);
							occupySlot.setStatus(2);
							for(VirtualLink vL:workVLinkList){
								occupySlot.getVirtualLinkList().add(vL);
							}							
						}
//						link.setMaxSlot(link.getMaxSlot()+slotNum1);
					}					
				}
				
				
				//新建光路
				String name1=srcNode.getName()+"-"+destNode.getName();
				Link newLink1=new Link(name1, optLayer.getLinklist().size(), "", ipLayer, srcNode, destNode, 1, 1);
				VirtualLink newVLink1=new VirtualLink(optNewRoute1.getCost(), optNewRoute1.getLength(), X1*slotNum1, X1*slotNum1-currentNodePair.getTrafficdemand());
				newVLink1.setNature(Constant.PROTECTION);
				newVLink1.setPhyLinkList(optNewRoute1.getLinklist());
				newLink1.getVirtualLinkList().add(newVLink1);
				ipLayer.addLink(newLink1);
				System.out.println("\t新建保护光路"+name1+"\tslot number="+slotNum1+"\t剩余容量为"+newVLink1.getRemanCapacity());
				System.out.print("\t");
				optNewRoute1.OutputRoute_node(optNewRoute1);
			}
		}
		
	}
	
	
	
	//将链路的虚拟链路按照剩余容量大小排序,从大到小
	public void sortVTLink(Link link){
		ArrayList<VirtualLink> vtLinkList=new ArrayList<VirtualLink>();
		if(link.getVirtualLinkList().size()==0){
			System.out.println("\t无法排序，无虚拟链路存在");
		}else{
			for(int i=0;i<link.getVirtualLinkList().size();i++){
				int m=i;
				for(int j=i+1;j<link.getVirtualLinkList().size();j++){

					if(link.getVirtualLinkList().get(m).getRemanCapacity()>link.getVirtualLinkList().get(j).getRemanCapacity()){
						m=j;
					}			
				}
				vtLinkList.add(0,link.getVirtualLinkList().get(m));
			}	
			link.getVirtualLinkList().clear();
			link.setVirtualLinkList(vtLinkList);
		}		
	}
	
	/*
	 * 频谱分配分析：
	 *        1.给定物理层的路由链路列表，考虑在这写链路上面分配频谱
	 *        2.查看slot的状态的时候，分三种情况：
	 *                        a.状态为free，此时，可以占用；
	 *                        b.状态为1，此时，仅工作路径可以占用；
	 *                        c.状态为2，此时保护路径占用，同时要查看占用了当前slot的所有工作路径的物理链路列表，
	 *                                 是否包含当前工作路径的物理链路，如果包含，则不能用，否则，可以用
	 * 解决方案：
	 *       1.就以上分析，考虑工作和保护的频谱分析分开进行，对于工作路径，只要状态！=0就不可用
	 *       2.对于保护路径，在slot里设置占用的虚拟链路列表，存储保护路径对应的工作路径的虚拟链路列表，用于查找和当前工作路径是否有重叠的地方
	 */
	
	public ArrayList<Integer> spectrumAllocation_1Route_SBPP(LinearRoute route,ArrayList<VirtualLink> workVLinkList){
		ArrayList<Link> routelink = route.getLinklist();//路由中存储的链路列表
		
		if (route.getSlotsnum() == 0) {
			System.out.println("no slots");
		}else{
			for (Link link : routelink) {
				link.getSlotsIndexInOneSW().clear();		
				for (int r = 0; r <= link.getSlotsArray().size() - route.getSlotsnum(); r++) {
					boolean s = true;
					for(int k = r; k < route.getSlotsnum() + r; k++) {
						Slot currentSlot=link.getSlotsArray().get(k);
						if(currentSlot.getStatus()==1){//1表示被工作路径占用
							s=false;
							break;
						}else{
							if((currentSlot.getStatus()==2)&&(currentSlot.workJoint(workVLinkList))){
								//2表示被保护路径占
								//后面表示有物理链路重叠
								s=false;
								break;								
							}
						}
					}
					if (s) //意味着SW中的F个slot都是可用的
						link.getSlotsIndexInOneSW().add(r);//将可用的SW的初始索引添加到Slotsindex()集合中去					
				}
			}
		}
		
		Link link = routelink.get(0);
		ArrayList<Integer> sameindex = new ArrayList<Integer>();
		sameindex.clear();
		
		for (int i = 0; i < link.getSlotsIndexInOneSW().size(); i++) {
			int index = link.getSlotsIndexInOneSW().get(i);
			int flag = 1;
			for (Link link2 : routelink) {
				if (!link2.getSlotsIndexInOneSW().contains(index)) {
					flag = 0;
					break;
				}
			}
			if (flag != 0) {
				sameindex.add(link.getSlotsIndexInOneSW().get(i));
			}
		}
		
		return sameindex;
	}
	
	/*public void SBPPProtectionGrooming(NodePair currentNodePair,Layer ipLayer,Layer optLayer,LinearRoute worRoute){
	 
	 *算法：对每个链路的所有虚拟链路按照剩余容量大小排序后，取出第一条虚拟链路：
     *         1：查看剩余容量是否够当前结点对用，如果够用，则执行2，否则执行4
     *         2：查看链路和工作链路所经过的物理层链路是否有重叠，如果没有，执行3，否则，取出下一条虚拟链路，执行1，若无剩余虚拟链路，执行4
     *         3：标记当前虚拟链路
     *         4：当前链路没有可用的虚拟链路,添加到constraint中
	 
Node srcNode=currentNodePair.getSrcNode();
Node destNode=currentNodePair.getDesNode();


SearchConstraint constraint1=new SearchConstraint();
Iterator<String> itr1=ipLayer.getLinklist().keySet().iterator();
while(itr1.hasNext()){
	Link link=(Link)(ipLayer.getLinklist().get(itr1.next()));
	VirtualLink chosenVLink=null;
	this.sortVTLink(link);
				
	NE:for(int k=0;k<link.getVirtualLinkList().size();k++){
		VirtualLink tempVLink=link.getVirtualLinkList().get(k);
		if(tempVLink.getRemanCapacity()>currentNodePair.getTrafficdemand()){
			//物理链路有重叠					
			Iterator<Link> itr_work=worRoute.getLinklist().iterator();
			while(itr_work.hasNext()){
				Link cuLink=(Link) itr_work.next();//current work link
				VirtualLink worVLink=cuLink.getVirtualLinkList().get(0);
				                                      //工作链路用的是虚拟链路中剩余容量最大的那个，他不用考虑无力重叠的问题
				for(int k2=0;k2<worVLink.getPhyLinkList().size();k2++){
					if(!(tempVLink.getPhyLinkList().contains(worVLink.getPhyLinkList().get(k2)))){//物理链路不重叠
						chosenVLink=tempVLink;    							
						break NE;
					}
				}
			}
			
		}else{
			break NE;//not enough remaining capacity
		}
	}				
	
	if(chosenVLink==null){
		constraint1.getExcludedLinklist().add(link);
	}else{
		link.setCost(chosenVLink.getCost());
		link.setLength(chosenVLink.getLength());
		link.setTempVirtualLink(chosenVLink);//标记该链路所选择的虚拟链路
	}
}


LinearRoute newRoute1=new LinearRoute("protection route",1,"");
RouteSearching rS1=new RouteSearching();
rS1.Dijkstras(srcNode, destNode, ipLayer, newRoute1, constraint1);

if(newRoute1.getLinklist().size()!=0){
	System.out.println("1.IP保护建立成功");
	System.out.print("\t");
	newRoute1.OutputRoute_node(newRoute1);
	//更新链路的容量信息
	for(Link link:newRoute1.getLinklist()){
		for(VirtualLink vLink:link.getVirtualLinkList()){
			if(vLink.getNature()==link.getTempVirtualLink().getNature()){
				vLink.setRemanCapacity(vLink.getRemanCapacity()-currentNodePair.getTrafficdemand());
				link.setTempVirtualLink(null);
				break;
			}
		}
	}
	//将tempvirtuallink设置为空，以便回收
	Iterator<String> itr1_d=ipLayer.getLinklist().keySet().iterator();
	while(itr1_d.hasNext()){
		Link link=(Link) (ipLayer.getLinklist().get(itr1_d.next()));
		link.setTempVirtualLink(null);
	}
}else{
	 
}
}*/

}
