package com.application.paintapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by Rajat Shrivastava on 17-05-2018.
 */

public class MouseProject {
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                MouseFrame frame = new MouseFrame();
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setIconImage(new ImageIcon("C:\\Users\\Rajat Shrivastava\\Desktop\\Wallpapers\\network.png").getImage());
                frame.setVisible(true);
            }
        });
    }
}

class MouseFrame extends JFrame {
    public MouseFrame() {
        ContainerPanel c = new ContainerPanel();
        add(c);
        System.out.println(c.m);
        JMenuBar menu = new JMenuBar();
        JMenu file = new JMenu("File");
        menu.add(file);
        JMenuItem save = new JMenuItem("Save");
        JMenuItem load = new JMenuItem("Load");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Start");
                c.m.save();
                System.out.println("End");
            }
        });
        load.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Start");
                c.m.load();
                System.out.println("End");
            }
        });
        file.add(save);
        file.add(load);
        setJMenuBar(menu);
        pack();
    }
}

class ContainerPanel extends JPanel {

    public MouseComponent m;

    public ContainerPanel() {
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        ColorButtonPanel c = new ColorButtonPanel();
        c.setMaximumSize(new Dimension(700,100));
        add(c);
        m = new MouseComponent();
        add(m);
    }
}

class ColorButtonPanel extends JPanel {
    public ColorButtonPanel() {
        setSize(200,200);
        setLocation(0,0);
        makeButton("Yellow",Color.YELLOW);
        makeButton("Blue",Color.BLUE);
        makeButton("Red",Color.RED);
        makeButton("Black",Color.BLACK);
        makeButton("Green",Color.GREEN);
        makeButton("White",Color.WHITE);
        makeButton("Pink",Color.PINK);
        makeButton("Orange",Color.ORANGE);
    }

    public void makeButton(String s, final Color c) {
        JButton button = new JButton(s);
        button.setBackground(c);
        button.setForeground(c);
        add(button);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MouseComponent z = (MouseComponent) getParent().getComponent(1);
                z.setPen(c);
            }
        });
    }
}

class  MouseComponent extends JComponent {

    class ColorRectangle implements Serializable {
        public Rectangle2D rect;
        private final Color c;

        public ColorRectangle(Color s) {
            rect = null;
            c = s;
        }

        public ColorRectangle(Double x, Double y, int width, int height, Color s) {
            rect = new Rectangle2D.Double(x,y,width,height);
            c = s;
        }

        public void drawRectangle(Graphics2D g2) {
            g2.setColor(c);
            g2.draw(rect);
            g2.fill(rect);
        }

        public void drawOutline(Graphics2D g2) {
            g2.setColor(c);
            g2.draw(rect);
        }
    }

    private static final int SIDELENGTH = 40;
    private ArrayList<ColorRectangle> squares;
    private ColorRectangle outline;
    private ColorRectangle current;
    private ColorRectangle temp;
    public Color k;

    public MouseComponent() {
        squares = new ArrayList<>();
        ColorRectangle z = new ColorRectangle(0.0,0.0,3000,1000,Color.WHITE);
        squares.add(z);
        outline = null;
        current = null;
        k = Color.BLACK;
        temp = null;

        addMouseListener(new MouseHandler());
        addMouseMotionListener(new MouseMotionHandler());
    }

    public void setPen(Color c) {
        k = c;
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(k);
        for (ColorRectangle r:squares) {
            r.drawRectangle(g2);
        }
        try{
            outline.drawOutline(g2);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private ColorRectangle find(Point2D p) {
        for (ColorRectangle r:squares)
            if (r.rect.contains(p))
                return r;
        return null;
    }

    private void add(Point2D p) {
        double x = p.getX();
        double y = p.getY();
        current = new ColorRectangle(x-SIDELENGTH/2,y-SIDELENGTH/2,SIDELENGTH,SIDELENGTH,k);
        squares.add(current);
        repaint();
    }

    private void remove(ColorRectangle r) {
        if (r == null)  return;
        if (r == current) current = null;
        squares.remove(r);
        repaint();
    }

    private void setOutline(Point2D p) {
        outline = new ColorRectangle(p.getX()-SIDELENGTH/2,p.getY()-SIDELENGTH/2,SIDELENGTH,SIDELENGTH,k);
        repaint();
    }

    public void save() {
        try {
            FileOutputStream ofile = new FileOutputStream("C:\\Users\\Rajat Shrivastava\\Desktop\\BadPaint.ser");
            ObjectOutputStream oos = new ObjectOutputStream(ofile);
            for (ColorRectangle r:squares) {
                oos.writeObject(r);
            }
            System.out.println("Saved");
            oos.close();
            ofile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            FileInputStream ifile = new FileInputStream("C:\\Users\\Rajat Shrivastava\\Desktop\\BadPaint.ser");
            ObjectInputStream ois = new ObjectInputStream(ifile);
            squares = new ArrayList<>();
            while(true) {
                temp = (ColorRectangle) ois.readObject();
                squares.add(temp);
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        repaint();
    }

    private class MouseHandler extends MouseAdapter {

        public void mousePressed(MouseEvent event) {
            current = find(event.getPoint());
            //if (current == null)
            add(event.getPoint());
        }

        public void mouseClicked(MouseEvent event) {
            current = find(event.getPoint());
            if (current != null && event.getClickCount()>=2)    remove(current);
        }

    }

    private class MouseMotionHandler implements MouseMotionListener {
        public void mouseMoved(MouseEvent event) {
            setOutline(event.getPoint());
            if (find(event.getPoint()) == null)
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            else
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public void mouseDragged(MouseEvent event) {
            if (current != null) {
                double x = event.getX();
                double y = event.getY();
                current.rect.setFrame(x-SIDELENGTH/2,y-SIDELENGTH/2,SIDELENGTH,SIDELENGTH);
                ColorRectangle o = new ColorRectangle(x-SIDELENGTH/2,y-SIDELENGTH/2,SIDELENGTH,SIDELENGTH,k);
                squares.add(o);
                repaint();
            }
        }
    }
}
