package org.example.uapsms3;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

public class TriviaApp {
    private JFrame frame;
    private JTextField questionField, answerField, indexField;
    private JTextArea displayArea;
    private ArrayList<Trivia> triviaList;

    public TriviaApp() {
        triviaList = new ArrayList<>();

        // Setup Frame
        frame = new JFrame("Trivia Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 700);
        frame.setLayout(new BorderLayout());

        // Title Panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Trivia Manager");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        titlePanel.setBackground(new Color(100, 149, 237));
        frame.add(titlePanel, BorderLayout.NORTH);

        // Main Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Question Input
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Enter Question:"), gbc);
        questionField = new JTextField(30);
        gbc.gridx = 1;
        mainPanel.add(questionField, gbc);

        // Answer Input
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Enter Answer:"), gbc);
        answerField = new JTextField(30);
        gbc.gridx = 1;
        mainPanel.add(answerField, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 3, 10, 10));

        JButton addButton = new JButton("Add Question");
        addButton.addActionListener(e -> addQuestion());
        buttonPanel.add(addButton);

        JButton viewButton = new JButton("View Questions");
        viewButton.addActionListener(e -> viewQuestions());
        buttonPanel.add(viewButton);

        JButton updateButton = new JButton("Update Question");
        updateButton.addActionListener(e -> updateQuestion());
        buttonPanel.add(updateButton);

        JButton deleteButton = new JButton("Delete Question");
        deleteButton.addActionListener(e -> deleteQuestion());
        buttonPanel.add(deleteButton);

        JButton clearButton = new JButton("Clear Fields");
        clearButton.addActionListener(e -> clearFields());
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        // Display Area
        displayArea = new JTextArea(20, 50);
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(displayArea);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        mainPanel.add(scrollPane, gbc);

        // Index Input for Update/Delete
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Enter Question Index (for Update/Delete):"), gbc);
        indexField = new JTextField(10);
        gbc.gridx = 1;
        mainPanel.add(indexField, gbc);

        frame.add(mainPanel, BorderLayout.CENTER);

        // Footer Panel
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(100, 149, 237));
        JLabel footerLabel = new JLabel("Trivia Manager - Enhance your knowledge");
        footerLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        footerPanel.add(footerLabel);
        frame.add(footerPanel, BorderLayout.SOUTH);

        // Show Frame
        frame.setVisible(true);

        // Start API Server
        startAPIServer();
    }

    private void addQuestion() {
        String question = questionField.getText();
        String answer = answerField.getText();
        if (question.isEmpty() || answer.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Both question and answer are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        triviaList.add(new Trivia(question, answer));
        clearFields();
        JOptionPane.showMessageDialog(frame, "Question added successfully!");
    }

    private void viewQuestions() {
        displayArea.setText("");
        if (triviaList.isEmpty()) {
            displayArea.append("No questions available.\n");
        } else {
            for (int i = 0; i < triviaList.size(); i++) {
                Trivia trivia = triviaList.get(i);
                displayArea.append((i + 1) + ". Q: " + trivia.getQuestion() + "\n   A: " + trivia.getAnswer() + "\n\n");
            }
        }
    }

    private void updateQuestion() {
        try {
            int index = Integer.parseInt(indexField.getText()) - 1;
            if (index < 0 || index >= triviaList.size()) {
                throw new IndexOutOfBoundsException();
            }
            String question = questionField.getText();
            String answer = answerField.getText();
            if (question.isEmpty() || answer.isEmpty()) {
                throw new IllegalArgumentException("Both question and answer are required!");
            }
            Trivia trivia = triviaList.get(index);
            trivia.setQuestion(question);
            trivia.setAnswer(answer);
            clearFields();
            JOptionPane.showMessageDialog(frame, "Question updated successfully!");
            viewQuestions();
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            JOptionPane.showMessageDialog(frame, "Invalid index!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(frame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteQuestion() {
        try {
            int index = Integer.parseInt(indexField.getText()) - 1;
            if (index < 0 || index >= triviaList.size()) {
                throw new IndexOutOfBoundsException();
            }
            triviaList.remove(index);
            indexField.setText("");
            JOptionPane.showMessageDialog(frame, "Question deleted successfully!");
            viewQuestions();
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            JOptionPane.showMessageDialog(frame, "Invalid index!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        questionField.setText("");
        answerField.setText("");
        indexField.setText("");
    }

    private void startAPIServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

            server.createContext("/trivia", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    StringBuilder response = new StringBuilder();
                    for (int i = 0; i < triviaList.size(); i++) {
                        Trivia trivia = triviaList.get(i);
                        response.append((i + 1)).append(". Q: ").append(trivia.getQuestion())
                                .append("\n   A: ").append(trivia.getAnswer()).append("\n\n");
                    }
                    byte[] bytes = response.toString().getBytes();
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                }
            });

            server.setExecutor(null);
            server.start();
            System.out.println("Server started at http://localhost:8000");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TriviaApp::new);
    }

    class Trivia {
        private String question;
        private String answer;

        public Trivia(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }
}







