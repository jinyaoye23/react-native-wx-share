package com.wxshare;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMusicObject;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXVideoObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import okhttp3.internal.Util;

public class WxShareModuleModule extends ReactContextBaseJavaModule implements IWXAPIEventHandler {

    private final ReactApplicationContext reactContext;
    private IWXAPI api = null;
    static String APP_ID = "";

    private final static String NOT_REGISTERED = "registerApp required.";
    private final static String INVOKE_FAILED = "WeChat API invoke returns false.";
    private final static String INVALID_ARGUMENT = "invalid argument.";

    public static final String TYPE_TEXT = "text";          //文字
    public static final String TYPE_IMAGE1 = "imageUrl";         //图片
    public static final String TYPE_IMAGE2 = "imageFile";         //图片
    public static final String TYPE_IMAGE3 = "imageResource";         //图片
    public static final String TYPE_WEB_PAGE = "news";      //网页
    public static final String TYPE_MUSIC = "audio";         //音乐
    public static final String TYPE_VIDEO = "video";         //视频

    public static final String OPTIONS_TITLE = "title";
    public static final String OPTIONS_DESC = "desc";
    public static final String OPTIONS_TAG_NAME = "tagName";
    public static final String OPTIONS_THUMB_SIZE = "thumbSize";
    public static final String OPTIONS_TRANSACTION = "transaction";
    public static final String OPTIONS_SCENE = "scene";
    public static final String OPTIONS_TYPE = "type";

    public static final String OPTIONS_TEXT = "text";

    public static final String OPTIONS_IMAGE_URL = "imageUrl";
    public static final String OPTIONS_IMAGE_PATH = "imagePath";

    public static final String OPTIONS_THUMB_IMAGE = "thumbImage";

    public static final String OPTIONS_WEBPAGE_URL = "webpageUrl";

    public static final String OPTIONS_MUSIC_URL = "musicUrl";
    public static final String OPTIONS_MUSIC_LOW_BAND_URL = "musicLowBandUrl";

    public static final String OPTIONS_VIDEO_URL = "videoUrl";
    public static final String OPTIONS_VIDEO_LOW_BAND_URL = "videoLowBandUrl";

