package cn.hdu.edu.predict;

import cn.hdu.edu.pojo.Geo;
import cn.hdu.edu.tools.JDBC;
import cn.hdu.edu.tools.StaticMethod;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.BiMap;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import net.librec.annotation.ModelData;
import net.librec.common.LibrecException;
import net.librec.math.algorithm.Randoms;
import net.librec.math.structure.DenseMatrix;
import net.librec.math.structure.DenseVector;
import net.librec.math.structure.SparseVector;
import net.librec.recommender.MatrixFactorizationRecommender;

import java.io.*;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

@ModelData({"isRanking", "fismrmse", "P", "Q", "itemBiases", "userBiases"})
public class PPSM extends MatrixFactorizationRecommender {

    protected static String cacheSpec;
    /**
     * user-items cache, item-users cache
     */
    protected LoadingCache<Integer, List<Integer>> userItemsCache;

    // 兴趣点坐标数据
    protected Map<Integer,Geo> poiLocMap = new HashMap<>();

    // 用户活动中心
    protected Map<Integer,Geo> userCenterCoordinateMap = new HashMap<>();

    /**
     * 训练矩阵的大小
     */
    private int nnz;
    /**
     * 相应参数
     */
    private float rho, alpha, lamda1, delta, itemBiasReg, lamda2,positionBiasReg;
    /**
     * 学习率
     */
    private double lRate;
    /**
     * 偏执
     */
    private DenseVector itemBiases, popuBiases;
    private DenseVector popu;
    private DenseVector positionBiases;
    /**
     * 兴趣点特征矩阵
     */
    private DenseMatrix P, Q;

    @Override
    protected void setup() throws LibrecException {

        super.setup();
        // 初始化参数
        P = new DenseMatrix(numItems, numFactors);
        Q = new DenseMatrix(numItems, numFactors);
        P.init(0,0.01);
        Q.init(0,0.01);
        popuBiases = new DenseVector(numUsers);
        itemBiases = new DenseVector(numItems);
        popu = new DenseVector(numItems);
        positionBiases = new DenseVector(numUsers);

        popuBiases.init(0,0.01);
        positionBiases.init(0,0.01);
        itemBiases.init(0,0.01);

        // 预先--计算兴趣点流行度
        calPop();

        // 预先--加载兴趣点位置信息
        loadPoiLocation();

        // 预先--计算用户活动中心
        calCenterCoordinate();

        // 从配置文件中获取参数值
        nnz = trainMatrix.size();
        rho = conf.getFloat("rec.recommender.rho");// 采样比
        alpha = conf.getFloat("rec.recommender.alpha",0.5f);
        delta = conf.getFloat("rec.recommender.delta",0.7f);

        lamda1 = conf.getFloat("rec.recommender.lamda1",0.6f);
        lamda2 = conf.getFloat("rec.recommender.lamda2",0.1f);

        itemBiasReg =conf.getFloat("rec.recommender.itemBiasReg",0.1f);
        positionBiasReg = conf.getFloat("rec.recommender.positionBiasReg",0.1f);

        LOG.info("lamda1:" + lamda1 + ",lamda2:" + lamda2);
        LOG.info("factor:" + numFactors + ",迭代次数:" + numIterations);
        LOG.info("delta:" + delta + ",alpha:" + alpha);

        lRate=conf.getDouble("rec.iteration.learnrate",0.0001);
        cacheSpec = conf.get("guava.cache.spec", "maximumSize=200,expireAfterAccess=2m");
        userItemsCache = trainMatrix.rowColumnsCache(cacheSpec);

    }

    /**
     * 计算兴趣点流行度
     */
    protected void calPop(){
        Map<Integer,Double> countMap = new HashMap<>();
        Table<Integer, Integer, Double> R = trainMatrix.getDataTable();
        for (Cell<Integer, Integer, Double> cell : R.cellSet()) {
            int u = cell.getRowKey();
            int i = cell.getColumnKey();
            double count_ui = cell.getValue();
            if (countMap.containsKey(i)){
                double count_i = countMap.get(i);
                countMap.put(i,count_i + count_ui);
            }else {
                countMap.put(i,count_ui);
            }
        }
        countMap = (Map<Integer, Double>) StaticMethod.sortMapByValue(countMap);
        for (int i : countMap.keySet()){
            popu.set(i,countMap.get(i)>100?1:countMap.get(i)/(100*1.0));
        }
    }

