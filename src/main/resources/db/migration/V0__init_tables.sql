create table category
(
    id   serial       not null
        constraint category_pkey
            primary key,
    name varchar(250) not null
);

create table answers
(
    id         serial                not null
        constraint answers_pkey
            primary key,
    text       varchar(250)          not null,
    questionid integer,
    isright    boolean default false not null
);

create table questions
(
    id       serial       not null
        constraint questions_pkey
            primary key,
    text     varchar(250) not null,
    category integer      not null,
    constraint cat_fkey foreign key (category) references category (id)
);

alter table answers
    add constraint fk_questions
        foreign key (questionid) references questions (id);