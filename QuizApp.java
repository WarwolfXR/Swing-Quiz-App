import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;
import java.io.*;
import java.util.*;

public class QuizApp extends JFrame implements ActionListener {
    // GUI Components
    JLabel questionLabel, timerLabel, welcomeLabel;
    JRadioButton[] options = new JRadioButton[4];
    JButton nextButton, submitButton, loginButton;
    ButtonGroup bg;

    int currentQuestion = 0, score = 0, timeLeft = 15;
    Timer timer;

    String username;

    // Inner class for questions
    class Question {
        String text;
        String[] choices;
        int correct;

        Question(String text, String[] choices, int correct) {
            this.text = text;
            this.choices = choices;
            this.correct = correct;
        }
    }

    ArrayList<Question> questionList = new ArrayList<>();

    // Login components
    JTextField usernameField;
    JPanel loginPanel;

    public QuizApp() {
        setTitle("Quiz App");
        setSize(600, 400);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setupLoginPanel();

        loadQuestionsFromFile("questions.txt");
    }

    void setupLoginPanel() {
        loginPanel = new JPanel(null);
        loginPanel.setBounds(0, 0, 600, 400);

        JLabel userLabel = new JLabel("Enter Username:");
        userLabel.setBounds(200, 100, 120, 30);
        loginPanel.add(userLabel);

        usernameField = new JTextField();
        usernameField.setBounds(320, 100, 150, 30);
        loginPanel.add(usernameField);

        loginButton = new JButton("Login");
        loginButton.setBounds(250, 160, 100, 30);
        loginButton.addActionListener(e -> {
            username = usernameField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username cannot be empty");
            } else {
                remove(loginPanel);
                setupQuizUI();
                revalidate();
                repaint();
                loadQuestion();
                startTimer();
            }
        });
        loginPanel.add(loginButton);

        add(loginPanel);
    }

    void setupQuizUI() {
        questionLabel = new JLabel();
        questionLabel.setBounds(50, 30, 500, 30);
        add(questionLabel);

        bg = new ButtonGroup();
        int y = 80;
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            options[i].setBounds(60, y, 400, 30);
            bg.add(options[i]);
            add(options[i]);
            y += 40;
        }

        nextButton = new JButton("Next");
        nextButton.setBounds(100, 250, 100, 30);
        nextButton.addActionListener(this);
        add(nextButton);

        submitButton = new JButton("Submit");
        submitButton.setBounds(250, 250, 100, 30);
        submitButton.setEnabled(false);
        submitButton.addActionListener(this);
        add(submitButton);

        timerLabel = new JLabel("Time left: 15");
        timerLabel.setBounds(400, 250, 150, 30);
        add(timerLabel);
    }

    void loadQuestionsFromFile(String filename) {
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        ArrayList<Question> allQuestions = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            String q = line;
            String[] opts = new String[4];
            for (int i = 0; i < 4; i++) {
                opts[i] = br.readLine();
            }
            int correct = Integer.parseInt(br.readLine());
            allQuestions.add(new Question(q, opts, correct));
        }
        // Shuffle and pick first 10 questions
        Collections.shuffle(allQuestions);
        questionList = new ArrayList<>(allQuestions.subList(0, Math.min(10, allQuestions.size())));
    } catch (IOException | NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Error reading file: " + e.getMessage());
        System.exit(1);
    }
}

    void loadQuestion() {
        Question q = questionList.get(currentQuestion);
        questionLabel.setText("Q" + (currentQuestion + 1) + ": " + q.text);
        for (int i = 0; i < 4; i++) {
            options[i].setText(q.choices[i]);
        }
        bg.clearSelection();
        timeLeft = 15;
        if (timer != null) {
            timerLabel.setText("Time left: " + timeLeft);
        }
    }

    void startTimer() {
        timer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText("Time left: " + timeLeft);
            if (timeLeft <= 0) {
                timer.stop();
                evaluateAnswer();
                nextStep();
            }
        });
        timer.start();
    }

    void evaluateAnswer() {
        int selected = -1;
        for (int i = 0; i < 4; i++) {
            if (options[i].isSelected()) {
                selected = i + 1;
                break;
            }
        }

        if (selected == questionList.get(currentQuestion).correct) {
            score++;
        }
    }

    void nextStep() {
        currentQuestion++;
        if (currentQuestion == questionList.size() - 1) {
            nextButton.setEnabled(false);
            submitButton.setEnabled(true);
        }
        if (currentQuestion < questionList.size()) {
            loadQuestion();
            startTimer();
        } else {
            finishQuiz();
        }
    }

    void finishQuiz() {
        saveScore(username, score);
        String leaderboard = getTopScores(5);
        String message = "Your score: " + score + "/" + questionList.size() + "\n\nTop 5 Scores:\n" + leaderboard;
        JOptionPane.showMessageDialog(this, message);
        System.exit(0);
    }

    void saveScore(String user, int sc) {
        try (FileWriter fw = new FileWriter("scores.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(user + "," + sc);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving score: " + e.getMessage());
        }
    }

    String getTopScores(int topN) {
        TreeMap<Integer, ArrayList<String>> scoreMap = new TreeMap<>(Collections.reverseOrder());
        try (BufferedReader br = new BufferedReader(new FileReader("scores.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String user = parts[0];
                    int sc = Integer.parseInt(parts[1]);
                    scoreMap.putIfAbsent(sc, new ArrayList<>());
                    scoreMap.get(sc).add(user);
                }
            }
        } catch (IOException e) {
            return "No scores available";
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Map.Entry<Integer, ArrayList<String>> entry : scoreMap.entrySet()) {
            int sc = entry.getKey();
            for (String user : entry.getValue()) {
                sb.append(user).append(": ").append(sc).append("\n");
                count++;
                if (count >= topN) break;
            }
            if (count >= topN) break;
        }
        if (sb.length() == 0) return "No scores available";
        return sb.toString();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        timer.stop();
        if (e.getSource() == nextButton) {
            evaluateAnswer();
            nextStep();
        } else if (e.getSource() == submitButton) {
            evaluateAnswer();
            finishQuiz();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuizApp().setVisible(true));
    }
}
