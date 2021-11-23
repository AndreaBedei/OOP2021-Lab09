package it.unibo.oop.lab.reactivegui02;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


/**
 * 
 * @author andrea.bedei2
 *
 */

public class ConcurrentGUI {
    
    private final JFrame frame = new JFrame("UP & DOWN");
    private final JPanel canvas = new JPanel();
    private final JButton JDown = new JButton("Down");
    private final JButton JUp = new JButton("Up");
    private final JButton JStop = new JButton("Stop");
    private final JLabel JNum = new JLabel();
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    
    public ConcurrentGUI() {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JNum.setText("0");
        canvas.setLayout(new FlowLayout(FlowLayout.CENTER));
        canvas.add(JNum);
        canvas.add(JUp);
        canvas.add(JDown);
        canvas.add(JStop);
        frame.getContentPane().add(canvas);
        frame.setVisible(true);
        final Agent agent = new Agent();
        new Thread(agent).start();       
        JStop.addActionListener(new ActionListener() {
            /**
             * event handler associated to action event on button stop.
             * 
             * @param e
             *            the action event that will be handled by this listener
             */
            @Override
            public void actionPerformed(final ActionEvent e) {
                // Agent should be final
                agent.stopCounting();
            }
        });
        JDown.addActionListener(new ActionListener() {
            /**
             * event handler associated to action event on button stop.
             * 
             * @param e
             *            the action event that will be handled by this listener
             */
            @Override
            public void actionPerformed(final ActionEvent e) {
                // Agent should be final
                agent.downCounting();
            }
        });
        JUp.addActionListener(new ActionListener() {
            /**
             * event handler associated to action event on button stop.
             * 
             * @param e
             *            the action event that will be handled by this listener
             */
            @Override
            public void actionPerformed(final ActionEvent e) {
                // Agent should be final
                agent.upCounting();
            }
        });
    }
    private class Agent implements Runnable {
        /*
         * Stop is volatile to ensure visibility. Look at:
         * 
         * http://archive.is/9PU5N - Sections 17.3 and 17.4
         * 
         * For more details on how to use volatile:
         * 
         * http://archive.is/4lsKW
         * 
         */
        private volatile boolean stop;
        private volatile int counter;
        private volatile boolean flag = true;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    /*
                     * All the operations on the GUI must be performed by the
                     * Event-Dispatch Thread (EDT)!
                     */
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            // This will happen in the EDT: since i'm reading counter it needs to be volatile.
                            ConcurrentGUI.this.JNum.setText(Integer.toString(Agent.this.counter));
                        }
                    });
                    /*
                     * SpotBugs shows a warning because the increment of a volatile variable is not atomic,
                     * so the concurrent access is potentially not safe. In the specific case of this exercise,
                     * we do synchronization with invokeAndWait, so it can be ignored.
                     *
                     * EXERCISE: Can you think of a solution that doesn't require counter to be volatile? (without
                     * using synchronized or locks)
                     */
                    if (flag) {
                        this.counter++;
                    } else {
                        this.counter--;
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    /*
                     * This is just a stack trace print, in a real program there
                     * should be some logging and decent error reporting
                     */
                    ex.printStackTrace();
                }
            }
        }

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
            JDown.setEnabled(false);
            JUp.setEnabled(false);
            JStop.setEnabled(false);
        }
        public void downCounting() {
            this.flag = false;
        }
        public void upCounting() {
            this.flag = true;
        }
    }
}
