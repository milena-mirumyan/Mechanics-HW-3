import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.*;

public class TimeTable extends JFrame implements ActionListener {

    private JPanel screen = new JPanel(), tools = new JPanel();
    private JButton tool[];
    private JTextField field[];
    private CourseArray courses;
    private Color CRScolor[] = {Color.RED, Color.GREEN, Color.BLACK};
    private LogFile logFile;
    private Autoassociator autoassociator; // Task 4: Autoassociator instance

    public TimeTable() {
        super("Dynamic Time Table");
        setSize(500, 800);
        setLayout(new FlowLayout());

        screen.setPreferredSize(new Dimension(400, 800));
        add(screen);

        setTools();
        add(tools);
        try {
            String clashFileName = field[2].getText(); // Get the clash file name from the field
            logFile = new LogFile(clashFileName); // Create a new log file with a unique name including the clash file name
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception
        }

        setVisible(true);
    }

    public void setTools() {
        String capField[] = {"Slots:", "Courses:", "Clash File:", "Iters:", "Shift:"};
        field = new JTextField[capField.length];

        String capButton[] = {"Load", "Start", "Step", "Print", "Exit", "Continue"};
        tool = new JButton[capButton.length];

        tools.setLayout(new GridLayout(2 * capField.length + capButton.length, 1));

        for (int i = 0; i < field.length; i++) {
            tools.add(new JLabel(capField[i]));
            field[i] = new JTextField(5);
            tools.add(field[i]);
        }

        for (int i = 0; i < tool.length; i++) {
            tool[i] = new JButton(capButton[i]);
            tool[i].addActionListener(this);
            tools.add(tool[i]);
        }

        field[0].setText("28");
        field[1].setText("543");
        field[2].setText("car-f-92.stu");
        field[3].setText("100");
        field[4].setText("12");
    }

    public void draw() {
        Graphics g = screen.getGraphics();
        int width = Integer.parseInt(field[0].getText()) * 10;
        for (int courseIndex = 1; courseIndex < courses.length(); courseIndex++) {
            g.setColor(CRScolor[courses.status(courseIndex) > 0 ? 0 : 1]);
            g.drawLine(0, courseIndex, width, courseIndex);
            g.setColor(CRScolor[CRScolor.length - 1]);
            g.drawLine(10 * courses.slot(courseIndex), courseIndex, 10 * courses.slot(courseIndex) + 10, courseIndex);
        }
    }

    private int getButtonIndex(JButton source) {
        int result = 0;
        while (source != tool[result]) result++;
        return result;
    }

    public void actionPerformed(ActionEvent click) {
        int min = 0, step = 0, clashes = 0;

        switch (getButtonIndex((JButton) click.getSource())) {
            case 0:
                int slots = Integer.parseInt(field[0].getText());
                courses = new CourseArray(Integer.parseInt(field[1].getText()) + 1, slots);
                courses.readClashes(field[2].getText());
                draw();
                break;
            case 1:
                min = Integer.MAX_VALUE;
                step = 0;
                for (int i = 1; i < courses.length(); i++) courses.setSlot(i, 0);
                for (int iteration = 1; iteration <= Integer.parseInt(field[3].getText()); iteration++) {
                    courses.iterate(Integer.parseInt(field[4].getText()));
                    draw();
                    clashes = courses.clashesLeft();
                    if (clashes < min) {
                        min = clashes;
                        step = iteration;
                    }
                }
                System.out.println("Shift = " + field[4].getText() + "\tMin clashes = " + min + "\tat step " + step);
                setVisible(true);
                logFile.write("NumOfSlots = " + field[0].getText() + "\tNumOfCourses = " + field[1].getText() + "\tIterations = " + field[3].getText() + "\tShift = " + field[4].getText() + "\tMin clashes = " + min + "\tat step " + step);
                break;
            case 2:
                courses.iterate(Integer.parseInt(field[4].getText()));
                draw();
                break;
            case 3:
                System.out.println("Exam\tSlot\tClashes");
                for (int i = 1; i < courses.length(); i++)
                    System.out.println(i + "\t" + courses.slot(i) + "\t" + courses.status(i));
                break;
            case 4:
                System.exit(0);
            case 5:
                min = Integer.MAX_VALUE;
                step = 0;
                if (courses.clashesLeft() > 0) {
                    for (int iteration = 1; iteration <= Integer.parseInt(field[3].getText()); iteration++) {
                        courses.iterate(Integer.parseInt(field[4].getText()));
                        draw();
                        clashes = courses.clashesLeft();
                        if (clashes < min) {
                            min = clashes;
                            step = iteration;
                        }
                    }
                    System.out.println("Shift = " + field[4].getText() + "\tMin clashes = " + min + "\tat step " + step);
                    logFile.write("NumOfSlots = " + field[0].getText() + "\tNumOfCourses = " + field[1].getText() + "\tIterations = " + field[3].getText() + "\tShift = " + field[4].getText() + "\tMin clashes = " + min + "\tat step " + step);
                    setVisible(true);
                } else {
                    System.out.println("The number of clashes is 0. The algorithm is complete.");
                }
        }
    }

    public void trainAutoassociatorWithClashFreeSlots(int[] clashFreeSlots) {
        autoassociator = new Autoassociator(courses);
        autoassociator.training(clashFreeSlots);
    }
    public boolean isClashFree(int slotIndex) {
        // Check each course and return true if no clashes are found for the specified timeslot
        for (int i = 1; i < courses.length(); i++) {
            if (courses.slot(i) == slotIndex && courses.status(i) > 0) {
                return false; // Clash found
            }
        }
        return true; // No clash found
    }

    public int[] findClashFreeSlots(int numSlots) {
        int[] clashFreeSlots = new int[numSlots];
        int count = 0;

        // Iterate over each timeslot and check for clashes
        for (int i = 0; i < numSlots; i++) {
            if (isClashFree(i)) {
                clashFreeSlots[count++] = i;
            }
        }

        // Resize the array to remove unused slots
        return Arrays.copyOf(clashFreeSlots, count);
    }

    public void closeLogFile() {
        logFile.close();
    }
    public void interruptIterationsWithUnitUpdates(int iterations, int shifts) {
        for (int i = 1; i <= iterations; i++) {
            // Run scheduling iteration
            tool[2].doClick(); // Click the Step button to run a single iteration

            // Identify clash-free timeslots
            int[] clashFreeSlots = findClashFreeSlots(Integer.parseInt(field[0].getText()));

            // Train Autoassociator with clash-free timeslots
            trainAutoassociatorWithClashFreeSlots(clashFreeSlots);


            // Perform unit updates using trained Autoassociator
            int[] updatedSlots = new int[]{autoassociator.unitUpdate(clashFreeSlots)};

        }
    }


    // Main method
    public static void main(String[] args) {
        TimeTable timeTable = new TimeTable();

        timeTable.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                timeTable.closeLogFile(); // Close the log file when the window is closed
                System.exit(0);
            }
        });

        // Set parameters, load clash information, and start scheduling

        timeTable.tool[0].doClick(); // Click the Load button to load clashes
        timeTable.tool[1].doClick(); // Click the Start button to start scheduling

        // Interrupt iterations with unit updates using trained Autoassociator
        int iterations = Integer.parseInt(timeTable.field[3].getText());
        int shifts = Integer.parseInt(timeTable.field[4].getText());
        timeTable.interruptIterationsWithUnitUpdates(iterations, shifts);
    }
}
