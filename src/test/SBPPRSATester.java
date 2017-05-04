package test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import demand.Request;
import general.Constant;
import network.Layer;
import network.NodePair;

public class SBPPRSATester {

	public static void main(String[] args) {
		double[] trafficLoadList={1.2,1.3,1.6,1.9,2.0,2.5,2.9};
		for(int i=0;i<trafficLoadList.length;i++){
			double erlangLoad=trafficLoadList[i];
			System.out.println("**************************\n erlang load ="+erlangLoad+"\n"
					+ "***************************");
			
			Layer optLayer=new Layer("optical", 0, "", null);
			optLayer.readTopology("E:\\其他\\RPtopology\\NODE6.csv");
			optLayer.generateNodepairs();
			
			ArrayList<Request> requestList=new ArrayList<Request>();
			Random randTime=new Random(1);
			Random randRate=new Random(3);
			Iterator<String> itr=optLayer.getNodepairlist().keySet().iterator();
			while(itr.hasNext()){
				NodePair nodePair=(NodePair)(optLayer.getNodepairlist().get(itr.next()));
				double arriveTime=(-1/erlangLoad)*(Math.log(randTime.nextDouble()));
				int rate=randRate.nextInt(391)+10;//10到400之间
				Request request=new Request(nodePair, rate, arriveTime, arriveTime+Math.log(randTime.nextDouble()), Constant.ARRIVAL);	
			    insertRequest(requestList, request);				
			}
			
			
			int requestNum=0;
			int totalRate=0;
			int totalBlockRate=0;
			while((requestNum<=1000000)&&(requestList.size()!=0)){
				
			}
		}
		
	}
	
	
	
	
	
	
	
	
	
	public static void insertRequest(ArrayList<Request> requestList, Request request){
		if(requestList.size()==0){
			requestList.add(0, request);
		}else{
			double occurTime;
			if(request.getReqType()==Constant.ARRIVAL)
				occurTime=request.getArrivalTime();
			else
				occurTime=request.getDepartTime();
			boolean inserted=false;
			for(int i=0;i<requestList.size();i++){
				Request currentRequest=requestList.get(i);
				double compareTime;
				if(currentRequest.getReqType()==Constant.ARRIVAL)
					compareTime=currentRequest.getArrivalTime();
				else
					compareTime=currentRequest.getDepartTime();
				if(occurTime<compareTime){
					requestList.add(i, request);
					inserted=true;
					break;
				}
			}
			if(!inserted){
				requestList.add(request);
			}
		}
	}
}
