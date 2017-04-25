package general;

import java.util.ArrayList;
import java.util.Iterator;

import network.Link;
import network.VirtualLink;

public class Slot {
	private ArrayList<VirtualLink> virtualLinkList;//存储占用当前slot的工作路径所占用的物理链路的列表
	private int status = 0; // 0为初始状态,可以用过occupiedsize是否为0先判断是否已经被占用，在通过status判断被工作路径或者保护路径占用；
							// 1为工作路径占用
							// 2为保护路径占用

	public Slot() {
		super();
		this.status = 0;
		this.virtualLinkList=new ArrayList<VirtualLink>();
	}
	//用于判断工作路径是否有物理链路重叠，有重叠返回true,否则返回false
	public boolean workJoint(ArrayList<VirtualLink> workVList){
		boolean flag=false;
		
		
		Iterator<VirtualLink> itr=virtualLinkList.iterator();
		Here:while(itr.hasNext()){
			VirtualLink vtLink=(VirtualLink) itr.next();
			Iterator<Link> itr1=vtLink.getPhyLinkList().iterator();
			while(itr1.hasNext()){
				Link link=(Link) itr1.next();
				for(int i=0;i<workVList.size();i++){
					if(workVList.get(i).getPhyLinkList().contains(link)){
						flag=true;
						break Here;
					}
				}
			}
		}
		
		return flag;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public ArrayList<VirtualLink> getVirtualLinkList() {
		return virtualLinkList;
	}

	public void setVirtualLinkList(ArrayList<VirtualLink> virtualLinkList) {
		this.virtualLinkList = virtualLinkList;
	}
	
	



}
