// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicScrollBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    CardLayoutTabbedPane tabs = new CardLayoutTabbedPane();
    tabs.addTab("JTree", new ColorIcon(Color.RED), new JScrollPane(new JTree()));
    tabs.addTab("JTable", new ColorIcon(Color.GREEN), new JScrollPane(new JTable(10, 3)));
    tabs.addTab("JTextArea", new ColorIcon(Color.BLUE), new JScrollPane(new JTextArea()));
    tabs.addTab("JButton", new ColorIcon(Color.CYAN), new JButton("JButton"));
    tabs.addTab("JCheckBox", new ColorIcon(Color.ORANGE), new JCheckBox("JCheckBox"));
    tabs.addTab("JRadioButton", new ColorIcon(Color.PINK), new JRadioButton("JRadioButton"));
    tabs.addTab("JSplitPane", new ColorIcon(Color.YELLOW), new JSplitPane());
    EventQueue.invokeLater(() -> tabs.getTabArea().getHorizontalScrollBar().setVisible(false));

    JPopupMenu popup = new JPopupMenu();
    popup.add("test: add");
    popup.add("test: delete");
    tabs.getTabArea().setComponentPopupMenu(popup);

    add(tabs);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class CardLayoutTabbedPane extends JPanel {
  private final CardLayout cardLayout = new CardLayout();
  private final JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
  private final JPanel contentsPanel = new JPanel(cardLayout);
  private final JButton hiddenTabs = new JButton("V");
  private final ButtonGroup group = new ButtonGroup();
  private final JScrollPane tabArea = new JScrollPane(tabPanel) {
    @Override public boolean isOptimizedDrawingEnabled() {
      return false; // JScrollBar is overlap
    }

    @Override public void updateUI() {
      super.updateUI();
      EventQueue.invokeLater(() -> {
        getVerticalScrollBar().setUI(new OverlappedScrollBarUI());
        getHorizontalScrollBar().setUI(new OverlappedScrollBarUI());
        setLayout(new OverlapScrollPaneLayout());
        setComponentZOrder(getVerticalScrollBar(), 0);
        setComponentZOrder(getHorizontalScrollBar(), 1);
        setComponentZOrder(getViewport(), 2);
      });
      setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
      setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      getVerticalScrollBar().setOpaque(false);
      getHorizontalScrollBar().setOpaque(false);
      setBackground(Color.DARK_GRAY);
      setViewportBorder(BorderFactory.createEmptyBorder());
      setBorder(BorderFactory.createEmptyBorder());
    }

    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.height = 18 + 6;
      return d;
    }
  };

  protected CardLayoutTabbedPane() {
    super(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    setBackground(new Color(16, 16, 16));
    tabPanel.setInheritsPopupMenu(true);
    hiddenTabs.setFont(hiddenTabs.getFont().deriveFont(8f));
    hiddenTabs.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
    hiddenTabs.setOpaque(false);
    hiddenTabs.setFocusable(false);
    hiddenTabs.setContentAreaFilled(false);
    JPanel header = new JPanel(new BorderLayout());
    header.add(new JLayer<>(tabArea, new HorizontalScrollLayerUI()));
    header.add(hiddenTabs, BorderLayout.EAST);
    add(header, BorderLayout.NORTH);
    add(contentsPanel);
  }

  protected JComponent createTabComponent(String title, Icon icon) {
    JToggleButton tab = new TabButton();
    tab.setInheritsPopupMenu(true);
    group.add(tab);
    tab.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
          ((AbstractButton) e.getComponent()).setSelected(true);
          cardLayout.show(contentsPanel, title);
        }
      }
    });
    EventQueue.invokeLater(() -> tab.setSelected(true));

    JLabel label = new JLabel(title, icon, SwingConstants.LEADING);
    label.setForeground(Color.WHITE);
    label.setIcon(icon);
    label.setOpaque(false);

    JButton close = new JButton(new CloseTabIcon(new Color(0xB0_B0_B0))) {
      @Override public Dimension getPreferredSize() {
        return new Dimension(12, 12);
      }
    };
    close.addActionListener(e -> System.out.println("dummy action: close button"));
    close.setBorder(BorderFactory.createEmptyBorder());
    close.setFocusable(false);
    close.setOpaque(false);
    // close.setFocusPainted(false);
    close.setContentAreaFilled(false);
    close.setPressedIcon(new CloseTabIcon(new Color(0xFE_FE_FE)));
    close.setRolloverIcon(new CloseTabIcon(new Color(0xA0_A0_A0)));

    tab.add(label);
    tab.add(close, BorderLayout.EAST);
    return tab;
  }

  public void addTab(String title, Icon icon, Component comp) {
    JComponent tab = createTabComponent(title, icon);
    tabPanel.add(tab);
    contentsPanel.add(comp, title);
    cardLayout.show(contentsPanel, title);
    EventQueue.invokeLater(() -> tabPanel.scrollRectToVisible(tab.getBounds()));
  }

  public JScrollPane getTabArea() {
    return tabArea;
  }

  @Override public void doLayout() {
    BoundedRangeModel m = tabArea.getHorizontalScrollBar().getModel();
    hiddenTabs.setVisible(m.getMaximum() - m.getExtent() > 0);
    super.doLayout();
  }
}

