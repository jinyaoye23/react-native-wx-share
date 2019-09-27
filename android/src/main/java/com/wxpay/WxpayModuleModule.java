package com.wxpay;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WxpayModuleModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private IWXAPI api;
    static String APP_ID = "";
    static Promise promise = null;

    public WxpayModuleModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        api = WXAPIFactory.createWXAPI(reactContext, null);

    }

    @Override
    public String getName() {
        return "WxpayModuleModule";
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    @ReactMethod
    public void registerApp(String APP_ID) { // 向微信注册
        WxpayModuleModule.APP_ID = APP_ID;
        api.registerApp(APP_ID);
    }

    @ReactMethod
    public void pay(final ReadableMap order, Promise promise) {
        WxpayModuleModule.promise = promise;
        PayReq request = new PayReq();
        request.appId = order.getString("appId");
        request.partnerId = order.getString("partnerId");
        request.prepayId= order.getString("prepayId");
        request.packageValue = order.getString("package");
        request.nonceStr= order.getString("nonceStr");
        request.timeStamp= order.getString("timeStamp");
        request.sign= order.getString("sign");
        api.sendReq(request);
    }

    @ReactMethod
    public void isSupported(Promise promise) { // 判断是否支持调用微信SDK
        boolean isSupported = api.isWXAppInstalled();
        promise.resolve(isSupported);
    }
}
