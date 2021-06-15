package cn.hdu.edu.nextPOI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * 构造转移概率矩阵
 */
public class TransMatrix {
    public Map<Integer,Map<Integer,Double>> transMatrixMap = new LinkedHashMap<>();
    public Map<Integer,Map<Integer,Map<Integer,Double>>> persTransMatrixMap = new LinkedHashMap<>();
    public Map<Integer,Map<Integer,Double>> weekendMatrixMap = new LinkedHashMap<>();
    public Map<Integer,Map<Integer,Double>> workdayMatrixMap = new LinkedHashMap<>();

    private Set<Integer> userSet = new HashSet<>();

    /**
     * 划分时间的转移概率矩阵
     */
    public void weekendTransMatrix(String weekendPath) throws IOException{
        Map<Integer,Map<Integer,Integer>> transCountMap = new HashMap<>();
        File weekend = new File(weekendPath);
        BufferedReader bf = new BufferedReader(new FileReader(weekend));
        String line = bf.readLine();
        while (line!=null){
            String[] trajaectory = line.split("\t")[1].split("->");

            for (int i = 1; i < trajaectory.length; i ++){
                int currentPOI = Integer.parseInt(trajaectory[i-1]);
                int nextPOI = Integer.parseInt(trajaectory[i]);
                if (transCountMap.containsKey(currentPOI)){
                    Map<Integer,Integer> tmpMap = transCountMap.get(currentPOI);
                    if (tmpMap.containsKey(nextPOI)){
                        int nextCount = tmpMap.get(nextPOI);
                        nextCount ++;
                        tmpMap.put(nextPOI,nextCount);
                    }else {
                        tmpMap.put(nextPOI,1);
                    }
                    transCountMap.put(currentPOI,tmpMap);
                }else {
                    Map<Integer,Integer> tmpMap = new HashMap<>();
                    tmpMap.put(nextPOI,1);
                    transCountMap.put(currentPOI,tmpMap);
                }
            }

            line = bf.readLine();
        }
//        System.out.println(transCountMap);
        for (int poiId : transCountMap.keySet()){
            int total = 0;
            Map<Integer,Integer> tmpMap = transCountMap.get(poiId);
            for (int nextId:tmpMap.keySet()){
                total += tmpMap.get(nextId);
            }
            Map<Integer,Double> singlePoiTransMap = new HashMap<>();
            for (int nextId:tmpMap.keySet()){
                singlePoiTransMap.put(nextId,tmpMap.get(nextId)/(total * 1.0));
            }
            weekendMatrixMap.put(poiId,singlePoiTransMap);
        }
    }

    /**
     * 划分时间的转移概率矩阵
     */
    public void workdayTransMatrix(String workdayPath) throws IOException{
        Map<Integer,Map<Integer,Integer>> transCountMap = new HashMap<>();
        File workday = new File(workdayPath);
        BufferedReader bf = new BufferedReader(new FileReader(workday));
        String line = bf.readLine();
        while (line!=null){
            String[] trajaectory = line.split("\t")[1].split("->");

            for (int i = 1; i < trajaectory.length; i ++){
                int currentPOI = Integer.parseInt(trajaectory[i-1]);
                int nextPOI = Integer.parseInt(trajaectory[i]);
                if (transCountMap.containsKey(currentPOI)){
                    Map<Integer,Integer> tmpMap = transCountMap.get(currentPOI);
                    if (tmpMap.containsKey(nextPOI)){
                        int nextCount = tmpMap.get(nextPOI);
                        nextCount ++;
                        tmpMap.put(nextPOI,nextCount);
                    }else {
                        tmpMap.put(nextPOI,1);
                    }
                    transCountMap.put(currentPOI,tmpMap);
                }else {
                    Map<Integer,Integer> tmpMap = new HashMap<>();
                    tmpMap.put(nextPOI,1);
                    transCountMap.put(currentPOI,tmpMap);
                }
            }

            line = bf.readLine();
        }
//        System.out.println(transCountMap);
        for (int poiId : transCountMap.keySet()){
            int total = 0;
            Map<Integer,Integer> tmpMap = transCountMap.get(poiId);
            for (int nextId:tmpMap.keySet()){
                total += tmpMap.get(nextId);
            }
            Map<Integer,Double> singlePoiTransMap = new HashMap<>();
            for (int nextId:tmpMap.keySet()){
                singlePoiTransMap.put(nextId,tmpMap.get(nextId)/(total * 1.0));
            }
            workdayMatrixMap.put(poiId,singlePoiTransMap);
        }
    }

