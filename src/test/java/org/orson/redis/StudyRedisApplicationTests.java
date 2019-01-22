package org.orson.redis;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.orson.redis.app.ArticleRepository;
import org.orson.redis.app.pojo.Article;
import org.orson.redis.app.util.IDGenerator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import java.util.*;

import static junit.framework.TestCase.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StudyRedisApplicationTests {


	private Jedis jedis;


	private ArticleRepository repository;

	@Before
	public void contextLoads() {
		jedis = mock(Jedis.class);
	}


	@Test
	public void vote_is_due() {
		Double createTime = (double) (System.currentTimeMillis() - 8 * 86400 * 1000);
		// 判断是否可以投票
		when(jedis.zscore(eq("time:"), anyString())).thenReturn(createTime);

		repository = new ArticleRepository(this.jedis);

		try {
			repository.vote("orson", "001");
			//防止调用方法没有错误导致unit test通过，记得要添加fail()
			fail();
		} catch (Exception e) {
			MatcherAssert.assertThat(e.getMessage(), is("Cannot vote a due article."));
		}
	}

	@Test
	public void vote_is_not_due() {
		Double createTime = (double) (System.currentTimeMillis() - 5 * 86400 * 1000);
		// 判断是否可以投票
		when(jedis.zscore(eq("time:"), anyString())).thenReturn(createTime);

		repository = new ArticleRepository(this.jedis);

		assertTrue(repository.vote("orson", "001"));
	}

	@Test
	public void vote_duplicate() {

		when(jedis.sismember(anyString(), anyString())).thenReturn(true);

		repository = new ArticleRepository(this.jedis);

		Boolean voted = repository.vote("orson", "001");

		assertTrue(!voted);

	}

	/**
	 * should not have such unit test, because we can ensure the testing machine has redis instance.
	 */
	//@Test
	public void vote_unit_test() {
		Jedis lh = new Jedis("localhost");

		ArticleRepository ar = new ArticleRepository(lh);

		Boolean isVoted = ar.vote("orson", "001");

		assertTrue(isVoted);
	}


	@Test
	public void post_article() {
		Article article = new Article();
		article.setId("002");
		article.setTitle("The Last Emperor");
		article.setPoster("OrsonJu");
		article.setLink("www.lastemperor.com");
		article.setTime(System.currentTimeMillis());
		article.setVotes(0);

		// create article successfully
		when(jedis.hset(eq("article:" + article.getId()), anyMap())).thenReturn(1L);


		when(jedis.zadd(eq("time:"), anyDouble(), eq("article:" + article.getId()))).thenReturn(1L);

		when(jedis.zadd(eq("score:"), anyDouble() + 432d, eq("article:" + article.getId()))).thenReturn(1L);


		repository = new ArticleRepository(this.jedis);


		String id = repository.post("orson", article);

		assertNotNull(id);
	}


	@Test
	public void get_articles_by_score() {
		//prepare set
		Set<String> articleSet = new TreeSet<>();
		articleSet.add("article:001");
		articleSet.add("article:002");

		when(jedis.zrevrange(eq("score:"), anyLong(), anyLong())).thenReturn(articleSet);

		//article 1
		Map<String, String> kvMap  = new HashMap<>();
		kvMap.put("title", "Thinking in Java");
		kvMap.put("link", "www.thing-in-java.com");
		kvMap.put("poster", "Orson Ju");
		kvMap.put("time", String.valueOf(System.currentTimeMillis()));
		kvMap.put("votes", String.valueOf(3));
		when(jedis.hgetAll(eq("article:001"))).thenReturn(kvMap);

		// article 2
		Map<String, String> kvMap2  = new HashMap<>();
		kvMap2.put("title", "The last emperor");
		kvMap2.put("link", "www.last-emperor.com");
		kvMap2.put("poster", "Martin Luo");
		kvMap2.put("time", String.valueOf(System.currentTimeMillis()));
		kvMap2.put("votes", String.valueOf(99));
		when(jedis.hgetAll(eq("article:002"))).thenReturn(kvMap2);


		this.repository = new ArticleRepository(this.jedis);


		// 可以测试到for each中到逻辑处理部分。
		List<Article> articles = repository.get("score:", 1, 10);

		assertTrue(articles.size() == 2);
	}


//	@Test
	public void user_case() {
		Jedis lh = new Jedis("localhost");

		ArticleRepository ar = new ArticleRepository(lh);

		//post a new article
		String id = IDGenerator.newId();
		Article article = new Article();
		article.setTitle("The day after the day");
		article.setPoster("Orson Ju");
		article.setLink("wwww.ldl.com");
		article.setVotes(0);
		article.setTime(System.currentTimeMillis());
		article.setId(id);
		ar.post("orson", article);

		//vote a article
		ar.vote("orson", article.getId());

		//add to a group
		ar.group(article.getId(), "novel");
		ar.group(article.getId(), "science");

		//ungroup
		ar.ungroup(article.getId(), "science");

		// get article
		ar.getArticleByGroup("novel", 1, 10);

		// in vote
		ar.invote("orson", article.getId());

	}


}

