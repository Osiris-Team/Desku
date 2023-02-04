package com.osiris.desku.swing;

import com.osiris.desku.App;
import com.osiris.desku.Route;
import com.osiris.desku.UI;
import com.osiris.desku.ui.EventType;
import com.osiris.desku.ui.events.ClickEvent;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.handler.CefMessageRouterHandlerAdapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 *
 */
public class NativeWindow extends JFrame {
    /**
     * List of components that have a JavaScript click listener attached. <br>
     * Each component here has exactly one click listener attached. <br>
     */
    public final CopyOnWriteArrayList<com.osiris.desku.ui.Component<?>> registeredJSOnClickListenerComponents = new CopyOnWriteArrayList<>();
    public CefBrowser browser;
    public Component browserUI;
    /**
     * Only not null if one of these constructors was used: <br>
     * {@link #NativeWindow(Route)} <br>
     * {@link #NativeWindow(UI)} <br>
     */
    public UI ui;

    public NativeWindow(Route route) throws IOException {
        this(route, false, false, 70, 60);
    }

    public NativeWindow(Route route, boolean isOffscreenRendered, boolean isTransparent, int widthPercent, int heightPercent) throws IOException {
        this(route.createUI(), isOffscreenRendered, isTransparent, widthPercent, heightPercent);
    }

    public NativeWindow(UI ui) throws IOException {
        this(ui, false, false, 70, 60);

    }

    public NativeWindow(UI ui, boolean isOffscreenRendered, boolean isTransparent, int widthPercent, int heightPercent) throws IOException {
        this("file:///" + ui.snapshotToTempFile().getAbsolutePath(), isOffscreenRendered, isTransparent, widthPercent, heightPercent);
        this.ui = ui;
        registerListeners(ui, ui.content);
        ui.content.forEachChildRecursive(child -> {
            registerListeners(ui, child);
        });
    }


    public NativeWindow(String startURL) {
        this(startURL,false, false);
    }

    public NativeWindow(String startURL, boolean isOffscreenRendered, boolean isTransparent) {
        this(startURL, isOffscreenRendered, isTransparent, 70, 60);
    }

