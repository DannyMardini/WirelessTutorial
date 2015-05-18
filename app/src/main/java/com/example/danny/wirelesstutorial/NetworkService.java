package com.example.danny.wirelesstutorial;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.net.InetAddress;

/**
 * Created by danny on 5/17/15.
 */
public class NetworkService {

    private Context mContext;
    private NsdManager mNsdManager;
    private NsdServiceInfo mServiceInfo;
    private NsdServiceInfo mService;
    private NsdManager.RegistrationListener mRegistrationListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.ResolveListener mResolveListener;
    private static String TAG = "dmd";
    private static String SERVICE_TYPE = "_http._tcp.";
    private static String DEFAULT_SERVICE_NAME = "Mafia";
    private String mServiceName = DEFAULT_SERVICE_NAME;

    private static final int PORT = 9000;

    public NetworkService (Context context) {
        mContext = context;
        mServiceInfo = new NsdServiceInfo();

        mServiceInfo.setServiceName(mServiceName);
        mServiceInfo.setServiceType(SERVICE_TYPE);
        mServiceInfo.setPort(PORT);

        mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);

        initializeRegistrationListener();
        initializeDiscoveryListener();
        initializeResolveListener();
    }

    public NetworkService (Context context, NsdServiceInfo mServiceInfo) {
        mContext = context;
        this.mServiceInfo = mServiceInfo;

        mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);

        initializeRegistrationListener();
        initializeDiscoveryListener();
        initializeResolveListener();
    }

    public void registerService() {
        mNsdManager.registerService(mServiceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }

    public void discoverServices() {
        Log.d(TAG, "discover services");
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }

    public void tearDown() {
        mNsdManager.unregisterService(mRegistrationListener);
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                mServiceName = NsdServiceInfo.getServiceName();
                Log.i(TAG, "Service Registered!!");
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo mServiceInfo, int errorCode) {
                // Registration failed!  Put debugging code here to determine why.
                Log.i (TAG, "Service Registration Failed!!");
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered.  This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                Log.i(TAG, "Service UnRegistered!!");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo mServiceInfo, int errorCode) {
                // Unregistration failed.  Put debugging code here to determine why.
                Log.i (TAG, "Service UnRegistration Failed!!");
            }
        };
    }

    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains(DEFAULT_SERVICE_NAME)){
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                mService = serviceInfo;
                int port = mService.getPort();
                InetAddress host = mService.getHost();
            }
        };
    }
}
