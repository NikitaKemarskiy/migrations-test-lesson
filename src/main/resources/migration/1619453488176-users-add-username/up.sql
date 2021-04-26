start transaction;
alter table users add column username varchar;
update users set username = split_part(email, '@', 1);
alter table users alter column username set not null;
commit;