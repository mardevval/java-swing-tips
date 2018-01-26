package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.io.Serializable;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        ColorItem[] model = {
            new ColorItem(Color.RED, "Red"),
            new ColorItem(Color.GREEN, "Green"),
            new ColorItem(Color.BLUE, "Blue"),
            new ColorItem(Color.CYAN, "Cyan"),
            new ColorItem(Color.ORANGE, "Orange"),
            new ColorItem(Color.MAGENTA, "Magenta")};

        JComboBox<ColorItem> combo00 = new JComboBox<>(model);

        JComboBox<ColorItem> combo01 = new JComboBox<>(model);
        combo01.setRenderer(new ComboForegroundRenderer(combo01));

        JComboBox<ColorItem> combo02 = new JComboBox<>(model);
        combo02.setRenderer(new ComboHtmlRenderer());

        Box box = Box.createVerticalBox();
        box.add(makeTitledPanel("default:", combo00));
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("setForeground:", combo01));
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("html tag:", combo02));
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class ColorItem implements Serializable {
    private static final long serialVersionUID = 1L;
    public final Color color;
    public final String description;
    protected ColorItem(Color color, String description) {
        this.color = color;
        this.description = description;
    }
    @Override public String toString() {
        return description;
    }
}

class ComboForegroundRenderer extends DefaultListCellRenderer {
    private final Color sbgc = new Color(240, 245, 250);
    private final JComboBox combo;
    protected ComboForegroundRenderer(JComboBox combo) {
        super();
        this.combo = combo;
    }
    @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof ColorItem) {
            ColorItem item = (ColorItem) value;
            Color ic = item.color;
            if (index < 0 && Objects.nonNull(ic) && !ic.equals(combo.getForeground())) {
                combo.setForeground(ic); // Windows, Motif Look&Feel
                list.setSelectionForeground(ic);
                list.setSelectionBackground(sbgc);
            }
            JLabel l = (JLabel) super.getListCellRendererComponent(list, item.description, index, isSelected, cellHasFocus);
            l.setForeground(ic);
            l.setBackground(isSelected ? sbgc : list.getBackground());
            // l.setText(item.description);
            return l;
        } else {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            return this;
        }
    }
}

class ComboHtmlRenderer extends DefaultListCellRenderer {
    private final Color sbgc = new Color(240, 245, 250);
    @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        ColorItem item = (ColorItem) value;
        if (index < 0) {
            list.setSelectionBackground(sbgc);
        }
        JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        // l.setText("<html><font color=" + hex(item.color) + ">" + item.description);
        l.setText(String.format("<html><font color='#%06X'>%s", item.color.getRGB() & 0xFFFFFF, item.description));
        l.setBackground(isSelected ? sbgc : list.getBackground());
        return l;
    }
//     private static String hex(Color c) {
//         return String.format("#%06X", c.getRGB() & 0xFFFFFF);
//     }
}
