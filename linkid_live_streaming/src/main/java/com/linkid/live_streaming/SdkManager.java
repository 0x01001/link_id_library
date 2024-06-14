package com.linkid.live_streaming;

import android.app.Application;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoIMSendBarrageMessageCallback;
import im.zego.zegoexpress.callback.IZegoIMSendBroadcastMessageCallback;
import im.zego.zegoexpress.callback.IZegoPublisherSetStreamExtraInfoCallback;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoStreamResourceMode;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoPlayerConfig;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingFragment;

public class SdkManager {
    private SdkData data;
    private IEventHandle eventHandle;
    private long appID = 18387906;
    private String appSign = "f05a167ef94557bcb846cbfdba5ee7af21f834e86432b6ed6f78a44b79341dc2";
    private List<ZegoUser> onlineUsers = new ArrayList<>();

    public void init(SdkData data, IEventHandle eventHandle) {
        Log.d("SdkManager", "init sdk...");
        this.data = data;
        this.eventHandle = eventHandle;
    }


    public void createEngine(Application application) {
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.appSign = appSign;
        profile.scenario = ZegoScenario.BROADCAST; // General scenario.
        profile.application = application;
        ZegoExpressEngine.createEngine(profile, null);
    }


    // destroy engine
    public void destroyEngine() {
        ZegoExpressEngine.destroyEngine(null);
    }


//    public void startPreview() {
//        ZegoCanvas previewCanvas = new ZegoCanvas(data.hostView);
//        ZegoExpressEngine.getEngine().startPreview(previewCanvas);
//    }

    public Fragment startPreview() {
        ZegoUIKitPrebuiltLiveStreamingConfig config;
        if (data.isHost) {
            config = ZegoUIKitPrebuiltLiveStreamingConfig.host();
        } else {
            config =  ZegoUIKitPrebuiltLiveStreamingConfig.audience();
        }

        return ZegoUIKitPrebuiltLiveStreamingFragment.newInstance(appID, appSign, data.userID, data.userName, data.roomID, config);
    }

    public void stopPreview() {
        ZegoExpressEngine.getEngine().stopPreview();
    }

    public void loginRoom() {
        ZegoUser user = new ZegoUser(data.userID, data.userName);
        ZegoRoomConfig roomConfig = new ZegoRoomConfig();
        roomConfig.isUserStatusNotify = true;
        ZegoExpressEngine.getEngine().loginRoom(data.roomID, user, roomConfig, (int error, JSONObject extendedData) -> {
            eventHandle.onLoginResult(error, data.isHost, extendedData);
        });
    }

    public void logoutRoom() {
        ZegoExpressEngine.getEngine().logoutRoom();
    }

    public void startPublish() {
        String streamID = data.roomID + "_" + data.userID + "_call";
        Log.d("streamID: ", streamID);
        ZegoExpressEngine.getEngine().startPublishingStream(streamID);
    }

    public void stopPublish() {
        ZegoExpressEngine.getEngine().stopPublishingStream();
    }

    public void startPlayStream(String streamID) {
        data.hostView.setVisibility(View.VISIBLE);
        ZegoCanvas playCanvas = new ZegoCanvas(data.hostView);

        ZegoPlayerConfig config = new ZegoPlayerConfig();
        config.resourceMode = ZegoStreamResourceMode.DEFAULT; 
        
        ZegoExpressEngine.getEngine().startPlayingStream(streamID, playCanvas, config);
    }

    public void stopPlayStream(String streamID) {
        ZegoExpressEngine.getEngine().stopPlayingStream(streamID);
        data.hostView.setVisibility(View.GONE);
    }

    public void sendMessage(String roomId, String msg){
        if (onlineUsers.size() < 500){
            ZegoExpressEngine.getEngine().sendBroadcastMessage(roomId, msg, new IZegoIMSendBroadcastMessageCallback() {
                /**  The callback to report the delivery result of the Broadcast Message */
                @Override
                public void onIMSendBroadcastMessageResult(int errorCode, long messageID) {
                    //Handle the delivery result of the Broadcast Message.
                    eventHandle.onSendBroadcastMessageResult(errorCode, messageID);
                }
            });
        }else{
            ZegoExpressEngine.getEngine().sendBarrageMessage(roomId, msg, new IZegoIMSendBarrageMessageCallback(){
                /** The callback to report the delivery result of the Barrage Message */
                @Override
                public void onIMSendBarrageMessageResult(int errorCode, String messageID) {
                    // Handle the delivery result of the Barrage Message.
//                    eventHandle.onSendBroadcastMessageResult(errorCode, messageID);
                }
            });
        }
    }

    public void startListenEvent() {
        ZegoExpressEngine.getEngine().setEventHandler(new IZegoEventHandler() {
            @Override
            // Callback for updates on the status of the streams in the room.
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonArray = objectMapper.writeValueAsString(streamList);
                    eventHandle.onRoomStreamUpdate(roomID, updateType.value(), jsonArray, extendedData);
                }
                catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            // Callback for updates on the status of other users in the room.
            // Users can only receive callbacks when the isUserStatusNotify property of ZegoRoomConfig is set to `true` when logging in to the room (loginRoom).
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                if (updateType == ZegoUpdateType.ADD) {
                    onlineUsers.addAll(userList);
                } else if (updateType == ZegoUpdateType.DELETE) {
                    onlineUsers.removeAll(userList);
                }
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonArray = objectMapper.writeValueAsString(userList);
                    eventHandle.onRoomUserUpdate(roomID, updateType.value(), jsonArray);
                }
                catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
            @Override
            // Callback for updates on the current user's room connection status.
            public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int i, JSONObject jsonObject) {
                if (data.isHost){
                    if (reason == ZegoRoomStateChangedReason.LOGINED) {
                        // Stream has started
                        Log.d("LiveStream", "Stream started: " + roomID);
                        String streamId = data.roomID + "_" + data.userID + "_main";
                        ZegoExpressEngine.getEngine().addPublishCdnUrl(streamId, "cdnURL", (int errorCode) -> {
                            if (errorCode == 0) {
                                Log.d("LiveStream", "CDN URL added successfully");
                            } else {
                                Log.d("LiveStream", "Failed to add CDN URL: " + errorCode);
                            }
                        });
                    } else if (reason == ZegoRoomStateChangedReason.LOGOUT) {
                        // Stream has stopped
                        Log.d("LiveStream", "Stream stopped: " + roomID);
                    }
                }
                eventHandle.onRoomStateChanged(roomID, reason.value(), i, jsonObject);
            }

            // Status notification of audio and video stream publishing.
            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
                eventHandle.onPublisherStateUpdate(streamID, state.value(), errorCode, extendedData);
            }

            // Status notifications of audio and video stream playing.
            // This callback is received when the status of audio and video stream
            // playing of a user changes. If an exception occurs during stream playing
            // due to a network interruption, the SDK automatically retries to play
            // the streams.
            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                eventHandle.onPlayerStateUpdate(streamID, state.value(), errorCode, extendedData);
            }
        });
    }

    public void stopListenEvent() {
        this.eventHandle = null;
        ZegoExpressEngine.getEngine().setEventHandler(null);
    }
}
