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
 * ��link�е�virtualinkList����
 * �����
 */
	/*public void gooming_VL_Sharable(Layer ipLayer,Layer optLayer){
		ArrayList<NodePair> nodePairList = Request.sortNodePair(ipLayer);
		for (int i = 0; i < nodePairList.size(); i++){
			NodePair currentNodePair = nodePairList.get(i);
			Node srcNode = currentNodePair.getSrcNode();
			Node destNode = currentNodePair.getDesNode();
			System.out.println("\n\n��ǰ�����Ľڵ���ǣ�" + srcNode.getName() + "--" + destNode.getName() + "\t����������Ϊ��"
					+ currentNodePair.getTrafficdemand());
			//��IP��Ӧ����������·Ӧ�������������ԣ�1��ʣ���������þ�OK��
			SearchConstraint constraint0=new SearchConstraint();
			Iterator<String> itr0=ipLayer.getLinklist().keySet().iterator();
			while(itr0.hasNext()){//ֻҪ����·�б��д���һ�������㹻���ɣ���֮�����û�У����ų�
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
				System.out.println("1.��IP��·�ɳɹ�����·��");
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
				if(((link.getCapacity()-link.getSumFlow())<currentNodePair.getTrafficdemand())||(link.getNature()==1)){
					tempDelLinkList.add(link);
					
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
			
			
			LinearRoute newWorkRouteIP=new LinearRoute("IPWorkingRoute",0,"");
			RouteSearching rsIPWork=new RouteSearching();
			rsIPWork.Dijkstras(srcNode, destNode, ipLayer, newWorkRouteIP, null);

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

	
}