    String tagName = null;
    String title = null;
    String desc = null;
    String transaction = null;
    Bitmap bitmap = null;           //分享的缩略图
    int thumbSize = 150;            //分享的缩略图大小
    int scene;                      //分享的方式(0:聊天界面，1:朋友圈，2:收藏)
    @ReactMethod
    public void registerApp(String APP_ID,Callback callback) { // 向微信注册
        Log.i("registerApp = ","123"+APP_ID);
        WxShareModuleModule.APP_ID = APP_ID;
        api = WXAPIFactory.createWXAPI(this.getReactApplicationContext().getBaseContext(), APP_ID, true);
        callback.invoke(null, api.registerApp(APP_ID));
    }
    public WxShareModuleModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        api = WXAPIFactory.createWXAPI(reactContext, null);
    }

    @Override
    public String getName() {
        return "WxShareModuleModule";
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    /**
     * 分享到朋友圈
     * @param data 数据
     * @param callback 回调
     */
    @ReactMethod
    public void shareToTimeline(ReadableMap data,Callback callback){
        Log.i("shareToTimeLine = ","123"+WxShareModuleModule.APP_ID);
        Log.i("timeline = ", String.valueOf(api));

        if (api == null) {
            callback.invoke(NOT_REGISTERED);
            return;
        }
//        _share(SendMessageToWX.Req.WXSceneTimeline,type, data, callback);
        sendReq(SendMessageToWX.Req.WXSceneTimeline, data, callback);
    }

    /**
     * 分享到会话框
     * @param data 数据
     * @param callback 回调
     */
    @ReactMethod
    public void shareToSession(ReadableMap data,Callback callback){
        Log.i("session = ", String.valueOf(api));
        if (api == null) {
            callback.invoke(NOT_REGISTERED);
            return;
        }
//        _share(SendMessageToWX.Req.WXSceneSession,type, data, callback);
        sendReq(SendMessageToWX.Req.WXSceneSession, data, callback);
    }

    /**
     * 分享到微信
     */
    @ReactMethod
    public void sendReq(final int scene,final ReadableMap options, final Callback callback) {
        Log.i("shareToTimeLine = ", String.valueOf(options));

        if (api == null) {
            if (callback != null) {
                callback.invoke("please registerApp before this !");
            }
        } else {
            if (options == null) {
                if (callback != null) {
                    callback.invoke("please setting options !");
                }
            } else {
                if (options.hasKey("type")) {
                    WXMediaMessage msg = new WXMediaMessage();

//                    int type = options.getInt(OPTIONS_TYPE);
                    switch (options.getString("type")) {
                        case TYPE_TEXT:
                            msg.mediaObject = getTextObj(options);
                            break;
                        case TYPE_IMAGE1:
                        case TYPE_IMAGE2:
                        case TYPE_IMAGE3:
                            msg.mediaObject = getImageObj(options);
                            break;
                        case TYPE_WEB_PAGE:
                            msg.mediaObject = getWebpageObj(options);
                            break;
                        case TYPE_MUSIC:
                            msg.mediaObject = getMusicObj(options);
                            break;
                        case TYPE_VIDEO:
                            msg.mediaObject = getVideoObj(options);
                            break;
                        default:
                            if (callback != null) {
                                callback.invoke("please check correct media type !");
                            }
                            break;
                    }

                    if (options.hasKey(OPTIONS_TITLE)) {
                        title = options.getString(OPTIONS_TITLE);
                    }
                    if (options.hasKey(OPTIONS_DESC)) {
                        desc = options.getString(OPTIONS_DESC);
                    }
                    if (options.hasKey(OPTIONS_TAG_NAME)) {
                        tagName = options.getString(OPTIONS_TAG_NAME);
                    }
                    if (options.hasKey(OPTIONS_THUMB_SIZE)) {
                        thumbSize = options.getInt(OPTIONS_THUMB_SIZE);
                    }
                    if (options.hasKey(OPTIONS_TRANSACTION)) {
                        transaction = options.getString(OPTIONS_TRANSACTION);
                    }

                    if (!TextUtils.isEmpty(title)) {
                        msg.title = title;
                    }
                    if (!TextUtils.isEmpty(desc)) {
                        msg.description = desc;
                    }
                    if (!TextUtils.isEmpty(tagName)) {
                        msg.mediaTagName = tagName;
                    }
                    if (bitmap != null) {
                        Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, thumbSize, thumbSize, true);
                        bitmap.recycle();
                        msg.thumbData = bmpToByteArray(thumbBmp, true);
                    }

                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.message = msg;
                    if (!TextUtils.isEmpty(transaction)) {
                        req.transaction = transaction;
                    } else {
                        req.transaction = String.valueOf(System.currentTimeMillis());
                    }
                    req.scene = scene;

                    boolean sendReqOK = api.sendReq(req);
                    if (callback != null) {
                        Log.i("shareResult = ", String.valueOf(sendReqOK));
                        callback.invoke(null, sendReqOK);
                    }
                } else {
                    if (callback != null) {
                        callback.invoke("please setting share type !");
                    }
                }
            }
        }
    }
    /**
     * <p/>
     * Bitmap to byte array
     */
    private byte[] bmpToByteArray(Bitmap bmp, boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取文本对象
     */
    private WXTextObject getTextObj(ReadableMap options) {
        WXTextObject textObject = new WXTextObject();
        if (options.hasKey(OPTIONS_TEXT)) {
            textObject.text = options.getString(OPTIONS_TEXT);
        }
        return textObject;
    }

    /**
     * 获取图片对象
     */
    private WXImageObject getImageObj(ReadableMap options) {
        WXImageObject imageObject = new WXImageObject();
        if (options.hasKey(OPTIONS_IMAGE_URL)) {
            String remoteUrl = options.getString(OPTIONS_IMAGE_URL);
//            imageObject.imageData = remoteUrl;
            try {
                bitmap = BitmapFactory.decodeStream(new URL(remoteUrl).openStream());
            } catch (IOException e) {
                bitmap = null;
                e.printStackTrace();
            }
        }
        if (options.hasKey(OPTIONS_IMAGE_PATH)) {
            String localPath = options.getString(OPTIONS_IMAGE_PATH);
            File file = new File(localPath);
            if (file.exists()) {
                imageObject.setImagePath(localPath);
                bitmap = BitmapFactory.decodeFile(localPath);
            } else {
                bitmap = null;
            }
        }
        return imageObject;
    }

    /**
     * 获取网页对象
     *
     */
    private WXWebpageObject getWebpageObj(ReadableMap options) {
        WXWebpageObject webpageObject = new WXWebpageObject();
        if (options.hasKey(OPTIONS_WEBPAGE_URL)) {
            webpageObject.webpageUrl = options.getString(OPTIONS_WEBPAGE_URL);
        }
        if(options.hasKey(OPTIONS_THUMB_IMAGE)){
            String thumbImage = options.getString(OPTIONS_THUMB_IMAGE);
            try {
                bitmap = BitmapFactory.decodeStream(new URL(thumbImage).openStream());
            } catch (IOException e) {
                bitmap = null;
                e.printStackTrace();
            }
        }
        return webpageObject;
    }

    /**
     * 获取音乐对象
     * */
    private WXMusicObject getMusicObj(ReadableMap options) {
        WXMusicObject musicObject = new WXMusicObject();
        if (options.hasKey(OPTIONS_MUSIC_URL)) {
            musicObject.musicUrl = options.getString(OPTIONS_MUSIC_URL);
        }
        if (options.hasKey(OPTIONS_MUSIC_LOW_BAND_URL)) {
            musicObject.musicLowBandUrl = options.getString(OPTIONS_MUSIC_LOW_BAND_URL);
        }

        if(options.hasKey(OPTIONS_THUMB_IMAGE)){
            String thumbImage = options.getString(OPTIONS_THUMB_IMAGE);
            try {
                bitmap = BitmapFactory.decodeStream(new URL(thumbImage).openStream());
            } catch (IOException e) {
                bitmap = null;
                e.printStackTrace();
            }
        }
        return musicObject;
    }

    /**
     * 获取视频对象
     * */
    private WXVideoObject getVideoObj(ReadableMap options){
        WXVideoObject videoObject = new WXVideoObject();
        if(options.hasKey(OPTIONS_VIDEO_URL)){
            videoObject.videoUrl = options.getString(OPTIONS_VIDEO_URL);
        }
        if(options.hasKey(OPTIONS_VIDEO_LOW_BAND_URL)){
            videoObject.videoLowBandUrl = options.getString(OPTIONS_VIDEO_LOW_BAND_URL);
        }
        if(options.hasKey(OPTIONS_THUMB_IMAGE)){
            String thumbImage = options.getString(OPTIONS_THUMB_IMAGE);
            try {
                bitmap = BitmapFactory.decodeStream(new URL(thumbImage).openStream());
            } catch (IOException e) {
                bitmap = null;
                e.printStackTrace();
            }
        }
        return videoObject;
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {

    }
}
