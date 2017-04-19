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
		

		// �ֽ��ڵ�԰��������Ӵ�С��˳��Խڵ������ArrayList<NodePair>
		ArrayList<NodePair> nodePairList = Request.sortNodePair(ipLayer);
		for (int i = 0; i < nodePairList.size(); i++) {
			// ��ȡ��ǰ�����Ľڵ�Ե�Ԫ�ڵ��Ŀ�Ľڵ�
			NodePair currentNodePair = nodePairList.get(i);
			Node srcNode = currentNodePair.getSrcNode();
			Node destNode = currentNodePair.getDesNode();
			System.out.println("\n\n\n----------------------------------------------------");
			System.out.println("��ǰ�����Ľڵ���ǣ�" + srcNode.getName() + "--" + destNode.getName() + "\t����������Ϊ��"
					+ currentNodePair.getTrafficdemand());
			System.out.println("-------------------------------------------------");
/*
 * //����iplayer���ų����������Լ��������Ե���·��
 * Ȼ����IP����������·�ɵ�ǰ����
 */			
			SearchConstraint constraint1=new SearchConstraint();
			Iterator<String> itr0=ipLayer.getLinklist().keySet().iterator();
			while(itr0.hasNext()){
				Link link=(Link) ipLayer.getLinklist().get(itr0.next());
				if(((link.getCapacity()-link.getSumFlow())<currentNodePair.getTrafficdemand())||(link.getNature()==1)){//���ԣ�0 ���� 1 ����
					constraint1.getExcludedLinklist().add(link);
					
				}
			}			
			LinearRoute newWorkRouteIP=new LinearRoute("IPWorkingRoute",0,"");
			RouteSearching rsIPWork=new RouteSearching();
			rsIPWork.Dijkstras(srcNode, destNode, ipLayer, newWorkRouteIP, constraint1);

			/*
			 * ������������·�ɹ���·���ɹ�
			 * 
			 */
			
			if(newWorkRouteIP.getLinklist().size()!=0){
				System.out.print("1.����·��IP��·�ɹ����ɼ�������Դ��·������***********************\n\t");
				newWorkRouteIP.OutputRoute_node(newWorkRouteIP);
				
				//����IP����·��Ϣ
				for(Link link: newWorkRouteIP.getLinklist()){
					link.setNature(Constant.WORK);
					link.setSumFlow(link.getSumFlow()+currentNodePair.getTrafficdemand());	
					}
/*
 * IP·�ɱ���·����ע���ų����������Լ��빤����·��������·�����ص�����·���Լ����еĹ���·��
 */
				SearchConstraint constraint2=new SearchConstraint();			
				Iterator<String> itr1=ipLayer.getLinklist().keySet().iterator();
				while(itr1.hasNext()){
					Link link=(Link)(ipLayer.getLinklist().get(itr1.next()));
					boolean flag=false;
					//����IP��·��������·��������·��������������·��  flag=true,����Ϊ false
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
					System.out.print("\n1.IP��·�ɱ���·���ɹ���·�����£�***********************\n\t");
					protectionRouteIP.OutputRoute_node(protectionRouteIP);
					System.out.println("\n\n");
					
					
					//����IP����·��Ϣ
					for(Link link:protectionRouteIP.getLinklist()){
						link.setNature(Constant.PROTECTION);
						link.setSumFlow(link.getSumFlow()+currentNodePair.getTrafficdemand());
					}
				}else{
					System.out.print("\n1.����·��IP��·��ʧ��*****************\n");
/****************************************************************
 * �ڹ��·�ɱ���·��,ɾ������·����������������·
 * **************************************************************
 */
//					�ų�����·����������������·
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
						System.out.println("\t����·���ڹ��·��ʧ�ܣ�Ъ����");
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
							System.out.println("\t������������Ƶ��");
						}else{
							System.out.println("\t������������Ƶ����Դ");
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
						System.out.println("\t�µı�����·Ϊ"+newLinkP.getName()+"\tCapacity"+newLinkP.getCapacity()+"\tʣ�ࣺ"+newLinkP.getIpRemainFlow());
						System.out.print("\t����·��Ϊ\n\t");
						protectionRouteOP.OutputRoute_node(protectionRouteOP);
					}
				}
			}
		
			else{
/*
 * 2.�ڹ��·�ɹ���·�����½���·���ų��������㣬���еĹ�����·���Լ����ι���·����������·���ص�����·
 */
				System.out.println("2.����·��IP��·��ʧ�ܣ�ת���㴦��");
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
				
					newWorkRouteOL.setSlotsnum(slotNum);
					ArrayList<Integer> slotIndex=Request.spectrumAllocationOneRoute_ReqList(newWorkRouteOL);
					/*
					 * �鿴�����Դ��������
					 */
					
					if(slotIndex.size()==0){
						System.out.println("�����ˣ�û�취����������Դ��");
					}else{
						System.out.println("\t��������������Դ");
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
					System.out.println("\t�¹�·:"+newLink.getName()+"\tslot��Ŀ��"+slotNum+"\tCapacity:"+newLink.getCapacity()+"\tʣ�������ǣ�"+(newLink.getCapacity()-newLink.getSumFlow()));
					System.out.print("\t��������������·Ϊ");
					newWorkRouteOL.OutputRoute_node(newWorkRouteOL);
					
					
/*
 * 2.IP��·�ɱ���·��
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
						System.out.println("2����·��IP��·�ɳɹ���·������**********************");
						System.out.print("\t");
						protectionRouteIP.OutputRoute_node(protectionRouteIP);
						
						for(Link link:protectionRouteIP.getLinklist()){
							link.setNature(Constant.PROTECTION);
							link.setSumFlow(link.getSumFlow()+currentNodePair.getTrafficdemand());
						}
					}else{
						System.out.println("2����·��IP��·��ʧ��***************************");
/************************
 *2.���·�ɱ���·�� 
 ***************************
 */

						//�ų��½��Ĺ���·����������������·
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
							System.out.println("2���·��ʧ�ܣ�������");
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
								System.out.println("\t��������������Դ");
							}else{
								System.out.println("\t��������������Դ");
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
							System.out.println("\t�µı�����·Ϊ��"+newLink1.getName()+"\tslotnum="+slotNum1+"\tʣ��"+newLink1.getIpRemainFlow());
							System.out.print("\t����·��Ϊ��");
							protectionOPRoute.OutputRoute_node(protectionOPRoute);
						}
					
					}
				}
					
				
			}
		}
		
	}


}
