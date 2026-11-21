package com.campusfind.ui.components;

import com.campusfind.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Base panel providing responsive, centered content inside a JScrollPane.
 * Automatically tracks viewport width and calculates natural content height
 * without magic minimum heights or horizontal scrollbars.
 */
public abstract class ResponsivePage extends JPanel {

    private final JScrollPane scrollPane;
    private final ScrollableContainer scrollableContainer;
    protected final JPanel contentPanel;
    protected final int maxContentWidth;
    private int lastComputedContentWidth = -1;

    public ResponsivePage(int maxContentWidth) {
        super(new BorderLayout());
        this.maxContentWidth = maxContentWidth;
        setBackground(Theme.PAGE_BACKGROUND);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        scrollableContainer = new ScrollableContainer(contentPanel, maxContentWidth);
        scrollableContainer.setOpaque(false);

        scrollPane = new JScrollPane(scrollableContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);

        scrollableContainer.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int viewportW = scrollableContainer.getWidth();
                if (viewportW > 0) {
                    int pad = (viewportW < Theme.BREAKPOINT_COMPACT) ? 16 : 24;
                    int contentW = Math.min(maxContentWidth, Math.max(300, viewportW - (2 * pad)));
                    if (contentW != lastComputedContentWidth) {
                        lastComputedContentWidth = contentW;
                        onContentWidthChanged(contentW);
                    }
                }
            }
        });
    }

    public ResponsivePage() {
        this(Theme.MAX_CONTENT_WIDTH);
    }

    /**
     * Subclasses override to update responsive subcomponents (PageHeader, card stacks, banners).
     */
    protected abstract void onContentWidthChanged(int contentWidth);

    public int getCurrentContentWidth() {
        if (lastComputedContentWidth > 0) {
            return lastComputedContentWidth;
        }
        int w = getWidth();
        int pad = (w < Theme.BREAKPOINT_COMPACT) ? 16 : 24;
        return Math.min(maxContentWidth, Math.max(300, (w > 0 ? w : Theme.WINDOW_DEFAULT.width) - (2 * pad)));
    }

    protected void addContent(Component c) {
        contentPanel.add(c);
    }

    protected void addContentStrut(int height) {
        contentPanel.add(Box.createVerticalStrut(height));
    }

    protected void clearContent() {
        contentPanel.removeAll();
    }

    /**
     * Container that implements Scrollable to track viewport width.
     */
    private static class ScrollableContainer extends JPanel implements Scrollable {
        private final JPanel content;
        private final int maxContentWidth;

        public ScrollableContainer(JPanel content, int maxContentWidth) {
            super(null); // Explicit layout in doLayout
            this.content = content;
            this.maxContentWidth = maxContentWidth;
            add(content);
        }

        @Override
        public void doLayout() {
            int w = getWidth();
            if (w <= 0) return;
            int pad = (w < Theme.BREAKPOINT_COMPACT) ? 16 : 24;
            int contentWidth = Math.min(maxContentWidth, Math.max(300, w - (2 * pad)));
            int x = Math.max(0, (w - contentWidth) / 2);

            content.setSize(contentWidth, Short.MAX_VALUE);
            content.doLayout();
            int prefH = content.getPreferredSize().height;
            content.setBounds(x, 0, contentWidth, prefH);
        }

        @Override
        public Dimension getPreferredSize() {
            int w = getWidth();
            int pad = (w < Theme.BREAKPOINT_COMPACT) ? 16 : 24;
            int contentWidth = Math.min(maxContentWidth, Math.max(300, (w > 0 ? w : Theme.WINDOW_DEFAULT.width) - (2 * pad)));
            content.setSize(contentWidth, Short.MAX_VALUE);
            content.doLayout();
            int h = content.getPreferredSize().height;
            return new Dimension(w > 0 ? w : maxContentWidth, h);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true; // Width always tracks viewport, preventing horizontal scrollbar
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false; // Natural height allows vertical scroll when needed
        }
    }
}
