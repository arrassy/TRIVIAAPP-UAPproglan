package org.example.uapsms3;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TriviaApp {

    private static final String API_URL = "https://opentdb.com/api.php?amount=1&type=boolean";

    public static void main(String[] args) {
        // Membuat frame utama
        JFrame frame = new JFrame("Trivia App");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Komponen
        JTextArea questionArea = new JTextArea("Click 'Get Question' to start.");
        questionArea.setWrapStyleWord(true);
        questionArea.setLineWrap(true);
        questionArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(questionArea);

        JPanel buttonPanel = new JPanel();
        JButton trueButton = new JButton("True");
        JButton falseButton = new JButton("False");
        JButton getQuestionButton = new JButton("Get Question");
        buttonPanel.add(trueButton);
        buttonPanel.add(falseButton);
        buttonPanel.add(getQuestionButton);

        JLabel resultLabel = new JLabel("Result: ");
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Tambahkan komponen ke frame
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.add(resultLabel, BorderLayout.NORTH);

        // Event handling
        final String[] currentQuestion = {null};
        final String[] currentAnswer = {null};

        getQuestionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String response = fetchQuestion();
                    String[] parsed = parseQuestion(response);
                    currentQuestion[0] = parsed[0];
                    currentAnswer[0] = parsed[1];
                    questionArea.setText(currentQuestion[0]);
                    resultLabel.setText("Result: ");
                } catch (Exception ex) {
                    questionArea.setText("Error fetching question: " + ex.getMessage());
                }
            }
        });

        trueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentAnswer[0] == null) {
                    resultLabel.setText("Result: Please fetch a question first!");
                    return;
                }
                if ("True".equalsIgnoreCase(currentAnswer[0])) {
                    resultLabel.setText("Result: Correct!");
                } else {
                    resultLabel.setText("Result: Wrong!");
                }
            }
        });

        falseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentAnswer[0] == null) {
                    resultLabel.setText("Result: Please fetch a question first!");
                    return;
                }
                if ("False".equalsIgnoreCase(currentAnswer[0])) {
                    resultLabel.setText("Result: Correct!");
                } else {
                    resultLabel.setText("Result: Wrong!");
                }
            }
        });

        frame.setVisible(true);
    }

    private static String fetchQuestion() throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Failed to fetch data: HTTP code " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        return response.toString();
    }

    private static String[] parseQuestion(String response) {
        String questionMarker = "\"question\":\"";
        String correctAnswerMarker = "\"correct_answer\":\"";

        int questionStart = response.indexOf(questionMarker) + questionMarker.length();
        int questionEnd = response.indexOf("\"", questionStart);
        String question = response.substring(questionStart, questionEnd).replaceAll("&quot;", "\"").replaceAll("&#039;", "'");

        int answerStart = response.indexOf(correctAnswerMarker) + correctAnswerMarker.length();
        int answerEnd = response.indexOf("\"", answerStart);
        String correctAnswer = response.substring(answerStart, answerEnd);

        return new String[]{question, correctAnswer};
    }
}



