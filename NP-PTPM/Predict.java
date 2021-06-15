package cn.hdu.edu.nextPOI;

import cn.hdu.edu.pojo.Geo;
import cn.hdu.edu.tools.JDBC;
import cn.hdu.edu.tools.StaticMethod;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.*;

/**
 * Created by wiyee on 2019/3/11.
 *
 */
public class Predict {

    public Map<Integer,Map<Integer,Double>> transMatrixMap = new HashMap<>();// 全局转移概率矩阵
    public Map<Integer,Map<Integer,Map<Integer,Double>>> persTransMatrixMap = new HashMap<>();// 个性化转移概率矩阵
    public Map<Integer,Map<Integer,Double>> weekendMatrixMap = new LinkedHashMap<>();
    public Map<Integer,Map<Integer,Double>> workdayMatrixMap = new LinkedHashMap<>();
    public int K = 1;
    public double alpha = 0;
    public int TT = 0;// 预测准确数
    public int dataSize = 0;

    public Map<Integer,Geo> poiLocMap = new HashMap<>(); // poi位置
    public Map<Integer,Map<Integer,Double>> countingMatrixMap = new HashMap<>(); // 由PPSM获得的评分矩阵

    /**
     * 加载兴趣点位置数据
     */
    void loadPoiLocation(){
        String sql = "SELECT * FROM poi_location_SFO";
        JDBC jdbc = new JDBC();
        try {
            ResultSet rs = jdbc.executeQuery(sql);
            while (rs.next()){
                int poiId = rs.getInt(1);
                double lat = rs.getDouble(2);
                double lon = rs.getDouble(3);
                poiLocMap.put(poiId,new Geo(lat,lon));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 加载评分矩阵
     */
    void loadCountingMatrix(String inputPath) throws IOException {
        LoadMatrix loadMatrix = new LoadMatrix();
        loadMatrix.loadFile(inputPath);
        countingMatrixMap = loadMatrix.matrixMap;
    }

    /**
     * 加载轨迹数据
     */
    void loadTrajectoryData(String inputPath) throws Exception {
        Set<Integer> trueSet = new HashSet<>();
        File file = new File(inputPath);
        BufferedReader bf = new BufferedReader(new FileReader(file));
        String line = bf.readLine();
        int lastUser = Integer.parseInt(line.split("\t")[0]);
        String[] trajaectory1 = line.split("\t")[1].split("->");
        for (int i = 0; i < trajaectory1.length; i ++){
            trueSet.add(Integer.parseInt(trajaectory1[i]));
        }
        while (line!=null){
            // 对测试集中每条数据进行预测
            String[] lineArray = line.split("\t");
            int userId = Integer.parseInt(lineArray[0]);
            String[] trajaectory = lineArray[1].split("->");
            String date = lineArray[2];

            for (int i = 1; i < trajaectory.length; i ++){
                int currentPOI = Integer.parseInt(trajaectory[i-1]);
                int nextPOI = Integer.parseInt(trajaectory[i]);
                // 预测方法
                //PPSM
                if (predict(userId,currentPOI,nextPOI,K)){
//                if (predictBySplitDay(userId,currentPOI,nextPOI,K,date)){
                    TT ++;
                }
                dataSize += K;
            }
            line = bf.readLine();
        }
//        System.out.println(dataSize);
        bf.close();

    }

    /**
     * PPSM推荐下一兴趣点
     */
    private boolean predictByUserRating(int userId, int currentPoi, int nextPoi, int K){
        Set<Integer> recSet = new HashSet<>();
        // （全局转移概率 * (1-phi) + 个性化用户转移概率 * (phi) ）* 评分预测
        Map<Integer,Double> ratingMap= countingMatrixMap.get(userId);
        if (!(poiLocMap.containsKey(currentPoi) && poiLocMap.containsKey(nextPoi)))
            return false;
        Geo locGeo = poiLocMap.get(currentPoi);
        Geo dstGeo = poiLocMap.get(nextPoi);
        Double distance = StaticMethod.getDistBetweenCoordinate(locGeo.getLon(),locGeo.getLat(),dstGeo.getLon(),dstGeo.getLat());
        Map<Integer,Double> newUserMatrixMap = new LinkedHashMap<>();
        for (int poiId : ratingMap.keySet()){
            if (poiId == currentPoi)
                continue;
            double score = ratingMap.get(poiId);
            newUserMatrixMap.put(poiId,score / (distance * 1.0));
        }
        // userMatrix 根据value排序
        newUserMatrixMap= (Map<Integer, Double>) StaticMethod.sortMapByValue(newUserMatrixMap);
        // 无法计算转移概率的（例如新用户），单纯靠评分预测
        // 靠评分推荐
        int index = 0;
        for (int poiId: newUserMatrixMap.keySet()){
            recSet.add(poiId);
            index ++;
            if (index == K)
                break;
        }
        // 推荐结果统计
        if (recSet.contains(nextPoi))
            return true;
        else
            return false;
    }

    /**
     * MC推荐下一兴趣点
     */
    private boolean predictByMC(int userId, int currentPoi, int nextPoi, int K){
        Set<Integer> recSet = new HashSet<>();
        if (!transMatrixMap.containsKey(currentPoi)){
            return false;
        }
        Map<Integer,Double> allMatrixMap = transMatrixMap.get(currentPoi);
        allMatrixMap = (Map<Integer, Double>) StaticMethod.sortMapByValue(allMatrixMap);
        Map<Integer,Double> recMapByAllTM = new HashMap<>();
        for (int poiId: allMatrixMap.keySet()) {
            recMapByAllTM.put(poiId, allMatrixMap.get(poiId));
        }
        // 排序取top-k
        recMapByAllTM = (Map<Integer, Double>) StaticMethod.sortMapByValue(recMapByAllTM);
        int indexK = 0;
        for (int poiId:recMapByAllTM.keySet()){
            recSet.add(poiId);
            indexK ++;
            if (indexK == K)
                break;
        }
        // 推荐结果统计
        if (recSet.contains(nextPoi))
            return true;
        else
            return false;
    }

    /**
     *

     */
    public boolean predictBySplitDay(int userId, int currentPoi, int nextPoi, int K, String date) throws Exception {
        int day = StaticMethod.dayForWeek(date);
        Set<Integer> recSet = new HashSet<>();
        if (day == 6 || day == 7){
            if (!weekendMatrixMap.containsKey(currentPoi)){
                return false;
            }
            Map<Integer,Double> allMatrixMap = weekendMatrixMap.get(currentPoi);
            allMatrixMap = (Map<Integer, Double>) StaticMethod.sortMapByValue(allMatrixMap);
            Map<Integer,Double> recMapByAllTM = new HashMap<>();
            for (int poiId: allMatrixMap.keySet()) {
                recMapByAllTM.put(poiId, allMatrixMap.get(poiId));
            }
            // 排序取top-k
            recMapByAllTM = (Map<Integer, Double>) StaticMethod.sortMapByValue(recMapByAllTM);
            int indexK = 0;
            for (int poiId:recMapByAllTM.keySet()){
                recSet.add(poiId);
                indexK ++;
                if (indexK == K)
                    break;
            }
        } else {
            if (!workdayMatrixMap.containsKey(currentPoi)){
                return false;
            }
            Map<Integer,Double> allMatrixMap = workdayMatrixMap.get(currentPoi);
            allMatrixMap = (Map<Integer, Double>) StaticMethod.sortMapByValue(allMatrixMap);
            Map<Integer,Double> recMapByAllTM = new HashMap<>();
            for (int poiId: allMatrixMap.keySet()) {
                recMapByAllTM.put(poiId, allMatrixMap.get(poiId));
            }
            // 排序取top-k
            recMapByAllTM = (Map<Integer, Double>) StaticMethod.sortMapByValue(recMapByAllTM);
            int indexK = 0;
            for (int poiId:recMapByAllTM.keySet()){
                recSet.add(poiId);
                indexK ++;
                if (indexK == K)
                    break;
            }
        }
        // 推荐结果统计
        if (recSet.contains(nextPoi))
            return true;
        else
            return false;
    }

    private boolean predict(int userId, int currentPoi, int nextPoi, int K){
        Set<Integer> recSet = new HashSet<>();
        // （全局转移概率 + 个性化用户转移概率 ）* 评分预测
        Map<Integer,Double> ratingMap= countingMatrixMap.get(userId);
        if (!(poiLocMap.containsKey(currentPoi) && poiLocMap.containsKey(nextPoi)))
            return false;
        Geo locGeo = poiLocMap.get(currentPoi);
        Geo dstGeo = poiLocMap.get(nextPoi);
        Double distance = StaticMethod.getDistBetweenCoordinate(locGeo.getLon(),locGeo.getLat(),dstGeo.getLon(),dstGeo.getLat());
        Map<Integer,Double> newUserMatrixMap = new LinkedHashMap<>();
        for (int poiId : ratingMap.keySet()){
            if (poiId == currentPoi)
                continue;
            double score = ratingMap.get(poiId);
            newUserMatrixMap.put(poiId,score / (distance * 1.0));
        }
        // userMatrix 根据value排序
        newUserMatrixMap= (Map<Integer, Double>) StaticMethod.sortMapByValue(newUserMatrixMap);
        // 全局都没有的，单纯靠评分预测
        if (!transMatrixMap.containsKey(currentPoi)){
            // 靠评分推荐
            int index = 0;
            for (int poiId: newUserMatrixMap.keySet()){
                recSet.add(poiId);
                index ++;
                if (index == K)
                    break;
            }
        } else if (persTransMatrixMap.containsKey(userId)){
            if (persTransMatrixMap.get(userId).containsKey(currentPoi)){
                // userMatrix 根据value排序
                Map<Integer,Double> userMatrixMap = persTransMatrixMap.get(userId).get(currentPoi);
                userMatrixMap = (Map<Integer, Double>) StaticMethod.sortMapByValue(userMatrixMap);
                Map<Integer,Double> allMatrixMap = transMatrixMap.get(currentPoi);
                allMatrixMap = (Map<Integer, Double>) StaticMethod.sortMapByValue(allMatrixMap);

                // 个性化推荐
                Map<Integer,Double> recMapByUserTM = new HashMap<>();
                Map<Integer,Double> recMapByAllTM = new HashMap<>();

                for (int poiId: userMatrixMap.keySet()){
                    recMapByUserTM.put(poiId,userMatrixMap.get(poiId) * alpha * ratingMap.get(nextPoi));

                }
                //全局推荐
                for (int poiId: allMatrixMap.keySet()){

                    recMapByAllTM.put(poiId,allMatrixMap.get(poiId) * (1 - alpha) * ratingMap.get(nextPoi));
                }
                // 合并
                for (int poiId: allMatrixMap.keySet()){
                    if (userMatrixMap.containsKey(poiId)){
                        double score = userMatrixMap.get(poiId) + allMatrixMap.get(poiId);
                        userMatrixMap.put(poiId,score);
                    } else {
                        userMatrixMap.put(poiId,allMatrixMap.get(poiId));
                    }
                }
                // 排序取top-k
                userMatrixMap = (Map<Integer, Double>) StaticMethod.sortMapByValue(userMatrixMap);
                int indexK = 0;
                for (int poiId:userMatrixMap.keySet()){
                    recSet.add(poiId);
                    indexK ++;
                    if (indexK == K)
                        break;
                }
            }
        } else { // 有全局 无个人
            Map<Integer,Double> allMatrixMap = transMatrixMap.get(currentPoi);
            allMatrixMap = (Map<Integer, Double>) StaticMethod.sortMapByValue(allMatrixMap);
            Map<Integer,Double> recMapByAllTM = new HashMap<>();
            //全局推荐

            for (int poiId: allMatrixMap.keySet()) {
                recMapByAllTM.put(poiId, allMatrixMap.get(poiId) * ratingMap.get(nextPoi));
            }
            // 排序取top-k
            recMapByAllTM = (Map<Integer, Double>) StaticMethod.sortMapByValue(recMapByAllTM);
            int indexK = 0;
            for (int poiId:recMapByAllTM.keySet()){
                recSet.add(poiId);
                indexK ++;
                if (indexK == K)
                    break;
            }
        }
        // 推荐结果统计
        if (recSet.contains(nextPoi))
            return true;
        else
            return false;
    }

    public static void main(String[] args) throws Exception {
        // 测试集数据
        String testPath = "F:\\rec\\output\\SFO_test_1552546882522.txt";
        String trainPath = "F:\\rec\\output\\SFO_train_1552457753329.txt";
        TransMatrix transMatrix = new TransMatrix();
        transMatrix.transMatrix(trainPath);
        transMatrix.personalTransMatrix(trainPath);
        Predict pre = new Predict();
        // 加载位置数据
        pre.loadPoiLocation();
        // 加载评分矩阵
        String countingMatrixPath = "F:\\rec\\output\\result\\SFO_OUT1552219792592.txt";
        pre.loadCountingMatrix(countingMatrixPath);
        pre.transMatrixMap = transMatrix.transMatrixMap;
        pre.persTransMatrixMap = transMatrix.persTransMatrixMap;
//        System.out.println(pre.persTransMatrixMap);
        pre.loadTrajectoryData(testPath);
        System.out.println("准确个：" + pre.TT);
        System.out.println("数据集:" + pre.dataSize / pre.K);
        System.out.println("准确率：" + pre.TT/(pre.dataSize / pre.K * 1.0));
    }
}