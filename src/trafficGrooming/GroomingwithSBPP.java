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

	int transponderNum=0;

	public int getTransponderNum() {
		return transponderNum;
	}

	public void setTransponderNum(int transponderNum) {
		this.transponderNum = transponderNum;
	}
	public void SBPPgrooming(Layer ipLayer,Layer optLayer){
		ArrayList<NodePair> nodePairList=Request.sortNodePair(ipLayer);
		for(int i=0;i<nodePairList.size();i++){
			NodePair currentNodePair=nodePairList.get(i);
			Node srcNode=currentNodePair.getSrcNode();
			Node destNode=currentNodePair.getDesNode();
			System.out.println("\n\n-------------------------------------------------------\n"
					+ "��ǰ�����Ľڵ���ǣ�" + srcNode.getName() + "--" + destNode.getName() + "\t����������Ϊ��"
					+ currentNodePair.getTrafficdemand()
					+"\n-------------------------------------------------------");
			
			
			/**
			 * ��Ip��·�ɹ���·����
			 *       1����IP���ÿ����·�����Ҹ���·��������·�е�ʣ������������·
			 *       2��������������ڵ�ǰ���Ե����������򽫶�Ӧ������·��cost��length��Ϣ���Ƶ���·��ȥ
			 *       3�����򽫸���·�ų�
			 *       4����ʣ���������·�ɸ�����
			 *       5:���·�ɳɹ�������������·��������Ϣ������Ļ������Ű�
			 *       6������Ļ����½���·
			 */
			SearchConstraint constraint0=new SearchConstraint();
			Iterator<String> itr0=ipLayer.getLinklist().keySet().iterator();
			while(itr0.hasNext()){
				Link link=(Link)(ipLayer.getLinklist().get(itr0.next()));
				double maxRemain=0.0;
				VirtualLink maxVTLink=null;
				for(VirtualLink vtLink:link.getVirtualLinkList()){
					if(vtLink.getRemanCapacity()>maxRemain){
						maxRemain=vtLink.getRemanCapacity();
						maxVTLink=vtLink;
					}
				}
				
				if(maxVTLink!=null){
					if(maxVTLink.getRemanCapacity()>currentNodePair.getTrafficdemand()){
						link.setCost(maxVTLink.getCost());
						link.setLength(maxVTLink.getLength());
					}else{
						constraint0.getExcludedLinklist().add(link);
						System.out.println("ɾ������·Ϊ"+link.getName());
					}
				}
			}
			
			
			LinearRoute newRoute0=new LinearRoute("",0,"");
			RouteSearching rS0=new RouteSearching();
			rS0.Dijkstras(srcNode, destNode, ipLayer, newRoute0, constraint0);
			
			
			if(newRoute0.getLinklist().size()!=0){
				System.out.println("1.·�ɳɹ�");
				System.out.print("\t");
				newRoute0.OutputRoute_node(newRoute0);
				for(Link link:newRoute0.getLinklist()){
					double maxRemain=0.0;
					VirtualLink maxVTLink=null;
					for(VirtualLink vtLink:link.getVirtualLinkList()){
						if(vtLink.getRemanCapacity()>maxRemain){
							maxRemain=vtLink.getRemanCapacity();
							maxVTLink=vtLink;
						}
					}
					if(maxVTLink!=null){
						maxVTLink.setRemanCapacity(maxVTLink.getRemanCapacity()-currentNodePair.getTrafficdemand());
						maxVTLink.setNature(Constant.WORK);
					}
					
				}
			}else{
				/**
				 * �½�����·����
				 *     1���ڹ����·��
				 *     2������Ƶ����Դ
				 *     3���½���·
				 */
				Node optSrcNode=optLayer.getNodelist().get(srcNode.getName());
				Node optDestNode=optLayer.getNodelist().get(destNode.getName());
				LinearRoute optNewRoute0=new LinearRoute("optwork",1,"");
				RouteSearching optRS0=new RouteSearching();
				optRS0.Dijkstras(optSrcNode, optDestNode, optLayer, optNewRoute0, null);
				
				
				if(optNewRoute0.getLinklist().size()==0){
					System.out.println("2.���·��ʧ��");
				}else{
					System.out.println("2.���·�ɳɹ�");
					this.setTransponderNum(this.getTransponderNum()+2);
					double len0=optNewRoute0.getLength();
					double X0=1;
					if(len0<500){
						X0=50;
					}else if(len0<1000){
						X0=37.5;
					}else if(len0<2000){
						X0=25;
					}else{
						X0=12.5;
					}
					int slotNum0=(int)(Math.ceil(currentNodePair.getTrafficdemand()/X0));
					
					optNewRoute0.setSlotsnum(slotNum0);
					ArrayList<Integer> index0=Request.spectrumAllocationOneRoute_ReqList(optNewRoute0);
					
					if(index0.size()==0){
						System.out.println("\t������������Ƶ��");
					}else{
						System.out.println("\t������������Ƶ��");
						for(Link link:optNewRoute0.getLinklist()){
							for(int index=index0.get(0);index<slotNum0+index0.get(0);index++){
								link.getSlotsArray().get(index).setStatus(1);
								
							}
							link.setMaxSlot(slotNum0+link.getMaxSlot());
						}						
					}
					
					
					String name=srcNode.getName()+"-"+destNode.getName();
					Link newLink0=new Link(name, ipLayer.getLinklist().size(), "", ipLayer, srcNode, destNode, 1, 1);
					VirtualLink newVTLink=new VirtualLink(optNewRoute0.getCost(),optNewRoute0.getLength(), X0*slotNum0, X0*slotNum0-currentNodePair.getTrafficdemand());
				    newVTLink.setNature(Constant.PROTECTION);
				    newVTLink.setPhyLinkList(optNewRoute0.getLinklist());
				    newLink0.getVirtualLinkList().add(newVTLink);
				    ipLayer.addLink(newLink0);
				    System.out.println("\t�½���·�ɹ�\t"+newLink0.getName()+"\tslot num:"+slotNum0);
				    System.out.print("\t");
				    optNewRoute0.OutputRoute_node(optNewRoute0);
				}
				
				
			}
			
		}
	}
}
