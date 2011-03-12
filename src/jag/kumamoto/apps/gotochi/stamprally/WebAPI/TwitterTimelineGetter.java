package jag.kumamoto.apps.gotochi.stamprally.WebAPI;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterTimelineGetter {
	public static class TweetData {
		public final long id;
		public final String text;
		
		public TweetData(String text, long id) {
			this.text = text;
			this.id = id;
		}
	}
	
	private final String ConsumerKey = "";
	private final String ConsumerSecret = "";

	private final String mUserName;
	private long mMaxId;

	private final Twitter mTwitter;
	
	private final Queue<TweetData> mTweetQueue = new LinkedList<TweetData>();
	private int mMaxQueueSize = 20;


	public TwitterTimelineGetter(String userName, long maxId) {
		this.mUserName = userName;
		this.mMaxId = maxId;

		ConfigurationBuilder builder = new ConfigurationBuilder();

		// Twitterにアプリケーション登録したら付与される、
		// Consumer keyとConsumer secretをBuilderにセット。
		builder.setOAuthConsumerKey(ConsumerKey);
		builder.setOAuthConsumerSecret(ConsumerSecret);

		mTwitter = new TwitterFactory(builder.build()).getInstance();
	}

	public long getMaxId() {
		TweetData data = mTweetQueue.peek();
		return data != null ? data.id : mMaxId;
	}

	public TweetData getTweet() {
		return mTweetQueue.poll();
	}
	
	public int getMaxQueueSize() {
		return mMaxQueueSize;
	}
	
	public void setMaxQueueSize(int size) {
		mMaxQueueSize = size;
	}

	public void requestTweet(boolean allowInReply, boolean allowReTweet) {
		try {
			ResponseList<Status> timeline = mTwitter.getUserTimeline(mUserName);
			Collections.reverse(timeline);

			for (Status status : timeline) {
				if (!allowInReply && status.getInReplyToStatusId() != -1) {
					// リプライのステータスは無視
					continue;
				} else if (!allowReTweet && status.isRetweet()) {
					// リツイートのステータスは無視
					continue;
				}
				
				long id = status.getId();
				if (id > mMaxId) {
					mMaxId = id;
					mTweetQueue.offer(new TweetData(status.getText(), id));
				}
			}
			
			//上限を超えないように超えた分は捨てる
			while(mMaxQueueSize <= mTweetQueue.size()) {
				mTweetQueue.poll();
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}

	}

	public String getNewestTweet(boolean allowInReply, boolean allowReTweet) {
		try {
			ResponseList<Status> timeline = mTwitter.getUserTimeline(mUserName);
			for (Status status : timeline) {
				if (!allowInReply && status.getInReplyToStatusId() != -1) {
					// リプライのステータスは無視
					continue;
				} else if (!allowReTweet && status.isRetweet()) {
					// リツイートのステータスは無視
					continue;
				}
				if (status.getId() > mMaxId) {
					mMaxId = status.getId();
					return status.getText();
				}
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}

		return null;
	}

}
