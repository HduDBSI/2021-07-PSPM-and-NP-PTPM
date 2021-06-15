// 之前写的模型

//package cn.hdu.edu.predict;
//
//import cn.hdu.edu.tools.StaticMethod;
//import com.google.common.cache.LoadingCache;
//import com.google.common.collect.BiMap;
//import com.google.common.collect.Table;
//import com.google.common.collect.Table.Cell;
//import net.librec.annotation.ModelData;
//import net.librec.common.LibrecException;
//import net.librec.math.algorithm.Randoms;
//import net.librec.math.structure.DenseMatrix;
//import net.librec.math.structure.DenseVector;
//import net.librec.math.structure.SparseVector;
//import net.librec.recommender.MatrixFactorizationRecommender;
//
//import java.io.*;
//import java.text.DecimalFormat;
//import java.util.*;
//import java.util.concurrent.ExecutionException;
//
//@ModelData({"isRanking", "fismrmse", "P", "Q", "itemBiases", "userBiases"})
//public class LFM extends MatrixFactorizationRecommender {
//
//    /**
//     * Guava cache configuration
//     */
//    protected static String cacheSpec;
//    /**
//     * user-items cache, item-users cache
//     */
//    protected LoadingCache<Integer, List<Integer>> userItemsCache;
//    /**
//     * train matrix size
//     */
//    private int nnz;
//    /**
//     * hyper-parameters
//     */
//    private float rho, alpha, beta, itemBiasReg, popBiasReg;
//    /**
//     * learning rate
//     */
//    private double lRate;
//    /**
//     * items and users biases vector
//     */
//    private DenseVector itemBiases, popuBiases;
//    private DenseVector popu;
//    /**
//     * two low-rank item matrices, an item-item similarity was learned as a product of these two matrices
//     */
//    private DenseMatrix P, Q;
//
//    @Override
//    protected void setup() throws LibrecException {
//
//        super.setup();
//
//        P = new DenseMatrix(numItems, numFactors);
//        Q = new DenseMatrix(numItems, numFactors);
//        P.init(0,0.01);
//        Q.init(0,0.01);
//        popuBiases = new DenseVector(numUsers);
//        itemBiases = new DenseVector(numItems);
//        popu = new DenseVector(numItems);
//
//        // 统计流行度
////        calPop();
//        popuBiases.init(0,0.01);
//        itemBiases.init(0,0.01);
//        nnz = trainMatrix.size();
//        rho = conf.getFloat("rec.recommender.rho");//3-15
//        alpha = conf.getFloat("rec.recommender.alpha",0.5f);
//        beta  = conf.getFloat("rec.recommender.beta",0.6f);
//
//
//        itemBiasReg =conf.getFloat("rec.recommender.itemBiasReg",0.1f);
//        popBiasReg =conf.getFloat("rec.recommender.userBiasReg",0.1f);
//
//        lRate=conf.getDouble("rec.iteration.learnrate",0.0001);
//        cacheSpec = conf.get("guava.cache.spec", "maximumSize=200,expireAfterAccess=2m");
//        userItemsCache = trainMatrix.rowColumnsCache(cacheSpec);
//
//    }
//
//    // 计算流行度
//    protected void calPop(){
//        Map<Integer,Double> countMap = new HashMap<>();
//        Table<Integer, Integer, Double> R = trainMatrix.getDataTable();
//        for (Cell<Integer, Integer, Double> cell : R.cellSet()) {
//            int u = cell.getRowKey();
//            int i = cell.getColumnKey();
//            double count_ui = cell.getValue();
//            if (countMap.containsKey(i)){
//                double count_i = countMap.get(i);
//                countMap.put(i,count_i + count_ui);
//            }else {
//                countMap.put(i,count_ui);
//            }
//        }
//        countMap = (Map<Integer, Double>) StaticMethod.sortMapByValue(countMap);
//        for (int i : countMap.keySet()){
//            popu.set(i,countMap.get(i)>100?1:countMap.get(i)/(100*1.0));
//        }
//
//    }
//
//    /**
//     * map根据value排序(desc)
//     * @param map
//     * @return
//     */
//    protected LinkedHashMap<?,Integer> sortMapByValue(Map<?,Integer> map){
//        List<Map.Entry<?, Integer>> list = new ArrayList<Map.Entry<?, Integer>>(map.entrySet());
//        Collections.sort(list, new Comparator<Map.Entry<?, Integer>>() {
//            public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
//                if ((o2.getValue() - o1.getValue())== 0)
//                    return 0;
//                else if ((o2.getValue() - o1.getValue()) > 0)
//                    return 1;
//                else return -1;
//            }
//        });
//        LinkedHashMap<Object,Integer> resultMap = new LinkedHashMap<Object, Integer>();
//        for(Map.Entry<?, Integer> t:list){
//            resultMap.put(t.getKey(),t.getValue());
//        }
//        return resultMap;
//    }
//
//    @Override
//    protected void trainModel() throws LibrecException{
//
//        int sampleSize = (int) (rho * nnz);
//        int totalSize = numUsers * numItems;
//        for (int iter = 1; iter <= numIterations; iter++) {
//            loss = 0;
//            // new training data by sampling negative values
//            Table<Integer, Integer, Double> R = trainMatrix.getDataTable();
//            // make a random sample of negative feedback (total - nnz)
//            List<Integer> indices = null;
//            try {
//                indices = Randoms.randInts(sampleSize, 0, totalSize - nnz);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            int index = 0, count = 0;
//            boolean isDone = false;
//            for (int u = 0; u < numUsers; u++) {
//                for (int j = 0; j < numItems; j++) {
//                    double ruj = trainMatrix.get(u, j);
//                    if (ruj != 0)
//                        continue; // rated items
//                    if (count++ == indices.get(index)) {
//                        R.put(u, j, 0.0);
//                        index++;
//                        if (index >= indices.size()) {
//                            isDone = true;
//                            break;
//                        }
//                    }
//                }
//                if (isDone)
//                    break;
//            }
//
//            // update throughout each user-item-rating (u, i, rui) cell
//            for (Cell<Integer, Integer, Double> cell : R.cellSet()) {
//                int u = cell.getRowKey();
//                int i = cell.getColumnKey();
//                double rui = cell.getValue();
//                // get n_u (n_u = |Ru| - 1)
//                SparseVector Ru = trainMatrix.row(u);
//                int n_u = Ru.size() - 1;
//                if (n_u == 0 || n_u == -1) {
//                    n_u = 1;
//                }
//                // get summation of P_j into X
//                DenseVector X = new DenseVector(numFactors);
//                for (int j : Ru.getIndex()) {
//                    if (i != j) {
//                        X = X.add(P.row(j));
//                    }
//                }
//                X = X.scale(Math.pow(n_u, -alpha));
//                // for efficiency, use the below code to predict rui instead of
//                // using "predict(u,j)"
//                double bi = itemBiases.get(i);
//                double bu = popuBiases.get(u);
//
//                double pui = bu * popu.get(i) + bi + Q.row(i).inner(X);
//
//                double eui = rui - pui;
//                loss += eui * eui;
//
//                // update bi
//                itemBiases.add(i, lRate * (eui - itemBiasReg * bi));
//                loss += itemBiasReg * bi * bi;
//                // update bu
//                popuBiases.add(u, lRate * (eui - popBiasReg * bu));
//                loss += itemBiasReg * bu * bu;
//                // update Qi
//                DenseVector deltaq = X.scale(eui).minus(Q.row(i).scale(beta));
//                loss += beta * Q.row(i).inner(Q.row(i));
//                Q.setRow(i, Q.row(i).add(deltaq.scale(lRate)));
//                // update Pj
//                for (int j : Ru.getIndex()) {
//                    if (i != j) {
//                        DenseVector deltap = Q.row(i).scale(eui*Math.pow(n_u, -alpha)).minus(P.row(j).scale(beta));
//                        loss += beta * P.row(j).inner(P.row(j));
//                        P.setRow(j, P.row(j).add(deltap.scale(lRate)));
//                    }
//                }
//            }
//            loss *= 0.5;
//            if (isConverged(iter) && earlyStop){
//                break;
//            }
//            updateLRate(iter);
//        }
//    }
//
//    @Override
//    protected double predict(int u, int j) throws LibrecException {
//        double pred = popuBiases.get(u) * popu.get(j) + itemBiases.get(j);
////        double pred = 0d;
//
//        List<Integer> ratedItems = null;
//        try {
//            ratedItems = userItemsCache.get(u);
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//
//        double sum = 0;
//        int count = 0;
//        for (int i : ratedItems) {
//            // for test, i and j will be always unequal as j is unrated
//            if (i != j) {
//                sum += DenseMatrix.rowMult(P, i, Q, j);
//                count++;
//            }
//        }
//
//        double wu = count - 1  > 0 ? Math.pow(count - 1, -alpha) : 0;
//        return pred + wu * sum;
//    }
//
//    /**
//     * 保存所有预测结果到文件，每个用户对每个地点的偏好特征
//     */
//    public boolean saveAllPredict(String outputFile) throws LibrecException {
//        BiMap<Integer, String> inverseUserIds = userMappingData.inverse();
//        BiMap<Integer, String> inverseItemIds = itemMappingData.inverse();
//        File file = new File(outputFile);
//        BufferedWriter writer = null;
//        DecimalFormat format = new DecimalFormat("#.##");
//        try {
//            if (!file.getParentFile().exists())
//                file.getParentFile().mkdirs();
//            writer = new BufferedWriter(new OutputStreamWriter(
//                    new FileOutputStream(file)));
//            for (int i = 0; i < numUsers; i++) {
//                for (int j = 0; j < numItems; j++) {
//                    double data = predict(i, j);
//                    writer.write(inverseUserIds.get(i) + "\t" + inverseItemIds.get(j) + "\t" + format.format(data) + "\n");
//                }
//            }
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        LOG.info("Result path is " + outputFile);
//        return true;
//    }
//
//}
