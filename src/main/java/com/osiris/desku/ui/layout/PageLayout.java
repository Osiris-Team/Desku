package com.osiris.desku.ui.layout;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.input.Button;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class PageLayout extends Component<PageLayout> {

    public Vertical content = new Vertical().childGap(true);
    public Navigator navigator = new Navigator(content);
    public Function<FetchDetails, List<Component<?>>> fetchChildComps;
    public int iStart, maxFetchCount;

    public PageLayout() {
        this.add(content, navigator);
        childVertical();
    }

    /**
     * Treats this component as layout/container,
     * and loads child components lazily, which means at scroll end. <br>
     * Recommended if large amounts of data must be displayed,
     * but cannot be loaded all in at once.
     */
    public PageLayout setDataProvider(int iStart, int maxFetchCount, Function<FetchDetails, List<Component<?>>> fetchChildComps) {
        this.iStart = iStart;
        this.maxFetchCount = maxFetchCount;
        this.fetchChildComps = fetchChildComps;
        content.laterWithOverlay((comp, overlay) -> {
            // Fetch data
            List<Component<?>> childComps = fetchChildComps.apply(new FetchDetails(iStart, iStart + maxFetchCount));
            content.removeAll();
            content.add(childComps);
            navigator.next.enable(true);
            navigator.previous.enable(true);
        });
        return _this;
    }

    public class Navigator extends Component<Navigator> {
        public Component<?> content;
        public Button previous;
        public Button next;

        public Navigator(Component<?> content) {
            this.content = content;

            Button previous = new Button("Previous").secondary().width("100%").sizeS().childStart()
                    .putStyle("border-top-right-radius", "0px")
                    .putStyle("border-bottom-right-radius", "0px");
            AtomicBoolean loaded = new AtomicBoolean(true);
            previous.onClick(e -> {
                synchronized (loaded) {
                    if (!loaded.get()) return; // Do not accept other clicks, until below is resolved
                    loaded.set(false);
                    previous.laterWithOverlay((comp, overlay) -> {
                        // Fetch data
                        iStart -= maxFetchCount;
                        List<Component<?>> childComps = fetchChildComps.apply(new FetchDetails(iStart, iStart + maxFetchCount));
                        content.removeAll();
                        content.add(childComps);
                        loaded.set(true);
                        previousAllowed();
                    });
                }
            });

            Button next = new Button("Next").secondary().width("100%").sizeS().childEnd()
                    .putStyle("border-top-left-radius", "0px")
                    .putStyle("border-bottom-left-radius", "0px");
            next.onClick(e -> {
                synchronized (loaded) {
                    if (!loaded.get()) return; // Do not accept other clicks, until below is resolved
                    loaded.set(false);
                    next.laterWithOverlay((comp, overlay) -> {
                        // Fetch data
                        iStart += maxFetchCount;
                        List<Component<?>> childComps = fetchChildComps.apply(new FetchDetails(iStart, iStart + maxFetchCount));
                        content.removeAll();
                        content.add(childComps);
                        loaded.set(true);
                        nextAllowed();
                    });
                }
            });

            this.previous = previous;
            this.next = next;
            add(previous, next);
        }

        boolean nextAllowed() {
            System.out.println(content.children.size());
            boolean bol = !content.children.isEmpty();
            System.out.println(bol);
            next.enable(bol);
            previous.enable(true);
            return bol;
        }

        boolean previousAllowed() {
            boolean bol = !content.children.isEmpty();
            previous.enable(bol);
            next.enable(true);
            return bol;
        }
    }

    public class FetchDetails {
        public int iStart, iEnd;

        public FetchDetails(int iStart, int iEnd) {
            this.iStart = iStart;
            this.iEnd = iEnd;
        }
    }
}