    /**
     * To display a simple browser window, it suffices completely to create an
     * instance of the class CefBrowser and to assign its UI component to your
     * application (e.g. to your content pane).
     * But to be more verbose, this CTOR keeps an instance of each object on the
     * way to the browser UI.
     */
    public NativeWindow(String startURL, boolean isOffscreenRendered, boolean isTransparent, int widthPercent, int heightPercent) {
        try {
            App.windows.add(this);
            // (4) One CefBrowser instance is responsible to control what you'll see on
            //     the UI component of the instance. It can be displayed off-screen
            //     rendered or windowed rendered. To get an instance of CefBrowser you
            //     have to call the method "createBrowser()" of your CefClient
            //     instances.
            //
            //     CefBrowser has methods like "goBack()", "goForward()", "loadURL()",
            //     and many more which are used to control the behavior of the displayed
            //     content. The UI is held within a UI-Compontent which can be accessed
            //     by calling the method "getUIComponent()" on the instance of CefBrowser.
            //     The UI component is inherited from a java.awt.Component and therefore
            //     it can be embedded into any AWT UI.
            browser = App.cefClient.createBrowser(startURL, isOffscreenRendered, isTransparent);
            browserUI = browser.getUIComponent();

            // (6) All UI components are assigned to the default content pane of this
            //     JFrame and afterwards the frame is made visible to the user.
            getContentPane().add(browserUI, BorderLayout.CENTER);
            if (widthPercent <= 0 || heightPercent <= 0) {
                widthPercent = 100;
                heightPercent = 100;
            }
            width(widthPercent);
            height(heightPercent);
            setIconImage(App.getIcon());
            setTitle(App.name);
            Swing.center(this);
            revalidate();
            setVisible(true);

            // (7) To take care of shutting down CEF accordingly, it's important to call
            //     the method "dispose()" of the CefApp instance if the Java
            //     application will be closed. Otherwise you'll get asserts from CEF.
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    App.windows.remove(this);
                    dispose();
                }
            });

            AtomicBoolean isLoaded = new AtomicBoolean(false);
            App.cefClient.addLoadHandler(new CefLoadHandlerAdapter() {
                @Override
                public void onLoadEnd(CefBrowser b, CefFrame frame, int httpStatusCode) {
                    if (b == browser) {
                        isLoaded.set(true);
                    }
                }
            });
            // JavaScript cannot be executed before the page is loaded
            while (!isLoaded.get()) Thread.yield();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        browser.doClose();
    }

    /**
     * @see #width(int)
     */
    public void widthFull() {
        width(100);
    }

    /**
     * This invalidates the container and thus to see changes in the UI
     * make sure execute {@link Component#revalidate()} manually.
     *
     * @param widthPercent 0 to 100% of the parent size (screen if null).
     */
    public void width(int widthPercent) {
        Objects.requireNonNull(this);
        updateWidth(this.getParent(), this, widthPercent);
    }

    /**
     * @see #height(int)
     */
    public void heightFull() {
        height(100);
    }

    /**
     * This invalidates the container and thus to see changes in the UI
     * make sure execute {@link Component#revalidate()} manually.
     *
     * @param heightPercent 0 to 100% of the parent size (screen if null).
     */
    public void height(int heightPercent) {
        updateHeight(this.getParent(), this, heightPercent);
    }

    private void updateWidth(Component parent, Component target, int widthPercent) {
        int parentWidth; // If no parent provided use the screen dimensions
        if (parent != null) parentWidth = parent.getWidth();
        else parentWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        Dimension size = new Dimension(parentWidth / 100 * widthPercent, target.getHeight());
        target.setSize(size);
        target.setPreferredSize(size);
        target.setMaximumSize(size);
    }

    private void updateHeight(Component parent, Component target, int heightPercent) {
        int parentHeight; // If no parent provided use the screen dimensions
        if (parent != null) parentHeight = parent.getHeight();
        else parentHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        Dimension size = new Dimension(target.getWidth(), parentHeight / 100 * heightPercent);
        target.setSize(size);
        target.setPreferredSize(size);
        target.setMaximumSize(size);
    }

    public NativeWindow plusX(int x) {
        this.setLocation(getX() + x, getY());
        return this;
    }

    public NativeWindow plusY(int y) {
        this.setLocation(getX(), getY() + y);
        return this;
    }

    /**
     * Returns new JS (JavaScript) code, that when executed in {@link #browser}
     * results in onJSFunctionExecuted being executed. <br>
     * It wraps around your jsCode and adds callback related stuff, as well as error handling.
     *
     * @param jsCode               modify the message variable in the provided JS (JavaScript) code to send information from JS to Java.
     *                             Your JS code could look like this: <br>
     *                             message = 'first second third etc...';
     * @param onJSFunctionExecuted executed when the provided jsCode executes successfully. String contains the message variable that can be set in your jsCode.
     * @param onJsFunctionFailed   executed when the provided jsCode threw an exception. String contains details about the exception/error.
     */
    public String addCallback(String jsCode, Consumer<String> onJSFunctionExecuted, Consumer<String> onJsFunctionFailed) {
        // 1. execute js code
        // 2. execute callback in java with params from js code
        // 3. return success to js code and execute it
        String id = "" + App.cefMessageRouterRequestId.getAndIncrement();
        App.cefMessageRouter.addHandler(new CefMessageRouterHandlerAdapter() {
            @Override
            public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback) {
                if (request.startsWith(id)) {
                    int iFirstSpace = request.indexOf(" ");
                    if (request.charAt(iFirstSpace - 1) == '!') // message looks like this: "3! Error details..." 3 is the id and can be any number
                        onJsFunctionFailed.accept(request.substring(iFirstSpace + 1));
                    else // message looks like this: "3 first second etc..." 3 is the id and can be any number
                        onJSFunctionExecuted.accept(request.substring(iFirstSpace + 1));
                    //callback.success("my_response");
                    return true;
                }
                return false;  // Not handled.
            }
        }, false);
        return "var message = '';\n" + // Separated by space
                "var error = null;\n" +
                "try{" + jsCode + "} catch (e) { error = e; }\n" +
                "window.cefQuery({request: '" + id + "'+(error == null ? (' '+message) : ('! '+error)),\n" +
                "                 persistent: false,\n" +
                "                 onSuccess: function(response) {},\n" + // for example: print(response);
                "                 onFailure: function(error_code, error_message) {} });\n";
    }

    private String jsGetComp(String varName, int id) {
        return "var " + varName + " = document.querySelectorAll('[java-id=\"" + id + "\"]')[0];\n";
    }

    public NativeWindow registerListeners(UI ui, com.osiris.desku.ui.Component<?> comp) {
        if (comp.uis.contains(ui)) return this;
        comp.uis.add(ui);

        // Attach Java event listeners
        comp.onAddedChild.addAction((childComp) -> {
            childComp.update();
            comp.element.appendChild(childComp.element);
            browser.executeJavaScript(jsGetComp("comp", comp.id) +
                            "var tempDiv = document.createElement('div');\n" +
                            "tempDiv.innerHTML = `" + childComp.element.outerHtml() + "`;\n" +
                            "comp.appendChild(tempDiv.firstChild);\n",
                    "internal", 0);
            registerListeners(ui, childComp);
        });
        comp.onRemovedChild.addAction((childComp) -> {
            childComp.update();
            childComp.element.remove();
            browser.executeJavaScript(jsGetComp("comp", comp.id) +
                            jsGetComp("childComp", childComp.id) +
                            "comp.removeChild(childComp);\n",
                    "internal", 0);
        });
        comp.onStyleChanged.addAction((attribute) -> {
            comp.element.attr(attribute.getKey(), attribute.getValue());
            browser.executeJavaScript(jsGetComp("comp", comp.id) +
                            "comp.setAttribute(`" + attribute.getKey() + "`,`" + attribute.getValue() + "`);\n",
                    "internal", 0);
        });
        comp.onJSListenerAdded.addAction((eventType) -> {
            attachJSEventListener(eventType, comp);
        });
        comp.onJSListenerRemoved.addAction((eventType) -> {
            // TODO
        });

        // Attach JavaScript event listeners
        if (!comp._onClick.actions.isEmpty())
            jsOnClick(ui, comp);
        return this;
    }

    private void attachJSEventListener(EventType eventType, com.osiris.desku.ui.Component<?> comp) {
        switch (eventType) {
            case CLICK:
                jsOnClick(ui, comp);
                break;
            default:
                throw new RuntimeException("Unknown event type " + eventType + ", thus failed to attach JavaScript listener for it.");
        }
    }

    public NativeWindow jsOnClick(UI ui, com.osiris.desku.ui.Component<?> comp) {
        if (registeredJSOnClickListenerComponents.contains(comp))
            return this; // Already registered
        registeredJSOnClickListenerComponents.add(comp);
        String jsNow = jsGetComp("comp", comp.id) +
                "comp.addEventListener(\"click\", (event) => {\n" +
                addCallback("" +
                        "function getObjProps(obj) {\n" +
                        "  var s = '{';\n" +
                        "  for (const key in obj) {\n" +
                        "    if (obj[key] !== obj && obj[key] !== null && obj[key] !== undefined) {\n" +
                        "      s += (`\"${key}\": \"${obj[key]}\",`);\n" +
                        "    }\n" +
                        "  }\n" +
                        "  if(s[s.length-1] == ',') s = s.slice(0, s.length-1);" + // Remove last ,
                        "  s += '}';\n" +
                        "  return s;\n" +
                        "}" +
                        "message = getObjProps(event)\n", (message) -> {
                    comp._onClick.execute(new ClickEvent(message, comp)); // Executes all listeners
                }, (error) -> {
                    throw new RuntimeException(error);
                }) + // JS code that triggers Java function gets executed on a click event for this component
                "});\n";

        browser.executeJavaScript(jsNow, "internal", 0);
        return this;
    }

    public DevToolsDialog openDevTools() {
        return new DevToolsDialog("DevTools", browser);
    }
}
