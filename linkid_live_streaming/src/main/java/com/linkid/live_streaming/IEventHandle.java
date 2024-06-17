package com.linkid.live_streaming;

import org.json.JSONObject;

// updateType: ADD(0), DELETE(1)
// roomStateChangedReason: LOGINING(0), LOGINED(1), LOGIN_FAILED(2), RECONNECTING(3), RECONNECTED(4), RECONNECT_FAILED(5), KICK_OUT(6), LOGOUT(7), LOGOUT_FAILED(8);
// publisherState: NO_PUBLISH(0), PUBLISH_REQUESTING(1), PUBLISHING(2);
// playerState: NO_PLAY(0), PLAY_REQUESTING(1), PLAYING(2);

public interface IEventHandle {
//    // Login room result callback.
//    public void onLoginResult(int errorCode, boolean isHost, JSONObject extendedData);
//
//    public void onRoomStreamUpdate(String roomID, int updateType, String streamList, JSONObject extendedData);
//
//    // Callback for updates on the status of other users in the room.
//    // Users can only receive callbacks when the isUserStatusNotify property of ZegoRoomConfig is set to `true` when logging in to the room (loginRoom).
//    public void onRoomUserUpdate(String roomID, int updateType, String userList);
//
//    // Callback for updates on the current user's room connection status.
//    public void onRoomStateChanged(String roomID, int roomStateChangedReason, int i, JSONObject jsonObject);
//
//    // Status notification of audio and video stream publishing.
//    public void onPublisherStateUpdate(String streamID, int publisherState, int errorCode, JSONObject extendedData);
//
//    // Status notifications of audio and video stream playing.
//    // This callback is received when the status of audio and video stream
//    // playing of a user changes. If an exception occurs during stream playing
//    // due to a network interruption, the SDK automatically retries to play
//    // the streams.
//    public void onPlayerStateUpdate(String streamID, int playerState, int errorCode, JSONObject extendedData);

    // The callback to report the delivery result of the Broadcast Message
    void onSendHeartResult(int errorCode, String messageID);
}
