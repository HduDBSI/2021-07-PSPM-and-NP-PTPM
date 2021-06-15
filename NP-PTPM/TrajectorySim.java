package cn.hdu.edu.nextPOI;

import cn.hdu.edu.tools.StaticMethod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * 计算轨迹相似用户
 * 使用余弦相似度 net.librec.similarity.CosineSimilarity.Java;
 */
public class TrajectorySim {
    // 用户轨迹数
    public Map<Integer,Map<String,Integer>> traMap = new HashMap<>();
    // 相似用户 <>
    public Map<Integer,Set<Integer>> simMap = new HashMap<>();
    // top-K 相似的K
    private int K = 20;

    // 统计轨迹数
    public void trajectoryCount(String inputPath) throws IOException{

        File file = new File(inputPath);
        BufferedReader bf = new BufferedReader(new FileReader(file));
        String line = bf.readLine();

        while (line!=null) {
            String[] lineArray = line.split("\t");
            int userId = Integer.parseInt(lineArray[0]);
//            System.out.println(userId);
            String[] trajaectory = lineArray[1].split("->");
            for (int i = 1; i < trajaectory.length; i ++) {
                String currentPOI = trajaectory[i - 1];
                String nextPOI = trajaectory[i];
                String singleTra = currentPOI + "-" + nextPOI;
                if (traMap.containsKey(userId)){
                    Map<String,Integer> userTraMap = traMap.get(userId);
                    int count = userTraMap.containsKey(singleTra)?userTraMap.get(singleTra) + 1:1;
                    userTraMap.put(singleTra,count);
                    traMap.put(userId,userTraMap);
                }else {
                    Map<String,Integer> userTraMap = new HashMap<>();
                    userTraMap.put(singleTra,1);
                    traMap.put(userId,userTraMap);
                }
            }
            line = bf.readLine();
        }
    }

    public void calSim(){
        Map<Integer,Map<Integer,Double>> similiarityMap = new HashMap<>();

        for (int userId: traMap.keySet()){
            for (int simUserId : traMap.keySet()){
                if (userId == simUserId)
                    continue;
                double sim = getSimilarity(traMap.get(userId),traMap.get(simUserId));
                if (similiarityMap.containsKey(userId)){
                    Map<Integer,Double> tmpMap = similiarityMap.get(userId);
                    tmpMap.put(simUserId,sim);
                    similiarityMap.put(userId,tmpMap);
                } else {
                    Map<Integer,Double> tmpMap = new HashMap<>();
                    tmpMap.put(simUserId,sim);
                    similiarityMap.put(userId,tmpMap);
                }
                if (similiarityMap.containsKey(simUserId)){
                    Map<Integer,Double> tmpMap = similiarityMap.get(simUserId);
                    tmpMap.put(userId,sim);
                    similiarityMap.put(simUserId,tmpMap);
                } else {
                    Map<Integer,Double> tmpMap = new HashMap<>();
                    tmpMap.put(userId,sim);
                    similiarityMap.put(simUserId,tmpMap);
                }
            }
        }
        // top-k相似用户提取
//        System.out.println(similiarityMap);
        for (int userId:similiarityMap.keySet()){
            int index = 0;
            Map<Integer,Double> tmpMap = (Map<Integer, Double>) StaticMethod.sortMapByValue(similiarityMap.get(userId));
            Set<Integer> tmpSet = new HashSet<>();
            for (int desPOI:tmpMap.keySet()){
                tmpSet.add(desPOI);
                index ++;
                if (index == K)
                    break;
            }
            simMap.put(userId,tmpSet);
        }
    }

    /**
     * calculate the similarity between thisList and thatList.
     *
     * @param thisList  this list
     * @param thatList  that list
     * @return similarity
     */
    protected double getSimilarity(Map<String,Integer> thisList, Map<String,Integer> thatList) {

        if (thisList == null || thatList == null || thisList.size() < 1 || thatList.size() < 1) {
            return Double.NaN;
        }

        double innerProduct = 0.0, thisPower2 = 0.0, thatPower2 = 0.0;

        for (String str:thisList.keySet()){
            if (thatList.containsKey(str)){
                innerProduct += thisList.get(str).doubleValue() * thatList.get(str).doubleValue();
            }
            thisPower2 += thisList.get(str).doubleValue() * thisList.get(str).doubleValue();
        }
        for (String str:thatList.keySet()){
            thatPower2 += thatList.get(str).doubleValue() * thatList.get(str).doubleValue();
        }
        return innerProduct / Math.sqrt(thisPower2 * thatPower2);
    }



    public static void main(String[] args) throws IOException {
        // train_data
        String trajectoryPath = "F:\\rec\\output\\SFO_train_1552457753329.txt";
        TrajectorySim ts = new TrajectorySim();
        ts.trajectoryCount(trajectoryPath);
        System.out.println(ts.traMap);
        ts.calSim();
        System.out.println(ts.simMap);
    }
}