    /**
     * 加载兴趣点坐标数据
     */
    private void loadPoiLocation(){
        Map<Integer,Geo> reLabeledGeoMap = new HashMap<>();
        try {
            String path = conf.get(("rec.recommender.location.path"));

            File file = new File(path);
            BufferedReader bf = new BufferedReader(new FileReader(file));
            String line = bf.readLine();
            while (line!=null){
                String[] strline = line.split("\t");
                int poiId = Integer.parseInt(strline[0]);
                double lat = Double.parseDouble(strline[1]);
                double lon = Double.parseDouble(strline[2]);
                reLabeledGeoMap.put(poiId,new Geo(lat,lon));
                line = bf.readLine();
            }
            bf.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        BiMap<Integer, String> inverseItemIds = itemMappingData.inverse();
        for (int j = 0; j < numItems; j++) {
            int item_id = Integer.parseInt(inverseItemIds.get(j));
            poiLocMap.put(j,reLabeledGeoMap.get(item_id));
        }
    }

    /**
     * 计算所有用户的活动中心
     */
    protected void calCenterCoordinate(){
        Table<Integer, Integer, Double> R = trainMatrix.getDataTable();
        Map<Integer,Map<Integer,Double>> RMap = R.rowMap();

        for (int userId:RMap.keySet()){
//            System.out.println(userId);
            Map<Integer,Double> uMap = RMap.get(userId);
            double sumLat = 0;
            double sumLon = 0;
            double total = 0;
            for (int poiId:uMap.keySet()){
                sumLat += poiLocMap.get(poiId).getLat() * uMap.get(poiId);
                sumLon += poiLocMap.get(poiId).getLon() * uMap.get(poiId);
                total += uMap.get(poiId);
            }
            userCenterCoordinateMap.put(userId,new Geo(sumLat/(total * 1.0),sumLon/(total * 1.0)));
        }
//        System.out.println(userCenterCoordinateMap.keySet().contains(18));
    }

    @Override
    protected void trainModel() throws LibrecException{

        int sampleSize = (int) (rho * nnz);
        int totalSize = numUsers * numItems;
        for (int iter = 1; iter <= numIterations; iter++) {
            loss = 0;
            // new training data by sampling negative values
            Table<Integer, Integer, Double> R = trainMatrix.getDataTable();
            // make a random sample of negative feedback (total - nnz)
            List<Integer> indices = null;
            try {
                indices = Randoms.randInts(sampleSize, 0, totalSize - nnz);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int index = 0, count = 0;
            boolean isDone = false;
            for (int u = 0; u < numUsers; u++) {
                for (int j = 0; j < numItems; j++) {
                    double ruj = trainMatrix.get(u, j);
                    if (ruj != 0)
                        continue; // rated items
                    if (count++ == indices.get(index)) {
                        R.put(u, j, 0.0);
                        index++;
                        if (index >= indices.size()) {
                            isDone = true;
                            break;
                        }
                    }
                }
                if (isDone)
                    break;
            }

            // update parameter
            for (Cell<Integer, Integer, Double> cell : R.cellSet()) {
                int u = cell.getRowKey();
                int i = cell.getColumnKey();
                double rui = cell.getValue();
                // get n_u (n_u = |Ru| - 1)
                SparseVector Ru = trainMatrix.row(u);
                int n_u = Ru.size() - 1;
                if (n_u == 0 || n_u == -1) {
                    n_u = 1;
                }
                // get summation of P_j into X
                DenseVector X = new DenseVector(numFactors);
                for (int j : Ru.getIndex()) {
                    if (i != j) {
                        X = X.add(P.row(j));
                    }
                }
                X = X.scale(Math.pow(n_u, -alpha));
                // for efficiency, use the below code to predict rui instead of
                // using "predict(u,j)"
                double bi = itemBiases.get(i);
				double bu = popuBiases.get(u);//ru
				double pu = positionBiases.get(u);
				double distance = 0;

                if (userCenterCoordinateMap.containsKey(u)){
                    distance = StaticMethod.getDistBetweenCoordinate(
                            userCenterCoordinateMap.get(u).getLon(),userCenterCoordinateMap.get(u).getLat(),
                            poiLocMap.get(i).getLon(),poiLocMap.get(i).getLat());
                }
                double pui = pu / Math.pow(distance + 1.0,delta) + bu * popu.get(i) + bi + Q.row(i).inner(X);

                double eui = rui - pui;
                loss += eui * eui;

                itemBiases.add(i, lRate * (eui - itemBiasReg * bi));
                loss += itemBiasReg * bi * bi;

                popuBiases.add(u, lRate * (eui - lamda2 * bu));
				loss += itemBiasReg * bu * bu;
				// update pu
                positionBiases.add(u,lRate * (eui - positionBiasReg * pu));

                // update Qi
                DenseVector deltaq = X.scale(eui).minus(Q.row(i).scale(lamda1));
                loss += lamda1 * Q.row(i).inner(Q.row(i));
                // Q.row(i)是个向量
                Q.setRow(i, Q.row(i).add(deltaq.scale(lRate)));
                // update Pj
                for (int j : Ru.getIndex()) {
                    if (i != j) {
                        DenseVector deltap = Q.row(i).scale(eui * Math.pow(n_u, -alpha)).minus(P.row(j).scale(lamda1));
                        loss += lamda1 * P.row(j).inner(P.row(j));
                        P.setRow(j, P.row(j).add(deltap.scale(lRate)));
                    }
                }
            }
            loss *= 0.5;
            if (isConverged(iter) && earlyStop){
                break;
            }
            updateLRate(iter);
        }
    }

    @Override
    protected double predict(int u, int j) throws LibrecException {
        double distance = 0;
        // 判断是否为新用户，因为新用户没有历史行为，无法计算活动中心
        if (userCenterCoordinateMap.containsKey(u)){
            distance = StaticMethod.getDistBetweenCoordinate(
                    userCenterCoordinateMap.get(u).getLon(),userCenterCoordinateMap.get(u).getLat(),
                    poiLocMap.get(j).getLon(),poiLocMap.get(j).getLat());
        }
        double pred = positionBiases.get(u) / Math.pow(distance + 1.0,delta) +  popuBiases.get(u) * popu.get(j) + itemBiases.get(j);

        List<Integer> ratedItems = null;
        try {
            ratedItems = userItemsCache.get(u);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        double sum = 0;
        int count = 0;
        for (int i : ratedItems) {
            // for test, i and j will be always unequal as j is unrated
            if (i != j) {
                sum += DenseMatrix.rowMult(P, i, Q, j);
                count++;
            }
        }

        double wu = count - 1  > 0 ? Math.pow(count - 1, -alpha) : 0;
        return pred + wu * sum;
    }

    /**
     * 保存所有预测结果到文件，每个用户对每个地点的偏好特征
     */
    public boolean saveAllPredict(String outputFile) throws LibrecException {
        BiMap<Integer, String> inverseUserIds = userMappingData.inverse();
        BiMap<Integer, String> inverseItemIds = itemMappingData.inverse();
        File file = new File(outputFile);
        BufferedWriter writer = null;
        DecimalFormat format = new DecimalFormat("#.##");
        try {
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file)));
            for (int i = 0; i < numUsers; i++) {
                for (int j = 0; j < numItems; j++) {
                    double data = predict(i, j);
                    writer.write(inverseUserIds.get(i) + "\t" + inverseItemIds.get(j) + "\t" + format.format(data) + "\n");
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        LOG.info("Result path is " + outputFile);
        return true;
    }

}