    /**
     * 计算全局概率转移矩阵
     */
    public void transMatrix(String inputPath) throws IOException{
        Map<Integer,Map<Integer,Integer>> transCountMap = new HashMap<>();
        File file = new File(inputPath);
        BufferedReader bf = new BufferedReader(new FileReader(file));
        String line = bf.readLine();
        while (line!=null){
            String[] trajaectory = line.split("\t")[1].split("->");

            for (int i = 1; i < trajaectory.length; i ++){
                int currentPOI = Integer.parseInt(trajaectory[i-1]);
                int nextPOI = Integer.parseInt(trajaectory[i]);
                if (transCountMap.containsKey(currentPOI)){
                    Map<Integer,Integer> tmpMap = transCountMap.get(currentPOI);
                    if (tmpMap.containsKey(nextPOI)){
                        int nextCount = tmpMap.get(nextPOI);
                        nextCount ++;
                        tmpMap.put(nextPOI,nextCount);
                    }else {
                        tmpMap.put(nextPOI,1);
                    }
                    transCountMap.put(currentPOI,tmpMap);
                }else {
                    Map<Integer,Integer> tmpMap = new HashMap<>();
                    tmpMap.put(nextPOI,1);
                    transCountMap.put(currentPOI,tmpMap);
                }
            }

            line = bf.readLine();
        }
//        System.out.println(transCountMap);
        for (int poiId : transCountMap.keySet()){
            int total = 0;
            Map<Integer,Integer> tmpMap = transCountMap.get(poiId);
            for (int nextId:tmpMap.keySet()){
                total += tmpMap.get(nextId);
            }
            Map<Integer,Double> singlePoiTransMap = new HashMap<>();
            for (int nextId:tmpMap.keySet()){
                singlePoiTransMap.put(nextId,tmpMap.get(nextId)/(total * 1.0));
            }
            transMatrixMap.put(poiId,singlePoiTransMap);
        }
    }

    /**
     * 个性化概率转移矩阵
     */
    public void personalTransMatrix(String trajectoryPath) throws IOException {
        TrajectorySim ts = new TrajectorySim();
        ts.trajectoryCount(trajectoryPath);
        ts.calSim();
        userSet = ts.simMap.keySet();

        for (int userId:userSet){
            Map<Integer,Map<Integer,Integer>> transCountMap = new HashMap<>();
            Set<Integer> simUserSet = ts.simMap.get(userId);

            simUserSet.add(userId);
            Map<Integer,Map<String,Integer>> countMap = ts.traMap;
            for (int id: simUserSet){
                for (String str : countMap.get(id).keySet()){
                    int currentPOI = Integer.parseInt(str.split("-")[0]);
                    int nextPOI = Integer.parseInt(str.split("-")[1]);
                    int count = countMap.get(id).get(str);

                    if (transCountMap.containsKey(currentPOI)){
                        Map<Integer,Integer> tmpMap = transCountMap.get(currentPOI);
                        if (tmpMap.containsKey(nextPOI)){
                            int nextCount = tmpMap.get(nextPOI);
                            nextCount += count;
                            tmpMap.put(nextPOI,nextCount);
                        }else {
                            tmpMap.put(nextPOI,count);
                        }
                        transCountMap.put(currentPOI,tmpMap);
                    }else {
                        Map<Integer,Integer> tmpMap = new HashMap<>();
                        tmpMap.put(nextPOI,count);
                        transCountMap.put(currentPOI,tmpMap);
                    }
                }
            }
//            System.out.println(transCountMap);
            Map<Integer,Map<Integer,Double>> userTransMatrixMap = new HashMap<>();
            for (int id : transCountMap.keySet()){
                int total = 0;
                Map<Integer,Integer> tmpMap = transCountMap.get(id);
                for (int nextId:tmpMap.keySet()){
                    total += tmpMap.get(nextId);
                }
                Map<Integer,Double> singlePoiTransMap = new HashMap<>();
                for (int nextId:tmpMap.keySet()){
                    singlePoiTransMap.put(nextId,tmpMap.get(nextId)/(total * 1.0));
                }

                userTransMatrixMap.put(id,singlePoiTransMap);
            }
            persTransMatrixMap.put(userId,userTransMatrixMap);
        }
    }

    public static void main(String[] args) throws IOException {
        String trajectoryPath = "F:\\rec\\output\\SFO_train_1552457753329.txt";
        TransMatrix transMatrix = new TransMatrix();
        transMatrix.transMatrix(trajectoryPath);
//        System.out.println(transMatrix.transMatrixMap);
        transMatrix.personalTransMatrix(trajectoryPath);
//        System.out.println(transMatrix.persTransMatrixMap);
    }
}
