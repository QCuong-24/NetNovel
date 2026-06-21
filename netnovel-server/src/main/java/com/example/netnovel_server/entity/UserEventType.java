package com.example.netnovel_server.entity;

/**
 * User interaction events used for recommendation system.
 */
public enum UserEventType {
    /**
     * User opened novel detail page.
     * Weak positive signal.
     */
    VIEW_NOVEL,
    
    /**
     * User opened a chapter for reading.
     * Stronger signal than VIEW_NOVEL.
     */
    VIEW_CHAPTER,

    /**
     * User followed a novel.
     * Positive signal preference signal.
     */
    FOLLOW_NOVEL,

    /**
     * User unfollowed a novel.
     */
    UNFOLLOW_NOVEL,

    /**
     * User liked a novel.
     * Strong positive preference signal.
     */
    LIKE_NOVEL,

    /**
     * User removed like from a novel.
     */
    UNLIKE_NOVEL,

    /**
     * User bookmarked a novel.
     * Indicates intention to read later.
     */
    BOOKMARK_NOVEL,

    /**
     * User removed bookmark from a novel.
     */    
    UNBOOKMARK_NOVEL,

    /**
     * User searched for novels.
     */
    SEARCH,

    /**
     * Recommendation item was shown to user.
     * Used for CTR measurement.
     */
    RECOMMENDATION_IMPRESSION,

    /**
     * User clicked a recommendation item.
     * Used for CTR and recommendation evaluation.
     */
    RECOMMENDATION_CLICK
}
