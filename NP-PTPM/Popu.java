package cn.hdu.edu.nextPOI;

import cn.hdu.edu.tools.StaticMethod;
import com.sun.org.apache.bcel.internal.generic.POP;

import java.io.*;
import java.util.*;

/**
 * 统计兴趣点排名进行推荐
 */
public class Popu {
    public int K = 1;
    public int TT = 0;
    public int dataSize = 0;
    private Map<Integer,Integer> popuMap = new HashMap<>();

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

            for (int i = 1; i < trajaectory.length; i ++){
                int nextPOI = Integer.parseInt(trajaectory[i]);
                if (predictByPopu(nextPOI,K)){
                    TT ++;
                }
                dataSize += 1;
            }
            line = bf.readLine();
        }
//        System.out.println(dataSize);
        bf.close();

    }


    private void loadTrainData(String input) throws IOException{
        File file = new File(input);
        BufferedReader bf = new BufferedReader(new FileReader(file));
        String line = bf.readLine();
        while (line!=null){
            int poiID = Integer.parseInt(line.split("\t")[1]);
            if (popuMap.containsKey(poiID)){
                int count = popuMap.get(poiID);
                popuMap.put(poiID,++count);
            } else {
                popuMap.put(poiID,1);
            }
            line = bf.readLine();
        }
    }

    private boolean predictByPopu(int nextPoi, int K){
        Set<Integer> recSet = new HashSet<>();
        popuMap = (Map<Integer, Integer>) sortMapByValue(popuMap);
        int indexK = 0;
        for (int poiId:popuMap.keySet()){
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
     * map根据value排序(desc)
     * @param map
     * @return
     */
    public static LinkedHashMap<?,Integer> sortMapByValue(Map<?,Integer> map){
        List<Map.Entry<?, Integer>> list = new ArrayList<Map.Entry<?, Integer>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<?, Integer>>() {
            public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
                if ((o2.getValue() - o1.getValue())== 0)
                    return 0;
                else if ((o2.getValue() - o1.getValue()) > 0)
                    return 1;
                else return -1;
            }
        });
        LinkedHashMap<Object,Integer> resultMap = new LinkedHashMap<Object, Integer>();
        for(Map.Entry<?, Integer> t:list){
            resultMap.put(t.getKey(),t.getValue());
        }
        return resultMap;
    }


    /**
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String input = "F:\\rec\\output\\SFO_with_score.txt";
        Popu popu = new Popu();
        popu.loadTrainData(input);
        System.out.println(popu.popuMap);
        String outputPath = "F:\\rec\\output\\result\\k\\popu\\Popu_525.txt";
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));
        String testPath = "F:\\rec\\output\\SFO_test_1552546882522.txt";
        // K
        for (int i = 1; i <= 10; i ++){
            popu.TT = 0;
            popu.dataSize = 0;
            popu.K = i;
            popu.loadTrajectoryData(testPath);
            writer.write("K=" + popu.K + ",准确率=" + popu.TT/(popu.dataSize* 1.0) + ", 数据集大小=" + popu.dataSize + ",准确个数=" + popu.TT + "\n");
        }
        writer.close();


    }
}
