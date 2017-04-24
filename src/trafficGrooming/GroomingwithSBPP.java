package trafficGrooming;

import java.util.ArrayList;
import java.util.Iterator;

import demand.Request;
import general.Constant;
import graphalgorithms.RouteSearching;
import graphalgorithms.SearchConstraint;
import network.Layer;
import network.Link;
import network.Node;
import network.NodePair;
import network.VirtualLink;
import subgraph.LinearRoute;

public class GroomingwithSBPP {

	int transponderNum = 0;

	public int getTransponderNum() {
		return transponderNum;
	}

	public void setTransponderNum(int transponderNum) {
		this.transponderNum = transponderNum;
	}

	public void SBPPgrooming(Layer ipLayer, Layer optLayer) {
		ArrayList<NodePair> nodePairList = Request.sortNodePair(ipLayer);
		for (int i = 0; i < nodePairList.size(); i++) {
			NodePair currentNodePair = nodePairList.get(i);
			Node srcNode = currentNodePair.getSrcNode();
			Node destNode = currentNodePair.getDesNode();
			System.out.println("-------------------------------------------------------\n" + "��ǰ�����Ľڵ���ǣ�"
					+ srcNode.getName() + "--" + destNode.getName() + "\t����������Ϊ��" + currentNodePair.getTrafficdemand()
					+ "\n-------------------------------------------------------");

			/**
			 * ��Ip��·�ɹ���·����
			 *         1����IP���ÿ����·�����Ҹ���·��������·�е�ʣ������������·
			 *         2��������������ڵ�ǰ���Ե����������򽫶�Ӧ������·��cost��length��Ϣ���Ƶ���·��ȥ 
			 *         3�����򽫸���·�ų�
			 *         4����ʣ���������·�ɸ����� 
			 *         5:���·�ɳɹ�������������·��������Ϣ������Ļ������Ű� 
			 *         6������Ļ����½���·
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
				System.out.println("1.·�ɳɹ�");
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
			} else {
				/**
				 * �½�����·���� 
				 *      1���ڹ����·�� 
				 *      2������Ƶ����Դ 
				 *      3���½���·
				 */
				Node optSrcNode = optLayer.getNodelist().get(srcNode.getName());
				Node optDestNode = optLayer.getNodelist().get(destNode.getName());
				LinearRoute optNewRoute0 = new LinearRoute("optwork", 1, "");
				RouteSearching optRS0 = new RouteSearching();
				optRS0.Dijkstras(optSrcNode, optDestNode, optLayer, optNewRoute0, null);

				if (optNewRoute0.getLinklist().size() == 0) {
					System.out.println("2.���·��ʧ��");
				} else {
					System.out.println("2.���·�ɳɹ�");
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
						System.out.println("\t������������Ƶ��");
					} else {
						System.out.println("\t������������Ƶ��");
						for (Link link : optNewRoute0.getLinklist()) {
							for (int index = index0.get(0); index < slotNum0 + index0.get(0); index++) {
								link.getSlotsArray().get(index).setStatus(1);

							}
							link.setMaxSlot(slotNum0 + link.getMaxSlot());
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
					System.out.println("\t�½���·�ɹ�\t" + newLink0.getName() + "\tslot num:" + slotNum0);
					System.out.print("\t");
					optNewRoute0.OutputRoute_node(optNewRoute0);
				}

			}
			System.out.println("\n\n\n");

		}

	}
	public void SBPPProtectionGrooming(NodePair currentNodePair,Layer ipLayer,Layer optLayer,LinearRoute worRoute){
		 /*
		 *�㷨����ÿ����·������������·����ʣ��������С�����ȡ����һ��������·��
	     *         1���鿴ʣ�������Ƿ񹻵�ǰ�����ã�������ã���ִ��2������ִ��4
	     *         2���鿴��·�͹�����·���������������·�Ƿ����ص������û�У�ִ��3������ȡ����һ��������·��ִ��1������ʣ��������·��ִ��4
	     *         3����ǵ�ǰ������·
	     *         4����ǰ��·û�п��õ�������·,��ӵ�constraint��
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
				//������·���ص�					
				Iterator<Link> itr_work=worRoute.getLinklist().iterator();
				while(itr_work.hasNext()){
					Link cuLink=(Link) itr_work.next();//current work link
					VirtualLink worVLink=cuLink.getVirtualLinkList().get(0);
					                                      //������·�õ���������·��ʣ�����������Ǹ��������ÿ��������ص�������
					for(int k2=0;k2<worVLink.getPhyLinkList().size();k2++){
						if(!(tempVLink.getPhyLinkList().contains(worVLink.getPhyLinkList().get(k2)))){//������·���ص�
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
			link.setTempVirtualLink(chosenVLink);//��Ǹ���·��ѡ���������·
		}
	}
	
	
	LinearRoute newRoute1=new LinearRoute("protection route",1,"");
	RouteSearching rS1=new RouteSearching();
	rS1.Dijkstras(srcNode, destNode, ipLayer, newRoute1, constraint1);
	
	if(newRoute1.getLinklist().size()!=0){
		System.out.println("1.IP���������ɹ�");
		System.out.print("\t");
		newRoute1.OutputRoute_node(newRoute1);
		//������·��������Ϣ
		for(Link link:newRoute1.getLinklist()){
			for(VirtualLink vLink:link.getVirtualLinkList()){
				if(vLink.getNature()==link.getTempVirtualLink().getNature()){
					vLink.setRemanCapacity(vLink.getRemanCapacity()-currentNodePair.getTrafficdemand());
					link.setTempVirtualLink(null);
					break;
				}
			}
		}
		//��tempvirtuallink����Ϊ�գ��Ա����
		Iterator<String> itr1_d=ipLayer.getLinklist().keySet().iterator();
		while(itr1_d.hasNext()){
			Link link=(Link) (ipLayer.getLinklist().get(itr1_d.next()));
			link.setTempVirtualLink(null);
		}
	}else{
		 
	}
	}
	public void protectionGrooming(NodePair currentNodePair,Layer ipLayer,Layer optLayer,LinearRoute worRoute){
		 /*
			 *�㷨����ÿ����·������������·����ʣ��������С�����ȡ����һ��������·��
		     *         1���鿴ʣ�������Ƿ񹻵�ǰ�����ã�������ã���ִ��2������ִ��4
		     *         2���鿴��·�͹�����·���������������·�Ƿ����ص������û�У�ִ��3������ȡ����һ��������·��ִ��1������ʣ��������·��ִ��4
		     *         3����ǵ�ǰ������·
		     *         4����ǰ��·û�п��õ�������·,��ӵ�constraint��
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
					//������·���ص�					
					Iterator<Link> itr_work=worRoute.getLinklist().iterator();
					while(itr_work.hasNext()){
						Link cuLink=(Link) itr_work.next();//current work link
						VirtualLink worVLink=cuLink.getVirtualLinkList().get(0);
						                                      //������·�õ���������·��ʣ�����������Ǹ��������ÿ��������ص�������
						for(int k2=0;k2<worVLink.getPhyLinkList().size();k2++){
							if(!(tempVLink.getPhyLinkList().contains(worVLink.getPhyLinkList().get(k2)))){//������·���ص�
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
				link.setTempVirtualLink(chosenVLink);//��Ǹ���·��ѡ���������·
			}
		}
		
		
		LinearRoute newRoute1=new LinearRoute("protection route",1,"");
		RouteSearching rS1=new RouteSearching();
		rS1.Dijkstras(srcNode, destNode, ipLayer, newRoute1, constraint1);
		
		if(newRoute1.getLinklist().size()!=0){
			System.out.println("1.IP���������ɹ�");
			System.out.print("\t");
			newRoute1.OutputRoute_node(newRoute1);
			//������·��������Ϣ
			for(Link link:newRoute1.getLinklist()){
				for(VirtualLink vLink:link.getVirtualLinkList()){
					if(vLink.getNature()==link.getTempVirtualLink().getNature()){
						vLink.setRemanCapacity(vLink.getRemanCapacity()-currentNodePair.getTrafficdemand());
						link.setTempVirtualLink(null);
						break;
					}
				}
			}
			//��tempvirtuallink����Ϊ�գ��Ա����
			Iterator<String> itr1_d=ipLayer.getLinklist().keySet().iterator();
			while(itr1_d.hasNext()){
				Link link=(Link) (ipLayer.getLinklist().get(itr1_d.next()));
				link.setTempVirtualLink(null);
			}
		}else{
			 
		}
		
	}
	
	
	
	//����·��������·����ʣ��������С����
	public void sortVTLink(Link link){
		ArrayList<VirtualLink> vtLinkList=new ArrayList<VirtualLink>();
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
