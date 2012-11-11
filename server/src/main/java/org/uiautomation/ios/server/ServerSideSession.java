package org.uiautomation.ios.server;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.uiautomation.ios.IOSCapabilities;
import org.uiautomation.ios.UIAModels.Session;
import org.uiautomation.ios.UIAModels.UIADriver;
import org.uiautomation.ios.client.uiamodels.impl.RemoteUIADriver;
import org.uiautomation.ios.mobileSafari.WebInspector;
import org.uiautomation.ios.server.application.IOSApplication;
import org.uiautomation.ios.server.instruments.CommunicationChannel;
import org.uiautomation.ios.server.instruments.InstrumentsManager;

public class ServerSideSession extends Session {

  private final IOSApplication application;
  private final InstrumentsManager instruments;
  private WebInspector inspector;
  public UIADriver nativeDriver;

  private String context;
  private boolean nativeMode = true;
  private int serverPort;



  public ServerSideSession(IOSApplication application, int serverPort) {
    super(UUID.randomUUID().toString());
    this.application = application;
    this.serverPort = serverPort;


    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        forceStop();
      }
    });
    instruments = new InstrumentsManager(serverPort);
    
  
  }

  public UIADriver getNativeDriver() {
    return nativeDriver;
  }

  public IOSApplication getApplication() {
    return application;
  }

  public CommunicationChannel communication() {
    return instruments.communicate();
  }

  public void stop() {
    instruments.stop();
  }

  public void forceStop() {
    instruments.forceStop();
  }

  public File getOutputFolder() {
    return instruments.getOutput();
  }

  public InstrumentsManager getInstruments() {
    return instruments;

  }

  public void start(IOSCapabilities capabilities) {
    instruments.startSession(capabilities.getDevice(), capabilities.getSDKVersion(),
        capabilities.getLocale(), capabilities.getLanguage(), application.getApplicationPath(),
        getSessionId(), capabilities.isTimeHack(), capabilities.getExtraSwitches());
    
    URL url = null;
    try {
      url = new URL("http://localhost:" + serverPort + "/wd/hub");
    } catch (Exception e) {
      e.printStackTrace();
    }
    nativeDriver = new RemoteUIADriver(url, new Session(instruments.getSessionId()));
  }


  public WebInspector getWebInspector() {
    if (inspector == null) {
      String bundleId = application.getMetadata("CFBundleIdentifier");
      try {
        this.inspector = new WebInspector(nativeDriver, bundleId);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return inspector;
  }

  public void setCurrentContext(String context) {
    if ("nativeView".equals(context)) {
      nativeMode = true;
    } else {
      nativeMode = false;
    }

  }

  public boolean isNative() {
    return nativeMode;
  }

}
