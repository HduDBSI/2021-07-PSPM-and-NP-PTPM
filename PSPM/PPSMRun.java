package cn.hdu.edu.predict;

import net.librec.common.LibrecException;
import net.librec.conf.Configuration;
import net.librec.data.model.TextDataModel;
import net.librec.eval.RecommenderEvaluator;
import net.librec.eval.ranking.PrecisionEvaluator;
import net.librec.eval.ranking.RecallEvaluator;
import net.librec.recommender.RecommenderContext;

import java.io.IOException;

public class PPSMRun{


    public void recommender() throws ClassNotFoundException, LibrecException, IOException {

        Long startTime = System.currentTimeMillis();

        Configuration.Resource resource = new Configuration.Resource("PDISM.properties");
        Configuration conf = new Configuration();
        conf.addResource(resource);

        String outDir  = "F:\\rec\\output";
        String outputPath  =  outDir + "\\result\\SFO_OUT\\SFO_OUT" + startTime +".txt";

        TextDataModel dataModel = new TextDataModel(conf);
        dataModel.buildDataModel();
        RecommenderContext context = new RecommenderContext(conf, dataModel);

        PPSM recommend = new PPSM();
        recommend.setContext(context);
        recommend.recommend(context);

        int k = 4;
        RecommenderEvaluator PRECISION = new PrecisionEvaluator();
        PRECISION.setTopN(k);
        System.out.println("top-k:" + k + ",PRECISION:" + recommend.evaluate(PRECISION));
        RecommenderEvaluator RECALL = new RecallEvaluator();
        RECALL.setTopN(k);
        System.out.println("top-k:" + k + ",RECALL:" + recommend.evaluate(RECALL));

        // 下面一行注释打开，可以将评分矩阵保存到本地路径
//        pbmfrecommend.saveAllPredict(outputPath);

    }

    public static void main(String[] args) throws LibrecException, IOException, ClassNotFoundException {
        PPSMRun run = new PPSMRun();
        run.recommender();
    }


}
