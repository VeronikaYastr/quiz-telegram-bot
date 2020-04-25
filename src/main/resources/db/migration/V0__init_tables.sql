create table answers
(
  id serial not null
    constraint answers_pkey
      primary key,
  text varchar(250) not null,
  questionid integer,
  isright boolean default false not null
);

create table questions
(
  id serial not null
    constraint questions_pkey
      primary key,
  text varchar(250) not null,
  category varchar(100) not null,
  likescount integer default 0 not null,
  dislikescount integer default 0 not null
);

alter table answers add constraint fk_questions
   foreign key (questionid) references questions (id);