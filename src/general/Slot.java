package general;

import java.util.ArrayList;

import demand.Request;

public class Slot {
	private ArrayList<Request> occupiedReqList;
	private int status = 0; // 0为初始状态,可以用过occupiedsize是否为0先判断是否已经被占用，在通过status判断被工作路径或者保护路径占用；
							// 1为工作路径占用
							// 2为保护路径占用

	public Slot() {
		super();
		this.status = 0;
		this.occupiedReqList = new ArrayList<Request>();
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public ArrayList<Request> getOccupiedReqList() {
		return occupiedReqList;
	}

	public void setOccupiedReqList(ArrayList<Request> occupiedReqList) {
		this.occupiedReqList = occupiedReqList;
	}

}
