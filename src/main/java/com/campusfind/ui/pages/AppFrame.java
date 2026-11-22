package com.campusfind.ui.pages;

import com.campusfind.service.AppSession;
import com.campusfind.service.ItemService;
import com.campusfind.service.KandidatService;
import com.campusfind.service.KlaimService;
import com.campusfind.service.NotificationService;
import com.campusfind.service.PosService;
import com.campusfind.ui.components.NavBar;
import com.campusfind.ui.theme.Theme;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class AppFrame extends JFrame {

    private final NavigationController controller;
    private final NavBar navBar;
    private final JPanel contentHolder;

    public AppFrame(ItemService itemService,
                    KlaimService klaimService,
                    PosService posService,
                    NotificationService notificationService,
                    KandidatService kandidatService,
                    AppSession appSession) {
        super("CampusFind — Layanan Rekonsiliasi & Kehilangan Kampus");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Theme.WINDOW_DEFAULT);
        setMinimumSize(Theme.WINDOW_MINIMUM);
        setLocationRelativeTo(null);

        this.controller = new NavigationController(
                this,
                itemService,
                klaimService,
                posService,
                notificationService,
                kandidatService,
                appSession
        );

        this.navBar = new NavBar(controller);
        this.contentHolder = new JPanel(new BorderLayout());
        this.contentHolder.setBackground(Theme.PAGE_BACKGROUND);

        controller.setViewSetter(this::setView);
        controller.setNotificationCountUpdater(navBar::setUnreadNotificationsCount);

        setLayout(new BorderLayout());
        add(navBar, BorderLayout.NORTH);
        add(contentHolder, BorderLayout.CENTER);

        // Start at TemuanPage
        controller.navigateToTemuan();
    }

    public AppFrame(ItemService itemService,
                    KlaimService klaimService,
                    PosService posService,
                    NotificationService notificationService,
                    AppSession appSession) {
        this(itemService, klaimService, posService, notificationService, null, appSession);
    }

    private void setView(JComponent component) {
        contentHolder.removeAll();
        contentHolder.add(component, BorderLayout.CENTER);
        contentHolder.revalidate();
        contentHolder.repaint();
    }
}
