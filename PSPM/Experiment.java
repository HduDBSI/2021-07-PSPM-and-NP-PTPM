package cn.hdu.edu.predict;

import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.ranking.PrecisionEvaluator;
import net.librec.eval.ranking.RecallEvaluator;
import net.librec.recommender.Recommender;
import net.librec.recommender.RecommenderContext;
import net.librec.recommender.cf.UserKNNRecommender;
import net.librec.recommender.cf.ranking.*;

import java.io.*;

/**
 * 参数实验
 */
public class Experiment {
    public static void main(String[] args) throws LibrecException, IOException, ClassNotFoundException {
        Long time = System.currentTimeMillis();
        String outputPath = "F:\\rec\\output\\result\\delta\\K_" + time + ".txt";
//        compareResult("WBPR");
        variousK(outputPath);
    }

    /**
     * delta
     * @param output
     * @throws ClassNotFoundException
     * @throws LibrecException
     * @throws IOException
     */
    private static void variousDelta(String output)throws ClassNotFoundException, LibrecException, IOException {
        File file = new File(output);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        for (double delta = 0.0;delta <= 1.5;delta += 0.1){
            System.out.println(delta);
            double acc = 0;
            double recall = 0;
            for (int i = 0; i < 5; i ++){
                Configuration.Resource resource = new Configuration.Resource("PDISM.properties");
                Configuration conf = new Configuration();
                conf.addResource(resource);
                conf.set("rec.recommender.delta",String.valueOf(delta));
                TextDataModel dataModel = new TextDataModel(conf);
                dataModel.buildDataModel();
                RecommenderContext context = new RecommenderContext(conf, dataModel);
                PPSM recommend = new PPSM();
                recommend.setContext(context);
                recommend.recommend(context);
                RecommenderEvaluator PRECISION = new PrecisionEvaluator();
                PRECISION.setTopN(5);
                RecommenderEvaluator RECALL = new RecallEvaluator();
                RECALL.setTopN(5);
                acc += recommend.evaluate(PRECISION);
                recall += recommend.evaluate(RECALL);
            }
            writer.write("delta:" + delta + ",accurate:" + acc/5.0 + ",recall:" + recall/5.0 + "\n");
        }
        writer.close();
    }

    /**
     * K
     * @param output
     * @throws ClassNotFoundException
     * @throws LibrecException
     * @throws IOException
     */
    private static void variousK(String output)throws ClassNotFoundException, LibrecException, IOException{
        File file = new File(output);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        for (int K = 1;K <= 1;K += 1){
            System.out.println(K);
            double acc = 0;
            double recall = 0;
            for (int i = 0; i < 10; i ++){
                Configuration.Resource resource = new Configuration.Resource("PDISM.properties");
                Configuration conf = new Configuration();
                conf.addResource(resource);
                TextDataModel dataModel = new TextDataModel(conf);
                dataModel.buildDataModel();
                RecommenderContext context = new RecommenderContext(conf, dataModel);
                PPSM recommend = new PPSM();
                recommend.setContext(context);
                recommend.recommend(context);
                RecommenderEvaluator PRECISION = new PrecisionEvaluator();
                PRECISION.setTopN(K);
                RecommenderEvaluator RECALL = new RecallEvaluator();
                RECALL.setTopN(K);
                acc += recommend.evaluate(PRECISION);
                recall += recommend.evaluate(RECALL);
            }
            writer.write("K:" + K + ",accurate:" + acc/5.0 + ",recall:" + recall/5.0 + "\n");
        }
        writer.close();
    }

