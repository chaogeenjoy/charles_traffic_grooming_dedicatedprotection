package general;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import graphalgorithms.RouteSearching;
import graphalgorithms.SearchConstraint;
import network.Layer;
import network.Link;
import network.Node;
import subgraph.LinearRoute;

public class SWP {
	/*
	 * �������������cost�߽���·�ɺ�Ƶ�׷��� 
	 * ��� ������
	 *        A�����
	 *        B������Ҫ��slot��Ŀ 
	 *        C�����Դ�ڵ��Ŀ�Ľڵ�
	 *        D���洢·�ɵ�LinearRoute���ʵ�� 
	 *        E��·��Ѱ�ҵ�constraint���ʵ�� 
	 *        F��������������
	 *���ڲ�����
	 *        A�����ҵ���·�ɴ洢��LinearRoute
	 * 
	 */
	public LinearRoute minCost_RSABasedOnSWP_LengthLimited(Layer optLayer, int FSNum, Node srcNode, Node destNode,
			SearchConstraint constraint,int nature,int maxLength) {
		LinearRoute newRoute=new LinearRoute("newRoute", 0,"");
		double cost=Double.MAX_VALUE;
		 LinearRoute tempRoute=new LinearRoute("tempRoute", 1,"");
		 int startIndex=0;
		 if(FSNum==0){
			 System.out.println("*************************************************" + "\n����Ƶ��Ҫ��FS����Ϊ0\n"
						+ "********************************************************");
		 }else{
			 for(int i=0;i<Constant.F-FSNum+1;i++){
				 routeOnOneSWP(optLayer, i, i+FSNum, srcNode, destNode, tempRoute, constraint);
				if((tempRoute.getLinklist().size()!=0)&&(tempRoute.getLength()<=maxLength)){
					if(tempRoute.getCost()<cost){
						cost=tempRoute.getCost();
						newRoute=tempRoute;
						startIndex=i;
					}
					 
				}
				
			 }
		 }
		// ����·����·��Ƶ��ʹ�����
		 if (newRoute.getLinklist().size() != 0) {
				for (int i = startIndex; i < startIndex + FSNum; i++) {
					for (Link link : newRoute.getLinklist()) {
						link.getSlotsArray().get(i).setStatus(nature);
					}
				}
				
		 }
		 return newRoute;
		 	
	}
	/*
	 * �������������cost�߽���·�ɺ�Ƶ�׷��� 
	 * ��� ������
	 *        A�����
	 *        B������Ҫ��slot��Ŀ 
	 *        C�����Դ�ڵ��Ŀ�Ľڵ�
	 *        D���洢·�ɵ�LinearRoute���ʵ�� 
	 *        E��·��Ѱ�ҵ�constraint���ʵ�� 
	 *���ڲ�����
	 *        A�����ҵ���·�ɴ洢��LinearRoute
	 * 
	 */
	public LinearRoute minCost_RSABasedOnSWP(Layer optLayer, int FSNum, Node srcNode, Node destNode,
			SearchConstraint constraint,int nature) {
		LinearRoute newRoute=new LinearRoute("newRoute", 0,"");
		double cost=Double.MAX_VALUE;
		 LinearRoute tempRoute=new LinearRoute("tempRoute", 1,"");
		 int startIndex=0;
		 if(FSNum==0){
			 System.out.println("*************************************************" + "\n����Ƶ��Ҫ��FS����Ϊ0\n"
						+ "********************************************************");
		 }else{
			 for(int i=0;i<Constant.F-FSNum+1;i++){
				 routeOnOneSWP(optLayer, i, i+FSNum, srcNode, destNode, tempRoute, constraint);
				if(tempRoute.getLinklist().size()!=0){
					if(tempRoute.getCost()<cost){
						cost=tempRoute.getCost();
						newRoute=tempRoute;
						startIndex=i;
					}
					 
				}
				
			 }
		 }
		// ����·����·��Ƶ��ʹ�����
		 if (newRoute.getLinklist().size() != 0) {
				for (int i = startIndex; i < startIndex + FSNum; i++) {
					for (Link link : newRoute.getLinklist()) {
						link.getSlotsArray().get(i).setStatus(nature);
					}
				}
				
		 }
		 return newRoute;
		 	
	}
	/*
	 * ������Ѱ��SWP,��SWP��·�����·����ֻҪ�����ɹ��ͽ��� 
	 * ��� ������ 
	 *          A����� 
	 *          B������Ҫ��slot��Ŀ 
	 *          C�����Դ�ڵ��Ŀ�Ľڵ�
	 *          D���洢·�ɵ�LinearRoute���ʵ�� 
	 *          E��·��Ѱ�ҵ�constraint���ʵ�� 
	 *          F:����Ѱ����·�����ԣ�
	 *���ڲ�����
	 *         ���ҵ���·�ɴ洢��LinearRoute���ʵ���� 
	 * 
	 */
	public LinearRoute firstFit_RSABasedOnSWP_LengthLimited(Layer optLayer, int FSNum, Node srcNode, Node destNode, 
			SearchConstraint constraint, int nature,int maxLength) {
		/*
		 * ɨ��ÿһ��SWP����·�ɲ�ѯ������һ���ҵ���ͷ���Ƶ�ף�����Ƶ����Դ��Ϣ
		 */
		LinearRoute newRoute=new LinearRoute("",0,"");
		LinearRoute tempRoute=new LinearRoute("",1,"");
		int startIndex = 0;
		if (FSNum == 0) {
			System.out.println("*************************************************" + "\n����Ƶ��Ҫ��FS����Ϊ0\n"
					+ "********************************************************");
		} else {
			for (int i = 0; i < Constant.F - FSNum + 1; i++) {

				routeOnOneSWP(optLayer, i, i + FSNum, srcNode, destNode,tempRoute, constraint);
				if ((tempRoute.getLinklist().size() != 0)&&(tempRoute.getLength()<maxLength)) {
					startIndex = i;
					newRoute=tempRoute;
					break;
				}
			}
		}
		// ����·����·��Ƶ��ʹ�����
		if (newRoute.getLinklist().size() != 0) {
			for (int i = startIndex; i < startIndex + FSNum; i++) {
				for (Link link : newRoute.getLinklist()) {
					link.getSlotsArray().get(i).setStatus(nature);
				}
			}
		}
		return newRoute;

	}
	/*
	 * ������Ѱ��SWP,��SWP��·�����·����ֻҪ�����ɹ��ͽ��� 
	 * ��� ������ 
	 *          A����� 
	 *          B������Ҫ��slot��Ŀ 
	 *          C�����Դ�ڵ��Ŀ�Ľڵ�
	 *          D���洢·�ɵ�LinearRoute���ʵ�� 
	 *          E��·��Ѱ�ҵ�constraint���ʵ�� 
	 *          F:����Ѱ����·�����ԣ�
	 *���ڲ�����
	 *         ���ҵ���·�ɴ洢��LinearRoute���ʵ���� 
	 * 
	 */
	public void firstFit_RSABasedOnSWP(Layer optLayer, int FSNum, Node srcNode, Node destNode, LinearRoute newRoute,
			SearchConstraint constraint, int nature) {
		/*
		 * ɨ��ÿһ��SWP����·�ɲ�ѯ������һ���ҵ���ͷ���Ƶ�ף�����Ƶ����Դ��Ϣ
		 */
		int startIndex = 0;
		if (FSNum == 0) {
			System.out.println("*************************************************" + "\n����Ƶ��Ҫ��FS����Ϊ0\n"
					+ "********************************************************");
		} else {
			for (int i = 0; i < Constant.F - FSNum + 1; i++) {

				routeOnOneSWP(optLayer, i, i + FSNum, srcNode, destNode, newRoute, constraint);
				if (newRoute.getLinklist().size() != 0) {
					startIndex = i;
					break;
				}
			}
		}
		// ����·����·��Ƶ��ʹ�����
		if (newRoute.getLinklist().size() != 0) {
			for (int i = startIndex; i < startIndex + FSNum; i++) {
				for (Link link : newRoute.getLinklist()) {
					link.getSlotsArray().get(i).setStatus(nature);
				}
			}
		}

	}

