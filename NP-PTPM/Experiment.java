package cn.hdu.edu.nextPOI;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class Experiment {
    public static void main(String[] args) throws Exception {
//        variousDay();
        variousK();
    }

    public static void variousDay() throws Exception {
        TransMatrix transMatrix = new TransMatrix();
        String workday = "F:\\rec\\output\\SFO_train_workday_1552546751161.txt";
        String weekend = "F:\\rec\\output\\SFO_train_weekend_1552546751161.txt";
        String testPath = "F:\\rec\\output\\SFO_test_1552546882522.txt";
        transMatrix.workdayTransMatrix(workday);
        transMatrix.weekendTransMatrix(weekend);
        Predict pre = new Predict();
        // 加载位置数据
        pre.loadPoiLocation();
//        // 加载评分矩阵
//        String countingMatrixPath = "F:\\rec\\output\\result\\SFO_OUT1552219792592.txt";
//        pre.loadCountingMatrix(countingMatrixPath);
        pre.workdayMatrixMap = transMatrix.workdayMatrixMap;
        pre.weekendMatrixMap = transMatrix.weekendMatrixMap;

        String outputPath = "F:\\rec\\output\\result\\splitDay\\our.txt";
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));
        // K
        for (int i = 1; i <= 10; i ++){
            pre.TT = 0;
            pre.dataSize = 0;
            pre.K = i;
            pre.loadTrajectoryData(testPath);
            writer.write("K=" + pre.K + ",准确率=" + pre.TT/(pre.dataSize / pre.K * 1.0) + ", 数据集大小=" + pre.dataSize / pre.K + ",准确个数=" + pre.TT + "\n");
        }
        writer.close();

    }

    public static void variousK() throws Exception {
        // 测试集数据
//        String testPath = "F:\\rec\\output\\SFO_test_1552457730402.txt";
        String testPath = "F:\\rec\\output\\SFO_test_1552546882522.txt";
        String trainPath = "F:\\rec\\output\\SFO_train_1552457753329.txt";
        // 预测评分矩阵
        String countingMatrixPath = "F:\\rec\\output\\result\\SFO_OUT1552219792592.txt";

        TransMatrix transMatrix = new TransMatrix();
        transMatrix.transMatrix(trainPath);
        transMatrix.personalTransMatrix(trainPath);
        Predict pre = new Predict();
        // 加载位置数据
        pre.loadPoiLocation();
        // 加载预测评分矩阵
        pre.loadCountingMatrix(countingMatrixPath);
        pre.transMatrixMap = transMatrix.transMatrixMap;
        pre.persTransMatrixMap = transMatrix.persTransMatrixMap;
//        System.out.println(pre.persTransMatrixMap);
        String outputPath = "F:\\rec\\output\\result\\K\\our_525.txt";
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath)));
        // K
        for (int i = 1; i <= 10; i ++){
            pre.TT = 0;
            pre.dataSize = 0;
            pre.K = i;
            pre.loadTrajectoryData(testPath);
            writer.write("K=" + pre.K + ",准确率=" + pre.TT/(pre.dataSize / pre.K * 1.0) + ", 数据集大小=" + pre.dataSize / pre.K + ",准确个数=" + pre.TT + "\n");
        }
        writer.close();
    }
}
