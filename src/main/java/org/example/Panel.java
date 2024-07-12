package org.example;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import javax.swing.*;

public final class Panel extends JPanel implements ActionListener {

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int BAR_WIDTH = 0x2;

    private int[] list;
    private boolean[] highlighted;
    private boolean[] sorted;
    private SortingAlgorithm algorithm;
    private Thread sortingThread;
    private final String[] algorithms = {"BubbleSort", "SelectionSort", "InsertionSort", "MergeSort", "QuickSort"};

    public JButton buttonRun = new JButton("Run");
    public JButton buttonCancel = new JButton("Cancel");
    public JButton buttonReset = new JButton("Reset");
    public JComboBox<String> comboBox = new JComboBox<>(algorithms);

    private JSlider speedSlider;
    private int delay;
    private int arraySize;
    private int barWidth;

    public Panel(JFrame frame) {
        arraySize = WINDOW_WIDTH / BAR_WIDTH; // Default array size
        list = new int[arraySize];
        highlighted = new boolean[list.length];
        sorted = new boolean[list.length];
        algorithm = new BubbleSort(this); // Default algorithm
        sortingThread = null;
        delay = 1000000; // Default delay value
        //barWidth = WINDOW_WIDTH / arraySize;
        initialize();
        frame.setResizable(false);
    }

    public void initialize() {
        JPanel buttonPanel = new JPanel(new FlowLayout());

        buttonRun.addActionListener(this);
        buttonCancel.addActionListener(this);
        buttonReset.addActionListener(this);
        comboBox.addActionListener(this);

        buttonPanel.add(buttonRun);
        buttonPanel.add(buttonCancel);
        buttonPanel.add(buttonReset);
        buttonPanel.add(comboBox);

        speedSlider = new JSlider(0, 100, 50); // Min value, max value, initial value
        speedSlider.setPaintLabels(true);
        speedSlider.setPaintTicks(true);
        speedSlider.setMajorTickSpacing(25);
        speedSlider.setMinorTickSpacing(5);
        speedSlider.setBorder(BorderFactory.createTitledBorder("Speed"));
        speedSlider.addChangeListener(e -> delay = 2000000 - (speedSlider.getValue() * 20000)); // Adjust multiplier to suit speed

        buttonPanel.add(speedSlider);
        add(buttonPanel, BorderLayout.NORTH);

        initList();
    }

    private void initList() {
        Random rand = new Random();
        for (int i = 0; i < list.length; i++) {
            list[i] = rand.nextInt(WINDOW_HEIGHT);
        }
        repaint();
    }

    public void resetList() {
        Random rand = new Random();
        for (int i = 0; i < list.length; i++) {
            list[i] = rand.nextInt(WINDOW_HEIGHT);
            highlighted[i] = false;
            sorted[i] = false;
            repaint();
        }
    }

    public void buttonsEnabled(boolean enabled) {
        buttonRun.setEnabled(enabled);
        buttonReset.setEnabled(enabled);
        comboBox.setEnabled(enabled);
    }

    public void startSorting() {
        if (sortingThread != null) {
            return;
        }
        sortingThread = new Thread(() -> {
            algorithm.sort(list);
            sortingThread = null;
            buttonsEnabled(true);
        });
        sortingThread.start();
        buttonsEnabled(false);
    }

    public void stopSorting() {
        if (sortingThread == null) {
            return;
        }
        sortingThread.interrupt();
        sortingThread = null;
        buttonsEnabled(true);
    }

    public boolean threadInterrupted() {
        return sortingThread == null;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        g.setColor(Color.WHITE);
        for (int i = 0; i < list.length; i++) {
            // int x = i * barWidth;
            int x = i * BAR_WIDTH;
            int y = WINDOW_HEIGHT - list[i];
            int height = list[i];
            if (sorted[i]) {
                g.setColor(Color.BLUE);
            } else if (highlighted[i]) {
                g.setColor(Color.RED);
            } else {
                g.setColor(Color.WHITE);
            }
            //g.fillRect(x, y, barWidth, height);
            g.fillRect(x, y, BAR_WIDTH, height);
        }
    }

    public interface SortingAlgorithm {
        void sort(int[] list);
    }

    public static class BubbleSort implements SortingAlgorithm {
        private final Panel panel;

        public BubbleSort(Panel panel) {
            this.panel = panel;
        }

        @Override
        public void sort(int[] list) {
            int n = list.length;
            for (int i = 0; i < n - 1; i++) {
                for (int j = 0; j < n - i - 1; j++) {
                    if (list[j] > list[j + 1]) {
                        int temp = list[j];
                        list[j] = list[j + 1];
                        list[j + 1] = temp;
                    }
                    panel.animate(j, panel.delay);
                }
            }
            panel.checkSorted(list);
        }
    }

    public static class SelectionSort implements SortingAlgorithm {
        private final Panel panel;

        public SelectionSort(Panel panel) {
            this.panel = panel;
        }

