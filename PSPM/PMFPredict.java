// 之前写的

//package cn.hdu.edu.predict;
//
//import cn.hdu.edu.nextPOI.LoadMatrix;
//import cn.hdu.edu.pojo.Geo;
//import cn.hdu.edu.tools.JDBC;
//import cn.hdu.edu.tools.StaticMethod;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.sql.ResultSet;
//import java.util.*;
//
///**
// * 老方法，矩阵分解之后除以距离调整。
// */
//public class PMFPredict {
//    public Map<Integer,Geo> poiLocMap = new HashMap<>();
//    public Map<Integer,Map<Integer,Double>> countingMatrixMap = new HashMap<>();
//    private int K = 5; //top-K
//    private int total = 0;
//    private int TT_sort = 0;
//    private int TT = 0;
//    private double alpha = 1;
//    /**
//     * 加载兴趣点位置数据
//     */
//    private void loadPoiLocation(){
//        String sql = "SELECT * FROM poi_location_SFO";
//        JDBC jdbc = new JDBC();
//        try {
//            ResultSet rs = jdbc.executeQuery(sql);
//            while (rs.next()){
//                int poiId = rs.getInt(1);
//                double lat = rs.getDouble(2);
//                double lon = rs.getDouble(3);
//                poiLocMap.put(poiId,new Geo(lat,lon));
//            }
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 加载评分矩阵
//     * @param inputPath
//     * @return
//     * @throws IOException
//     */
//    private void loadCountingMatrix(String inputPath) throws IOException {
//
//        LoadMatrix loadMatrix = new LoadMatrix();
//        loadMatrix.loadFile(inputPath);
//        countingMatrixMap = loadMatrix.matrixMap;
//    }
//
//    /**
//     * 加载轨迹历史数据
//     */
//    private void loadTrajectoryData(String inputPath) throws IOException{
//        File file = new File(inputPath);
//        BufferedReader bf = new BufferedReader(new FileReader(file));
//        String line = bf.readLine();
//        while (line!=null){
////            System.out.println(line);
//        // 对测试集中每条数据进行预测
//            String[] lineArray = line.split("\t");
//            int userId = Integer.parseInt(lineArray[0]);
//            String[] trajaectory = lineArray[1].split("->");
////            System.out.println(trajaectory.length);
//            for (int i = 1; i < trajaectory.length; i ++){
//                total += K/5;
//                int currentLoc = Integer.parseInt(trajaectory[i-1]);
//                int destinationPoi = Integer.parseInt(trajaectory[i]);
//                predict(userId,currentLoc,destinationPoi,countingMatrixMap.get(userId),K);
//            }
//
//            line = bf.readLine();
//        }
//
//        bf.close();
//    }
//
//    /**
//     * 传统未考虑距离方法
//     * @param inputPath
//     * @throws IOException
//     */
//    private void loadTrajectoryData2(String inputPath) throws IOException{
//        Set<Integer> trueSet = new HashSet<>();
//        File file = new File(inputPath);
//        BufferedReader bf = new BufferedReader(new FileReader(file));
//        String line = bf.readLine();
//        int lastUser = Integer.parseInt(line.split("\t")[0]);
//        String[] trajaectory1 = line.split("\t")[1].split("->");
//        for (int i = 0; i < trajaectory1.length; i ++){
//            trueSet.add(Integer.parseInt(trajaectory1[i]));
//        }
//        while (line!=null){
////            System.out.println(line);
//            // 对测试集中每条数据进行预测
//            String[] lineArray = line.split("\t");
//            int userId = Integer.parseInt(lineArray[0]);
//            String[] trajaectory = lineArray[1].split("->");
//            if (userId == lastUser){
//                for (int i = 0; i < trajaectory.length; i ++){
//                    trueSet.add(Integer.parseInt(trajaectory[i]));
//                }
//            } else {
//                predict(userId,trueSet,countingMatrixMap.get(userId));
//                trueSet.clear();
//                lastUser = userId;
//                for (int i = 0; i < trajaectory.length; i ++){
//                    trueSet.add(Integer.parseInt(trajaectory[i]));
//                }
//            }
//            line = bf.readLine();
//        }
//
//        bf.close();
//    }
//
//
//    /**
//     *
//     * @param userId 用户Id
//     * @param currentLoc 当前所在位置（当前poi）
//     * @param destinationPoi 下一个兴趣点正确位置
//     * @param userMatrixMap 用户评分矩阵
//     * @param K top-K中K值
//     */
//    private void predict(int userId, int currentLoc, int destinationPoi, Map<Integer,Double> userMatrixMap, int K){
//
//        // 根据currentLoc 修正UserMatrixMap
//        if (!(poiLocMap.containsKey(currentLoc) && poiLocMap.containsKey(destinationPoi)))
//            return;
//        Geo locGeo = poiLocMap.get(currentLoc);
//        Geo dstGeo = poiLocMap.get(destinationPoi);
//        Double distance = StaticMethod.getDistBetweenCoordinate(locGeo.getLon(),locGeo.getLat(),dstGeo.getLon(),dstGeo.getLat());
////        System.out.println(distance);
//        Map<Integer,Double> newUserMatrixMap = new LinkedHashMap<>();
//        for (int poiId : userMatrixMap.keySet()){
//            if (poiId == currentLoc)
//                continue;
//            double score = userMatrixMap.get(poiId);
//            newUserMatrixMap.put(poiId,score / (Math.pow(distance+1,1)*1.0));
//        }
//        // userMatrix 根据value排序
//        newUserMatrixMap= (Map<Integer, Double>) StaticMethod.sortMapByValue(newUserMatrixMap);
//        userMatrixMap = (Map<Integer, Double>) StaticMethod.sortMapByValue(userMatrixMap);
//
//        // 修正后推荐
//        Set<Integer> recSet = new HashSet<>();
//        int index = 0;
//        for (int poiId: newUserMatrixMap.keySet()){
//            recSet.add(poiId);
//            index ++;
//            if (index == K)
//                break;
//        }
////        if (recSetWithoutDis.contains(destinationPoi))
////            TT ++;
//        if (recSet.contains(destinationPoi))
//            TT_sort++;
//
//    }
//
//    private void predict(int userId,Set<Integer> trueSet, Map<Integer,Double> userMatrixMap){
//        userMatrixMap = (Map<Integer, Double>) StaticMethod.sortMapByValue(userMatrixMap);
//
//        Set<Integer> rec = new HashSet<>();
//        int index1 = 0;
//        for (int poiId: userMatrixMap.keySet()){
//            rec.add(poiId);
//            index1++;
//            if (index1 == K)
//                break;
//        }
//        total += rec.size();
//        for (int POI:trueSet){
//            if (rec.contains(POI))
//                TT_sort ++;
//        }
//    }
//
//    public static void main(String[] args) throws IOException {
//        PMFPredict pmf = new PMFPredict();
//        // 加载位置数据
//        pmf.loadPoiLocation();
//        // 加载评分矩阵
////        String countingMatrixPath = "F:\\rec\\output\\result\\SFO_OUT\\SFO_OUT1553065527681.txt";
//        String countingMatrixPath = "F:\\rec\\output\\result\\SFO_OUT\\SFO_OUT1553137342697.txt";
//        pmf.loadCountingMatrix(countingMatrixPath);
//        // 加载轨迹数据
//        String trajectoryPath = "F:\\rec\\output\\SFO_with_date_trajectory1552219584904.txt";
//        pmf.loadTrajectoryData2(trajectoryPath);
//        pmf.loadTrajectoryData(trajectoryPath);
//
//        System.out.println(pmf.TT_sort + " " + pmf.total);
//        System.out.println("准确率:" + (pmf.TT_sort/(pmf.total * 1.0)));
//
//    }
//}