    /**
     * 迭代次数
     * @param output
     * @throws ClassNotFoundException
     * @throws LibrecException
     * @throws IOException
     */
    private static void variousIterator(String output)throws ClassNotFoundException, LibrecException, IOException{
        File file = new File(output);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        for (int iterator = 10;iterator <= 150;iterator += 10){
            System.out.println(iterator);
            double acc = 0;
            double recall = 0;
            for (int i = 0; i < 5; i ++){
                Configuration.Resource resource = new Configuration.Resource("PDISM.properties");
                Configuration conf = new Configuration();
                conf.addResource(resource);
                conf.set("rec.iterator.maximum",String.valueOf(iterator));
                TextDataModel dataModel = new TextDataModel(conf);
                dataModel.buildDataModel();
                RecommenderContext context = new RecommenderContext(conf, dataModel);
                PPSM recommend = new PPSM();
                recommend.setContext(context);
                recommend.recommend(context);
                RecommenderEvaluator PRECISION = new PrecisionEvaluator();
                PRECISION.setTopN(5);
                RecommenderEvaluator RECALL = new RecallEvaluator();
                RECALL.setTopN(5);
                acc += recommend.evaluate(PRECISION);
                recall += recommend.evaluate(RECALL);
            }
            writer.write("iterator:" + iterator + ",accurate:" + acc/5.0 + ",recall:" + recall/5.0 + "\n");
        }
        writer.close();
    }

    private static void variousBeta(String output) throws ClassNotFoundException, LibrecException, IOException{

        File file = new File(output);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        for (double beta = 0.0;beta <= 1;beta += 0.1){
            System.out.println(beta);
            double acc = 0;
            double recall = 0;
            for (int i = 0; i < 5; i ++){
                Configuration.Resource resource = new Configuration.Resource("PDISM.properties");
                Configuration conf = new Configuration();
                conf.addResource(resource);
                conf.set("rec.recommender.beta",String.valueOf(beta));
                TextDataModel dataModel = new TextDataModel(conf);
                dataModel.buildDataModel();
                RecommenderContext context = new RecommenderContext(conf, dataModel);
                PPSM recommend = new PPSM();
                recommend.setContext(context);
                recommend.recommend(context);
                RecommenderEvaluator PRECISION = new PrecisionEvaluator();
                PRECISION.setTopN(5);
                RecommenderEvaluator RECALL = new RecallEvaluator();
                RECALL.setTopN(5);
                acc += recommend.evaluate(PRECISION);
                recall += recommend.evaluate(RECALL);
            }
            writer.write("delta:" + beta + ",accurate:" + acc/5.0 + ",recall:" + recall/5.0 + "\n");
        }
        writer.close();
    }

    private static void variousPopBiasReg(String output) throws ClassNotFoundException, LibrecException, IOException{

        File file = new File(output);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        for (double lamda2 = 0.0;lamda2 <= 1;lamda2 += 0.1){
            System.out.println(lamda2);
            double acc = 0;
            double recall = 0;
            for (int i = 0; i < 5; i ++){
                Configuration.Resource resource = new Configuration.Resource("PDISM.properties");
                Configuration conf = new Configuration();
                conf.addResource(resource);
                conf.set("rec.recommender.popBiasReg",String.valueOf(lamda2));
                TextDataModel dataModel = new TextDataModel(conf);
                dataModel.buildDataModel();
                RecommenderContext context = new RecommenderContext(conf, dataModel);
                PPSM recommend = new PPSM();
                recommend.setContext(context);
                recommend.recommend(context);
                RecommenderEvaluator PRECISION = new PrecisionEvaluator();
                PRECISION.setTopN(5);
                RecommenderEvaluator RECALL = new RecallEvaluator();
                RECALL.setTopN(5);
                acc += recommend.evaluate(PRECISION);
                recall += recommend.evaluate(RECALL);
            }
            writer.write("delta:" + lamda2 + ",accurate:" + acc/5.0 + ",recall:" + recall/5.0 + "\n");
        }
        writer.close();
    }

    private static void variousAlpha(String output) throws ClassNotFoundException, LibrecException, IOException{

        File file = new File(output);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        for (double alpha = 0.0;alpha <= 1;alpha += 0.1){

            double acc = 0;
            double recall = 0;
            for (int i = 0; i < 5; i ++){
                Configuration.Resource resource = new Configuration.Resource("PDISM.properties");
                Configuration conf = new Configuration();
                conf.addResource(resource);
                conf.set("rec.recommender.alpha",String.valueOf(alpha));
                TextDataModel dataModel = new TextDataModel(conf);
                dataModel.buildDataModel();
                RecommenderContext context = new RecommenderContext(conf, dataModel);
                PPSM recommend = new PPSM();
                recommend.setContext(context);
                recommend.recommend(context);
                RecommenderEvaluator PRECISION = new PrecisionEvaluator();
                PRECISION.setTopN(5);
                RecommenderEvaluator RECALL = new RecallEvaluator();
                RECALL.setTopN(5);
                acc += recommend.evaluate(PRECISION);
                recall += recommend.evaluate(RECALL);
            }
            writer.write("alpha:" + alpha + ",accurate:" + acc/5.0 + ",recall:" + recall/5.0 + "\n");
        }
        writer.close();
    }

