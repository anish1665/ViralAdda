package com.ronak.viral.adda.providers.soundcloud.player.player;

import com.ronak.viral.adda.providers.soundcloud.api.object.TrackObject;

/**
 * Listener used to catch events performed on the play playlist.
 */
public interface CheerleaderPlaylistListener {

    /**
     * Called when a tracks has been added to the player playlist.
     *
     * @param track track added.
     */
    public void onTrackAdded(TrackObject track);


    /**
     * Called when a tracks has been removed from the player playlist.
     *
     * @param track   track removed.
     * @param isEmpty true if the playlist is empty after deletion.
     */
    public void onTrackRemoved(TrackObject track, boolean isEmpty);
}
