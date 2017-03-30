package demand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import general.Constant;
import network.Layer;
import network.Link;
import network.NodePair;
import subgraph.LinearRoute;

public class Request {
	private NodePair nodepair;
	private double bandwidth;
	private int slots;
	private ArrayList<Link> route;
	private int protection_index;

	public Request(NodePair nodepair, int slots) {
		this.setNodepair(nodepair);
		nodepair.setSlotsnum(slots);
		this.setRoute(nodepair.getLinearroutelist().get(0).getLinklist());
		this.setSlots(slots);
	}
	



	public Request() {
		super();
		// TODO Auto-generated constructor stub
	}




	public static ArrayList<NodePair> sortNodePair(Layer iplayer) {
		ArrayList<NodePair> nodePairList = new ArrayList<NodePair>();
		for (int i = 0; i < iplayer.getNodepair_num(); i++) {
			HashMap<String, NodePair> map = iplayer.getNodepairlist();

			Iterator<String> itr = map.keySet().iterator();
			NodePair tempNodePair = new NodePair("", 0, "", null, null, null);
			tempNodePair.setTrafficdemand(10000000);
			while (itr.hasNext()) {
				NodePair nodePair = (NodePair) (map.get(itr.next()));
				if (nodePair.getArrange_status() == Constant.UNORDER) {
					if (nodePair.getTrafficdemand() < tempNodePair.getTrafficdemand()) {
						tempNodePair = nodePair;
					}
				}
			}
			nodePairList.add(0, tempNodePair);
			HashMap<String, NodePair> map1 = iplayer.getNodepairlist();
			Iterator<String> itr1 = map1.keySet().iterator();
			while (itr1.hasNext()) {
				NodePair nodePair1 = (NodePair) (map1.get(itr1.next()));
				if (nodePair1 == tempNodePair) {
					nodePair1.setArrange_status(Constant.ORDERED);
				}
			}

		}
		return nodePairList;
	}
	public static ArrayList<Integer> spectrumAllocationOneRoute_ReqList(LinearRoute route) {
		ArrayList<Link> routelink = route.getLinklist();//·���д洢����·�б�
		/**
		 * ��ǿ�ͱ�����
		 */
		if (route.getSlotsnum() == 0) {
			System.out.println("no slots");
		}else{
			for (Link link : routelink) {
				link.getSlotsIndexInOneSW().clear();			//
				for (int r = 0; r <= link.getSlotsArray().size() - route.getSlotsnum(); r++) {
					int s = 1;
					for(int k = r; k < route.getSlotsnum() + r; k++) {
						if (link.getSlotsArray().get(k).getStatus()==1) {//ֻҪһ��SW����һ��slot��ռ�ã�s=0����ζ�Ÿ�SW������
							s = 0;
							break;
						}
					}
					if (s != 0) //s!=0��ζ��SW�е�F��slot���ǿ��õ�
						link.getSlotsIndexInOneSW().add(r);//�����õ�SW�ĳ�ʼ������ӵ�Slotsindex()������ȥ					
				}
			}
		}
		/**
		 * ��������
		 */

		Link link = routelink.get(0);
		ArrayList<Integer> sameindex = new ArrayList<Integer>();
		sameindex.clear();
		/*
		 * �������ѭ������·����·1�е����п��õ�SW�ĳ�ʼ����
		 *     ����������·���е�������·���в��ң��ж��Ƿ������������
		 *     
		 *     Ŀ����Ϊ�˵õ�������·�������������������õ�SW��
		 */
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
	public ArrayList<Integer> spectrumAllocationOneRoute(LinearRoute route) {
		ArrayList<Link> routelink = route.getLinklist();
		for (Link link : routelink) {
			if (route.getSlotsnum() == 0) {
				System.out.println("noslots");
				break;
			}
			link.getSlotsIndexInOneSW().clear();

			for (int i = 0; i <= link.getSlotsArray().size() - route.getSlotsnum(); i++) {
				if (link.getSlotsArray().get(i) == null) {
					int s = 1;
					for (int k = i; k < route.getSlotsnum() + i; k++) {

						if (link.getSlotsArray().get(k) != null) {
							s = 0;
							break;
						}

					}
					if (s != 0) {
						link.getSlotsIndexInOneSW().add(i);

					}
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

	public ArrayList<Integer> spectrumAllocationOneRoute_index(LinearRoute route, int original_index) {
		ArrayList<Link> routelink = route.getLinklist();
		for (Link link : routelink) {
			if (route.getSlotsnum() == 0) {
				System.out.println("noslots");
				break;
			}
			link.getSlotsIndexInOneSW().clear();

			for (int i = 0; i <= original_index; i++) {
				if (link.getSlotsArray().get(i) == null) {
					int s = 1;
					for (int k = i; k < route.getSlotsnum() + i; k++) {

						if (link.getSlotsArray().get(k) != null) {
							s = 0;
							break;
						}

					}
					if (s != 0) {
						link.getSlotsIndexInOneSW().add(i);

					}
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

	public void setNodepair(NodePair nodepair) {
		this.nodepair = nodepair;
	}

	public NodePair getNodepair() {
		return nodepair;
	}

	public void setBandwidth(double bandwith) {
		this.bandwidth = bandwith;
	}

	public double getBandwidth() {
		return bandwidth;
	}

	public void setRoute(ArrayList<Link> route) {
		this.route = route;
	}

	public ArrayList<Link> getRoute() {
		return route;
	}

	public void setSlots(int slots) {
		this.slots = slots;
	}

	public int getSlots() {
		return slots;
	}

	public void setprotection_index(int firstindex) {
		this.protection_index = firstindex;
	}

	public int getprotection_index() {
		return protection_index;
	}

}
