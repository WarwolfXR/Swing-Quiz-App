# Swing Quiz App

A simple multiple-choice quiz application built using **Java Swing**.

---

## Features

- User login with username input  
- Loads questions from an external `questions.txt` file  
- Randomly selects 10 questions per quiz session  
- Timer for each question (15 seconds)  
- Score tracking and saving to a `scores.txt` file  
- Displays top 5 high scores at the end of the quiz  
- Dark-themed, sleek UI with centered components  

---

## How to Run

1. **Clone the repository:**

   ```bash
   git clone https://github.com/WarwolfXR/Swing-Quiz-App.git
   cd Swing-Quiz-App

2. **Ensure you have Java installed (JDK 8 or higher).**

3. **Compile the Java file:**

```bash
javac QuizApp.java
```
4. **Run the application:**

```bash
java QuizApp
```

**File Format**
> questions.txt
*Contains the quiz questions in the following format (no blank lines between questions):*

```
Question text
Option 1
Option 2
Option 3
Option 4
Correct option number (1-4)
```
**Example:**
```
What is the capital of France?
Berlin
Madrid
Paris
Rome
3
```
> scores.txt
*Stores usernames and their scores in this CSV format:*
```
username,score
```
---
## Dependencies:
**Java SE (Standard Edition) — no external libraries required.**