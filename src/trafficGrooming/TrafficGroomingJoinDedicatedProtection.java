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
		

		// �ֽ��ڵ�԰��������Ӵ�С��˳��Խڵ������ArrayList<NodePair>
		ArrayList<NodePair> nodePairList = Request.sortNodePair(ipLayer);
		for (int i = 0; i < nodePairList.size(); i++) {
			// ��ȡ��ǰ�����Ľڵ�Ե�Ԫ�ڵ��Ŀ�Ľڵ�
			NodePair currentNodePair = nodePairList.get(i);
			Node srcNode = currentNodePair.getSrcNode();
			Node destNode = currentNodePair.getDesNode();
			System.out.println("\n\n��ǰ�����Ľڵ���ǣ�" + srcNode.getName() + "--" + destNode.getName() + "\t����������Ϊ��"
					+ currentNodePair.getTrafficdemand());
			/*
			 * //����iplayer���ų����������Լ��������Ե���·��
			 * Ȼ����IP����������·�ɵ�ǰ����
			 */			
			ArrayList<Link> tempDelLinkList = new ArrayList<Link>();
			Iterator<String> itr0=ipLayer.getLinklist().keySet().iterator();
			while(itr0.hasNext()){
				Link link=(Link) ipLayer.getLinklist().get(itr0.next());
//				System.out.println("IP��洢����·��"+link.getName()+"\tʣ������Ϊ��"+(link.getCapacity()-link.getSumFlow()));
				if(((link.getCapacity()-link.getSumFlow())<currentNodePair.getTrafficdemand())||(link.getNature()==1)){
					tempDelLinkList.add(link);
					
					//ipLayer.removeLink(link.getName());
				}
			}
			for(Link link:tempDelLinkList){
				ipLayer.removeLink(link.getName());			   
			}

			/*
			 * ���ʣ�����·
			 */
			Iterator<String> itr10=ipLayer.getLinklist().keySet().iterator();
			while(itr10.hasNext()){
				Link linkn=(Link) ipLayer.getLinklist().get(itr10.next());
				System.out.println("ʣ�����·��"+(linkn.getName())+"\t"+(linkn.getCapacity()-linkn.getSumFlow())+"\t"+linkn.getCost()+"\t"+linkn.getLength());
			}
			
			/*
			 * ����ڵ�
			 */
			/*Iterator<String> itr11=ipLayer.getNodelist().keySet().iterator();
			while(itr11.hasNext()){
				Node node=(Node) ipLayer.getNodelist().get(itr11.next());
				System.out.println("\n\nʣ��ڵ�Ϊ��"+node.getName()+"\t�����ڵĽڵ�Ϊ��");
				for(Node node1:node.getNeinodelist()){
					System.out.print("\t"+node1.getName());
				}
			}*/
			LinearRoute newWorkRouteIP=new LinearRoute("IPWorkingRoute",0,"");
			RouteSearching rsIPWork=new RouteSearching();
			rsIPWork.Dijkstras(srcNode, destNode, ipLayer, newWorkRouteIP, null);
//			newWorkRouteIP.OutputRoute_node(newWorkRouteIP);
			//��Ip����·�ɳɹ��Ժ󣬻ָ��ų��Ľڵ�
			for(Link link: tempDelLinkList){
				ipLayer.addLink(link);
			}
			tempDelLinkList.clear();
		
			/*
			 * ������������·�ɹ���·���ɹ�
			 * 
			 */
			
			if(newWorkRouteIP.getLinklist().size()!=0){
				System.out.println("\n\n******************��IP��·�ɹ����ɹ���·����������Դ��***********************\n\n");
				newWorkRouteIP.OutputRoute_node(newWorkRouteIP);
				for(Link link: newWorkRouteIP.getLinklist()){
					link.setNature(0);
					link.setSumFlow(link.getSumFlow()+currentNodePair.getTrafficdemand());					
				}
			}
			
			/*
			 * ������������·�ɹ���·��ʧ��
			 */
			else{
			/*
			 * �ڹ��·�ɣ��½���·���ų��������㣬���еĹ�����·���Լ����ι���·����������·���ص�����·
			 */
				System.out.println("��IP��·�ɹ���·��ʧ�ܣ�ת���㴦��");
				Node srcNode_OP=optLayer.getNodelist().get(srcNode.getName());
				Node destNode_OP=optLayer.getNodelist().get(destNode.getName());
				LinearRoute newWorkRouteOL=new LinearRoute("OptWorkingRoute",1,"");
				RouteSearching rsOLWork=new RouteSearching();
				rsOLWork.Dijkstras(srcNode_OP, destNode_OP, optLayer, newWorkRouteOL, null);
				if(newWorkRouteOL.getLinklist().size()==0){
					System.out.println("����·���ڹ��·��ʧ�ܣ����½���·������Ъ���ˣ�û�ޣ���������");
				}
				/*
				 * �ڹ��·�ɹ���·����������·�ɹ��������ɣ�������������Դ�����Ǳ���·����
				 */
				else{
					this.setTransponderNum(this.getTransponderNum()+2);
					
					//����Traffic Demand�Լ�·��������ȷ��ɶ�أ���������slot�ĸ���
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
//					System.out.println("��ǰ�ڵ�Թ���·��ռ�õĹ���slot������"+slotNum);
					
					
					newWorkRouteOL.setSlotsnum(slotNum);
					ArrayList<Integer> slotIndex=Request.spectrumAllocationOneRoute_ReqList(newWorkRouteOL);
					/*
					 * �鿴�����Դ��������
					 */
					
					if(slotIndex.size()==0){
						System.out.println("�����ˣ�û�취����������Դ��");
					}else{
						//���¹��ռ�õ���·����Ϣ
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
					System.out.println("�¹�·:"+newLink.getName()+"\tslot��Ŀ��"+slotNum+"\tCapacity:"+newLink.getCapacity()+"\tʣ�������ǣ�"+(newLink.getCapacity()-newLink.getSumFlow()));
					System.out.print("��������������·Ϊ");
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
