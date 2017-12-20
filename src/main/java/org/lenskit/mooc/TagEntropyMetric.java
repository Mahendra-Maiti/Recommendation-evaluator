package org.lenskit.mooc;

import org.lenskit.LenskitRecommender;
import org.lenskit.api.Recommender;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.Entity;
import org.lenskit.eval.traintest.AlgorithmInstance;
import org.lenskit.eval.traintest.DataSet;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricColumn;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.eval.traintest.metrics.TypedMetricResult;
import org.lenskit.eval.traintest.recommend.TopNMetric;
import org.lenskit.mooc.cbf.TagData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Metric that measures how long a TopN list actually is.
 */
public class TagEntropyMetric extends TopNMetric<TagEntropyMetric.Context> {
    private static final Logger logger = LoggerFactory.getLogger(TagEntropyMetric.class);

    /**
     * Construct a new tag entropy metric metric.
     */
    public TagEntropyMetric() {
        super(TagEntropyResult.class, TagEntropyResult.class);
    }

    @Nonnull
    @Override
    public MetricResult measureUser(TestUser user, int expectedSize, ResultList recommendations, Context context) {
        if (recommendations == null || recommendations.isEmpty()) {
            return MetricResult.empty();
            // no results for this user.
        }
        int n = recommendations.size();

        // get tag data from the context so we can use it
        DataAccessObject dao = context.getDAO();
        double entropy = 0;

        // You can get a movie's tags with:
        // dao.query(TagData.ITEM_TAG_TYPE).withAttribute(TagData.ITEM_ID, res.getId()).get();
        // Each entity's tag can be retrieved with 'itemTag.get(TagData.TAG)'

        List<Long> Item_Ids=recommendations.idList();

        HashMap<Long, HashMap<String, Integer> > movie_map=new HashMap<>();
        HashSet<String> tags=new HashSet<>();

        for(Long it: Item_Ids)
        {
            List<Entity> itemTags = dao.query(TagData.ITEM_TAG_TYPE).withAttribute(TagData.ITEM_ID,it).get();

            HashMap<String, Integer> tag_map=new HashMap<>();

            for(Entity itag: itemTags) //loop for evert tag in the movie
            {
                String tag_name = itag.get(TagData.TAG);

                tags.add(tag_name);

                if(tag_map.containsKey(tag_name)){
                    tag_map.put(tag_name,tag_map.get(tag_name)+1);
                }
                else{
                    tag_map.put(tag_name,1);
                }

                //tag_map.put(name,(tag_map.containsKey(name)?tag_map.get(name)+1:1.0));

            }

            movie_map.put(it,tag_map);

        }


        //HashMap<String, Double> prob_map=new HashMap<String, Double>();

        for(String tag: tags)
        {


            Double prob_val=0.0;

            for(Long id: Item_Ids) //for every movie in the recommendation
            {
                Double total_tag_count=0.0;

                HashMap<String,Integer> t_map=movie_map.get(id);
              //  logger.info("tmap size {}",t_map.size());

                for(Map.Entry<String,Integer> e : t_map.entrySet())
                {
                    total_tag_count+=(double)(e.getValue()).intValue();
                }

                if(t_map.containsKey(tag))
                {

                    prob_val+=((t_map.get(tag))/total_tag_count);
                //    logger.info("incr {}",(t_map.get(tag)));
                }

            }
            prob_val/=n;



            //logger.info(" prob_val {}",prob_val);

            entropy=entropy+(-prob_val*(Math.log(prob_val)/Math.log(2.0)));

        }

        context.addUser(entropy);

        return new TagEntropyResult(entropy);
    }

    @Nullable
    @Override
    public Context createContext(AlgorithmInstance algorithm, DataSet dataSet, Recommender recommender) {
        return new Context((LenskitRecommender) recommender);
    }

    @Nonnull
    @Override
    public MetricResult getAggregateMeasurements(Context context) {
        return new TagEntropyResult(context.getMeanEntropy());
    }

    public static class TagEntropyResult extends TypedMetricResult {
        @MetricColumn("TopN.TagEntropy")
        public final double entropy;

        public TagEntropyResult(double ent) {
            entropy = ent;
        }

    }

    public static class Context {
        private LenskitRecommender recommender;
        private double totalEntropy;
        private int userCount;

        /**
         * Create a new context for evaluating a particular recommender.
         *
         * @param rec The recommender being evaluated.
         */
        public Context(LenskitRecommender rec) {
            recommender = rec;
        }

        /**
         * Get the recommender being evaluated.
         *
         * @return The recommender being evaluated.
         */
        public LenskitRecommender getRecommender() {
            return recommender;
        }

        /**
         * Get the DAO for the current recommender evaluation.
         */
        public DataAccessObject getDAO() {
            return recommender.get(DataAccessObject.class);
        }

        /**
         * Add the entropy for a user to this context.
         *
         * @param entropy The entropy for one user.
         */
        public void addUser(double entropy) {
            totalEntropy += entropy;
            userCount += 1;
        }

        /**
         * Get the average entropy over all users.
         *
         * @return The average entropy over all users.
         */
        public double getMeanEntropy() {
            return totalEntropy / userCount;
        }
    }
}