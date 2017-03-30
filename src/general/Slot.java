package general;

import java.util.ArrayList;

import demand.Request;

public class Slot {
	private ArrayList<Request> occupiedReqList;
	private int status = 0; // 0Ϊ��ʼ״̬,�����ù�occupiedsize�Ƿ�Ϊ0���ж��Ƿ��Ѿ���ռ�ã���ͨ��status�жϱ�����·�����߱���·��ռ�ã�
							// 1Ϊ����·��ռ��
							// 2Ϊ����·��ռ��

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
