INSERT INTO questions (text, category)
VALUES ('Кто первым доказал периодичность появления комет?', 3);
INSERT INTO questions (text, category)
VALUES ('Какая поэма есть у В.В.Маяковского?', 2);
INSERT INTO questions (text, category)
VALUES ('Сонет - поэтический жанр, в котором регламентируется количество строк. Сколько их должно быть?',2);
INSERT INTO questions (text, category)
VALUES ('В какой стране правило около двух десятков королей с одинаковыми именами?', 1);
INSERT INTO questions (text, category)
VALUES ('Сколько глаз у пчелы?', 7);
INSERT INTO questions (text, category)
VALUES ('Как звали  французского генерала, именем которого назван определенный фасон брюк?  ', 1);
INSERT INTO questions (text, category)
VALUES ('Столица Сан-Томе и Принсипи?', 8);
INSERT INTO questions (text, category)
VALUES ('Сколько воды может выпить верблюд после долгой жажды?', 7);
INSERT INTO questions (text, category)
VALUES ('Какое животное намеренно глотает камни?', 7);
INSERT INTO questions (text, category)
VALUES ('Сколько метров отделяет голову взрослого жирафа от земли?', 7);
INSERT INTO questions (text, category)
VALUES ('У какого животного самый большой мозг?', 7);
INSERT INTO questions (text, category)
VALUES ('Сколько глазных век у собаки?', 7);
INSERT INTO questions (text, category)
VALUES ('Сколько жизни во сне проводит ленивец?', 7);
INSERT INTO questions (text, category)
VALUES ('Какому животному ежегодно посвящен день, приходящийся на 2 февраля?', 7);

INSERT INTO questions (text, category)
VALUES ('С помощью чего осуществляется непрерывное наблюдение за поверхностью Земли?', 8);
INSERT INTO questions (text, category)
VALUES ('Антарктиду открыла экспедиция из ...?', 8);
INSERT INTO questions (text, category)
VALUES ('Как называлась освоенная человечеством часть мира?', 8);
INSERT INTO questions (text, category)
VALUES ('На флаге какой страны изображен синий крест на белом фоне?', 8);
INSERT INTO questions (text, category)
VALUES ('Флаг какой страны состоит из одного цвета?', 8);
INSERT INTO questions (text, category)
VALUES ('Флаг какой страны имеет форму квадрата?', 8);
INSERT INTO questions (text, category)
VALUES ('На флаге какой страны изображён символ ислама?', 8);

INSERT INTO answers (text, questionid, isright)
VALUES ('Галлей', 1, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('Коперник', 1, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Кеплер', 1, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Галилей', 1, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Флейта-позвоночник 🎺', 2, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('Свирель-губы 💋', 2, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Скрипка-ладони 🎻', 2, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Барабан-нервы 🥁', 2, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('14', 3, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('12', 3, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('10', 3, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('16', 3, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Франция', 4, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('Великобритания', 4, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Австрия', 4, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Германия', 4, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('5', 5, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('4', 5, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('3', 5, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('2', 5, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Шаровары', 6, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Галифе', 6, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('Клеш', 6, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Джинсы', 6, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Сан-Томе', 7, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('Принсипи', 7, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Пици', 7, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Валли', 7, false);

INSERT INTO answers (text, questionid, isright)
VALUES ('10л', 8, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('20л', 8, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('40л', 8, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('60л', 8, true);

INSERT INTO answers (text, questionid, isright)
VALUES ('Слон', 9, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Гиппопотам', 9, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Крокодил', 9, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('Лев', 9, false);

INSERT INTO answers (text, questionid, isright)
VALUES ('3', 10, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('4', 10, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('5', 10, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('6', 10, false);

INSERT INTO answers (text, questionid, isright)
VALUES ('Обезьяна', 11, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Слон', 11, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('Медведь', 11, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Жираф', 11, false);

INSERT INTO answers (text, questionid, isright)
VALUES ('1', 12, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('2', 12, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('3', 12, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('4', 12, false);

INSERT INTO answers (text, questionid, isright)
VALUES ('30%', 13, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('50%', 13, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('75%', 13, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('85%', 13, false);

INSERT INTO answers (text, questionid, isright)
VALUES ('Кот', 14, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('Сурок', 14, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Енот', 14, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Панда', 14, false);

INSERT INTO answers (text, questionid, isright)
VALUES ('Спутники', 15, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('Камеры', 15, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Телескопы', 15, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Не осуществляется', 15, false);

INSERT INTO answers (text, questionid, isright)
VALUES ('США', 16, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Китая', 16, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Германии', 16, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('России', 16, true);

INSERT INTO answers (text, questionid, isright)
VALUES ('Эллада', 17, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Ойкумена', 17, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('Ноосфера', 17, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Зоосфера', 17, false);

INSERT INTO answers (text, questionid, isright)
VALUES ('Франция', 18, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Швеция', 18, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Финляндия', 18, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('Италия', 18, false);

INSERT INTO answers (text, questionid, isright)
VALUES ('Китай', 19, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Турция', 19, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Испания', 19, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Ливия', 19, true);

INSERT INTO answers (text, questionid, isright)
VALUES ('Франция', 20, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Таиланд', 20, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Швейцария', 20, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('Уганда', 20, false);

INSERT INTO answers (text, questionid, isright)
VALUES ('Сирия', 21, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Мавритания', 21, true);
INSERT INTO answers (text, questionid, isright)
VALUES ('Казахстан', 21, false);
INSERT INTO answers (text, questionid, isright)
VALUES ('Египет', 21, false);