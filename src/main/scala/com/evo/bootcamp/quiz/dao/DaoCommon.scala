package com.evo.bootcamp.quiz.dao

object DaoCommon {
  protected final val answerId1 = 0
  protected final val answerId2 = 1
  protected final val answerId3 = 2
  protected final val answerId4 = 3
  protected final val questionId1 = 0
  protected final val questionId2 = 1
  protected final val questionId3 = 2

  final val answersSql =
    """CREATE TABLE answers (
      | id INT PRIMARY KEY,
      | text VARCHAR(250) NOT NULL,
      | questionId INT NULL
      | );
      |""".stripMargin

  final val questionsSql =
    """CREATE TABLE questions (
      | id INT PRIMARY KEY,
      | text VARCHAR(250) NOT NULL,
      | category VARCHAR(100) NOT NULL,
      | likesCount INT DEFAULT 0 NOT NULL,
      | disLikesCount INT DEFAULT 0 NOT NULL,
      | rightAnswer INT NOT NULL,
      | FOREIGN KEY (rightAnswer) REFERENCES answers(id));
      |""".stripMargin

  final val populateDataSql =
    s"""
       |INSERT INTO answers (id, text) VALUES
       |  ('$answerId1', 'Pips'),
       |  ('$answerId2', 'Pups'),
       |  ('$answerId3', 'Pipu-pipu'),
       |  ('$answerId4', 'Kotik');
       |
       |INSERT INTO questions (id, text, category, rightAnswer) VALUES
       |  ('$questionId1', 'Who is Danik?', 'Love', '$answerId1');
       |
       |INSERT INTO questions (id, text, category, rightAnswer) VALUES
       |  ('$questionId2', 'Question2?', 'Hey', '$answerId2');
       |
       |INSERT INTO questions (id, text, category, rightAnswer) VALUES
       |  ('$questionId3', 'Question3?', 'Hey', '$answerId3');
       |
       |ALTER TABLE answers ADD CONSTRAINT FK_QUESTIONS FOREIGN KEY (questionId)
       |REFERENCES questions(id);
       |
       |UPDATE answers SET questionId='$questionId1' WHERE id='$answerId1';
       |UPDATE answers SET questionId='$questionId1' WHERE id='$answerId2';
       |UPDATE answers SET questionId='$questionId1' WHERE id='$answerId3';
       |UPDATE answers SET questionId='$questionId1' WHERE id='$answerId4';
       |""".stripMargin

}