	/*
	 * ��������һ��SWPƽ���ϵ�·�� 
	 * ��ڲ����� 
	 *        A����� 
	 *        B����ʼ�ͽ�β��slot���� 
	 *        C�����Դ�ڵ��Ŀ�Ľڵ� 
	 *        D���洢·�ɵ�LinearRoute���ʵ�� 
	 *        E��·��Ѱ�ҵ�constraint���ʵ�� 
	 *���ڲ�����
	 *        ���ҵ���·�ɴ洢��LinearRoute���ʵ����
	 */

	public static void routeOnOneSWP(Layer optLayer, int startIndex, int endIndex, Node srcNode, Node destNode,
			LinearRoute newRoute, SearchConstraint constraint) {
		/*
		 * �Ȳ��Ҹò��е���·�б�������·�б��е�slotarray�еĴ�start��end��Щslot��ռ�õ���·��ӵ�
		 * ����������ȥ����·�ɣ�����·�ɵ�ʱ��ͻ�������Щ��·�� �����Ժ�����˸���ÿ����·��slotռ�����
		 */
		ArrayList<Link> tempDelList = new ArrayList<Link>();
		HashMap<String, Link> linkList = optLayer.getLinklist();
		Iterator<String> itr = linkList.keySet().iterator();
		while (itr.hasNext()) {
			Link link = (Link) linkList.get(itr.next());
			for (int i = startIndex; i < endIndex; i++) {
				if (link.getSlotsArray().get(i).getStatus() != 0) {
					// ����ռ�õ���·��ӵ�constraint�е���·�б���ȥ
					tempDelList.add(link);
					break;
				}
			}
		}
		for (Link link : tempDelList) {
			optLayer.removeLink(link.getName());
		}
		RouteSearching newRS = new RouteSearching();
		newRS.Dijkstras(srcNode, destNode, optLayer, newRoute, constraint);
		for (Link link : tempDelList) {
			optLayer.addLink(link);
		}

	}
}
