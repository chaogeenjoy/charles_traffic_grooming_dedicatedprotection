package network;

import java.util.ArrayList;

import general.CommonObject;
import general.Constant;
import general.Slot;

public class Link extends CommonObject {

	private Layer associatedLayer = null; // the layer that the link belongs to
	private Node nodeA = null; // node A
	private Node nodeB = null; // node B
	private double length = 0; // physical distance of the link
	private double cost = 0;// the cost of the link
	private int status = Constant.UNVISITED;// the visited status
	private int[] waveStatus = new int[Constant.WAVE_PRE_FIBER]; // 链路的波长占用状态
	private double capacity = 0;// 建立的光通道所含有的总共的流量
	private double sumFlow = 0;// 经过链路的网络流量
	private double ipRemainFlow = 0;
	private int nature;// 所属工作或者保护的属性: 0为工作 1为保护
	
	private int maxSlot;// 最大使用slot
	private int waveNum = 0; // 链路所承载的波长数，用于计算该链路所需的光纤数
	private ArrayList<Slot> slotsArray;
	private ArrayList<Integer> slotsIndexInOneSW;
	private ArrayList<VirtualLink> virtualLinkList=new ArrayList<VirtualLink>();
	private VirtualLink tempVirtualLink=null;
//	private ArrayList
	private ArrayList<Link> physicalLink;// 在物理层所经过的link

	public Link(String name, int index, String comments, Layer associatedLayer, Node nodeA, Node nodeB, double length,
			double cost) {
		super(name, index, comments);
		this.associatedLayer = associatedLayer;
		this.nodeA = nodeA;
		this.nodeB = nodeB;
		this.length = length;
		this.cost = cost;
		status = Constant.UNVISITED;
		this.slotsIndexInOneSW = new ArrayList<Integer>();

		this.slotsArray = new ArrayList<Slot>();
		// System.out.println("size="+slotsarray.size());
		for (int i = 0; i < Constant.F; i++) {
			Slot slot = new Slot();
			this.slotsArray.add(slot);
		}
		for (int k = 0; k < Constant.WAVE_PRE_FIBER; k++)
			this.waveStatus[k] = Constant.FREE;
	}

	public Layer getAssociatedLayer() {
		return associatedLayer;
	}

	public void setAssociatedLayer(Layer associatedLayer) {
		this.associatedLayer = associatedLayer;
	}

	public Node getNodeA() {
		return nodeA;
	}

	public void setNodeA(Node nodeA) {
		this.nodeA = nodeA;
	}

	public Node getNodeB() {
		return nodeB;
	}

	public void setNodeB(Node nodeB) {
		this.nodeB = nodeB;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int[] getWaveStatus() {
		return waveStatus;
	}

	public void setWaveStatus(int[] waveStatus) {
		this.waveStatus = waveStatus;
	}

	public double getCapacity() {
		return capacity;
	}

	public void setCapacity(double capacity) {
		this.capacity = capacity;
	}

	public double getSumFlow() {
		return sumFlow;
	}

	public void setSumFlow(double sumFlow) {
		this.sumFlow = sumFlow;
	}

	public double getIpRemainFlow() {
		return ipRemainFlow;
	}

	public void setIpRemainFlow(double ipRemainFlow) {
		this.ipRemainFlow = ipRemainFlow;
	}

	public int getNature() {
		return nature;
	}

	public void setNature(int nature) {
		this.nature = nature;
	}

	public int getMaxSlot() {
		return maxSlot;
	}

	public void setMaxSlot(int maxSlot) {
		this.maxSlot = maxSlot;
	}

	public int getWaveNum() {
		return waveNum;
	}

	public void setWaveNum(int waveNum) {
		this.waveNum = waveNum;
	}

	public ArrayList<Slot> getSlotsArray() {
		return slotsArray;
	}

	public void setSlotsArray(ArrayList<Slot> slotsArray) {
		this.slotsArray = slotsArray;
	}

	public ArrayList<Integer> getSlotsIndexInOneSW() {
		return slotsIndexInOneSW;
	}

	public void setSlotsIndexInOneSW(ArrayList<Integer> slotsIndexInOneSW) {
		this.slotsIndexInOneSW = slotsIndexInOneSW;
	}
	

	public ArrayList<VirtualLink> getVirtualLinkList() {
		return virtualLinkList;
	}

	public void setVirtualLinkList(ArrayList<VirtualLink> virtualLinkList) {
		this.virtualLinkList = virtualLinkList;
	}
	
	

	public VirtualLink getTempVirtualLink() {
		return tempVirtualLink;
	}

	public void setTempVirtualLink(VirtualLink tempVirtualLink) {
		this.tempVirtualLink = tempVirtualLink;
	}

	public ArrayList<Link> getPhysicalLink() {
		return physicalLink;
	}

	
	public void setPhysicalLink(ArrayList<Link> physicalLink) {
		this.physicalLink = physicalLink;
	}

}