    private static void variousFactor(String output) throws ClassNotFoundException, LibrecException, IOException{

        File file = new File(output);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        for (int  factor = 10;factor <= 100;factor += 10){

            double acc = 0;
            double recall = 0;
            for (int f = 0; f < 5; f ++){
                Configuration.Resource resource = new Configuration.Resource("PDISM.properties");
                Configuration conf = new Configuration();
                conf.addResource(resource);
                conf.set("rec.factor.number",String.valueOf(factor));
                TextDataModel dataModel = new TextDataModel(conf);
                dataModel.buildDataModel();
                RecommenderContext context = new RecommenderContext(conf, dataModel);
                PPSM recommend = new PPSM();
                recommend.setContext(context);
                recommend.recommend(context);
                RecommenderEvaluator PRECISION = new PrecisionEvaluator();
                PRECISION.setTopN(5);
                RecommenderEvaluator RECALL = new RecallEvaluator();
                RECALL.setTopN(5);
                acc += recommend.evaluate(PRECISION);
                recall += recommend.evaluate(RECALL);
            }
            writer.write("factor:" + factor + ",accurate:" + acc/5.0 + ",recall:" + recall/5.0 + "\n");
        }
        writer.close();
    }

    private static void compareResult(String algorithm) throws ClassNotFoundException, LibrecException, IOException{
        Long time = System.currentTimeMillis();
        String output = "F:\\rec\\output\\result\\delta\\" + algorithm + "_" + time +  ".txt";
        File file = new File(output);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        for (int K = 1; K <10; K += 2){
            System.out.println("K=" + K);
            double acc = 0;
            double recall = 0;
            for (int i = 0; i < 10; i ++){

                Recommender recommend = null;
                Configuration.Resource resource = null;
                switch (algorithm){
                    case "EALS":
                        recommend = new EALSRecommender();
                        resource = new Configuration.Resource("rec\\cf\\ranking\\eals-test.properties");
                        break;
                    case "WBPR":
                        recommend = new WBPRRecommender();
                        resource = new Configuration.Resource("rec\\cf\\ranking\\wbpr-test.properties");
                        break;
                    case "AoBPR":
                        recommend = new AoBPRRecommender();
                        resource = new Configuration.Resource("rec\\cf\\ranking\\aobpr-test.properties");
                        break;
                    case "GBPR":
                        recommend = new GBPRRecommender();
                        resource = new Configuration.Resource("rec\\cf\\ranking\\gbpr-test.properties");
                        break;
                    case "SLIM":
                        recommend = new SLIMRecommender();
                        resource = new Configuration.Resource("rec\\cf\\ranking\\slim-test.properties");
                        break;
                    default:
                        System.err.println("no such algorithm");
                        break;
                }

                Configuration conf = new Configuration();
                conf.addResource(resource);
                TextDataModel dataModel = new TextDataModel(conf);
                dataModel.buildDataModel();
                RecommenderContext context = new RecommenderContext(conf, dataModel);

                recommend.setContext(context);
                recommend.recommend(context);
                RecommenderEvaluator PRECISION = new PrecisionEvaluator();
                PRECISION.setTopN(K);
                RecommenderEvaluator RECALL = new RecallEvaluator();
                RECALL.setTopN(K);
                acc += recommend.evaluate(PRECISION);
                recall += recommend.evaluate(RECALL);
            }
            writer.write("algorithm: "+ algorithm + ", K=:"+ K+",accurate:" + acc/5.0 + ",recall:" + recall/5.0 + "\n");
        }
        writer.close();
    }
}
