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
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window
        setResizable(false);

        // Set dark theme colors globally
        UIManager.put("Panel.background", new Color(34, 34, 34));
        UIManager.put("Label.foreground", Color.WHITE);
        UIManager.put("Button.background", new Color(64, 64, 64));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("RadioButton.background", new Color(34, 34, 34));
        UIManager.put("RadioButton.foreground", Color.WHITE);
        UIManager.put("RadioButton.select", new Color(100, 149, 237)); // Cornflower blue for selection
        UIManager.put("TextField.background", new Color(64, 64, 64));
        UIManager.put("TextField.foreground", Color.WHITE);
        UIManager.put("TextField.caretForeground", Color.WHITE);

        // Force update UI for these settings (after changing UIManager)
        SwingUtilities.updateComponentTreeUI(this);

        setupLoginPanel();

        loadQuestionsFromFile("questions.txt");
    }

    void setupLoginPanel() {
        loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setBackground(new Color(34, 34, 34));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel userLabel = new JLabel("Enter Username:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        loginPanel.add(userLabel, gbc);

        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(usernameField, gbc);

        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
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
        loginPanel.add(loginButton, gbc);

        add(loginPanel);
    }

    void setupQuizUI() {
        // Main panel with dark background
        JPanel quizPanel = new JPanel(new BorderLayout(10, 10));
        quizPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        quizPanel.setBackground(new Color(34, 34, 34));

        // Question label at top
        questionLabel = new JLabel("Question");
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        questionLabel.setForeground(Color.WHITE);
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        questionLabel.setPreferredSize(new Dimension(550, 50));
        quizPanel.add(questionLabel, BorderLayout.NORTH);

        // Options panel - vertical box layout
        JPanel optionsPanel = new JPanel();
        optionsPanel.setBackground(new Color(34, 34, 34));
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 10));

        bg = new ButtonGroup();
        Font optionFont = new Font("Segoe UI", Font.PLAIN, 16);
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            options[i].setFont(optionFont);
            options[i].setBackground(new Color(34, 34, 34));
            options[i].setForeground(Color.WHITE);
            bg.add(options[i]);
            optionsPanel.add(options[i]);
            optionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        quizPanel.add(optionsPanel, BorderLayout.CENTER);

        // Bottom panel with buttons and timer
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        bottomPanel.setBackground(new Color(34, 34, 34));

        nextButton = new JButton("Next");
        nextButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nextButton.addActionListener(this);

        submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        submitButton.setEnabled(false);
        submitButton.addActionListener(this);

        timerLabel = new JLabel("Time left: 15");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        timerLabel.setForeground(Color.WHITE);

        bottomPanel.add(nextButton);
        bottomPanel.add(submitButton);
        bottomPanel.add(timerLabel);

        quizPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(quizPanel);
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
        if (timerLabel != null) {
            timerLabel.setText("Time left: " + timeLeft);
        }
    }

    void startTimer() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
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
                String u = parts[0].trim();
                String scoreStr = parts[1].trim();
                try {
                    int s = Integer.parseInt(scoreStr);
                    scoreMap.putIfAbsent(s, new ArrayList<>());
                    scoreMap.get(s).add(u);
                } catch (NumberFormatException nfe) {
                    // skip lines with invalid scores
                }
            }
        }
    } catch (IOException e) {
        // If file not found or error reading, return message instead of failing silently
        return "No scores available.";
    }

    StringBuilder sb = new StringBuilder();
    int count = 0;
    outer:
    for (Map.Entry<Integer, ArrayList<String>> entry : scoreMap.entrySet()) {
        for (String user : entry.getValue()) {
            sb.append(user).append(" : ").append(entry.getKey()).append("\n");
            count++;
            if (count >= topN) break outer;
        }
    }

    if (sb.length() == 0) return "No scores yet";
    return sb.toString();
}


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == nextButton) {
            timer.stop();
            evaluateAnswer();
            nextStep();
        } else if (e.getSource() == submitButton) {
            timer.stop();
            evaluateAnswer();
            finishQuiz();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new QuizApp().setVisible(true);
        });
    }
}