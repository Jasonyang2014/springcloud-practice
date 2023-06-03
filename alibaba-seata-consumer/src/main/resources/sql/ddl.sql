use seata_test;
drop table if exists t_user;
create table t_user(
id int not null auto_increment primary key,
name varchar(20) not null comment '姓名',
balance decimal(8,2) default 0.0 comment '金额'
)engine innodb;