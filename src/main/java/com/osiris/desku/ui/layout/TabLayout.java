package com.osiris.desku.ui.layout;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.utils.NoValue;
import com.osiris.jlib.logger.AL;

import java.util.function.Consumer;

public class TabLayout extends Component<TabLayout, NoValue> {
    public ListLayout tabs = new ListLayout();
    public Horizontal pages = new Horizontal();

    public TabLayout() {
        super(NoValue.GET);
        childVertical();
        add(tabs, pages);
        tabs.addClass("nav nav-tabs");
        Consumer<AddedChildEvent> superTabsAdd = tabs._add;
        tabs._add = e -> {
            e.childComp.addClass("nav-link");
            e.childComp.sty("cursor", "pointer");
            e.childComp.onClick(c -> {
                selectTab(e.childComp);
            });
            superTabsAdd.accept(e);
        };
    }

    public TabLayout selectTab(Component<?,?> tab) {
        int iPage = tabs.children.indexOf(tab);
        if (iPage < 0) {
            AL.warn("Failed to select tab and show its page, since provided tab does not exist in tabs: " + tabs + " as html: " + tabs.element.outerHtml());
            return this;
        }
        for (Component<?,?> t : tabs.children) {
            t.removeClass("active");
        }
        tab.addClass("active");

        for (Component<?,?> p : pages.children) {
            p.visible(false);
        }
        Component<?,?> page = pages.children.get(iPage);
        page.visible(true);
        return this;
    }

    public TabLayout addTabAndPage(String tab, Component<?,?> page) {
        return addTabAndPage(new Text(tab), page);
    }

    public TabLayout addTabAndPage(Component<?,?> tab, Component<?,?> page) {
        page.visible(false);

        tabs.add(tab);
        pages.add(page);

        if (tabs.children.size() == 1) // First tab is always visible
            selectTab(tab);
        return this;
    }
}
