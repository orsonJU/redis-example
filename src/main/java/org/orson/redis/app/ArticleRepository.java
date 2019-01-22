package org.orson.redis.app;

import org.orson.redis.app.pojo.Article;
import org.orson.redis.app.util.IDGenerator;
import org.springframework.util.Assert;
import redis.clients.jedis.Jedis;

import java.util.*;


public class ArticleRepository {


    private Jedis jedis;

    public ArticleRepository(Jedis jedis) {
        this.jedis = jedis;
    }

    /**
     * To vote a article.
     *
     * It is forbid to vote a article If it is created over 7 days.
     *
     * Obtain the latest score of an article and save the vote list.
     * @param userId
     * @param articleId
     * @return
     */
    public Boolean vote(final String userId, final String articleId) {
        String articleKey = String.format("article:%s", articleId);
        String votedKey = String.format("voted:%s", articleId);
        String userKey = String.format("user:%s", userId);

        // prevent duplicate vote
        if(jedis.sismember(votedKey, userId)) {
            return false;
        }

        Double createTime = jedis.zscore("time:", articleKey);
        // get the due time of the article
        Double dueTime = createTime + 7 * 86400 * 1000;
        if(dueTime < System.currentTimeMillis()) {
            throw new RuntimeException("Cannot vote a due article.");
        }
        jedis.sadd(votedKey, userKey);
        jedis.zincrby("score:", 432000, articleKey); // 432000
        jedis.hincrBy(articleKey, "votes", 1);
        return true;
    }


    /**
     * @param userId
     * @param articleId
     * @return
     */
    public Boolean invote(final String userId, final String articleId) {
        String destSet = String.format("voted:%s", articleId);
        String userKey = String.format("user:%s", userId);
        String articleKey = String.format("article:%s", articleId);

        Long zrem = jedis.srem(destSet, userKey);

        if(zrem > 0) {
            Double zincrby = jedis.zincrby("score:", -1, articleKey);
            jedis.hincrBy(articleKey, "votes", -1 );
        }
        return true;
    }

    public Boolean revertVote(final String userId, final String articleId) {

        String voteKey = String.format("voted:%s", articleId);
        String rejectKey = String.format("rejected:%s", articleId);
        String userKey = String.format("user:%s", userId);


        if(jedis.sismember(rejectKey, userKey)) {
            this.invote(userId, articleId);
        }else if(jedis.sismember(voteKey, userKey)) {
            this.invote(userId, articleId);
        }else {
            // 0 vote & 0 reject, no changes needed
        }

        return true;
    }

    /**
     * Post a new article.
     *
     * Obtain the create time and latest score.
     * @param userId
     * @param article
     * @return
     */
    public String post(final String userId, final Article article) {
        //generate unique id, so there won't exists an article with the ID.
        String id = Optional.of(article.getId()).isPresent() ? article.getId() : IDGenerator.newId();

        article.setId(id);
        Map<String, String> kvs = new HashMap<>();
        kvs.put("title", article.getTitle());
        kvs.put("link", article.getLink());
        kvs.put("poster", article.getPoster());
        kvs.put("time", String.valueOf(article.getTime()));
        kvs.put("votes", String.valueOf(article.getVotes()));

        String articleHashKey = String.format("article:%s", id);


        //post
        Long isAdded = jedis.hset(articleHashKey, kvs);

        if(isAdded == 0) {
            throw new RuntimeException("Article post failed.");
        }

        Long time = jedis.zadd("time:", article.getTime(), articleHashKey);
        Long score = jedis.zadd("score:", article.getTime() + 432000, articleHashKey); //432 seconds

        return id;
    }


    /**
     * get articles by @{order}, e.g. score: or time:
     * @param order
     * @param page
     * @param size
     * @return
     */
    public List<Article> get(final String order, final Integer page, final Integer size) {

        Assert.isTrue(page > 0, "page must be an integer");
        Assert.isTrue(size > 0, "size must be an integer");

        Long start = (long) ((page - 1) * size);
        Long end = start + size - 1;

        //zrevrange order start end
        Set<String> zrevrange = jedis.zrevrange(order, start, end);

        List<Article> articleList = new ArrayList<>();
        zrevrange.forEach(key -> {
            Map<String, String> kvMap = jedis.hgetAll(String.format("article:%s", key.split(":")[1]));
            Article article = new Article();
            article.setId(key.split(":")[1]);
            article.setTitle(kvMap.get("title"));
            article.setLink(kvMap.get("link"));
            article.setPoster(kvMap.get("poster"));
            article.setTime(Long.valueOf(kvMap.get("time")));
            article.setVotes(Integer.valueOf(kvMap.get("votes")));
            articleList.add(article);
        });
        return articleList;
    }


    /**
     * Remove an ariticle from a set of groups.
     * @param articleId
     * @param remove
     */
    public void ungroup(final String articleId, final String ...remove) {
        Assert.notNull(articleId, "articleId must not be null.");
        String articleKey = String.format("article:%s", articleId);
        Arrays.stream(remove).forEach(group -> jedis.srem(String.format("group:%s", group), articleKey));
    }

    /**
     * Add an article to a set of group.
     * @param articleId
     * @param add
     */
    public void group(final String articleId, final String ...add) {
        Assert.notNull(articleId, "articleId must not be null.");
        String articleKey = String.format("article:%s", articleId);
        Arrays.stream(add).forEach(group -> jedis.sadd(String.format("group:%s", group), articleKey));
    }


    /**
     * @param group
     * @param page
     * @param size
     * @return
     */
    public List<Article> getArticleByGroup(final String group, final Integer page, final Integer size) {
        String destSet = String.format("score:%s", group);
        String groupSet = String.format("group:%s", group);
        String scoreSet = "score:";

        //zinterstore
        if(!jedis.exists(destSet)) {
            jedis.zinterstore(destSet, groupSet, scoreSet);
            jedis.expire(destSet, 60);
        }

        return this.get(destSet, page, size);
    }
}