        @Override
        public void sort(int[] list) {
            int n = list.length;
            for (int i = 0; i < n - 1; i++) {
                int minIdx = i;
                for (int j = i + 1; j < n; j++) {
                    if (list[j] < list[minIdx]) {
                        minIdx = j;
                    }
                    panel.animate(j, panel.delay);
                }
                int temp = list[minIdx];
                list[minIdx] = list[i];
                list[i] = temp;
            }
            panel.checkSorted(list);
        }
    }

    public static class InsertionSort implements SortingAlgorithm {
        private final Panel panel;

        public InsertionSort(Panel panel) {
            this.panel = panel;
        }

        @Override
        public void sort(int[] list) {
            int n = list.length;
            for (int i = 1; i < n; i++) {
                int key = list[i];
                int j = i - 1;
                while (j >= 0 && list[j] > key) {
                    list[j + 1] = list[j];
                    panel.animate(j, panel.delay);
                    j--;
                }
                list[j + 1] = key;
                panel.animate(j + 1, panel.delay); // Highlight the insertion point
            }
            panel.checkSorted(list);
        }
    }

    public static class MergeSort implements SortingAlgorithm {
        private final Panel panel;

        public MergeSort(Panel panel) {
            this.panel = panel;
        }

        @Override
        public void sort(int[] list) {
            mergeSort(list, 0, list.length - 1);
            panel.checkSorted(list);
        }

        private void mergeSort(int[] list, int left, int right) {
            if (left < right) {
                int middle = (left + right) / 2;
                mergeSort(list, left, middle);
                mergeSort(list, middle + 1, right);
                merge(list, left, middle, right);
            }
        }

        private void merge(int[] list, int left, int middle, int right) {
            int n1 = middle - left + 1;
            int n2 = right - middle;

            int[] leftArray = new int[n1];
            int[] rightArray = new int[n2];

            System.arraycopy(list, left, leftArray, 0, n1);
            System.arraycopy(list, middle + 1, rightArray, 0, n2);

            int i = 0, j = 0, k = left;
            while (i < n1 && j < n2) {
                if (leftArray[i] <= rightArray[j]) {
                    list[k] = leftArray[i];
                    i++;
                } else {
                    list[k] = rightArray[j];
                    j++;
                }
                panel.animate(k, panel.delay);
                k++;
            }

            while (i < n1) {
                list[k] = leftArray[i];
                panel.animate(k, panel.delay);
                i++;
                k++;
            }

            while (j < n2) {
                list[k] = rightArray[j];
                panel.animate(k, panel.delay);
                j++;
                k++;
            }
        }
    }

    public static class QuickSort implements SortingAlgorithm {
        private final Panel panel;

        public QuickSort(Panel panel) {
            this.panel = panel;
        }

        @Override
        public void sort(int[] list) {
            quickSort(list, 0, list.length - 1);
            panel.checkSorted(list);
        }

        private void quickSort(int[] list, int low, int high) {
            if (low < high) {
                int pi = partition(list, low, high);
                quickSort(list, low, pi - 1);
                quickSort(list, pi + 1, high);
            }
        }

        private int partition(int[] list, int low, int high) {
            int pivot = list[high];
            int i = (low - 1);
            for (int j = low; j < high; j++) {
                if (list[j] < pivot) {
                    i++;
                    int temp = list[i];
                    list[i] = list[j];
                    list[j] = temp;
                }
                panel.animate(j, panel.delay);
            }
            int temp = list[i + 1];
            list[i + 1] = list[high];
            list[high] = temp;
            panel.animate(high, panel.delay);
            return i + 1;
        }
    }

    public void checkSorted(int[] list) {
        sorted[0] = true;
        for (int i = 1; i < list.length; i++) {
            if (list[i] < list[i - 1]) {
                return;
            }
            sorted[i] = true;
            animate(i, 1000000);
        }
        buttonsEnabled(true); // Enable buttons after sorting is done
    }

    public void animate(int j, long delay) {
        highlighted[j] = true;
        if (j + 1 < highlighted.length) {
            highlighted[j + 1] = true;
        }
        repaint();
        long startTime = System.nanoTime();
        while (System.nanoTime() - startTime < delay) {
            try {
                Thread.sleep(0); // Use Thread.sleep(0) to yield the processor
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt(); // Restore interrupted status
                break;
            }
        }
        highlighted[j] = false;
        if (j + 1 < highlighted.length) {
            highlighted[j + 1] = false;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttonRun) {
            startSorting();
        } else if (e.getSource() == buttonCancel) {
            stopSorting();
        } else if (e.getSource() == buttonReset) {
            resetList();
        } else if (e.getSource() == comboBox) {
            String selected = (String) comboBox.getSelectedItem();
            if (selected != null) {
                switch (selected) {
                    case "BubbleSort":
                        algorithm = new BubbleSort(this);
                        break;
                    case "SelectionSort":
                        algorithm = new SelectionSort(this);
                        break;
                    case "InsertionSort":
                        algorithm = new InsertionSort(this);
                        break;
                    case "MergeSort":
                        algorithm = new MergeSort(this);
                        break;
                    case "QuickSort":
                        algorithm = new QuickSort(this);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid Algorithm: " + selected);
                }
            }
        }
    }
}