class TabButton extends JToggleButton {
  private final transient Border emptyBorder = BorderFactory.createEmptyBorder(2, 4, 4, 4);
  private final transient Border selectedBorder = BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(0xFA_00_AA_FF, true)),
      BorderFactory.createEmptyBorder(2, 4, 1, 4));
  private final transient Color pressedColor = new Color(32, 32, 32);
  private final transient Color selectedColor = new Color(48, 32, 32);
  private final transient Color rolloverColor = new Color(48, 48, 48);

  protected TabButton() {
    super();
  }

  @Override public void updateUI() {
    super.updateUI();
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(2, 4, 4, 4));
    setContentAreaFilled(false);
    setFocusPainted(false);
    setOpaque(true);
  }

  @Override protected void fireStateChanged() {
    ButtonModel model = getModel();
    if (model.isEnabled()) {
      if (model.isPressed() && model.isArmed()) {
        setBackground(pressedColor);
        setBorder(selectedBorder);
      } else if (model.isSelected()) {
        setBackground(selectedColor);
        setBorder(selectedBorder);
      } else if (isRolloverEnabled() && model.isRollover()) {
        setBackground(rolloverColor);
        setBorder(emptyBorder);
      } else {
        setBackground(Color.GRAY);
        setBorder(emptyBorder);
      }
    } else {
      setBackground(Color.GRAY);
      setBorder(emptyBorder);
    }
    super.fireStateChanged();
  }
}

class CloseTabIcon implements Icon {
  private final Color color;

  protected CloseTabIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.translate(x, y);
    g2.setPaint(color);
    g2.drawLine(3, 3, 9, 9);
    g2.drawLine(9, 3, 3, 9);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 12;
  }

  @Override public int getIconHeight() {
    return 12;
  }
}

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillOval(1, 1, getIconWidth() - 2, getIconHeight() - 2);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}

class OverlapScrollPaneLayout extends ScrollPaneLayout {
  private static final int BAR_SIZE = 5;

  @Override public void layoutContainer(Container parent) {
    if (parent instanceof JScrollPane) {
      JScrollPane scrollPane = (JScrollPane) parent;
      Rectangle availR = SwingUtilities.calculateInnerArea(scrollPane, null);

      if (Objects.nonNull(colHead) && colHead.isVisible()) {
        Rectangle colHeadR = new Rectangle(0, availR.y, 0, 0);
        int colHeadHeight = Math.min(availR.height, colHead.getPreferredSize().height);
        colHeadR.height = colHeadHeight;
        availR.y += colHeadHeight;
        availR.height -= colHeadHeight;
        colHeadR.width = availR.width;
        colHeadR.x = availR.x;
        colHead.setBounds(colHeadR);
      }

      Optional.ofNullable(viewport).ifPresent(v -> v.setBounds(availR));

      Optional.ofNullable(vsb).ifPresent(sb -> {
        sb.setLocation(availR.x + availR.width - BAR_SIZE, availR.y);
        sb.setSize(BAR_SIZE, availR.height - BAR_SIZE);
        // sb.setVisible(true);
      });

      Optional.ofNullable(hsb).ifPresent(sb -> {
        sb.setLocation(availR.x, availR.y + availR.height - BAR_SIZE);
        sb.setSize(availR.width, BAR_SIZE);
        // sb.setVisible(true);
      });
    }
  }
}

class ZeroSizeButton extends JButton {
  private static final Dimension ZERO_SIZE = new Dimension();

  @Override public Dimension getPreferredSize() {
    return ZERO_SIZE;
  }
}

class OverlappedScrollBarUI extends BasicScrollBarUI {
  private static final Color DEFAULT_COLOR = new Color(0xAA_16_32_64, true);
  // private static final Color DRAGGING_COLOR = new Color(0xFA_FA_FA_FA, true);

  @Override protected JButton createDecreaseButton(int orientation) {
    return new ZeroSizeButton();
  }

  @Override protected JButton createIncreaseButton(int orientation) {
    return new ZeroSizeButton();
  }

  @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
    // Graphics2D g2 = (Graphics2D) g.create();
    // g2.setPaint(new Color(100, 100, 100, 100));
    // g2.fillRect(r.x, r.y, r.width - 1, r.height - 1);
    // g2.dispose();
  }

  @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
    if (c.isEnabled() && !r.isEmpty()) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(DEFAULT_COLOR);
      g2.fillRect(r.x, r.y, r.width - 1, r.height - 1);
      g2.dispose();
    }
  }
}

class HorizontalScrollLayerUI extends LayerUI<JScrollPane> {
  private boolean isDragging;

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK
          | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    JScrollBar hsb = l.getView().getHorizontalScrollBar();
    switch (e.getID()) {
      case MouseEvent.MOUSE_ENTERED:
        hsb.setVisible(true);
        break;
      case MouseEvent.MOUSE_EXITED:
        if (!isDragging) {
          hsb.setVisible(false);
        }
        break;
      case MouseEvent.MOUSE_RELEASED:
        if (isDragging) {
          isDragging = false;
          hsb.setVisible(false);
        }
        break;
      default:
        break;
    }
  }

  @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JScrollPane> l) {
    if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
      isDragging = true;
    }
  }

  @Override protected void processMouseWheelEvent(MouseWheelEvent e, JLayer<? extends JScrollPane> l) {
    JScrollPane scroll = l.getView();
    JScrollBar hsb = scroll.getHorizontalScrollBar();
    JViewport vport = scroll.getViewport();
    Point vp = vport.getViewPosition();
    vp.translate(hsb.getBlockIncrement() * e.getWheelRotation(), 0);
    JComponent v = (JComponent) SwingUtilities.getUnwrappedView(vport);
    v.scrollRectToVisible(new Rectangle(vp, vport.getSize()));
  }
}
