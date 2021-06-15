package cn.hdu.edu.nextPOI;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by wiyee on 2019/3/10.
 * 加载预测评分矩阵
 */
public class LoadMatrix {

    public Map<Integer,Map<Integer,Double>> matrixMap = new HashMap<>();
    public Set<Integer> userSet = new HashSet<>();
    public Set<Integer> poiSet = new HashSet<>();

    public void loadFile(String inputPath) throws IOException {
        File file = new File(inputPath);
        BufferedReader bf = new BufferedReader(new FileReader(file));
        String line = bf.readLine();
        while (line!=null){
            String[] lineSplit = line.split("\t");
            int userId = Integer.parseInt(lineSplit[0]);
            userSet.add(userId);
            int poiId = Integer.parseInt(lineSplit[1]);
            poiSet.add(poiId);
            // 去掉负分数
            double score = Double.parseDouble(lineSplit[2]);
            if (matrixMap.containsKey(userId)){
                Map<Integer,Double> tmpMap = matrixMap.get(userId);
                tmpMap.put(poiId,score);
                matrixMap.put(userId,tmpMap);
            }else {
                Map<Integer,Double> tmpMap = new HashMap<>();
                tmpMap.put(poiId,score);
                matrixMap.put(userId,tmpMap);
            }
            line = bf.readLine();

        }
        bf.close();
    }

    public static void main(String[] args) throws IOException {
        String input = "F:\\rec\\output\\result\\SFO_OUT1552219792592.txt";
    }
}
